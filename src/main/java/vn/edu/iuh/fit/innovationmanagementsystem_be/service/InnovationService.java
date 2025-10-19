package vn.edu.iuh.fit.innovationmanagementsystem_be.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.Innovation;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.InnovationPhase;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.User;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.InnovationStatusEnum;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.FormDataRequest;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.UpdateFormDataRequest;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.InnovationFormDataRequest;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.FormDataResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.InnovationFormDataResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.InnovationResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.InnovationStatisticsDTO;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.InnovationAcademicYearStatisticsDTO;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.UpcomingDeadlineResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.UpcomingDeadlinesResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.exception.IdInvalidException;
import vn.edu.iuh.fit.innovationmanagementsystem_be.mapper.InnovationMapper;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.InnovationRepository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.InnovationPhaseRepository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.utils.ResultPaginationDTO;
import vn.edu.iuh.fit.innovationmanagementsystem_be.utils.Utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.Arrays;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

@Service
@Transactional
public class InnovationService {

        private final InnovationRepository innovationRepository;
        private final InnovationPhaseRepository innovationPhaseRepository;
        private final FormDataService formDataService;
        private final InnovationMapper innovationMapper;
        private final UserService userService;
        private final DigitalSignatureService digitalSignatureService;
        private final InnovationRoundService innovationRoundService;
        private final ActivityLogService activityLogService;

        public InnovationService(InnovationRepository innovationRepository,
                        InnovationPhaseRepository innovationPhaseRepository,
                        FormDataService formDataService,
                        InnovationMapper innovationMapper,
                        UserService userService,
                        DigitalSignatureService digitalSignatureService,
                        InnovationRoundService innovationRoundService,
                        ActivityLogService activityLogService) {
                this.innovationRepository = innovationRepository;
                this.innovationPhaseRepository = innovationPhaseRepository;
                this.formDataService = formDataService;
                this.innovationMapper = innovationMapper;
                this.userService = userService;
                this.digitalSignatureService = digitalSignatureService;
                this.innovationRoundService = innovationRoundService;
                this.activityLogService = activityLogService;
        }

        // 1. Lấy tất cả sáng kiến
        public ResultPaginationDTO getAllInnovations(Specification<Innovation> specification, Pageable pageable) {

                if (pageable.getSort().isUnsorted()) {
                        pageable = org.springframework.data.domain.PageRequest.of(
                                        pageable.getPageNumber(),
                                        pageable.getPageSize(),
                                        org.springframework.data.domain.Sort.by("createdAt").descending());
                }

                Page<Innovation> innovations = innovationRepository.findAll(specification, pageable);
                Page<InnovationResponse> responses = innovations.map(innovationMapper::toInnovationResponse);
                return Utils.toResultPaginationDTO(responses, pageable);
        }

        // 2. Lấy sáng kiến bởi ID
        public InnovationResponse getInnovationById(String id) {
                Innovation innovation = innovationRepository.findById(id)
                                .orElseThrow(() -> new IdInvalidException("Không tìm thấy sáng kiến với ID: " + id));
                return innovationMapper.toInnovationResponse(innovation);
        }

