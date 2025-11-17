package vn.edu.iuh.fit.innovationmanagementsystem_be.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.Innovation;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.InnovationPhase;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.User;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.FormField;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.FormData;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.InnovationStatusEnum;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.InnovationPhaseTypeEnum;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.FieldTypeEnum;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.UserRoleEnum;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.FormDataRequest;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.CreateInnovationWithTemplatesRequest;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.TemplateDataRequest;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.FormDataResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.FormFieldResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.InnovationFormDataResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.InnovationResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.InnovationStatisticsDTO;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.InnovationAcademicYearStatisticsDTO;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.UpcomingDeadlineResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.UpcomingDeadlinesResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.exception.IdInvalidException;
import vn.edu.iuh.fit.innovationmanagementsystem_be.mapper.InnovationMapper;
import vn.edu.iuh.fit.innovationmanagementsystem_be.mapper.FormFieldMapper;
import vn.edu.iuh.fit.innovationmanagementsystem_be.mapper.FormDataMapper;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.InnovationRepository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.FormDataRepository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.InnovationPhaseRepository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.FormFieldRepository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.FormTemplateRepository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.InnovationRoundRepository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.CoInnovationRepository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.UserRepository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.CoInnovation;

import java.util.ArrayList;
import vn.edu.iuh.fit.innovationmanagementsystem_be.utils.ResultPaginationDTO;
import vn.edu.iuh.fit.innovationmanagementsystem_be.utils.Utils;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.Arrays;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

@Service
@Transactional
public class InnovationService {

        private static final Logger logger = LoggerFactory.getLogger(InnovationService.class);

        private final InnovationRepository innovationRepository;
        private final InnovationPhaseRepository innovationPhaseRepository;
        private final FormDataService formDataService;
        private final InnovationMapper innovationMapper;
        private final FormFieldMapper formFieldMapper;
        private final FormDataMapper formDataMapper;
        private final FormDataRepository formDataRepository;
        private final UserService userService;
        private final DigitalSignatureService digitalSignatureService;
        private final InnovationRoundService innovationRoundService;
        private final ActivityLogService activityLogService;
        private final FormFieldRepository formFieldRepository;
        private final FormTemplateRepository formTemplateRepository;
        private final InnovationRoundRepository innovationRoundRepository;
        private final ObjectMapper objectMapper;
        private final CoInnovationRepository coInnovationRepository;
        private final UserRepository userRepository;

        public InnovationService(InnovationRepository innovationRepository,
                        InnovationPhaseRepository innovationPhaseRepository,
                        FormDataService formDataService,
                        InnovationMapper innovationMapper,
                        FormFieldMapper formFieldMapper,
                        FormDataMapper formDataMapper,
                        FormDataRepository formDataRepository,
                        UserService userService,
                        DigitalSignatureService digitalSignatureService,
                        InnovationRoundService innovationRoundService,
                        ActivityLogService activityLogService,
                        FormFieldRepository formFieldRepository,
                        FormTemplateRepository formTemplateRepository,
                        InnovationRoundRepository innovationRoundRepository,
                        ObjectMapper objectMapper,
                        CoInnovationRepository coInnovationRepository,
                        UserRepository userRepository) {
                this.innovationRepository = innovationRepository;
                this.innovationPhaseRepository = innovationPhaseRepository;
                this.formDataService = formDataService;
                this.innovationMapper = innovationMapper;
                this.formFieldMapper = formFieldMapper;
                this.formDataMapper = formDataMapper;
                this.formDataRepository = formDataRepository;
                this.userService = userService;
                this.digitalSignatureService = digitalSignatureService;
                this.innovationRoundService = innovationRoundService;
                this.activityLogService = activityLogService;
                this.formFieldRepository = formFieldRepository;
                this.formTemplateRepository = formTemplateRepository;
                this.innovationRoundRepository = innovationRoundRepository;
                this.objectMapper = objectMapper;
                this.coInnovationRepository = coInnovationRepository;
                this.userRepository = userRepository;
        }

        // 1. Lấy tất cả sáng kiến của user hiện tại với filter
        public ResultPaginationDTO getAllInnovationsByCurrentUserWithFilter(Specification<Innovation> specification,
                        Pageable pageable) {
                if (pageable.getSort().isUnsorted()) {
                        pageable = PageRequest.of(
                                        pageable.getPageNumber(),
                                        pageable.getPageSize(),
                                        Sort.by("createdAt").descending());
                }

                String currentUserId = userService.getCurrentUserId();

                Specification<Innovation> userSpec = (root, query, criteriaBuilder) -> criteriaBuilder
                                .equal(root.get("user").get("id"), currentUserId);

                Specification<Innovation> combinedSpec = userSpec.and(specification);

                Page<Innovation> innovations = innovationRepository.findAll(combinedSpec, pageable);
                Page<InnovationResponse> responses = innovations.map(innovationMapper::toInnovationResponse);
                return Utils.toResultPaginationDTO(responses, pageable);
        }

