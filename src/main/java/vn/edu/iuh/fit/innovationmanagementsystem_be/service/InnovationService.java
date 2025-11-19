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

import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.Attachment;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.Innovation;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.InnovationPhase;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.User;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.FormField;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.FormData;
// import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.AttachmentTypeEnum;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.InnovationStatusEnum;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.InnovationPhaseTypeEnum;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.FieldTypeEnum;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.UserRoleEnum;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.PhaseStatusEnum;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.TemplateTypeEnum;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.DocumentTypeEnum;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.FormTemplate;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.FormDataRequest;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.CreateInnovationWithTemplatesRequest;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.TemplateDataRequest;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.DigitalSignatureRequest;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.FormDataResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.FormFieldResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.InnovationFormDataResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.InnovationResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.MyInnovationResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.InnovationStatisticsDTO;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.InnovationAcademicYearStatisticsDTO;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.UpcomingDeadlineResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.UpcomingDeadlinesResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.TemplateSignatureResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.TemplateFormDataResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.exception.IdInvalidException;
import vn.edu.iuh.fit.innovationmanagementsystem_be.mapper.InnovationMapper;
import vn.edu.iuh.fit.innovationmanagementsystem_be.mapper.FormFieldMapper;
import vn.edu.iuh.fit.innovationmanagementsystem_be.mapper.FormDataMapper;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.AttachmentRepository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.InnovationRepository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.FormDataRepository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.InnovationPhaseRepository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.FormFieldRepository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.FormTemplateRepository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.CoInnovationRepository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.UserRepository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.DepartmentPhaseRepository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.UserSignatureProfileRepository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.DigitalSignatureRepository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.CoInnovation;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.DepartmentPhase;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.UserSignatureProfile;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.CAStatusEnum;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Collections;
import vn.edu.iuh.fit.innovationmanagementsystem_be.utils.ResultPaginationDTO;
import vn.edu.iuh.fit.innovationmanagementsystem_be.utils.Utils;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.Arrays;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
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
        private final ObjectMapper objectMapper;
        private final CoInnovationRepository coInnovationRepository;
        private final UserRepository userRepository;
        private final NotificationService notificationService;
        private final DepartmentPhaseRepository departmentPhaseRepository;
        private final AttachmentRepository attachmentRepository;
        private final FileService fileService;
        private final PdfGeneratorService pdfGeneratorService;
        private final UserSignatureProfileRepository userSignatureProfileRepository;
        private final CertificateAuthorityService certificateAuthorityService;
        private final DigitalSignatureRepository digitalSignatureRepository;

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
                        ObjectMapper objectMapper,
                        CoInnovationRepository coInnovationRepository,
                        UserRepository userRepository,
                        NotificationService notificationService,
                        DepartmentPhaseRepository departmentPhaseRepository,
                        AttachmentRepository attachmentRepository,
                        FileService fileService,
                        PdfGeneratorService pdfGeneratorService,
                        UserSignatureProfileRepository userSignatureProfileRepository,
                        CertificateAuthorityService certificateAuthorityService,
                        DigitalSignatureRepository digitalSignatureRepository) {
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
                this.objectMapper = objectMapper;
                this.coInnovationRepository = coInnovationRepository;
                this.userRepository = userRepository;
                this.notificationService = notificationService;
                this.departmentPhaseRepository = departmentPhaseRepository;
                this.attachmentRepository = attachmentRepository;
                this.fileService = fileService;
                this.pdfGeneratorService = pdfGeneratorService;
                this.userSignatureProfileRepository = userSignatureProfileRepository;
                this.certificateAuthorityService = certificateAuthorityService;
                this.digitalSignatureRepository = digitalSignatureRepository;
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

                List<String> innovationIds = innovations.getContent().stream()
                                .map(Innovation::getId)
                                .collect(Collectors.toList());

                List<FormData> allFormData = innovationIds.isEmpty()
                                ? new ArrayList<>()
                                : formDataRepository.findByInnovationIdsWithRelations(innovationIds);

                Map<String, Integer> authorCountMap = allFormData.stream()
                                .filter(fd -> fd.getFormField() != null
                                                && "danh_sach_tac_gia".equals(fd.getFormField().getFieldKey()))
                                .collect(Collectors.groupingBy(
                                                fd -> fd.getInnovation().getId(),
                                                Collectors.collectingAndThen(
                                                                Collectors.toList(),
                                                                list -> list.stream()
                                                                                .mapToInt(this::countAuthorsFromFormData)
                                                                                .max()
                                                                                .orElse(0))));

                Page<MyInnovationResponse> responses = innovations.map(innovation -> {
                        int authorCount = authorCountMap.getOrDefault(innovation.getId(), 0);
                        return toMyInnovationResponse(innovation, authorCount);
                });
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
                Page<InnovationResponse> responses = innovations.map(innovation -> {
                        InnovationResponse response = innovationMapper.toInnovationResponse(innovation);
                        response.setSubmissionTimeRemainingSeconds(getSubmissionTimeRemainingSeconds(innovation));
                        return response;
                });
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

                // Kiểm tra CA status của user trước khi tạo/nộp sáng kiến
                validateUserCAStatus(currentUser, request.getStatus());

                // 4.1 Kiểm tra DepartmentPhase SUBMISSION của khoa hiện tại
                Innovation existingInnovation = null;

                if (currentUser.getDepartment() != null && innovationPhase.getInnovationRound() != null) {
                        Optional<DepartmentPhase> departmentPhaseOpt = departmentPhaseRepository
                                        .findByDepartmentIdAndInnovationRoundIdAndPhaseType(
                                                        currentUser.getDepartment().getId(),
                                                        innovationPhase.getInnovationRound().getId(),
                                                        InnovationPhaseTypeEnum.SUBMISSION);

                        DepartmentPhase departmentPhase = departmentPhaseOpt.orElseThrow(() -> new IdInvalidException(
                                        "Khoa của bạn chưa được cấu hình giai đoạn SUBMISSION cho đợt này. Vui lòng liên hệ quản trị viên."));

                        if (!(departmentPhase.getPhaseStatus() == PhaseStatusEnum.SCHEDULED
                                        || departmentPhase.getPhaseStatus() == PhaseStatusEnum.ACTIVE)) {
                                throw new IdInvalidException(
                                                "Bạn chỉ được tạo/nộp sáng kiến khi giai đoạn SUBMISSION của khoa đang ở trạng thái ĐÃ LÊN LỊCH hoặc ĐANG HOẠT ĐỘNG.");
                        }
                }

                // Xử lý trường hợp update DRAFT (cả khi status là DRAFT hoặc SUBMITTED)
                if (request.getInnovationId() != null && !request.getInnovationId().isEmpty()) {
                        existingInnovation = innovationRepository.findById(request.getInnovationId())
                                        .orElseThrow(() -> new IdInvalidException(
                                                        "Không tìm thấy sáng kiến với ID: "
                                                                        + request.getInnovationId()));

                        // Kiểm tra quyền sở hữu
                        if (!existingInnovation.getUser().getId().equals(currentUser.getId())) {
                                throw new IdInvalidException("Bạn không có quyền chỉnh sửa sáng kiến này");
                        }

                        // Nếu status là SUBMITTED, kiểm tra innovation hiện tại phải là DRAFT
                        if (request.getStatus() == InnovationStatusEnum.SUBMITTED) {
                                if (existingInnovation.getStatus() != InnovationStatusEnum.DRAFT) {
                                        throw new IdInvalidException(
                                                        "Chỉ có thể nộp sáng kiến ở trạng thái DRAFT. Sáng kiến hiện tại đang ở trạng thái: "
                                                                        + existingInnovation.getStatus());
                                }

                                // Kiểm tra phase phải khớp khi nộp
                                if (existingInnovation.getInnovationPhase().getId() != null
                                                && !existingInnovation.getInnovationPhase().getId()
                                                                .equals(innovationPhase.getId())) {
                                        throw new IdInvalidException(
                                                        "Không thể nộp sáng kiến vì giai đoạn không khớp. Vui lòng kiểm tra lại.");
                                }
                        }

                        // Nếu status là DRAFT, chỉ cho phép update nếu innovation hiện tại cũng là
                        // DRAFT
                        if (request.getStatus() == InnovationStatusEnum.DRAFT) {
                                if (existingInnovation.getStatus() != InnovationStatusEnum.DRAFT) {
                                        throw new IdInvalidException(
                                                        "Chỉ có thể cập nhật sáng kiến ở trạng thái DRAFT. Sáng kiến hiện tại đang ở trạng thái: "
                                                                        + existingInnovation.getStatus());
                                }
                        }
                }

                // Kiểm tra allow_late_submission của department phase (chỉ khi nộp sáng kiến)
                LocalDate submissionDeadlineDate = null;
                List<SignatureProcessingResult> signatureResults = Collections.emptyList();
                if (request.getStatus() == InnovationStatusEnum.SUBMITTED
                                && currentUser.getDepartment() != null
                                && innovationPhase.getInnovationRound() != null) {
                        Optional<DepartmentPhase> departmentPhaseOpt = departmentPhaseRepository
                                        .findByDepartmentIdAndInnovationRoundIdAndPhaseType(
                                                        currentUser.getDepartment().getId(),
                                                        innovationPhase.getInnovationRound().getId(),
                                                        InnovationPhaseTypeEnum.SUBMISSION);

                        if (departmentPhaseOpt.isPresent()) {
                                DepartmentPhase departmentPhase = departmentPhaseOpt.get();
                                LocalDate currentDate = LocalDate.now();
                                LocalDate phaseEndDate = departmentPhase.getPhaseEndDate();
                                submissionDeadlineDate = phaseEndDate;

                                // Kiểm tra xem có vượt quá deadline không
                                if (currentDate.isAfter(phaseEndDate)) {
                                        // Đã vượt quá deadline
                                        if (Boolean.FALSE.equals(departmentPhase.getAllowLateSubmission())) {
                                                // Không cho phép nộp trễ
                                                throw new IdInvalidException(
                                                                "Đã hết hạn nộp sáng kiến. Hạn nộp là: "
                                                                                + phaseEndDate.format(DateTimeFormatter
                                                                                                .ofPattern("dd/MM/yyyy"))
                                                                                + ". Khoa không cho phép nộp trễ.");
                                        } else {
                                                // Cho phép nộp trễ, nhưng cần kiểm tra xem giai đoạn SCORING hoặc
                                                // ANNOUNCEMENT đã active chưa
                                                // Kiểm tra xem có phase SCORING hoặc ANNOUNCEMENT đang ACTIVE không
                                                List<DepartmentPhase> scoringPhases = departmentPhaseRepository
                                                                .findByDepartmentIdAndInnovationRoundId(
                                                                                currentUser.getDepartment().getId(),
                                                                                innovationPhase.getInnovationRound()
                                                                                                .getId())
                                                                .stream()
                                                                .filter(dp -> (dp
                                                                                .getPhaseType() == InnovationPhaseTypeEnum.SCORING
                                                                                || dp.getPhaseType() == InnovationPhaseTypeEnum.ANNOUNCEMENT)
                                                                                && dp.getPhaseStatus() == PhaseStatusEnum.ACTIVE
                                                                                && !currentDate.isBefore(
                                                                                                dp.getPhaseStartDate())
                                                                                && !currentDate.isAfter(
                                                                                                dp.getPhaseEndDate()))
                                                                .collect(Collectors.toList());

                                                if (!scoringPhases.isEmpty()) {
                                                        // Đã có giai đoạn SCORING hoặc ANNOUNCEMENT đang active, không
                                                        // cho phép nộp
                                                        String activePhaseNames = scoringPhases.stream()
                                                                        .map(DepartmentPhase::getName)
                                                                        .collect(Collectors.joining(", "));
                                                        throw new IdInvalidException(
                                                                        "Không thể nộp sáng kiến vì giai đoạn "
                                                                                        + activePhaseNames
                                                                                        + " đang trong thời gian hoạt động. Vui lòng liên hệ quản trị viên để biết thêm chi tiết.");
                                                }

                                                // Cho phép nộp trễ, lưu deadline date để FE tự tính toán
                                                Long lateDays = ChronoUnit.DAYS.between(phaseEndDate, currentDate);
                                                logger.info("Người dùng nộp sáng kiến trễ {} ngày. Hạn nộp: {}, Ngày nộp: {}",
                                                                lateDays, phaseEndDate, currentDate);
                                        }
                                }
                        }
                }

                // Kiểm tra chữ ký trước khi nộp sáng kiến (chỉ khi status = SUBMITTED)
                if (request.getStatus() == InnovationStatusEnum.SUBMITTED) {
                        validateSignaturesBeforeSubmit(request);
                }

                // Tạo hoặc update Innovation
                Innovation savedInnovation;
                if (existingInnovation != null) {
                        // Update existing DRAFT innovation
                        existingInnovation.setInnovationName(request.getInnovationName());
                        existingInnovation.setStatus(request.getStatus());
                        existingInnovation.setIsScore(request.getIsScore() != null ? request.getIsScore() : false);
                        existingInnovation.setBasisText(request.getBasisText());
                        savedInnovation = innovationRepository.save(existingInnovation);

                        // Xóa FormData cũ, CoInnovation cũ và Attachment cũ
                        formDataRepository.deleteByInnovationId(savedInnovation.getId());
                        coInnovationRepository.deleteByInnovationId(savedInnovation.getId());
                        attachmentRepository.deleteByInnovationId(savedInnovation.getId());

                        // Tạo activity log
                        String activityMessage = request.getStatus() == InnovationStatusEnum.DRAFT
                                        ? "Bạn đã cập nhật bản nháp sáng kiến '" + savedInnovation.getInnovationName()
                                                        + "'"
                                        : "Bạn đã nộp sáng kiến '" + savedInnovation.getInnovationName() + "'";
                        activityLogService.createActivityLog(
                                        currentUser.getId(),
                                        savedInnovation.getId(),
                                        savedInnovation.getInnovationName(),
                                        request.getStatus(),
                                        activityMessage);
                } else {
                        // Tạo Innovation mới
                        Innovation innovation = new Innovation();
                        innovation.setInnovationName(request.getInnovationName());
                        innovation.setUser(currentUser);
                        innovation.setDepartment(currentUser.getDepartment());
                        innovation.setInnovationPhase(innovationPhase);
                        innovation.setInnovationRound(innovationPhase.getInnovationRound());
                        innovation.setIsScore(request.getIsScore() != null ? request.getIsScore() : false);
                        innovation.setStatus(request.getStatus());
                        innovation.setBasisText(request.getBasisText());

                        savedInnovation = innovationRepository.save(innovation);

                        // Tạo activity log
                        String activityMessage = request.getStatus() == InnovationStatusEnum.DRAFT
                                        ? "Bạn đã tạo bản nháp sáng kiến '" + savedInnovation.getInnovationName() + "'"
                                        : "Bạn đã tạo sáng kiến mới '" + savedInnovation.getInnovationName() + "'";
                        activityLogService.createActivityLog(
                                        currentUser.getId(),
                                        savedInnovation.getId(),
                                        savedInnovation.getInnovationName(),
                                        request.getStatus(),
                                        activityMessage);
                }

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

                // Tạo Attachment từ formData (tạo liên kết giữa Innovation và Attachment)
                createAttachmentsFromFormData(savedInnovation.getId());

                // Xử lý đồng sáng kiến từ formData
                processCoInnovations(savedInnovation, request.getTemplates());

                if (request.getStatus() == InnovationStatusEnum.SUBMITTED) {
                        signatureResults = signInnovationDocuments(savedInnovation, request);
                }

                // Gửi thông báo cho user khi tạo sáng kiến thành công
                try {
                        notificationService.notifyUserOnInnovationCreated(
                                        currentUser.getId(),
                                        savedInnovation.getId(),
                                        savedInnovation.getInnovationName(),
                                        savedInnovation.getStatus());
                } catch (Exception e) {
                        logger.error("Lỗi khi gửi thông báo tạo sáng kiến: {}", e.getMessage(), e);
                }

                InnovationFormDataResponse response = new InnovationFormDataResponse();
                InnovationResponse innovationResponse = innovationMapper.toInnovationResponse(savedInnovation);

                // Tính số giây còn lại/trễ từ deadline
                Long timeRemainingSeconds = null;
                if (submissionDeadlineDate != null) {
                        timeRemainingSeconds = calculateTimeRemainingSeconds(submissionDeadlineDate);
                } else {
                        timeRemainingSeconds = getSubmissionTimeRemainingSeconds(savedInnovation);
                }
                innovationResponse.setSubmissionTimeRemainingSeconds(timeRemainingSeconds);
                response.setInnovation(innovationResponse);
                // response.setFormDataList(formDataResponses);
                response.setTemplates(buildTemplateFormDataResponses(formDataResponses));
                response.setTemplateSignatures(buildTemplateSignatureResponses(signatureResults));
                response.setSubmissionTimeRemainingSeconds(timeRemainingSeconds);

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
                InnovationResponse innovationResponse = innovationMapper.toInnovationResponse(innovation);
                Long timeRemainingSeconds = getSubmissionTimeRemainingSeconds(innovation);
                innovationResponse.setSubmissionTimeRemainingSeconds(timeRemainingSeconds);
                response.setInnovation(innovationResponse);
                // response.setFormDataList(formDataResponses);
                response.setTemplates(buildTemplateFormDataResponses(formDataResponses));
                response.setTemplateSignatures(Collections.emptyList());
                response.setSubmissionTimeRemainingSeconds(timeRemainingSeconds);

                logger.info("===== END GET INNOVATION WITH FORM DATA =====");
                return response;
        }

        // 6. Lấy Innovation & FormData theo ID của user hiện tại (chỉ cho phép xem sáng
        // kiến của chính mình)
        public InnovationFormDataResponse getMyInnovationWithFormDataById(String innovationId) {
                logger.info("===== GET MY INNOVATION WITH FORM DATA BY ID =====");
                logger.info("Innovation ID: {}", innovationId);

                String currentUserId = userService.getCurrentUserId();

                Innovation innovation = innovationRepository.findById(innovationId)
                                .orElseThrow(() -> {
                                        logger.error("Không tìm thấy sáng kiến với ID: {}", innovationId);
                                        return new IdInvalidException(
                                                        "Không tìm thấy sáng kiến với ID: " + innovationId);
                                });

                if (innovation.getUser() == null || !innovation.getUser().getId().equals(currentUserId)) {
                        logger.error("User {} không có quyền xem sáng kiến {}", currentUserId, innovationId);
                        throw new IdInvalidException("Bạn không có quyền xem sáng kiến này");
                }

                logger.info("Innovation found: {} - {}", innovation.getId(), innovation.getInnovationName());

                List<FormData> formDataList = formDataRepository.findByInnovationIdWithRelations(innovationId);
                logger.info("FormData count found: {}", formDataList.size());

                List<FormDataResponse> formDataResponses = new ArrayList<>();

                for (FormData formData : formDataList) {
                        logger.debug("Processing FormData ID: {}", formData.getId());

                        FormDataResponse formDataResponse = formDataMapper.toFormDataResponse(formData);

                        if (formData.getFormField() != null) {
                                FormField formField = formData.getFormField();

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

                                FormFieldResponse formFieldResponse = formFieldMapper
                                                .toFormFieldResponse(formField);
                                logger.debug("FormFieldResponse mapped with formTemplateId: {}",
                                                formFieldResponse.getFormTemplateId());
                        } else {
                                logger.warn("FormField is null for FormData ID: {}", formData.getId());
                        }

                        formDataResponses.add(formDataResponse);
                }

                logger.info("Total FormDataResponse created: {}", formDataResponses.size());

                InnovationFormDataResponse response = new InnovationFormDataResponse();
                InnovationResponse innovationResponse = innovationMapper.toInnovationResponse(innovation);
                Long timeRemainingSeconds = getSubmissionTimeRemainingSeconds(innovation);
                innovationResponse.setSubmissionTimeRemainingSeconds(timeRemainingSeconds);
                response.setInnovation(innovationResponse);
                // response.setFormDataList(formDataResponses);
                response.setTemplates(buildTemplateFormDataResponses(formDataResponses));
                response.setTemplateSignatures(Collections.emptyList());
                response.setSubmissionTimeRemainingSeconds(timeRemainingSeconds);

                logger.info("===== END GET MY INNOVATION WITH FORM DATA =====");
                return response;
        }

        // 7. Lấy Innovation & FormData theo ID cho QUAN_TRI_VIEN_KHOA và TRUONG_KHOA
        // (chỉ cho phép xem sáng kiến của phòng ban mình)
        public InnovationFormDataResponse getDepartmentInnovationWithFormDataById(String innovationId) {
                logger.info("===== GET DEPARTMENT INNOVATION WITH FORM DATA BY ID =====");
                logger.info("Innovation ID: {}", innovationId);

                User currentUser = userService.getCurrentUser();

                boolean hasQuanTriVienKhoaRole = currentUser.getUserRoles().stream()
                                .anyMatch(userRole -> userRole.getRole()
                                                .getRoleName() == UserRoleEnum.QUAN_TRI_VIEN_KHOA);

                boolean hasTruongKhoaRole = currentUser.getUserRoles().stream()
                                .anyMatch(userRole -> userRole.getRole().getRoleName() == UserRoleEnum.TRUONG_KHOA);

                if (!hasQuanTriVienKhoaRole && !hasTruongKhoaRole) {
                        logger.error("User {} không có quyền QUAN_TRI_VIEN_KHOA hoặc TRUONG_KHOA", currentUser.getId());
                        throw new IdInvalidException(
                                        "Chỉ QUAN_TRI_VIEN_KHOA hoặc TRUONG_KHOA mới có quyền xem sáng kiến của phòng ban");
                }

                Innovation innovation = innovationRepository.findById(innovationId)
                                .orElseThrow(() -> {
                                        logger.error("Không tìm thấy sáng kiến với ID: {}", innovationId);
                                        return new IdInvalidException(
                                                        "Không tìm thấy sáng kiến với ID: " + innovationId);
                                });

                if (innovation.getDepartment() == null || currentUser.getDepartment() == null) {
                        logger.error("Innovation hoặc User không có phòng ban");
                        throw new IdInvalidException("Không thể xác định phòng ban của sáng kiến hoặc người dùng");
                }

                if (!innovation.getDepartment().getId().equals(currentUser.getDepartment().getId())) {
                        logger.error("User {} không có quyền xem sáng kiến {} của phòng ban khác", currentUser.getId(),
                                        innovationId);
                        throw new IdInvalidException(
                                        "Bạn chỉ có thể xem sáng kiến của phòng ban mình. Phòng ban của sáng kiến: "
                                                        + innovation.getDepartment().getDepartmentName()
                                                        + ", Phòng ban của bạn: "
                                                        + currentUser.getDepartment().getDepartmentName());
                }

                logger.info("Innovation found: {} - {}", innovation.getId(), innovation.getInnovationName());

                List<FormData> formDataList = formDataRepository.findByInnovationIdWithRelations(innovationId);
                logger.info("FormData count found: {}", formDataList.size());

                List<FormDataResponse> formDataResponses = new ArrayList<>();

                for (FormData formData : formDataList) {
                        logger.debug("Processing FormData ID: {}", formData.getId());

                        FormDataResponse formDataResponse = formDataMapper.toFormDataResponse(formData);

                        if (formData.getFormField() != null) {
                                FormField formField = formData.getFormField();

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

                                FormFieldResponse formFieldResponse = formFieldMapper
                                                .toFormFieldResponse(formField);
                                logger.debug("FormFieldResponse mapped with formTemplateId: {}",
                                                formFieldResponse.getFormTemplateId());
                        } else {
                                logger.warn("FormField is null for FormData ID: {}", formData.getId());
                        }

                        formDataResponses.add(formDataResponse);
                }

                logger.info("Total FormDataResponse created: {}", formDataResponses.size());

                InnovationFormDataResponse response = new InnovationFormDataResponse();
                InnovationResponse innovationResponse = innovationMapper.toInnovationResponse(innovation);
                Long timeRemainingSeconds = getSubmissionTimeRemainingSeconds(innovation);
                innovationResponse.setSubmissionTimeRemainingSeconds(timeRemainingSeconds);
                response.setInnovation(innovationResponse);
                // response.setFormDataList(formDataResponses);
                response.setTemplates(buildTemplateFormDataResponses(formDataResponses));
                response.setTemplateSignatures(Collections.emptyList());
                response.setSubmissionTimeRemainingSeconds(timeRemainingSeconds);

                logger.info("===== END GET DEPARTMENT INNOVATION WITH FORM DATA =====");
                return response;
        }

        // 8. Lấy tất cả sáng kiến của phòng ban với filter cho QUAN_TRI_VIEN_KHOA và
        // TRUONG_KHOA
        public ResultPaginationDTO getAllDepartmentInnovationsWithFilter(Specification<Innovation> specification,
                        Pageable pageable) {
                if (pageable.getSort().isUnsorted()) {
                        pageable = PageRequest.of(
                                        pageable.getPageNumber(),
                                        pageable.getPageSize(),
                                        Sort.by("createdAt").descending());
                }

                User currentUser = userService.getCurrentUser();

                boolean hasQuanTriVienKhoaRole = currentUser.getUserRoles().stream()
                                .anyMatch(userRole -> userRole.getRole()
                                                .getRoleName() == UserRoleEnum.QUAN_TRI_VIEN_KHOA);

                boolean hasTruongKhoaRole = currentUser.getUserRoles().stream()
                                .anyMatch(userRole -> userRole.getRole().getRoleName() == UserRoleEnum.TRUONG_KHOA);

                if (!hasQuanTriVienKhoaRole && !hasTruongKhoaRole) {
                        logger.error("User {} không có quyền QUAN_TRI_VIEN_KHOA hoặc TRUONG_KHOA", currentUser.getId());
                        throw new IdInvalidException(
                                        "Chỉ QUAN_TRI_VIEN_KHOA hoặc TRUONG_KHOA mới có quyền xem sáng kiến của phòng ban");
                }

                if (currentUser.getDepartment() == null) {
                        logger.error("User {} không có phòng ban", currentUser.getId());
                        throw new IdInvalidException("Không thể xác định phòng ban của người dùng");
                }

                String departmentId = currentUser.getDepartment().getId();

                Specification<Innovation> departmentSpec = (root, query, criteriaBuilder) -> criteriaBuilder
                                .equal(root.get("department").get("id"), departmentId);

                Specification<Innovation> notDraftSpec = (root, query, criteriaBuilder) -> criteriaBuilder
                                .notEqual(root.get("status"), InnovationStatusEnum.DRAFT);

                Specification<Innovation> combinedSpec = departmentSpec.and(notDraftSpec).and(specification);

                Page<Innovation> innovations = innovationRepository.findAll(combinedSpec, pageable);

                List<String> innovationIds = innovations.getContent().stream()
                                .map(Innovation::getId)
                                .collect(Collectors.toList());

                List<FormData> allFormData = innovationIds.isEmpty()
                                ? new ArrayList<>()
                                : formDataRepository.findByInnovationIdsWithRelations(innovationIds);

                Map<String, Integer> authorCountMap = allFormData.stream()
                                .filter(fd -> fd.getFormField() != null
                                                && "danh_sach_tac_gia".equals(fd.getFormField().getFieldKey()))
                                .collect(Collectors.groupingBy(
                                                fd -> fd.getInnovation().getId(),
                                                Collectors.collectingAndThen(
                                                                Collectors.toList(),
                                                                list -> list.stream()
                                                                                .mapToInt(this::countAuthorsFromFormData)
                                                                                .max()
                                                                                .orElse(0))));

                Page<MyInnovationResponse> responses = innovations.map(innovation -> {
                        int authorCount = authorCountMap.getOrDefault(innovation.getId(), 0);
                        return toMyInnovationResponse(innovation, authorCount);
                });
                return Utils.toResultPaginationDTO(responses, pageable);
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
         * Kiểm tra CA status của user trước khi tạo/nộp sáng kiến
         */
        private void validateUserCAStatus(User user, InnovationStatusEnum status) {
                // Lấy user signature profile
                Optional<UserSignatureProfile> signatureProfileOpt = userSignatureProfileRepository
                                .findByUserId(user.getId());

                if (signatureProfileOpt.isEmpty()) {
                        // Nếu user chưa có signature profile, cho phép tạo DRAFT nhưng không cho phép
                        // SUBMITTED
                        if (status == InnovationStatusEnum.SUBMITTED) {
                                throw new IdInvalidException(
                                                "Bạn chưa có hồ sơ chữ ký số. Vui lòng tạo hồ sơ chữ ký số trước khi nộp sáng kiến.");
                        }
                        return;
                }

                UserSignatureProfile signatureProfile = signatureProfileOpt.get();

                // Kiểm tra certificate status của user
                CAStatusEnum certificateStatus = signatureProfile.getCertificateStatus();
                if (certificateStatus == CAStatusEnum.EXPIRED || certificateStatus == CAStatusEnum.REVOKED) {
                        throw new IdInvalidException(
                                        "Không thể tạo/nộp sáng kiến vì chứng chỉ số của bạn đã "
                                                        + (certificateStatus == CAStatusEnum.EXPIRED ? "hết hạn"
                                                                        : "bị thu hồi")
                                                        + ". Vui lòng cập nhật chứng chỉ số trước khi tiếp tục.");
                }

                // Kiểm tra CA status nếu có CA liên kết
                if (signatureProfile.getCertificateAuthority() != null) {
                        String caId = signatureProfile.getCertificateAuthority().getId();
                        boolean canUse = certificateAuthorityService.canUseCAForSigning(caId);
                        if (!canUse) {
                                throw new IdInvalidException(
                                                "Không thể tạo/nộp sáng kiến vì CA nội bộ của bạn không thể sử dụng. "
                                                                + "CA chưa được xác minh hoặc đã hết hạn. Vui lòng liên hệ quản trị viên.");
                        }
                }
        }

        /**
         * Kiểm tra chữ ký tác giả cho mẫu 1 (DON_DE_NGHI) và mẫu 2 (BAO_CAO_MO_TA)
         * trước khi nộp sáng kiến
         */
        private void validateSignaturesBeforeSubmit(CreateInnovationWithTemplatesRequest request) {
                List<String> missingSignatures = new ArrayList<>();

                // Duyệt qua tất cả các template trong request
                for (TemplateDataRequest templateRequest : request.getTemplates()) {
                        // Lấy FormTemplate để kiểm tra templateType
                        FormTemplate formTemplate = formTemplateRepository
                                        .findById(templateRequest.getTemplateId())
                                        .orElse(null);

                        // Chỉ kiểm tra chữ ký cho mẫu 1 (DON_DE_NGHI) và mẫu 2 (BAO_CAO_MO_TA)
                        if (formTemplate == null
                                        || (formTemplate.getTemplateType() != TemplateTypeEnum.DON_DE_NGHI
                                                        && formTemplate.getTemplateType() != TemplateTypeEnum.BAO_CAO_MO_TA)) {
                                continue;
                        }

                        // Lấy tất cả FormField của template
                        List<FormField> formFields = formFieldRepository
                                        .findByTemplateId(templateRequest.getTemplateId());

                        // Lấy tất cả FormField có fieldType = SIGNATURE và signingRole = GIANG_VIEN
                        // (tác giả)
                        // (bao gồm cả trong children và table columns)
                        List<FormField> authorSignatureFields = getAuthorSignatureFields(formFields,
                                        templateRequest.getTemplateId());

                        // Lấy formData từ request (có thể null hoặc empty)
                        Map<String, Object> formData = templateRequest.getFormData();
                        if (formData == null) {
                                formData = Map.of();
                        }

                        // Kiểm tra từng signature field của tác giả
                        for (FormField signatureField : authorSignatureFields) {
                                String fieldKey = signatureField.getFieldKey();
                                String fieldLabel = signatureField.getLabel();

                                // Kiểm tra xem fieldKey có trong formData không
                                if (!formData.containsKey(fieldKey)) {
                                        missingSignatures.add(fieldLabel + " (Template: "
                                                        + formTemplate.getTemplateType().getValue() + ")");
                                        continue;
                                }

                                // Kiểm tra fieldValue không null và không empty
                                Object fieldValue = formData.get(fieldKey);
                                if (fieldValue == null) {
                                        missingSignatures.add(fieldLabel + " (Template: "
                                                        + formTemplate.getTemplateType().getValue() + ")");
                                        continue;
                                }

                                // Kiểm tra nếu là String thì không được empty
                                if (fieldValue instanceof String && ((String) fieldValue).trim().isEmpty()) {
                                        missingSignatures.add(fieldLabel + " (Template: "
                                                        + formTemplate.getTemplateType().getValue() + ")");
                                        continue;
                                }

                                // Kiểm tra nếu là JsonNode thì không được null hoặc empty
                                if (fieldValue instanceof JsonNode) {
                                        JsonNode jsonNode = (JsonNode) fieldValue;
                                        if (jsonNode.isNull() || jsonNode.isMissingNode()
                                                        || (jsonNode.isTextual()
                                                                        && jsonNode.asText().trim().isEmpty())) {
                                                missingSignatures.add(fieldLabel + " (Template: "
                                                                + formTemplate.getTemplateType().getValue() + ")");
                                        }
                                }
                        }
                }

                // Nếu có chữ ký bị thiếu, throw exception
                if (!missingSignatures.isEmpty()) {
                        String missingList = String.join(", ", missingSignatures);
                        throw new IdInvalidException(
                                        "Không thể nộp sáng kiến vì thiếu chữ ký. Các chữ ký còn thiếu: " + missingList
                                                        + ". Vui lòng kiểm tra lại và ký đầy đủ trước khi nộp.");
                }
        }

        /**
         * Lấy tất cả FormField có fieldType = SIGNATURE và signingRole = GIANG_VIEN
         * (tác giả)
         * (bao gồm cả trong children và table columns)
         */
        private List<FormField> getAuthorSignatureFields(List<FormField> formFields, String templateId) {
                List<FormField> signatureFields = new ArrayList<>();

                for (FormField field : formFields) {
                        // Kiểm tra field ở level root - chỉ lấy SIGNATURE của tác giả (GIANG_VIEN)
                        if (field.getFieldType() == FieldTypeEnum.SIGNATURE
                                        && field.getSigningRole() == UserRoleEnum.GIANG_VIEN) {
                                signatureFields.add(field);
                        }

                        // Kiểm tra trong children
                        if (field.getChildren() != null && field.getChildren().isArray()) {
                                List<FormField> childSignatureFields = getAuthorSignatureFieldsFromChildren(
                                                field.getChildren(),
                                                templateId);
                                signatureFields.addAll(childSignatureFields);
                        }

                        // Kiểm tra trong table columns
                        if (field.getFieldType() == FieldTypeEnum.TABLE && field.getTableConfig() != null) {
                                List<FormField> tableSignatureFields = getAuthorSignatureFieldsFromTableColumns(
                                                field.getTableConfig(), templateId);
                                signatureFields.addAll(tableSignatureFields);
                        }
                }

                return signatureFields;
        }

        /**
         * Lấy các FormField SIGNATURE của tác giả (GIANG_VIEN) từ children JSON node
         */
        private List<FormField> getAuthorSignatureFieldsFromChildren(JsonNode childrenNode, String templateId) {
                List<FormField> signatureFields = new ArrayList<>();

                try {
                        for (JsonNode childNode : childrenNode) {
                                JsonNode typeNode = childNode.get("type");
                                if (typeNode != null) {
                                        try {
                                                FieldTypeEnum fieldType = FieldTypeEnum.valueOf(typeNode.asText());
                                                if (fieldType == FieldTypeEnum.SIGNATURE) {
                                                        // Kiểm tra signingRole = GIANG_VIEN
                                                        JsonNode signingRoleNode = childNode.get("signingRole");
                                                        if (signingRoleNode != null) {
                                                                try {
                                                                        UserRoleEnum signingRole = UserRoleEnum
                                                                                        .valueOf(signingRoleNode
                                                                                                        .asText());
                                                                        if (signingRole == UserRoleEnum.GIANG_VIEN) {
                                                                                FormField signatureField = createFormFieldFromJson(
                                                                                                childNode,
                                                                                                templateId);
                                                                                signatureFields.add(signatureField);
                                                                        }
                                                                } catch (IllegalArgumentException e) {
                                                                        // Ignore invalid enum values
                                                                }
                                                        }
                                                }
                                        } catch (IllegalArgumentException e) {
                                                // Ignore invalid enum values
                                        }
                                }

                                // Đệ quy tìm trong children của children
                                JsonNode childChildrenNode = childNode.get("children");
                                if (childChildrenNode != null && childChildrenNode.isArray()) {
                                        signatureFields.addAll(
                                                        getAuthorSignatureFieldsFromChildren(childChildrenNode,
                                                                        templateId));
                                }
                        }
                } catch (Exception e) {
                        logger.error("Lỗi khi lấy signature fields từ children: {}", e.getMessage(), e);
                }

                return signatureFields;
        }

        /**
         * Lấy các FormField SIGNATURE của tác giả (GIANG_VIEN) từ table columns
         */
        private List<FormField> getAuthorSignatureFieldsFromTableColumns(JsonNode tableConfig, String templateId) {
                List<FormField> signatureFields = new ArrayList<>();

                try {
                        JsonNode columnsNode = tableConfig.get("columns");
                        if (columnsNode != null && columnsNode.isArray()) {
                                for (JsonNode columnNode : columnsNode) {
                                        JsonNode typeNode = columnNode.get("type");
                                        if (typeNode != null) {
                                                try {
                                                        FieldTypeEnum fieldType = FieldTypeEnum
                                                                        .valueOf(typeNode.asText());
                                                        if (fieldType == FieldTypeEnum.SIGNATURE) {
                                                                // Kiểm tra signingRole = GIANG_VIEN
                                                                JsonNode signingRoleNode = columnNode
                                                                                .get("signingRole");
                                                                if (signingRoleNode != null) {
                                                                        try {
                                                                                UserRoleEnum signingRole = UserRoleEnum
                                                                                                .valueOf(signingRoleNode
                                                                                                                .asText());
                                                                                if (signingRole == UserRoleEnum.GIANG_VIEN) {
                                                                                        FormField signatureField = createFormFieldFromJson(
                                                                                                        columnNode,
                                                                                                        templateId);
                                                                                        signatureFields.add(
                                                                                                        signatureField);
                                                                                }
                                                                        } catch (IllegalArgumentException e) {
                                                                                // Ignore invalid enum values
                                                                        }
                                                                }
                                                        }
                                                } catch (IllegalArgumentException e) {
                                                        // Ignore invalid enum values
                                                }
                                        }
                                }
                        }
                } catch (Exception e) {
                        logger.error("Lỗi khi lấy signature fields từ table columns: {}", e.getMessage(), e);
                }

                return signatureFields;
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

        /**
         * Tính số giây trễ từ deadline date đến hiện tại
         * 
         * @param deadlineDate Deadline date (LocalDate)
         * @return Số giây đã trễ (dương nếu đã quá deadline, 0 nếu chưa quá deadline,
         *         null nếu deadlineDate null)
         */
        private Long calculateTimeRemainingSeconds(LocalDate deadlineDate) {
                if (deadlineDate == null) {
                        return null;
                }

                // Chuyển deadline date thành LocalDateTime (cuối ngày - 23:59:59)
                LocalDateTime deadlineDateTime = deadlineDate.atTime(LocalTime.MAX);
                LocalDateTime now = LocalDateTime.now();

                // Tính số giây từ deadline đến hiện tại (số giây đã trễ)
                // Nếu chưa quá deadline: trả về 0
                // Nếu đã quá deadline: trả về số giây dương (số giây đã trễ từ deadline)
                long secondsBetween = ChronoUnit.SECONDS.between(deadlineDateTime, now);

                if (secondsBetween <= 0) {
                        return 0L;
                } else {
                        return secondsBetween;
                }
        }

        /**
         * Lấy số giây trễ từ deadline của submission phase
         * 
         * @param innovation Innovation entity
         * @return Số giây đã trễ (dương nếu đã quá deadline, 0 nếu chưa quá deadline,
         *         null nếu không có deadline)
         */
        private Long getSubmissionTimeRemainingSeconds(Innovation innovation) {
                if (innovation == null || innovation.getDepartment() == null
                                || innovation.getInnovationRound() == null) {
                        return null;
                }

                try {
                        Optional<DepartmentPhase> departmentPhaseOpt = departmentPhaseRepository
                                        .findByDepartmentIdAndInnovationRoundIdAndPhaseType(
                                                        innovation.getDepartment().getId(),
                                                        innovation.getInnovationRound().getId(),
                                                        InnovationPhaseTypeEnum.SUBMISSION);

                        if (departmentPhaseOpt.isEmpty()) {
                                return null;
                        }

                        DepartmentPhase departmentPhase = departmentPhaseOpt.get();
                        LocalDate deadlineDate = departmentPhase.getPhaseEndDate();
                        return calculateTimeRemainingSeconds(deadlineDate);
                } catch (Exception e) {
                        logger.warn("Lỗi khi tính số giây deadline cho innovation {}: {}", innovation.getId(),
                                        e.getMessage());
                        return null;
                }
        }

        /**
         * Đếm số tác giả từ FormData có fieldKey = "danh_sach_tac_gia"
         * 
         * @param formData FormData chứa danh sách tác giả
         * @return Số lượng tác giả (>= 2 thì là đồng sáng kiến)
         */
        private int countAuthorsFromFormData(FormData formData) {
                if (formData == null || formData.getFieldValue() == null) {
                        return 0;
                }

                try {
                        JsonNode fieldValue = formData.getFieldValue();
                        JsonNode valueNode = null;

                        if (fieldValue.has("value")) {
                                valueNode = fieldValue.get("value");
                        } else if (fieldValue.isArray()) {
                                valueNode = fieldValue;
                        }

                        if (valueNode != null && valueNode.isArray()) {
                                return valueNode.size();
                        }
                } catch (Exception e) {
                        logger.warn("Lỗi khi đếm số tác giả từ FormData: {}", e.getMessage());
                }

                return 0;
        }

        /**
         * Map Innovation entity sang MyInnovationResponse
         * 
         * @param innovation  Innovation entity
         * @param authorCount Số lượng tác giả từ danh_sach_tac_gia
         * @return MyInnovationResponse
         */
        private MyInnovationResponse toMyInnovationResponse(Innovation innovation, int authorCount) {
                MyInnovationResponse response = new MyInnovationResponse();
                response.setInnovationId(innovation.getId());
                response.setInnovationName(innovation.getInnovationName());
                response.setStatus(innovation.getStatus());
                response.setSubmissionTimeRemainingSeconds(getSubmissionTimeRemainingSeconds(innovation));
                response.setIsScore(innovation.getIsScore());

                if (innovation.getUser() != null) {
                        response.setAuthorName(innovation.getUser().getFullName());
                }

                if (innovation.getInnovationRound() != null) {
                        response.setAcademicYear(innovation.getInnovationRound().getAcademicYear());
                        response.setInnovationRoundName(innovation.getInnovationRound().getName());
                }

                response.setIsCoAuthor(authorCount >= 2);

                response.setCreatedAt(innovation.getCreatedAt());
                response.setUpdatedAt(innovation.getUpdatedAt());

                return response;
        }

        private List<SignatureProcessingResult> signInnovationDocuments(
                        Innovation innovation,
                        CreateInnovationWithTemplatesRequest request) {

                if (request.getTemplates() == null || request.getTemplates().isEmpty()) {
                        throw new IdInvalidException("Danh sách template không được để trống khi nộp sáng kiến.");
                }

                List<SignatureProcessingResult> signatureResults = new ArrayList<>();

                for (TemplateDataRequest templateRequest : request.getTemplates()) {
                        FormTemplate formTemplate = formTemplateRepository.findById(templateRequest.getTemplateId())
                                        .orElseThrow(() -> new IdInvalidException(
                                                        "Không tìm thấy template với ID: "
                                                                        + templateRequest.getTemplateId()));

                        TemplateTypeEnum templateType = formTemplate.getTemplateType();
                        DocumentTypeEnum documentType = mapTemplateTypeToDocumentType(templateType);

                        String encodedHtml = templateRequest.getHtmlContentBase64();

                        // Chỉ yêu cầu htmlContentBase64 bắt buộc cho DON_DE_NGHI và BAO_CAO_MO_TA
                        boolean isRequiredTemplate = templateType == TemplateTypeEnum.DON_DE_NGHI
                                        || templateType == TemplateTypeEnum.BAO_CAO_MO_TA;

                        if (isRequiredTemplate && (encodedHtml == null || encodedHtml.isBlank())) {
                                throw new IdInvalidException(
                                                "htmlContentBase64 của template "
                                                                + formTemplate.getTemplateType().getValue()
                                                                + " không được để trống khi nộp sáng kiến.");
                        }

                        // Nếu template không bắt buộc và htmlContentBase64 trống thì skip
                        if (!isRequiredTemplate && (encodedHtml == null || encodedHtml.isBlank())) {
                                continue;
                        }

                        String htmlContent = Utils.decode(encodedHtml);
                        if (htmlContent == null || htmlContent.isBlank()) {
                                throw new IdInvalidException(
                                                "Nội dung HTML sau khi giải mã của template "
                                                                + formTemplate.getTemplateType().getValue()
                                                                + " đang trống.");
                        }

                        generateAndStoreTemplatePdf(innovation, formTemplate, htmlContent);

                        if (templateType != TemplateTypeEnum.DON_DE_NGHI
                                        && templateType != TemplateTypeEnum.BAO_CAO_MO_TA) {
                                continue;
                        }

                        if (documentType == null) {
                                continue;
                        }

                        String documentHash = digitalSignatureService
                                        .generateDocumentHash(htmlContent.getBytes(StandardCharsets.UTF_8));
                        String signatureHash = digitalSignatureService.generateSignatureForDocument(documentHash);

                        DigitalSignatureRequest signatureRequest = new DigitalSignatureRequest();
                        signatureRequest.setInnovationId(innovation.getId());
                        signatureRequest.setDocumentType(documentType);
                        signatureRequest.setSignedAsRole(UserRoleEnum.GIANG_VIEN);
                        signatureRequest.setDocumentHash(documentHash);
                        signatureRequest.setSignatureHash(signatureHash);

                        digitalSignatureService.createDigitalSignature(signatureRequest);

                        signatureResults.add(new SignatureProcessingResult(
                                        templateRequest.getTemplateId(),
                                        formTemplate.getTemplateType(),
                                        documentType,
                                        documentHash,
                                        signatureHash));
                }

                if (signatureResults.isEmpty()) {
                        throw new IdInvalidException("Không tìm thấy template hợp lệ để ký số.");
                }

                return signatureResults;
        }

        private void generateAndStoreTemplatePdf(
                        Innovation innovation,
                        FormTemplate formTemplate,
                        String htmlContent) {

                try {
                        byte[] pdfBytes = pdfGeneratorService.convertHtmlToPdf(htmlContent);
                        String fileName = buildTemplatePdfFileName(innovation.getId(), formTemplate.getId());
                        String objectName = fileService.uploadBytes(pdfBytes, fileName, "application/pdf");

                        attachmentRepository.deleteByInnovationIdAndTemplateId(
                                        innovation.getId(),
                                        formTemplate.getId());

                        Attachment attachment = new Attachment();
                        attachment.setInnovation(innovation);
                        attachment.setTemplateId(formTemplate.getId());
                        attachment.setFileName(fileName);
                        attachment.setOriginalFileName(resolveTemplateOriginalFileName(formTemplate));
                        attachment.setFileSize((long) pdfBytes.length);
                        attachment.setPathUrl(objectName);

                        attachmentRepository.save(attachment);
                } catch (IdInvalidException e) {
                        throw e;
                } catch (Exception e) {
                        String errorMessage = e.getMessage();
                        if (errorMessage == null || errorMessage.isBlank()) {
                                errorMessage = e.getClass().getSimpleName();
                                if (e.getCause() != null && e.getCause().getMessage() != null) {
                                        errorMessage += ": " + e.getCause().getMessage();
                                }
                        }
                        throw new IdInvalidException(
                                        "Không thể lưu PDF cho template "
                                                        + formTemplate.getTemplateType().getValue()
                                                        + ": "
                                                        + errorMessage);
                }
        }

        private String buildTemplatePdfFileName(String innovationId, String templateId) {
                return innovationId + "_" + templateId + ".pdf";
        }

        private String resolveTemplateOriginalFileName(FormTemplate formTemplate) {
                if (formTemplate != null && formTemplate.getTemplateType() != null) {
                        return formTemplate.getTemplateType().getValue() + ".pdf";
                }
                return "template.pdf";
        }

        private DocumentTypeEnum mapTemplateTypeToDocumentType(TemplateTypeEnum templateType) {
                if (templateType == null) {
                        return null;
                }

                switch (templateType) {
                        case DON_DE_NGHI:
                                return DocumentTypeEnum.FORM_1;
                        case BAO_CAO_MO_TA:
                                return DocumentTypeEnum.FORM_2;
                        case BIEN_BAN_HOP:
                                return DocumentTypeEnum.REPORT_MAU_3;
                        case TONG_HOP_DE_NGHI:
                                return DocumentTypeEnum.REPORT_MAU_4;
                        case TONG_HOP_CHAM_DIEM:
                                return DocumentTypeEnum.REPORT_MAU_5;
                        case PHIEU_DANH_GIA:
                                return DocumentTypeEnum.REPORT_MAU_7;
                        default:
                                return null;
                }
        }

        private List<TemplateSignatureResponse> buildTemplateSignatureResponses(
                        List<SignatureProcessingResult> signatureResults) {
                if (signatureResults == null || signatureResults.isEmpty()) {
                        return new ArrayList<>();
                }

                return signatureResults.stream()
                                .map(result -> new TemplateSignatureResponse(
                                                result.templateId(),
                                                result.templateType(),
                                                result.documentType(),
                                                result.documentHash(),
                                                result.signatureHash()))
                                .collect(Collectors.toList());
        }

        private List<TemplateFormDataResponse> buildTemplateFormDataResponses(
                        List<FormDataResponse> formDataResponses) {
                if (formDataResponses == null || formDataResponses.isEmpty()) {
                        return new ArrayList<>();
                }

                Map<String, LinkedHashMap<String, JsonNode>> groupedByTemplate = new LinkedHashMap<>();

                for (FormDataResponse formDataResponse : formDataResponses) {
                        String templateId = formDataResponse.getTemplateId();
                        if (templateId == null || templateId.isBlank()) {
                                continue;
                        }

                        String fieldKey = extractEffectiveFieldKey(formDataResponse);
                        if (fieldKey == null || fieldKey.isBlank()) {
                                continue;
                        }

                        JsonNode valueNode = extractEffectiveFieldValue(formDataResponse);
                        groupedByTemplate
                                        .computeIfAbsent(templateId, id -> new LinkedHashMap<>())
                                        .put(fieldKey, valueNode);
                }

                return groupedByTemplate.entrySet()
                                .stream()
                                .map(entry -> new TemplateFormDataResponse(entry.getKey(), entry.getValue()))
                                .collect(Collectors.toList());
        }

        private String extractEffectiveFieldKey(FormDataResponse formDataResponse) {
                JsonNode fieldValue = formDataResponse.getFieldValue();
                if (fieldValue != null && fieldValue.has("fieldKey")) {
                        JsonNode keyNode = fieldValue.get("fieldKey");
                        if (keyNode != null && keyNode.isTextual()) {
                                return keyNode.asText();
                        }
                }
                return formDataResponse.getFormFieldKey();
        }

        private JsonNode extractEffectiveFieldValue(FormDataResponse formDataResponse) {
                JsonNode fieldValue = formDataResponse.getFieldValue();
                if (fieldValue == null) {
                        return objectMapper.nullNode();
                }
                if (fieldValue.has("value")) {
                        JsonNode valueNode = fieldValue.get("value");
                        return valueNode != null ? valueNode : objectMapper.nullNode();
                }
                return fieldValue;
        }

        private void deleteFileSafely(String fileName, String innovationId, String displayName,
                        Set<String> deletedFileNames) {
                if (fileName == null || fileName.isBlank()) {
                        return;
                }

                if (deletedFileNames.contains(fileName)) {
                        return;
                }

                try {
                        fileService.deleteFile(fileName);
                        deletedFileNames.add(fileName);
                } catch (Exception e) {
                        logger.error("Không thể xóa file {} của innovation {}: {}", fileName, innovationId,
                                        e.getMessage());
                        String finalDisplayName = (displayName != null && !displayName.isBlank()) ? displayName
                                        : fileName;
                        throw new IdInvalidException("Không thể xóa tệp đính kèm: " + finalDisplayName);
                }
        }

        private Set<String> collectFileNamesFromFormData(List<FormData> formDataList) {
                Set<String> fileNames = new HashSet<>();
                if (formDataList == null || formDataList.isEmpty()) {
                        return fileNames;
                }

                for (FormData formData : formDataList) {
                        if (formData == null || formData.getFormField() == null) {
                                continue;
                        }

                        FormField formField = formData.getFormField();
                        JsonNode fieldValue = formData.getFieldValue();

                        if (fieldValue == null || fieldValue.isNull()) {
                                continue;
                        }

                        FieldTypeEnum fieldType = formField.getFieldType();

                        if (fieldType == FieldTypeEnum.FILE) {
                                fileNames.addAll(extractFileNamesFromValueNode(fieldValue));
                                continue;
                        }

                        if (fieldValue.isObject() && fieldValue.has("fieldKey") && fieldValue.has("value")) {
                                String nestedFieldKey = fieldValue.get("fieldKey").asText();
                                FieldTypeEnum nestedType = resolveNestedFieldType(formField, nestedFieldKey);
                                if (nestedType == FieldTypeEnum.FILE) {
                                        fileNames.addAll(extractFileNamesFromValueNode(fieldValue.get("value")));
                                }
                                continue;
                        }

                        if (fieldValue.isArray()) {
                                fileNames.addAll(extractFileNamesFromNestedNode(formField, fieldValue));
                        }
                }

                return fileNames;
        }

        private Set<String> extractFileNamesFromValueNode(JsonNode valueNode) {
                Set<String> fileNames = new HashSet<>();
                if (valueNode == null || valueNode.isNull()) {
                        return fileNames;
                }

                if (valueNode.isTextual()) {
                        String fileName = valueNode.asText().trim();
                        if (!fileName.isEmpty()) {
                                fileNames.add(fileName);
                        }
                        return fileNames;
                }

                if (valueNode.isArray()) {
                        for (JsonNode item : valueNode) {
                                fileNames.addAll(extractFileNamesFromValueNode(item));
                        }
                        return fileNames;
                }

                if (valueNode.isObject()) {
                        if (valueNode.hasNonNull("pathUrl")) {
                                String fileName = valueNode.get("pathUrl").asText().trim();
                                if (!fileName.isEmpty()) {
                                        fileNames.add(fileName);
                                }
                        } else if (valueNode.hasNonNull("fileName")) {
                                String fileName = valueNode.get("fileName").asText().trim();
                                if (!fileName.isEmpty()) {
                                        fileNames.add(fileName);
                                }
                        } else if (valueNode.has("value")) {
                                fileNames.addAll(extractFileNamesFromValueNode(valueNode.get("value")));
                        }
                }

                return fileNames;
        }

        private Set<String> extractFileNamesFromNestedNode(FormField parentField, JsonNode node) {
                Set<String> fileNames = new HashSet<>();
                if (node == null || node.isNull()) {
                        return fileNames;
                }

                if (node.isObject()) {
                        if (node.has("fieldKey") && node.has("value")) {
                                String nestedFieldKey = node.get("fieldKey").asText();
                                FieldTypeEnum nestedType = resolveNestedFieldType(parentField, nestedFieldKey);
                                if (nestedType == FieldTypeEnum.FILE) {
                                        fileNames.addAll(extractFileNamesFromValueNode(node.get("value")));
                                }
                        } else if (parentField != null && parentField.getFieldType() == FieldTypeEnum.TABLE) {
                                Set<String> fileColumns = getFileColumnKeys(parentField);
                                for (String columnKey : fileColumns) {
                                        JsonNode cellNode = node.get(columnKey);
                                        if (cellNode != null) {
                                                fileNames.addAll(extractFileNamesFromValueNode(cellNode));
                                        }
                                }
                        }
                } else if (node.isArray()) {
                        for (JsonNode child : node) {
                                fileNames.addAll(extractFileNamesFromNestedNode(parentField, child));
                        }
                }

                return fileNames;
        }

        private FieldTypeEnum resolveNestedFieldType(FormField parentField, String childFieldKey) {
                if (parentField == null || childFieldKey == null || childFieldKey.isBlank()) {
                        return null;
                }

                FormField parentWithTemplate = parentField;
                if (parentField.getFormTemplate() == null) {
                        parentWithTemplate = formFieldRepository.findByIdWithTemplate(parentField.getId())
                                        .orElse(parentField);
                        if (parentField.getFormTemplate() == null) {
                                parentField.setFormTemplate(parentWithTemplate.getFormTemplate());
                        }
                }

                String templateId = parentWithTemplate.getFormTemplate() != null
                                ? parentWithTemplate.getFormTemplate().getId()
                                : null;

                if (parentWithTemplate.getChildren() != null && parentWithTemplate.getChildren().isArray()) {
                        FormField childField = findFormFieldInChildren(parentWithTemplate.getChildren(), childFieldKey,
                                        templateId);
                        if (childField != null && childField.getFieldType() != null) {
                                return childField.getFieldType();
                        }
                }

                if (parentWithTemplate.getFieldType() == FieldTypeEnum.TABLE
                                && parentWithTemplate.getTableConfig() != null) {
                        FormField childField = findFormFieldInTableColumns(parentWithTemplate.getTableConfig(),
                                        childFieldKey, templateId);
                        if (childField != null && childField.getFieldType() != null) {
                                return childField.getFieldType();
                        }
                }

                return null;
        }

        private Set<String> getFileColumnKeys(FormField tableField) {
                Set<String> fileColumns = new HashSet<>();
                if (tableField == null || tableField.getTableConfig() == null) {
                        return fileColumns;
                }

                JsonNode tableConfig = tableField.getTableConfig();
                JsonNode columnsNode = tableConfig.get("columns");
                if (columnsNode == null || !columnsNode.isArray()) {
                        return fileColumns;
                }

                for (JsonNode columnNode : columnsNode) {
                        JsonNode keyNode = columnNode.get("key");
                        JsonNode typeNode = columnNode.get("type");
                        if (keyNode == null || typeNode == null) {
                                continue;
                        }

                        try {
                                FieldTypeEnum columnType = FieldTypeEnum.valueOf(typeNode.asText());
                                if (columnType == FieldTypeEnum.FILE) {
                                        fileColumns.add(keyNode.asText());
                                }
                        } catch (IllegalArgumentException e) {
                                logger.warn("Không thể xác định type cho column {} trong bảng {}", keyNode.asText(),
                                                tableField.getFieldKey());
                        }
                }

                return fileColumns;
        }

        // 8. Xóa sáng kiến trạng thái DRAFT của user hiện tại
        public void deleteMyDraftInnovation(String innovationId) {
                logger.info("===== DELETE MY DRAFT INNOVATION =====");
                logger.info("Innovation ID: {}", innovationId);

                String currentUserId = userService.getCurrentUserId();

                Innovation innovation = innovationRepository.findById(innovationId)
                                .orElseThrow(() -> new IdInvalidException(
                                                "Không tìm thấy sáng kiến với ID: " + innovationId));

                if (innovation.getUser() == null || !innovation.getUser().getId().equals(currentUserId)) {
                        logger.error("User {} không có quyền xóa sáng kiến {}", currentUserId, innovationId);
                        throw new IdInvalidException("Bạn chỉ có thể xóa sáng kiến của chính mình");
                }

                if (innovation.getStatus() != InnovationStatusEnum.DRAFT) {
                        logger.error("Innovation {} không ở trạng thái DRAFT. Trạng thái hiện tại: {}", innovationId,
                                        innovation.getStatus());
                        throw new IdInvalidException("Chỉ có thể xóa sáng kiến ở trạng thái DRAFT");
                }

                // Xóa file từ Attachment (giờ đã có quan hệ thực sự)
                List<Attachment> attachments = attachmentRepository.findByInnovationId(innovationId);
                Set<String> deletedFileNames = new HashSet<>();

                for (Attachment attachment : attachments) {
                        String pathUrl = attachment.getPathUrl();
                        if (pathUrl == null || pathUrl.isBlank()) {
                                continue;
                        }
                        String displayName = attachment.getOriginalFileName();
                        if (displayName == null || displayName.isBlank()) {
                                displayName = attachment.getFileName();
                        }
                        deleteFileSafely(pathUrl, innovationId, displayName, deletedFileNames);
                }

                formDataRepository.deleteByInnovationId(innovationId);
                coInnovationRepository.deleteByInnovationId(innovationId);
                digitalSignatureRepository.deleteByInnovationId(innovationId);
                innovationRepository.delete(innovation);

                logger.info("===== DELETE MY DRAFT INNOVATION SUCCESS =====");
        }

        // 9. Tạo Attachment từ formData để tạo liên kết giữa Innovation và Attachment
        private void createAttachmentsFromFormData(String innovationId) {
                logger.info("===== CREATE ATTACHMENTS FROM FORMDATA =====");
                logger.info("Innovation ID: {}", innovationId);

                List<FormData> formDataList = formDataRepository.findByInnovationIdWithRelations(innovationId);
                if (formDataList == null || formDataList.isEmpty()) {
                        logger.info("Không có formData nào để tạo Attachment");
                        return;
                }

                Innovation innovation = innovationRepository.findById(innovationId)
                                .orElseThrow(() -> new IdInvalidException(
                                                "Không tìm thấy Innovation với ID: " + innovationId));

                Set<String> processedFiles = new HashSet<>();

                for (FormData formData : formDataList) {
                        if (formData == null || formData.getFormField() == null) {
                                continue;
                        }

                        FormField formField = formData.getFormField();
                        JsonNode fieldValue = formData.getFieldValue();

                        if (fieldValue == null || fieldValue.isNull()) {
                                continue;
                        }

                        FieldTypeEnum fieldType = formField.getFieldType();
                        String templateId = null;
                        if (formField.getFormTemplate() != null) {
                                templateId = formField.getFormTemplate().getId();
                        }

                        if (fieldType == FieldTypeEnum.FILE) {
                                createAttachmentsFromValueNode(innovation, templateId, fieldValue, processedFiles);
                                continue;
                        }

                        if (fieldValue.isObject() && fieldValue.has("fieldKey") && fieldValue.has("value")) {
                                String nestedFieldKey = fieldValue.get("fieldKey").asText();
                                FieldTypeEnum nestedType = resolveNestedFieldType(formField, nestedFieldKey);
                                if (nestedType == FieldTypeEnum.FILE) {
                                        createAttachmentsFromValueNode(innovation, templateId, fieldValue.get("value"),
                                                        processedFiles);
                                }
                                continue;
                        }

                        if (fieldValue.isArray()) {
                                createAttachmentsFromNestedNode(innovation, templateId, formField, fieldValue,
                                                processedFiles);
                        }
                }

                logger.info("===== CREATE ATTACHMENTS FROM FORMDATA SUCCESS =====");
        }

        private void createAttachmentsFromValueNode(Innovation innovation, String templateId, JsonNode valueNode,
                        Set<String> processedFiles) {
                if (valueNode == null || valueNode.isNull()) {
                        return;
                }

                if (valueNode.isTextual()) {
                        String fileName = valueNode.asText().trim();
                        if (!fileName.isEmpty() && !processedFiles.contains(fileName)) {
                                createAttachment(innovation, templateId, fileName, null, null);
                                processedFiles.add(fileName);
                        }
                        return;
                }

                if (valueNode.isArray()) {
                        for (JsonNode item : valueNode) {
                                createAttachmentsFromValueNode(innovation, templateId, item, processedFiles);
                        }
                        return;
                }

                if (valueNode.isObject()) {
                        String pathUrl = null;
                        String originalFileName = null;

                        if (valueNode.hasNonNull("pathUrl")) {
                                pathUrl = valueNode.get("pathUrl").asText().trim();
                        } else if (valueNode.hasNonNull("fileName")) {
                                pathUrl = valueNode.get("fileName").asText().trim();
                        }

                        if (valueNode.hasNonNull("originalFileName")) {
                                originalFileName = valueNode.get("originalFileName").asText().trim();
                        }

                        if (pathUrl != null && !pathUrl.isEmpty() && !processedFiles.contains(pathUrl)) {
                                createAttachment(innovation, templateId, pathUrl, originalFileName, null);
                                processedFiles.add(pathUrl);
                        } else if (valueNode.has("value")) {
                                createAttachmentsFromValueNode(innovation, templateId, valueNode.get("value"),
                                                processedFiles);
                        }
                }
        }

        private void createAttachmentsFromNestedNode(Innovation innovation, String templateId, FormField parentField,
                        JsonNode node, Set<String> processedFiles) {
                if (node == null || node.isNull()) {
                        return;
                }

                if (node.isObject()) {
                        if (node.has("fieldKey") && node.has("value")) {
                                String nestedFieldKey = node.get("fieldKey").asText();
                                FieldTypeEnum nestedType = resolveNestedFieldType(parentField, nestedFieldKey);
                                if (nestedType == FieldTypeEnum.FILE) {
                                        createAttachmentsFromValueNode(innovation, templateId, node.get("value"),
                                                        processedFiles);
                                }
                        } else if (parentField != null && parentField.getFieldType() == FieldTypeEnum.TABLE) {
                                Set<String> fileColumns = getFileColumnKeys(parentField);
                                for (String columnKey : fileColumns) {
                                        JsonNode cellNode = node.get(columnKey);
                                        if (cellNode != null) {
                                                createAttachmentsFromValueNode(innovation, templateId, cellNode,
                                                                processedFiles);
                                        }
                                }
                        }
                } else if (node.isArray()) {
                        for (JsonNode child : node) {
                                createAttachmentsFromNestedNode(innovation, templateId, parentField, child,
                                                processedFiles);
                        }
                }
        }

        private void createAttachment(Innovation innovation, String templateId, String pathUrl,
                        String originalFileName, Long fileSize) {
                if (pathUrl == null || pathUrl.isBlank()) {
                        return;
                }

                // Kiểm tra xem Attachment đã tồn tại chưa (dựa trên innovationId và pathUrl)
                List<Attachment> existingAttachments = attachmentRepository.findByInnovationId(innovation.getId());
                boolean alreadyExists = existingAttachments.stream()
                                .anyMatch(att -> pathUrl.equals(att.getPathUrl()));
                if (alreadyExists) {
                        logger.debug("Attachment đã tồn tại cho innovation {} và pathUrl {}", innovation.getId(),
                                        pathUrl);
                        return;
                }

                Attachment attachment = new Attachment();
                attachment.setInnovation(innovation);
                attachment.setTemplateId(templateId);
                attachment.setPathUrl(pathUrl);
                attachment.setFileName(pathUrl);

                if (originalFileName != null && !originalFileName.isBlank()) {
                        attachment.setOriginalFileName(originalFileName);
                } else {
                        attachment.setOriginalFileName(pathUrl);
                }

                if (fileSize != null) {
                        attachment.setFileSize(fileSize);
                }

                attachmentRepository.save(attachment);
                logger.debug("Đã tạo Attachment: innovationId={}, templateId={}, pathUrl={}", innovation.getId(),
                                templateId, pathUrl);
        }

        private record SignatureProcessingResult(
                        String templateId,
                        TemplateTypeEnum templateType,
                        DocumentTypeEnum documentType,
                        String documentHash,
                        String signatureHash) {
        }

}