        // 3. Tạo sáng kiến & Submit Form Data (Tạo sáng kiến tự động khi điền form)
        public InnovationFormDataResponse createInnovationAndSubmitFormData(InnovationFormDataRequest request) {

                String actionType = request.getActionType() != null ? request.getActionType().toUpperCase() : "DRAFT";
                if (!InnovationStatusEnum.DRAFT.name().equals(actionType)
                                && !InnovationStatusEnum.SUBMITTED.name().equals(actionType)) {
                        throw new IdInvalidException(
                                        "Action type chỉ được là DRAFT hoặc SUBMITTED. Các trạng thái khác sẽ được xử lý bởi hội đồng chấm điểm.");
                }

                InnovationPhase innovationPhase = innovationPhaseRepository.findById(request.getInnovationPhaseId())
                                .orElseThrow(() -> new IdInvalidException(
                                                "Không tìm thấy giai đoạn sáng kiến với ID: "
                                                                + request.getInnovationPhaseId()));

                User currentUser = userService.getCurrentUser();

                Innovation innovation = new Innovation();
                innovation.setInnovationName(request.getInnovationName());
                innovation.setUser(currentUser);
                innovation.setDepartment(currentUser.getDepartment());
                innovation.setInnovationPhase(innovationPhase);
                innovation.setIsScore(request.getIsScore() != null ? request.getIsScore() : false);

                if (InnovationStatusEnum.SUBMITTED.name().equals(actionType)) {
                        innovation.setStatus(InnovationStatusEnum.DRAFT);
                } else {
                        innovation.setStatus(InnovationStatusEnum.DRAFT);
                }

                Innovation savedInnovation = innovationRepository.save(innovation);

                // Tạo activity log
                activityLogService.createActivityLog(
                                currentUser.getId(),
                                savedInnovation.getId(),
                                savedInnovation.getInnovationName(),
                                InnovationStatusEnum.DRAFT,
                                "Bạn đã tạo sáng kiến mới '" + savedInnovation.getInnovationName() + "'");

                List<FormDataResponse> formDataResponses = request.getFormDataItems().stream()
                                .map(item -> {
                                        FormDataRequest createRequest = new FormDataRequest();
                                        createRequest.setFieldValue(item.getFieldValue());
                                        createRequest.setFormFieldId(item.getFormFieldId());
                                        createRequest.setInnovationId(savedInnovation.getId());
                                        return formDataService.createFormData(createRequest);
                                })
                                .collect(Collectors.toList());

                // Tạo documentHash từ dữ liệu form
                String documentHash = generateDocumentHash(request.getFormDataItems(), request.getTemplateId());

                InnovationFormDataResponse response = new InnovationFormDataResponse();
                response.setInnovation(innovationMapper.toInnovationResponse(savedInnovation));
                response.setFormDataList(formDataResponses);
                response.setDocumentHash(documentHash);

                return response;
        }

        // 4. Cập nhật FormData sáng kiến (Cập nhật FormData cho sáng kiến đã tồn tại)
        public InnovationFormDataResponse updateInnovationFormData(String innovationId,
                        InnovationFormDataRequest request) {

                String actionType = request.getActionType() != null ? request.getActionType().toUpperCase() : "DRAFT";
                if (!"DRAFT".equals(actionType) && !"SUBMITTED".equals(actionType)) {
                        throw new IdInvalidException(
                                        "Action type chỉ được là DRAFT hoặc SUBMITTED. Các trạng thái khác sẽ được xử lý bởi hội đồng chấm điểm.");
                }

                Innovation innovation = innovationRepository.findById(innovationId)
                                .orElseThrow(() -> new IdInvalidException(
                                                "Không tìm thấy sáng kiến với ID: " + innovationId));

                if (!userService.isOwnerOfInnovation(innovation.getUser().getId())) {
                        throw new IdInvalidException("Bạn không có quyền chỉnh sửa sáng kiến này");
                }

                // Chỉ cho phép chỉnh sửa khi ở trạng thái DRAFT)
                if (innovation.getStatus() != InnovationStatusEnum.DRAFT) {
                        throw new IdInvalidException(
                                        "Chỉ có thể chỉnh sửa sáng kiến ở trạng thái DRAFT. Sáng kiến hiện tại đang ở trạng thái: "
                                                        + innovation.getStatus());
                }

                // Xử lý các mục form data (cập nhật tồn tại hoặc tạo mới)
                List<FormDataResponse> formDataResponses = request.getFormDataItems().stream()
                                .<FormDataResponse>map(item -> {
                                        if (item.getDataId() != null && !item.getDataId().trim().isEmpty()) {
                                                // Update existing form data
                                                UpdateFormDataRequest updateRequest = new UpdateFormDataRequest();
                                                updateRequest.setFieldValue(item.getFieldValue());
                                                updateRequest.setFormFieldId(item.getFormFieldId());
                                                updateRequest.setInnovationId(innovationId);
                                                return formDataService.updateFormData(item.getDataId(), updateRequest);
                                        } else {
                                                // Create new form data
                                                FormDataRequest createRequest = new FormDataRequest();
                                                createRequest.setFieldValue(item.getFieldValue());
                                                createRequest.setFormFieldId(item.getFormFieldId());
                                                createRequest.setInnovationId(innovationId);
                                                return formDataService.createFormData(createRequest);
                                        }
                                })
                                .collect(Collectors.toList());

                // Cập nhật trạng thái sáng kiến nếu SUBMITTED
                if (InnovationStatusEnum.SUBMITTED.name().equals(actionType)) {
                        // Kiểm tra xem đã điền đủ cả 2 mẫu chưa
                        if (!hasCompletedBothTemplates(innovationId)) {
                                throw new IdInvalidException(
                                                "Chỉ có thể SUBMITTED khi đã điền xong cả 2 mẫu form. Vui lòng hoàn thành mẫu còn lại trước khi nộp.");
                        }

                        // Kiểm tra xem cả 2 mẫu đã được ký đủ chưa
                        if (!digitalSignatureService.isBothFormsFullySigned(innovationId)) {
                                throw new IdInvalidException(
                                                "Chỉ có thể SUBMITTED khi cả 2 mẫu đã được ký đủ. Vui lòng hoàn thành chữ ký số cho các mẫu còn lại.");
                        }

                        innovation.setStatus(InnovationStatusEnum.SUBMITTED);
                        innovationRepository.save(innovation);

                        // Tạo activity log
                        activityLogService.createActivityLog(
                                        innovation.getUser().getId(),
                                        innovation.getId(),
                                        innovation.getInnovationName(),
                                        InnovationStatusEnum.SUBMITTED,
                                        "Bạn đã nộp sáng kiến '" + innovation.getInnovationName() + "'");
                }

                // Tạo documentHash từ dữ liệu form
                String documentHash = generateDocumentHash(request.getFormDataItems(), request.getTemplateId());

                InnovationFormDataResponse response = new InnovationFormDataResponse();
                response.setInnovation(innovationMapper.toInnovationResponse(innovation));
                response.setFormDataList(formDataResponses);
                response.setDocumentHash(documentHash);

                return response;
        }