        // 2. Lấy tất cả sáng kiến
        public ResultPaginationDTO getAllInnovations(Specification<Innovation> specification, Pageable pageable) {

                if (pageable.getSort().isUnsorted()) {
                        pageable = PageRequest.of(
                                        pageable.getPageNumber(),
                                        pageable.getPageSize(),
                                        Sort.by("createdAt").descending());
                }

                Page<Innovation> innovations = innovationRepository.findAll(specification, pageable);
                Page<InnovationResponse> responses = innovations.map(innovationMapper::toInnovationResponse);
                return Utils.toResultPaginationDTO(responses, pageable);
        }

        // 3. Thống kê innovation cho giảng viên
        public InnovationStatisticsDTO getInnovationStatisticsForCurrentUser() {
                User currentUser = userService.getCurrentUser();
                String userId = currentUser.getId();

                // Thống kê cơ bản
                long totalInnovations = innovationRepository.countByUserId(userId);

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

        // 3. Lấy thống kê sáng kiến theo năm học cho user hiện tại - OK
        public InnovationAcademicYearStatisticsDTO getInnovationStatisticsByAcademicYearForCurrentUser() {
                User currentUser = userService.getCurrentUser();
                String userId = currentUser.getId();
                return getInnovationStatisticsByAcademicYear(userId);
        }

        /**
         * Helper method: Lấy thống kê sáng kiến theo năm học cho user hiện tại
         */
        private InnovationAcademicYearStatisticsDTO getInnovationStatisticsByAcademicYear(String userId) {
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

        // 4. Lấy hạn chót sắp tới từ round hiện tại - OK
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

        // 4. Tạo Innovation & Submit FormData theo nhiều Template
        public InnovationFormDataResponse createInnovationWithMultipleTemplates(
                        CreateInnovationWithTemplatesRequest request) {

                if (request.getStatus() != InnovationStatusEnum.DRAFT
                                && request.getStatus() != InnovationStatusEnum.SUBMITTED) {
                        throw new IdInvalidException(
                                        "Status chỉ được là DRAFT hoặc SUBMITTED. Các trạng thái khác sẽ được xử lý bởi hội đồng chấm điểm.");
                }

                InnovationPhase innovationPhase;
                if (request.getInnovationPhaseId() != null && !request.getInnovationPhaseId().isEmpty()) {
                        innovationPhase = innovationPhaseRepository.findById(request.getInnovationPhaseId())
                                        .orElseThrow(() -> new IdInvalidException(
                                                        "Không tìm thấy giai đoạn sáng kiến với ID: "
                                                                        + request.getInnovationPhaseId()));
                } else {
                        innovationPhase = innovationPhaseRepository
                                        .findSubmissionPhaseByOpenRound(InnovationPhaseTypeEnum.SUBMISSION)
                                        .orElseThrow(() -> new IdInvalidException(
                                                        "Không tìm thấy giai đoạn SUBMISSION của round OPEN. Vui lòng kiểm tra lại."));
                }

                User currentUser = userService.getCurrentUser();

                // Tạo Innovation
                Innovation innovation = new Innovation();
                innovation.setInnovationName(request.getInnovationName());
                innovation.setUser(currentUser);
                innovation.setDepartment(currentUser.getDepartment());
                innovation.setInnovationPhase(innovationPhase);
                innovation.setInnovationRound(innovationPhase.getInnovationRound());
                innovation.setIsScore(request.getIsScore() != null ? request.getIsScore() : false);
                innovation.setStatus(request.getStatus());
                innovation.setBasisText(request.getBasisText());

                Innovation savedInnovation = innovationRepository.save(innovation);

                // Tạo activity log
                activityLogService.createActivityLog(
                                currentUser.getId(),
                                savedInnovation.getId(),
                                savedInnovation.getInnovationName(),
                                request.getStatus(),
                                "Bạn đã tạo sáng kiến mới '" + savedInnovation.getInnovationName() + "'");

                // Lưu FormData cho tất cả các template
                List<FormDataResponse> formDataResponses = new ArrayList<>();

                for (TemplateDataRequest templateRequest : request.getTemplates()) {
                        if (templateRequest.getFormData() != null && !templateRequest.getFormData().isEmpty()) {
                                // Tìm FormField theo templateId
                                List<FormField> formFields = formFieldRepository
                                                .findByTemplateId(templateRequest.getTemplateId());

                                for (Map.Entry<String, Object> entry : templateRequest.getFormData().entrySet()) {
                                        String fieldKey = entry.getKey();
                                        Object fieldValue = entry.getValue();

                                        // Tìm FormField theo fieldKey trong danh sách formFields (bao gồm cả children)
                                        FormFieldSearchResult searchResult = findFormFieldByKeyWithParent(formFields,
                                                        fieldKey, templateRequest.getTemplateId());

                                        if (searchResult == null || searchResult.getFormField() == null) {
                                                throw new IdInvalidException(
                                                                "Không tìm thấy FormField với fieldKey: "
                                                                                + fieldKey + " trong template "
                                                                                + templateRequest
                                                                                                .getTemplateId());
                                        }

                                        FormField targetFormField = searchResult.getFormField();

                                        // Nếu field nằm trong children của parent field, cần lấy parent field
                                        if (searchResult.hasParentField()) {
                                                // Dùng parent field để lưu FormData
                                                FormField parentField = searchResult.getParentField();
                                                // Tạo một JSON object chứa fieldKey và value để lưu vào fieldValue
                                                ObjectNode dataNode = objectMapper.createObjectNode();
                                                dataNode.put("fieldKey", fieldKey);
                                                dataNode.set("value",
                                                                fieldValue != null
                                                                                ? objectMapper.valueToTree(fieldValue)
                                                                                : objectMapper.valueToTree(""));

                                                FormDataRequest formDataRequest = new FormDataRequest();
                                                formDataRequest.setFieldValue(dataNode);
                                                formDataRequest.setFormFieldId(parentField.getId());
                                                formDataRequest.setInnovationId(savedInnovation.getId());

                                                FormDataResponse formDataResponse = formDataService
                                                                .createFormData(formDataRequest);
                                                formDataResponses.add(formDataResponse);
                                        } else {
                                                // Field ở level root, lưu bình thường
                                                FormDataRequest formDataRequest = new FormDataRequest();

                                                // Chuyển đổi fieldValue sang JsonNode
                                                JsonNode jsonNodeValue = null;
                                                if (fieldValue != null) {
                                                        if (fieldValue instanceof String) {
                                                                // Nếu là String, wrap trong JSON string
                                                                jsonNodeValue = objectMapper.valueToTree(fieldValue);
                                                        } else {
                                                                // Nếu là object phức tạp, chuyển sang JsonNode
                                                                jsonNodeValue = objectMapper.valueToTree(fieldValue);
                                                        }
                                                } else {
                                                        jsonNodeValue = objectMapper.valueToTree("");
                                                }

                                                formDataRequest.setFieldValue(jsonNodeValue);
                                                formDataRequest.setFormFieldId(targetFormField.getId());
                                                formDataRequest.setInnovationId(savedInnovation.getId());

                                                FormDataResponse formDataResponse = formDataService
                                                                .createFormData(formDataRequest);
                                                formDataResponses.add(formDataResponse);
                                        }
                                }
                        }
                }

                // Xử lý đồng sáng kiến từ formData
                processCoInnovations(savedInnovation, request.getTemplates());

                InnovationFormDataResponse response = new InnovationFormDataResponse();
                response.setInnovation(innovationMapper.toInnovationResponse(savedInnovation));
                response.setFormDataList(formDataResponses);
                response.setDocumentHash(null);

                return response;
        }

        // 5. Lấy Innovation & FormData theo ID (bao gồm FormField đầy đủ)
        public InnovationFormDataResponse getInnovationWithFormDataById(String innovationId) {
                logger.info("===== GET INNOVATION WITH FORM DATA BY ID =====");
                logger.info("Innovation ID: {}", innovationId);

                // Lấy Innovation
                Innovation innovation = innovationRepository.findById(innovationId)
                                .orElseThrow(() -> {
                                        logger.error("Không tìm thấy sáng kiến với ID: {}", innovationId);
                                        return new IdInvalidException(
                                                        "Không tìm thấy sáng kiến với ID: " + innovationId);
                                });

                logger.info("Innovation found: {} - {}", innovation.getId(), innovation.getInnovationName());

                // Lấy tất cả FormData của innovation (đã có FormField được load với relations)
                List<FormData> formDataList = formDataRepository.findByInnovationIdWithRelations(innovationId);
                logger.info("FormData count found: {}", formDataList.size());

                if (formDataList.isEmpty()) {
                        logger.warn("Không tìm thấy FormData nào cho Innovation ID: {}", innovationId);
                        // Kiểm tra xem có FormData nào trong database không (không có relations)
                        long totalFormData = formDataRepository.count();
                        logger.info("Total FormData records in database: {}", totalFormData);

                        // Kiểm tra bằng cách query trực tiếp
                        List<FormData> allFormDataForInnovation = formDataRepository.findAll().stream()
                                        .filter(fd -> fd.getInnovation() != null &&
                                                        innovationId.equals(fd.getInnovation().getId()))
                                        .collect(Collectors.toList());
                        logger.info("FormData found with direct query (no relations): {}",
                                        allFormDataForInnovation.size());
                }

                // Map FormData sang FormDataResponse và thêm FormFieldResponse đầy đủ
                List<FormDataResponse> formDataResponses = new ArrayList<>();

                for (FormData formData : formDataList) {
                        logger.debug("Processing FormData ID: {}", formData.getId());
                        logger.debug("FormData fieldValue: {}", formData.getFieldValue());

                        FormDataResponse formDataResponse = formDataMapper.toFormDataResponse(formData);
                        logger.debug("Mapped FormDataResponse ID: {}, formFieldKey: {}",
                                        formDataResponse.getId(), formDataResponse.getFormFieldKey());

                        // Đảm bảo FormField được load đầy đủ với formTemplate
                        if (formData.getFormField() != null) {
                                FormField formField = formData.getFormField();
                                logger.debug("FormField found: ID={}, fieldKey={}, label={}",
                                                formField.getId(), formField.getFieldKey(), formField.getLabel());

                                // Nếu formTemplate chưa được load hoặc cần refresh, load lại
                                if (formField.getFormTemplate() == null) {
                                        logger.warn("FormTemplate is null for FormField ID: {}, loading again...",
                                                        formField.getId());
                                        formField = formFieldRepository.findByIdWithTemplate(formField.getId())
                                                        .orElse(formField);
                                }

                                if (formField.getFormTemplate() != null) {
                                        logger.debug("FormTemplate loaded: ID={}",
                                                        formField.getFormTemplate().getId());
                                }

                                // Map FormField đầy đủ sang FormFieldResponse
                                FormFieldResponse formFieldResponse = formFieldMapper
                                                .toFormFieldResponse(formField);
                                // formDataResponse.setFormField(formFieldResponse);
                                logger.debug("FormFieldResponse mapped with formTemplateId: {}",
                                                formFieldResponse.getFormTemplateId());
                        } else {
                                logger.warn("FormField is null for FormData ID: {}", formData.getId());
                        }

                        formDataResponses.add(formDataResponse);
                }

                logger.info("Total FormDataResponse created: {}", formDataResponses.size());

                // Tạo response
                InnovationFormDataResponse response = new InnovationFormDataResponse();
                response.setInnovation(innovationMapper.toInnovationResponse(innovation));
                response.setFormDataList(formDataResponses);
                response.setDocumentHash(null);

                logger.info("===== END GET INNOVATION WITH FORM DATA =====");
                return response;
        }

        // // 4. Cập nhật FormData sáng kiến (Cập nhật FormData cho sáng kiến đã tồn
        // tại)
        // public InnovationFormDataResponse updateInnovationFormData(String
        // innovationId,
        // InnovationFormDataRequest request) {

        // String actionType = request.getActionType() != null ?
        // request.getActionType().toUpperCase() : "DRAFT";
        // if (!"DRAFT".equals(actionType) && !"SUBMITTED".equals(actionType)) {
        // throw new IdInvalidException(
        // "Action type chỉ được là DRAFT hoặc SUBMITTED. Các trạng thái khác sẽ được xử
        // lý bởi hội đồng chấm điểm.");
        // }

        // Innovation innovation = innovationRepository.findById(innovationId)
        // .orElseThrow(() -> new IdInvalidException(
        // "Không tìm thấy sáng kiến với ID: " + innovationId));

        // if (!userService.isOwnerOfInnovation(innovation.getUser().getId())) {
        // throw new IdInvalidException("Bạn không có quyền chỉnh sửa sáng kiến này");
        // }

        // // Chỉ cho phép chỉnh sửa khi ở trạng thái DRAFT)
        // if (innovation.getStatus() != InnovationStatusEnum.DRAFT) {
        // throw new IdInvalidException(
        // "Chỉ có thể chỉnh sửa sáng kiến ở trạng thái DRAFT. Sáng kiến hiện tại đang ở
        // trạng thái: "
        // + innovation.getStatus());
        // }

        // // Xử lý các mục form data (cập nhật tồn tại hoặc tạo mới)
        // List<FormDataResponse> formDataResponses =
        // request.getFormDataItems().stream()
        // .<FormDataResponse>map(item -> {
        // if (item.getDataId() != null && !item.getDataId().trim().isEmpty()) {
        // // Update existing form data
        // UpdateFormDataRequest updateRequest = new UpdateFormDataRequest();
        // updateRequest.setFieldValue(item.getFieldValue());
        // updateRequest.setFormFieldId(item.getFormFieldId());
        // updateRequest.setInnovationId(innovationId);
        // return formDataService.updateFormData(item.getDataId(), updateRequest);
        // } else {
        // // Create new form data
        // FormDataRequest createRequest = new FormDataRequest();
        // createRequest.setFieldValue(item.getFieldValue());
        // createRequest.setFormFieldId(item.getFormFieldId());
        // createRequest.setInnovationId(innovationId);
        // return formDataService.createFormData(createRequest);
        // }
        // })
        // .collect(Collectors.toList());

        // // Cập nhật trạng thái sáng kiến nếu SUBMITTED
        // if (InnovationStatusEnum.SUBMITTED.name().equals(actionType)) {
        // // Kiểm tra xem đã điền đủ cả 2 mẫu chưa
        // if (!hasCompletedBothTemplates(innovationId)) {
        // throw new IdInvalidException(
        // "Chỉ có thể SUBMITTED khi đã điền xong cả 2 mẫu form. Vui lòng hoàn thành mẫu
        // còn lại trước khi nộp.");
        // }

        // // Kiểm tra xem cả 2 mẫu đã được ký đủ chưa
        // if (!digitalSignatureService.isBothFormsFullySigned(innovationId)) {
        // throw new IdInvalidException(
        // "Chỉ có thể SUBMITTED khi cả 2 mẫu đã được ký đủ. Vui lòng hoàn thành chữ ký
        // số cho các mẫu còn lại.");
        // }

        // innovation.setStatus(InnovationStatusEnum.SUBMITTED);
        // innovationRepository.save(innovation);

        // // Tạo activity log
        // activityLogService.createActivityLog(
        // innovation.getUser().getId(),
        // innovation.getId(),
        // innovation.getInnovationName(),
        // InnovationStatusEnum.SUBMITTED,
        // "Bạn đã nộp sáng kiến '" + innovation.getInnovationName() + "'");
        // }

        // // Tạo documentHash từ dữ liệu form
        // String documentHash = generateDocumentHash(request.getFormDataItems(),
        // request.getTemplateId());

        // InnovationFormDataResponse response = new InnovationFormDataResponse();
        // response.setInnovation(innovationMapper.toInnovationResponse(innovation));
        // response.setFormDataList(formDataResponses);
        // response.setDocumentHash(documentHash);

        // return response;
        // }

        // // 5. Lấy FormData sáng kiến
        // public InnovationFormDataResponse getInnovationFormData(String innovationId,
        // String templateId) {

        // Innovation innovation = innovationRepository.findById(innovationId)
        // .orElseThrow(() -> new IdInvalidException(
        // "Không tìm thấy sáng kiến với ID: " + innovationId));

        // if (!userService.isOwnerOfInnovation(innovation.getUser().getId())) {
        // throw new IdInvalidException("Bạn không có quyền xem thông tin sáng kiến
        // này");
        // }

        // List<FormDataResponse> formDataList;
        // if (templateId != null) {
        // formDataList = formDataService.getFormDataWithFormFields(innovationId,
        // templateId);
        // } else {
        // formDataList = formDataService.getFormDataByInnovationId(innovationId);
        // }

        // // Tạo documentHash từ dữ liệu form hiện tại
        // String documentHash = generateDocumentHashFromFormData(formDataList,
        // templateId);

        // InnovationFormDataResponse response = new InnovationFormDataResponse();
        // response.setInnovation(innovationMapper.toInnovationResponse(innovation));
        // response.setFormDataList(formDataList);
        // response.setDocumentHash(documentHash);

        // return response;
        // }

        // /*
        // * Helper method: Kiểm tra xem innovation đã có form data cho cả 2 template
        // chưa
        // */
        // private boolean hasCompletedBothTemplates(String innovationId) {
        // // Lấy tất cả form data của innovation
        // List<FormDataResponse> allFormData =
        // formDataService.getFormDataByInnovationId(innovationId);

        // if (allFormData.isEmpty()) {
        // return false;
        // }

        // // Lấy danh sách các template ID đã có form data
        // Set<String> completedTemplateIds = allFormData.stream()
        // .map(FormDataResponse::getTemplateId)
        // .filter(Objects::nonNull)
        // .collect(Collectors.toSet());

        // // Kiểm tra xem có ít nhất 2 template khác nhau không
        // return completedTemplateIds.size() >= 2;
        // }

        // // 10. Duyệt sáng kiến
        // public InnovationResponse approveInnovation(String innovationId, String
        // reason) {
        // Innovation innovation = innovationRepository.findById(innovationId)
        // .orElseThrow(() -> new IdInvalidException(
        // "Không tìm thấy sáng kiến với ID: " + innovationId));

        // // Cập nhật status dựa trên level hiện tại
        // InnovationStatusEnum currentStatus = innovation.getStatus();
        // InnovationStatusEnum newStatus;

        // switch (currentStatus) {
        // case SUBMITTED:
        // newStatus = InnovationStatusEnum.KHOA_APPROVED;
        // break;
        // case KHOA_APPROVED:
        // newStatus = InnovationStatusEnum.TRUONG_APPROVED;
        // break;
        // case TRUONG_APPROVED:
        // newStatus = InnovationStatusEnum.FINAL_APPROVED;
        // break;
        // default:
        // throw new IdInvalidException(
        // "Không thể duyệt sáng kiến ở trạng thái hiện tại: " + currentStatus);
        // }

        // innovation.setStatus(newStatus);
        // Innovation savedInnovation = innovationRepository.save(innovation);

        // // Tạo activity log
        // activityLogService.createActivityLog(
        // innovation.getUser().getId(),
        // innovation.getId(),
        // innovation.getInnovationName(),
        // InnovationStatusEnum.KHOA_APPROVED,
        // "Sáng kiến '" + innovation.getInnovationName() + "' đã được duyệt" +
        // (reason != null ? " - " + reason : ""));

        // return innovationMapper.toInnovationResponse(savedInnovation);
        // }

        // // 11. Từ chối sáng kiến
        // public InnovationResponse rejectInnovation(String innovationId, String
        // reason) {
        // Innovation innovation = innovationRepository.findById(innovationId)
        // .orElseThrow(() -> new IdInvalidException(
        // "Không tìm thấy sáng kiến với ID: " + innovationId));

        // // Cập nhật status dựa trên level hiện tại
        // InnovationStatusEnum currentStatus = innovation.getStatus();
        // InnovationStatusEnum newStatus;

        // switch (currentStatus) {
        // case SUBMITTED:
        // newStatus = InnovationStatusEnum.KHOA_REJECTED;
        // break;
        // case KHOA_APPROVED:
        // newStatus = InnovationStatusEnum.TRUONG_REJECTED;
        // break;
        // default:
        // throw new IdInvalidException(
        // "Không thể từ chối sáng kiến ở trạng thái hiện tại: " + currentStatus);
        // }

        // innovation.setStatus(newStatus);
        // Innovation savedInnovation = innovationRepository.save(innovation);

        // // Tạo activity log
        // activityLogService.createActivityLog(
        // innovation.getUser().getId(),
        // innovation.getId(),
        // innovation.getInnovationName(),
        // InnovationStatusEnum.KHOA_REJECTED,
        // "Sáng kiến '" + innovation.getInnovationName() + "' đã bị từ chối" +
        // (reason != null ? " - " + reason : ""));

        // return innovationMapper.toInnovationResponse(savedInnovation);
        // }

        /**
         * Tìm FormField theo fieldKey trong danh sách formFields và cả trong children
         * Trả về result chứa FormField và parent field (nếu có)
         */
        private FormFieldSearchResult findFormFieldByKeyWithParent(List<FormField> formFields, String fieldKey,
                        String templateId) {
                // Tìm ở level đầu tiên
                for (FormField field : formFields) {
                        if (fieldKey.equals(field.getFieldKey())) {
                                return new FormFieldSearchResult(field, null);
                        }

                        // Nếu field có children, tìm trong children
                        if (field.getChildren() != null && field.getChildren().isArray()) {
                                FormField foundField = findFormFieldInChildren(field.getChildren(), fieldKey,
                                                templateId);
                                if (foundField != null) {
                                        // Trả về parent field
                                        return new FormFieldSearchResult(foundField, field);
                                }
                        }

                        // Nếu field có tableConfig (dùng cho TABLE field), tìm trong columns
                        if (field.getFieldType() != null && field.getFieldType() == FieldTypeEnum.TABLE
                                        && field.getTableConfig() != null) {
                                FormField foundField = findFormFieldInTableColumns(field.getTableConfig(), fieldKey,
                                                templateId);
                                if (foundField != null) {
                                        // Trả về parent field (TABLE field)
                                        return new FormFieldSearchResult(foundField, field);
                                }
                        }
                }

                return null;
        }

        /**
         * Inner class để lưu kết quả tìm kiếm FormField
         */
        private static class FormFieldSearchResult {
                private FormField formField;
                private FormField parentField;

                public FormFieldSearchResult(FormField formField, FormField parentField) {
                        this.formField = formField;
                        this.parentField = parentField;
                }

                public FormField getFormField() {
                        return formField;
                }

                public FormField getParentField() {
                        return parentField;
                }

                public boolean hasParentField() {
                        return parentField != null;
                }
        }

        /**
         * Tạo FormField từ JSON node
         */
        private FormField createFormFieldFromJson(JsonNode childNode, String templateId) {
                FormField field = new FormField();

                // Map tất cả các field từ JSON
                JsonNode idNode = childNode.get("id");
                if (idNode != null) {
                        field.setId(idNode.asText());
                }

                if (childNode.get("fieldKey") != null) {
                        field.setFieldKey(childNode.get("fieldKey").asText());
                }

                if (childNode.get("label") != null) {
                        field.setLabel(childNode.get("label").asText());
                }

                JsonNode typeNode = childNode.get("type");
                if (typeNode != null) {
                        try {
                                field.setFieldType(FieldTypeEnum.valueOf(typeNode.asText()));
                        } catch (IllegalArgumentException e) {
                                field.setFieldType(null);
                        }
                }

                JsonNode requiredNode = childNode.get("required");
                if (requiredNode != null) {
                        field.setRequired(requiredNode.asBoolean());
                }

                JsonNode isReadOnlyNode = childNode.get("isReadOnly");
                if (isReadOnlyNode != null) {
                        field.setIsReadOnly(isReadOnlyNode.asBoolean());
                }

                JsonNode optionsNode = childNode.get("options");
                if (optionsNode != null) {
                        field.setOptions(optionsNode);
                }

                JsonNode referenceConfigNode = childNode.get("referenceConfig");
                if (referenceConfigNode != null) {
                        field.setReferenceConfig(referenceConfigNode);
                }

                JsonNode userDataConfigNode = childNode.get("userDataConfig");
                if (userDataConfigNode != null) {
                        field.setUserDataConfig(userDataConfigNode);
                }

                JsonNode innovationDataConfigNode = childNode.get("innovationDataConfig");
                if (innovationDataConfigNode != null) {
                        field.setInnovationDataConfig(innovationDataConfigNode);
                }

                JsonNode contributionConfigNode = childNode.get("contributionConfig");
                if (contributionConfigNode != null) {
                        field.setContributionConfig(contributionConfigNode);
                }

                JsonNode signingRoleNode = childNode.get("signingRole");
                if (signingRoleNode != null) {
                        try {
                                field.setSigningRole(UserRoleEnum.valueOf(signingRoleNode.asText()));
                        } catch (IllegalArgumentException e) {
                                field.setSigningRole(null);
                        }
                }

                JsonNode tableConfigNode = childNode.get("tableConfig");
                if (tableConfigNode != null) {
                        field.setTableConfig(tableConfigNode);
                }

                JsonNode repeatableNode = childNode.get("repeatable");
                if (repeatableNode != null) {
                        field.setRepeatable(repeatableNode.asBoolean());
                }

                // Lấy formTemplate từ templateId
                if (templateId != null) {
                        field.setFormTemplate(formTemplateRepository.findById(templateId).orElse(null));
                }

                return field;
        }

        /**
         * Tìm FormField trong children JSON node
         */
        private FormField findFormFieldInChildren(JsonNode childrenNode, String fieldKey, String templateId) {
                try {
                        for (JsonNode childNode : childrenNode) {
                                JsonNode childFieldKeyNode = childNode.get("fieldKey");
                                if (childFieldKeyNode != null && fieldKey.equals(childFieldKeyNode.asText())) {
                                        // Children trong database chỉ lưu dưới dạng JSON, không phải entity riêng
                                        // Nên tạo FormField object từ JSON
                                        return createFormFieldFromJson(childNode, templateId);
                                }

                                // Nếu child cũng có children, đệ quy tìm
                                JsonNode childChildrenNode = childNode.get("children");
                                if (childChildrenNode != null && childChildrenNode.isArray()) {
                                        FormField foundField = findFormFieldInChildren(childChildrenNode, fieldKey,
                                                        templateId);
                                        if (foundField != null) {
                                                return foundField;
                                        }
                                }
                        }
                } catch (Exception e) {
                        throw new IdInvalidException("Lỗi khi tìm FormField trong children: " + e.getMessage());
                }

                return null;
        }

        /**
         * Tìm FormField trong tableConfig.columns
         */
        private FormField findFormFieldInTableColumns(JsonNode tableConfig, String fieldKey, String templateId) {
                try {
                        JsonNode columnsNode = tableConfig.get("columns");
                        if (columnsNode != null && columnsNode.isArray()) {
                                for (JsonNode columnNode : columnsNode) {
                                        JsonNode columnKeyNode = columnNode.get("key");
                                        if (columnKeyNode != null && fieldKey.equals(columnKeyNode.asText())) {
                                                // Tạo FormField object từ column JSON
                                                return createFormFieldFromJson(columnNode, templateId);
                                        }
                                }
                        }
                } catch (Exception e) {
                        throw new IdInvalidException("Lỗi khi tìm FormField trong tableConfig: " + e.getMessage());
                }

                return null;
        }

        // 5. Xử lý đồng sáng kiến từ formData
        private void processCoInnovations(Innovation innovation, List<TemplateDataRequest> templates) {
                // Xóa các đồng sáng kiến cũ (nếu có)
                coInnovationRepository.deleteByInnovationId(innovation.getId());

                // Duyệt qua tất cả templates để tìm field chứa thông tin đồng sáng kiến
                for (TemplateDataRequest templateRequest : templates) {
                        if (templateRequest.getFormData() == null || templateRequest.getFormData().isEmpty()) {
                                continue;
                        }

                        for (Map.Entry<String, Object> entry : templateRequest.getFormData().entrySet()) {
                                String fieldKey = entry.getKey();
                                Object fieldValue = entry.getValue();

                                // Kiểm tra xem fieldKey có bắt đầu bằng "bang_dong" không (đồng tác giả)
                                if (fieldKey != null && fieldKey.startsWith("bang_dong") && fieldValue != null) {
                                        try {
                                                // Chuyển đổi fieldValue sang JsonNode để xử lý
                                                JsonNode valueNode = objectMapper.valueToTree(fieldValue);

                                                // Kiểm tra xem có phải là mảng không
                                                if (valueNode.isArray()) {
                                                        // Duyệt qua từng item trong mảng
                                                        for (JsonNode itemNode : valueNode) {
                                                                processCoInnovationItem(innovation, itemNode);
                                                        }
                                                }
                                        } catch (Exception e) {
                                                throw new IdInvalidException(
                                                                "Lỗi khi xử lý đồng sáng kiến: " + e.getMessage());
                                        }
                                }
                        }
                }
        }

        // 6. Xử lý một item đồng sáng kiến
        private void processCoInnovationItem(Innovation innovation, JsonNode itemNode) {
                try {
                        // Extract thông tin từ itemNode
                        JsonNode hoVaTenNode = itemNode.get("ho_va_ten");
                        JsonNode maNhanSuNode = itemNode.get("ma_nhan_su");
                        JsonNode noiCongTacNode = itemNode.get("noi_cong_tac_hoac_noi_thuong_tru");

                        // Kiểm tra mã nhân sự - bắt buộc phải có để tìm User
                        if (maNhanSuNode == null || maNhanSuNode.isNull()
                                        || maNhanSuNode.asText().trim().isEmpty()) {
                                throw new IdInvalidException(
                                                "Không tìm thấy mã nhân sự trong item đồng sáng kiến, bỏ qua item này");
                        }

                        String maNhanSu = maNhanSuNode.asText().trim();
                        String hoVaTen = hoVaTenNode != null && !hoVaTenNode.isNull()
                                        ? hoVaTenNode.asText().trim()
                                        : "";
                        String noiCongTac = noiCongTacNode != null && !noiCongTacNode.isNull()
                                        ? noiCongTacNode.asText().trim()
                                        : "";

                        // Tìm User theo mã nhân sự
                        Optional<User> userOpt = userRepository.findByPersonnelId(maNhanSu);

                        if (userOpt.isEmpty()) {
                                throw new IdInvalidException(
                                                "Không tìm thấy User với mã nhân sự: " + maNhanSu);
                        }

                        User user = userOpt.get();

                        // Kiểm tra xem đã có CoInnovation cho user này và innovation này chưa
                        boolean exists = coInnovationRepository.findByInnovationId(innovation.getId()).stream()
                                        .anyMatch(co -> co.getUser().getId().equals(user.getId()));

                        if (exists) {
                                return; // Đã tồn tại, bỏ qua
                        }

                        // Tạo CoInnovation mới
                        CoInnovation coInnovation = new CoInnovation();
                        coInnovation.setInnovation(innovation);
                        coInnovation.setUser(user);
                        // Sử dụng tên từ formData nếu có, nếu không thì dùng tên từ User
                        coInnovation.setCoInnovatorFullName(
                                        !hoVaTen.isEmpty() ? hoVaTen : user.getFullName());
                        coInnovation.setCoInnovatorDepartmentName(
                                        !noiCongTac.isEmpty() ? noiCongTac
                                                        : (user.getDepartment() != null
                                                                        ? user.getDepartment().getDepartmentName()
                                                                        : ""));

                        // Tạo contactInfo từ các thông tin có sẵn
                        StringBuilder contactInfo = new StringBuilder();
                        if (user.getEmail() != null && !user.getEmail().isEmpty()) {
                                contactInfo.append("Email: ").append(user.getEmail());
                        }
                        if (user.getPersonnelId() != null && !user.getPersonnelId().isEmpty()) {
                                if (contactInfo.length() > 0) {
                                        contactInfo.append("; ");
                                }
                                contactInfo.append("Mã NV: ").append(user.getPersonnelId());
                        }
                        coInnovation.setContactInfo(
                                        contactInfo.length() > 0 ? contactInfo.toString()
                                                        : "Chưa có thông tin liên hệ");

                        coInnovationRepository.save(coInnovation);
                } catch (Exception e) {
                        throw new IdInvalidException(
                                        "Lỗi khi xử lý item đồng sáng kiến: " + e.getMessage());
                }
        }

}