        // 5. Lấy FormData sáng kiến
        public InnovationFormDataResponse getInnovationFormData(String innovationId, String templateId) {

                Innovation innovation = innovationRepository.findById(innovationId)
                                .orElseThrow(() -> new IdInvalidException(
                                                "Không tìm thấy sáng kiến với ID: " + innovationId));

                if (!userService.isOwnerOfInnovation(innovation.getUser().getId())) {
                        throw new IdInvalidException("Bạn không có quyền xem thông tin sáng kiến này");
                }

                List<FormDataResponse> formDataList;
                if (templateId != null) {
                        formDataList = formDataService.getFormDataWithFormFields(innovationId, templateId);
                } else {
                        formDataList = formDataService.getFormDataByInnovationId(innovationId);
                }

                // Tạo documentHash từ dữ liệu form hiện tại
                String documentHash = generateDocumentHashFromFormData(formDataList, templateId);

                InnovationFormDataResponse response = new InnovationFormDataResponse();
                response.setInnovation(innovationMapper.toInnovationResponse(innovation));
                response.setFormDataList(formDataList);
                response.setDocumentHash(documentHash);

                return response;
        }

        // 6. Lấy sáng kiến bởi User và Status
        public ResultPaginationDTO getInnovationsByUserAndStatus(String status, Pageable pageable) {
                if (status == null || status.trim().isEmpty()) {
                        throw new IdInvalidException("Status không được để trống");
                }

                if (pageable.getSort().isUnsorted()) {
                        pageable = org.springframework.data.domain.PageRequest.of(
                                        pageable.getPageNumber(),
                                        pageable.getPageSize(),
                                        org.springframework.data.domain.Sort.by("createdAt").descending());
                }

                try {
                        InnovationStatusEnum statusEnum = InnovationStatusEnum.valueOf(status.toUpperCase());
                        String currentUserId = userService.getCurrentUserId();
                        Page<Innovation> innovations = innovationRepository.findByUserIdAndStatus(currentUserId,
                                        statusEnum,
                                        pageable);
                        Page<InnovationResponse> responses = innovations.map(innovationMapper::toInnovationResponse);
                        return Utils.toResultPaginationDTO(responses, pageable);
                } catch (IllegalArgumentException e) {
                        throw new IdInvalidException("Status không hợp lệ: " + status + ". Các status hợp lệ: " +
                                        java.util.Arrays.toString(InnovationStatusEnum.values()));
                }
        }

        // 7. Thống kê innovation cho giảng viên
        public InnovationStatisticsDTO getInnovationStatisticsForCurrentUser() {
                User currentUser = userService.getCurrentUser();
                String userId = currentUser.getId();

                // Thống kê cơ bản
                long totalInnovations = innovationRepository.countByUserId(userId);

                // Các status cho từng loại
                List<InnovationStatusEnum> submittedStatuses = Arrays.asList(
                                InnovationStatusEnum.DRAFT,
                                InnovationStatusEnum.SUBMITTED,
                                InnovationStatusEnum.PENDING_KHOA_REVIEW,
                                InnovationStatusEnum.KHOA_REVIEWED,
                                InnovationStatusEnum.KHOA_APPROVED,
                                InnovationStatusEnum.PENDING_TRUONG_REVIEW,
                                InnovationStatusEnum.TRUONG_REVIEWED);

                List<InnovationStatusEnum> approvedStatuses = Arrays.asList(
                                InnovationStatusEnum.TRUONG_APPROVED,
                                InnovationStatusEnum.FINAL_APPROVED);

                List<InnovationStatusEnum> rejectedStatuses = Arrays.asList(
                                InnovationStatusEnum.TRUONG_REJECTED,
                                InnovationStatusEnum.KHOA_REJECTED);

                // Đếm số lượng
                long submittedInnovations = innovationRepository.countByUserIdAndStatusIn(userId, submittedStatuses);
                long approvedInnovations = innovationRepository.countByUserIdAndStatusIn(userId, approvedStatuses);
                long rejectedInnovations = innovationRepository.countByUserIdAndStatusIn(userId, rejectedStatuses);

                // Tính phần trăm
                double achievedPercentage = totalInnovations > 0 ? (double) approvedInnovations / totalInnovations * 100
                                : 0.0;
                double notAchievedPercentage = totalInnovations > 0
                                ? (double) rejectedInnovations / totalInnovations * 100
                                : 0.0;
                double pendingPercentage = totalInnovations > 0 ? (double) submittedInnovations / totalInnovations * 100
                                : 0.0;

                return InnovationStatisticsDTO.builder()
                                .totalInnovations(totalInnovations)
                                .submittedInnovations(submittedInnovations)
                                .approvedInnovations(approvedInnovations)
                                .rejectedInnovations(rejectedInnovations)
                                .achievedPercentage(Math.round(achievedPercentage * 100.0) / 100.0)
                                .notAchievedPercentage(Math.round(notAchievedPercentage * 100.0) / 100.0)
                                .pendingPercentage(Math.round(pendingPercentage * 100.0) / 100.0)
                                .build();

                /**
                 * pendingPercentage = DRAFT + SUBMITTED + PENDING_KHOA_REVIEW + KHOA_REVIEWED +
                 * KHOA_APPROVED + PENDING_TRUONG_REVIEW + TRUONG_REVIEWED
                 * achievedPercentage = FINAL_APPROVED + TRUONG_APPROVED
                 * notAchievedPercentage = KHOA_REJECTED + TRUONG_REJECTED
                 */
        }

        // 8.Lấy thống kê sáng kiến theo năm học cho user hiện tại
        public InnovationAcademicYearStatisticsDTO getInnovationStatisticsByAcademicYear(String userId) {
                // Lấy thống kê tổng số sáng kiến theo năm học
                List<Object[]> totalInnovationsByYear = innovationRepository
                                .countInnovationsByAcademicYearAndUserId(userId);
                List<Object[]> submittedInnovationsByYear = innovationRepository
                                .countSubmittedInnovationsByAcademicYearAndUserId(userId);
                List<Object[]> approvedInnovationsByYear = innovationRepository
                                .countApprovedInnovationsByAcademicYearAndUserId(userId);
                List<Object[]> rejectedInnovationsByYear = innovationRepository
                                .countRejectedInnovationsByAcademicYearAndUserId(userId);
                List<Object[]> pendingInnovationsByYear = innovationRepository
                                .countPendingInnovationsByAcademicYearAndUserId(userId);

                Map<String, Long> totalMap = totalInnovationsByYear.stream()
                                .collect(Collectors.toMap(
                                                arr -> (String) arr[0],
                                                arr -> (Long) arr[1]));

                Map<String, Long> submittedMap = submittedInnovationsByYear.stream()
                                .collect(Collectors.toMap(
                                                arr -> (String) arr[0],
                                                arr -> (Long) arr[1]));

                Map<String, Long> approvedMap = approvedInnovationsByYear.stream()
                                .collect(Collectors.toMap(
                                                arr -> (String) arr[0],
                                                arr -> (Long) arr[1]));

                Map<String, Long> rejectedMap = rejectedInnovationsByYear.stream()
                                .collect(Collectors.toMap(
                                                arr -> (String) arr[0],
                                                arr -> (Long) arr[1]));

                Map<String, Long> pendingMap = pendingInnovationsByYear.stream()
                                .collect(Collectors.toMap(
                                                arr -> (String) arr[0],
                                                arr -> (Long) arr[1]));

                // Tạo danh sách tất cả năm học
                Set<String> allAcademicYears = new java.util.HashSet<>();
                allAcademicYears.addAll(totalMap.keySet());
                allAcademicYears.addAll(submittedMap.keySet());
                allAcademicYears.addAll(approvedMap.keySet());
                allAcademicYears.addAll(rejectedMap.keySet());
                allAcademicYears.addAll(pendingMap.keySet());

                // Sắp xếp năm học
                List<String> sortedAcademicYears = allAcademicYears.stream()
                                .sorted()
                                .collect(Collectors.toList());

                // Tạo danh sách dữ liệu theo năm học
                List<InnovationAcademicYearStatisticsDTO.AcademicYearData> academicYearDataList = sortedAcademicYears
                                .stream()
                                .map(academicYear -> {
                                        long totalInnovations = totalMap.getOrDefault(academicYear, 0L);
                                        long submittedInnovations = submittedMap.getOrDefault(academicYear, 0L);
                                        long approvedInnovations = approvedMap.getOrDefault(academicYear, 0L);
                                        long rejectedInnovations = rejectedMap.getOrDefault(academicYear, 0L);
                                        long pendingInnovations = pendingMap.getOrDefault(academicYear, 0L);

                                        // Tính phần trăm
                                        double approvedPercentage = totalInnovations > 0
                                                        ? Math.round((double) approvedInnovations / totalInnovations
                                                                        * 100 * 100.0) / 100.0
                                                        : 0.0;
                                        double rejectedPercentage = totalInnovations > 0
                                                        ? Math.round((double) rejectedInnovations / totalInnovations
                                                                        * 100 * 100.0) / 100.0
                                                        : 0.0;
                                        double pendingPercentage = totalInnovations > 0
                                                        ? Math.round((double) pendingInnovations / totalInnovations
                                                                        * 100 * 100.0) / 100.0
                                                        : 0.0;

                                        return InnovationAcademicYearStatisticsDTO.AcademicYearData.builder()
                                                        .academicYear(academicYear)
                                                        .totalInnovations(totalInnovations)
                                                        .submittedInnovations(submittedInnovations)
                                                        .approvedInnovations(approvedInnovations)
                                                        .rejectedInnovations(rejectedInnovations)
                                                        .pendingInnovations(pendingInnovations)
                                                        .approvedPercentage(approvedPercentage)
                                                        .rejectedPercentage(rejectedPercentage)
                                                        .pendingPercentage(pendingPercentage)
                                                        .build();
                                })
                                .collect(Collectors.toList());

                // Tính tổng số sáng kiến và số năm học
                long totalInnovations = totalMap.values().stream().mapToLong(Long::longValue).sum();

                return InnovationAcademicYearStatisticsDTO.builder()
                                .academicYearData(academicYearDataList)
                                .totalInnovations(totalInnovations)
                                .totalAcademicYears(sortedAcademicYears.size())
                                .build();
        }

        // 9. Lấy thống kê sáng kiến theo năm học cho user hiện tại
        public InnovationAcademicYearStatisticsDTO getInnovationStatisticsByAcademicYearForCurrentUser() {
                User currentUser = userService.getCurrentUser();
                String userId = currentUser.getId();
                return getInnovationStatisticsByAcademicYear(userId);
        }

        // 10. Lấy hạn chót sắp tới từ round hiện tại
        public UpcomingDeadlinesResponse getUpcomingDeadlines() {
                // Lấy round hiện tại
                var currentRound = innovationRoundService.getCurrentRound();
                if (currentRound == null) {
                        return UpcomingDeadlinesResponse.builder()
                                        .upcomingDeadlines(List.of())
                                        .totalDeadlines(0)
                                        .currentRoundName("Không có đợt sáng kiến nào đang diễn ra")
                                        .academicYear("")
                                        .build();
                }

                // Lấy tất cả phases của round hiện tại
                List<InnovationPhase> phases = innovationPhaseRepository
                                .findByInnovationRoundIdOrderByPhaseOrder(currentRound.getId());

                LocalDate today = LocalDate.now();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

                List<UpcomingDeadlineResponse> upcomingDeadlines = phases.stream()
                                .filter(phase -> phase.getPhaseEndDate().isAfter(today)
                                                || phase.getPhaseEndDate().isEqual(today))
                                .map(phase -> {
                                        long daysRemaining = ChronoUnit.DAYS.between(today, phase.getPhaseEndDate());

                                        return UpcomingDeadlineResponse.builder()
                                                        .id(phase.getId())
                                                        .title(generateDeadlineTitle(phase))
                                                        .deadlineDate(phase.getPhaseEndDate())
                                                        .formattedDate(phase.getPhaseEndDate().format(formatter))
                                                        .daysRemaining(daysRemaining)
                                                        .phaseType(phase.getPhaseType().getValue())
                                                        .level(phase.getLevel() != null ? phase.getLevel().getValue()
                                                                        : "")
                                                        .description(phase.getDescription())
                                                        .isDeadline(phase.getIsDeadline())
                                                        .build();
                                })
                                .sorted((a, b) -> a.getDeadlineDate().compareTo(b.getDeadlineDate()))
                                .collect(Collectors.toList());

                return UpcomingDeadlinesResponse.builder()
                                .upcomingDeadlines(upcomingDeadlines)
                                .totalDeadlines(upcomingDeadlines.size())
                                .currentRoundName(currentRound.getName())
                                .academicYear(currentRound.getAcademicYear())
                                .build();
        }

        /*
         * Helper method: Tạo tiêu đề cho deadline dựa trên phase
         */
        private String generateDeadlineTitle(InnovationPhase phase) {
                String baseTitle = phase.getName();

                switch (phase.getPhaseType()) {
                        case SUBMISSION:
                                return "Hạn nộp " + baseTitle;
                        case SCORING:
                                return "Hạn chấm điểm " + baseTitle;
                        case ANNOUNCEMENT:
                                return "Hạn công bố " + baseTitle;
                        default:
                                return baseTitle;
                }
        }

        /*
         * Helper method: Kiểm tra xem innovation đã có form data cho cả 2 template chưa
         */
        private boolean hasCompletedBothTemplates(String innovationId) {
                // Lấy tất cả form data của innovation
                List<FormDataResponse> allFormData = formDataService.getFormDataByInnovationId(innovationId);

                if (allFormData.isEmpty()) {
                        return false;
                }

                // Lấy danh sách các template ID đã có form data
                Set<String> completedTemplateIds = allFormData.stream()
                                .map(FormDataResponse::getTemplateId)
                                .filter(Objects::nonNull)
                                .collect(Collectors.toSet());

                // Kiểm tra xem có ít nhất 2 template khác nhau không
                return completedTemplateIds.size() >= 2;
        }

        /*
         * Helper method: Tạo documentHash từ dữ liệu form
         */
        private String generateDocumentHash(List<InnovationFormDataRequest.FormDataItemRequest> formDataItems,
                        String templateId) {
                try {
                        // Tạo chuỗi dữ liệu để hash
                        StringBuilder dataBuilder = new StringBuilder();
                        dataBuilder.append("templateId:").append(templateId).append("|");

                        // Sắp xếp formDataItems theo formFieldId để đảm bảo tính nhất quán
                        formDataItems.stream()
                                        .sorted((a, b) -> a.getFormFieldId().compareTo(b.getFormFieldId()))
                                        .forEach(item -> {
                                                dataBuilder.append("fieldId:").append(item.getFormFieldId())
                                                                .append("|value:").append(item.getFieldValue())
                                                                .append("|");
                                        });

                        String dataString = dataBuilder.toString();

                        // Tạo SHA-256 hash
                        MessageDigest digest = MessageDigest.getInstance("SHA-256");
                        byte[] hash = digest.digest(dataString.getBytes());
                        String hashString = Base64.getEncoder().encodeToString(hash);

                        return "sha256:" + hashString;
                } catch (NoSuchAlgorithmException e) {
                        throw new IdInvalidException("Không thể tạo document hash: " + e.getMessage(), e);
                }
        }

        /*
         * Helper method: Tạo documentHash từ FormDataResponse
         */
        private String generateDocumentHashFromFormData(List<FormDataResponse> formDataList, String templateId) {
                try {
                        // Tạo chuỗi dữ liệu để hash
                        StringBuilder dataBuilder = new StringBuilder();
                        dataBuilder.append("templateId:").append(templateId).append("|");

                        // Sắp xếp formDataList theo formFieldId để đảm bảo tính nhất quán
                        formDataList.stream()
                                        .sorted((a, b) -> a.getFormFieldId().compareTo(b.getFormFieldId()))
                                        .forEach(item -> {
                                                dataBuilder.append("fieldId:").append(item.getFormFieldId())
                                                                .append("|value:").append(item.getFieldValue())
                                                                .append("|");
                                        });

                        String dataString = dataBuilder.toString();

                        // Tạo SHA-256 hash
                        MessageDigest digest = MessageDigest.getInstance("SHA-256");
                        byte[] hash = digest.digest(dataString.getBytes());
                        String hashString = Base64.getEncoder().encodeToString(hash);

                        return "sha256:" + hashString;
                } catch (NoSuchAlgorithmException e) {
                        throw new IdInvalidException("Không thể tạo document hash: " + e.getMessage(), e);
                }
        }

        // 10. Duyệt sáng kiến
        public InnovationResponse approveInnovation(String innovationId, String reason) {
                Innovation innovation = innovationRepository.findById(innovationId)
                                .orElseThrow(() -> new IdInvalidException(
                                                "Không tìm thấy sáng kiến với ID: " + innovationId));

                // Cập nhật status dựa trên level hiện tại
                InnovationStatusEnum currentStatus = innovation.getStatus();
                InnovationStatusEnum newStatus;

                switch (currentStatus) {
                        case SUBMITTED:
                                newStatus = InnovationStatusEnum.KHOA_APPROVED;
                                break;
                        case KHOA_APPROVED:
                                newStatus = InnovationStatusEnum.TRUONG_APPROVED;
                                break;
                        case TRUONG_APPROVED:
                                newStatus = InnovationStatusEnum.FINAL_APPROVED;
                                break;
                        default:
                                throw new IdInvalidException(
                                                "Không thể duyệt sáng kiến ở trạng thái hiện tại: " + currentStatus);
                }

                innovation.setStatus(newStatus);
                Innovation savedInnovation = innovationRepository.save(innovation);

                // Tạo activity log
                activityLogService.createActivityLog(
                                innovation.getUser().getId(),
                                innovation.getId(),
                                innovation.getInnovationName(),
                                InnovationStatusEnum.KHOA_APPROVED,
                                "Sáng kiến '" + innovation.getInnovationName() + "' đã được duyệt" +
                                                (reason != null ? " - " + reason : ""));

                return innovationMapper.toInnovationResponse(savedInnovation);
        }

        // 11. Từ chối sáng kiến
        public InnovationResponse rejectInnovation(String innovationId, String reason) {
                Innovation innovation = innovationRepository.findById(innovationId)
                                .orElseThrow(() -> new IdInvalidException(
                                                "Không tìm thấy sáng kiến với ID: " + innovationId));

                // Cập nhật status dựa trên level hiện tại
                InnovationStatusEnum currentStatus = innovation.getStatus();
                InnovationStatusEnum newStatus;

                switch (currentStatus) {
                        case SUBMITTED:
                                newStatus = InnovationStatusEnum.KHOA_REJECTED;
                                break;
                        case KHOA_APPROVED:
                                newStatus = InnovationStatusEnum.TRUONG_REJECTED;
                                break;
                        default:
                                throw new IdInvalidException(
                                                "Không thể từ chối sáng kiến ở trạng thái hiện tại: " + currentStatus);
                }

                innovation.setStatus(newStatus);
                Innovation savedInnovation = innovationRepository.save(innovation);

                // Tạo activity log
                activityLogService.createActivityLog(
                                innovation.getUser().getId(),
                                innovation.getId(),
                                innovation.getInnovationName(),
                                InnovationStatusEnum.KHOA_REJECTED,
                                "Sáng kiến '" + innovation.getInnovationName() + "' đã bị từ chối" +
                                                (reason != null ? " - " + reason : ""));

                return innovationMapper.toInnovationResponse(savedInnovation);
        }

}
