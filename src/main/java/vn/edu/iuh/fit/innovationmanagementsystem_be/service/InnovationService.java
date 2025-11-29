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
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.FormDataRequest;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.CreateInnovationWithTemplatesRequest;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.TemplateDataRequest;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.FilterMyInnovationRequest;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.FilterAdminInnovationRequest;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.FormDataResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.InnovationFormDataResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.InnovationResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.MyInnovationResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.InnovationStatisticsDTO;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.InnovationAcademicYearStatisticsDTO;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.UpcomingDeadlinesResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.InnovationDetailResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.DepartmentInnovationDetailResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.InnovationScoringDetailResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.AttachmentInfo;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.CoAuthorResponse;
import java.util.stream.Collectors;
import vn.edu.iuh.fit.innovationmanagementsystem_be.exception.IdInvalidException;
import vn.edu.iuh.fit.innovationmanagementsystem_be.mapper.InnovationMapper;
import vn.edu.iuh.fit.innovationmanagementsystem_be.mapper.FormDataMapper;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.AttachmentRepository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.InnovationRepository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.FormDataRepository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.InnovationPhaseRepository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.FormFieldRepository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.FormTemplateRepository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.CoInnovationRepository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.DepartmentPhaseRepository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.UserSignatureProfileRepository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.DigitalSignatureRepository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.CoInnovation;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.DepartmentPhase;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.UserSignatureProfile;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.FormTemplate;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.DigitalSignature;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.CAStatusEnum;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.TemplateTypeEnum;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.DocumentTypeEnum;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.SignatureStatusEnum;

import java.util.ArrayList;
import java.util.Collections;
import vn.edu.iuh.fit.innovationmanagementsystem_be.utils.ResultPaginationDTO;
import vn.edu.iuh.fit.innovationmanagementsystem_be.utils.Utils;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.Optional;
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
        private final FormDataMapper formDataMapper;
        private final FormDataRepository formDataRepository;
        private final UserService userService;
        private final ActivityLogService activityLogService;
        private final FormFieldRepository formFieldRepository;
        private final FormTemplateRepository formTemplateRepository;
        private final ObjectMapper objectMapper;
        private final CoInnovationRepository coInnovationRepository;
        private final NotificationService notificationService;
        private final DepartmentPhaseRepository departmentPhaseRepository;
        private final AttachmentRepository attachmentRepository;
        private final FileService fileService;
        private final UserSignatureProfileRepository userSignatureProfileRepository;
        private final CertificateAuthorityService certificateAuthorityService;
        private final DigitalSignatureRepository digitalSignatureRepository;
        private final InnovationQueryService innovationQueryService;
        private final InnovationStatisticsService innovationStatisticsService;
        private final InnovationFormService innovationFormService;
        private final InnovationCoAuthorService innovationCoAuthorService;
        private final InnovationSignatureService innovationSignatureService;
        private final DigitalSignatureService digitalSignatureService;

        public InnovationService(InnovationRepository innovationRepository,
                        InnovationPhaseRepository innovationPhaseRepository,
                        FormDataService formDataService,
                        InnovationMapper innovationMapper,
                        FormDataMapper formDataMapper,
                        FormDataRepository formDataRepository,
                        UserService userService,
                        ActivityLogService activityLogService,
                        FormFieldRepository formFieldRepository,
                        FormTemplateRepository formTemplateRepository,
                        ObjectMapper objectMapper,
                        CoInnovationRepository coInnovationRepository,
                        NotificationService notificationService,
                        DepartmentPhaseRepository departmentPhaseRepository,
                        AttachmentRepository attachmentRepository,
                        FileService fileService,
                        UserSignatureProfileRepository userSignatureProfileRepository,
                        CertificateAuthorityService certificateAuthorityService,
                        DigitalSignatureRepository digitalSignatureRepository,
                        InnovationQueryService innovationQueryService,
                        InnovationStatisticsService innovationStatisticsService,
                        InnovationFormService innovationFormService,
                        InnovationCoAuthorService innovationCoAuthorService,
                        InnovationSignatureService innovationSignatureService,
                        DigitalSignatureService digitalSignatureService) {
                this.innovationRepository = innovationRepository;
                this.innovationPhaseRepository = innovationPhaseRepository;
                this.formDataService = formDataService;
                this.innovationMapper = innovationMapper;
                this.formDataMapper = formDataMapper;
                this.formDataRepository = formDataRepository;
                this.userService = userService;
                this.activityLogService = activityLogService;
                this.formFieldRepository = formFieldRepository;
                this.formTemplateRepository = formTemplateRepository;
                this.objectMapper = objectMapper;
                this.coInnovationRepository = coInnovationRepository;
                this.notificationService = notificationService;
                this.departmentPhaseRepository = departmentPhaseRepository;
                this.attachmentRepository = attachmentRepository;
                this.fileService = fileService;
                this.userSignatureProfileRepository = userSignatureProfileRepository;
                this.certificateAuthorityService = certificateAuthorityService;
                this.digitalSignatureRepository = digitalSignatureRepository;
                this.innovationQueryService = innovationQueryService;
                this.innovationStatisticsService = innovationStatisticsService;
                this.innovationFormService = innovationFormService;
                this.innovationCoAuthorService = innovationCoAuthorService;
                this.innovationSignatureService = innovationSignatureService;
                this.digitalSignatureService = digitalSignatureService;
        }

        // 1. Lấy tất cả sáng kiến của user hiện tại với filter chi tiết
        public ResultPaginationDTO getAllInnovationsByCurrentUserWithDetailedFilter(
                        FilterMyInnovationRequest filterRequest, Pageable pageable) {
                return innovationQueryService.getAllInnovationsByCurrentUserWithDetailedFilter(filterRequest,
                                pageable);
        }

        // 3. Lấy tất cả sáng kiến
        public ResultPaginationDTO getAllInnovations(Specification<Innovation> specification, Pageable pageable) {
                return innovationQueryService.getAllInnovations(specification, pageable);
        }

        // 2. Thống kê innovation cho giảng viên
        public InnovationStatisticsDTO getInnovationStatisticsForCurrentUser() {
                return innovationStatisticsService.getInnovationStatisticsForCurrentUser();
        }

        // 3. Lấy thống kê sáng kiến theo năm học cho user hiện tại
        public InnovationAcademicYearStatisticsDTO getInnovationStatisticsByAcademicYearForCurrentUser() {
                return innovationStatisticsService.getInnovationStatisticsByAcademicYearForCurrentUser();
        }

        // 4. Lấy hạn chót sắp tới từ round hiện tại
        public UpcomingDeadlinesResponse getUpcomingDeadlines() {
                return innovationStatisticsService.getUpcomingDeadlines();
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
                List<InnovationSignatureService.SignatureProcessingResult> signatureResults = Collections.emptyList();
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
                                        }
                                }
                        }
                }

                // Kiểm tra chữ ký trước khi nộp sáng kiến (chỉ khi status = SUBMITTED)
                if (request.getStatus() == InnovationStatusEnum.SUBMITTED) {
                        innovationSignatureService.validateSignaturesBeforeSubmit(request);
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

                        // Lấy danh sách file đính kèm cũ TRƯỚC KHI xóa Attachment (để còn metadata)
                        List<Attachment> oldAttachments = attachmentRepository
                                        .findByInnovationId(savedInnovation.getId());
                        Set<String> oldUserFileNames = new HashSet<>();
                        for (Attachment attachment : oldAttachments) {
                                String fileName = attachment.getFileName();
                                // Chỉ lấy file đính kèm user (có templateId và KHÔNG phải PDF template)
                                if (attachment.getTemplateId() != null && fileName != null) {
                                        // PDF template có pattern: {innovationId}_{templateId}.pdf
                                        String templatePdfPattern = savedInnovation.getId() + "_"
                                                        + attachment.getTemplateId() + ".pdf";
                                        if (!fileName.equals(templatePdfPattern)) {
                                                oldUserFileNames.add(fileName);
                                        }
                                }
                        }

                        // Lấy danh sách file mới từ request
                        Set<String> newFileNames = new HashSet<>();
                        for (TemplateDataRequest templateRequest : request.getTemplates()) {
                                if (templateRequest.getFormData() != null && !templateRequest.getFormData().isEmpty()) {
                                        for (Map.Entry<String, Object> entry : templateRequest.getFormData()
                                                        .entrySet()) {
                                                Object fieldValue = entry.getValue();
                                                if (fieldValue != null) {
                                                        JsonNode valueNode = objectMapper.valueToTree(fieldValue);
                                                        newFileNames.addAll(extractFileNamesFromValueNode(valueNode));
                                                }
                                        }
                                }
                        }

                        // Xóa file cũ trong MinIO TRƯỚC (chỉ xóa file user không còn dùng)
                        if (!oldUserFileNames.isEmpty()) {
                                for (String fileName : oldUserFileNames) {
                                        if (!newFileNames.contains(fileName)) {
                                                try {
                                                        if (fileService.fileExists(fileName)) {
                                                                fileService.deleteFile(fileName);
                                                                logger.info("Đã xóa file đính kèm cũ: {}", fileName);
                                                        }
                                                } catch (Exception e) {
                                                        logger.error("Lỗi khi xóa file {}: {}", fileName,
                                                                        e.getMessage());
                                                }
                                        }
                                }
                        }

                        // SAU ĐÓ mới xóa FormData cũ, CoInnovation cũ và Attachment cũ
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
                                        InnovationFormService.FormFieldSearchResult searchResult = innovationFormService
                                                        .findFormFieldByKeyWithParent(formFields,
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
                innovationCoAuthorService.processCoInnovations(savedInnovation, request.getTemplates());

                if (request.getStatus() == InnovationStatusEnum.SUBMITTED) {
                        signatureResults = innovationSignatureService.signInnovationDocuments(savedInnovation, request);
                }

                // Tạo Attachment từ formData (tạo liên kết giữa Innovation và Attachment)
                // Gọi sau khi tạo PDF để tránh file đính kèm bị xóa khi generate PDF
                createAttachmentsFromFormData(savedInnovation.getId());

                // Gửi thông báo cho user khi nộp sáng kiến thành công (chỉ khi status là
                // SUBMITTED)
                if (request.getStatus() == InnovationStatusEnum.SUBMITTED) {
                        try {
                                notificationService.notifyUserOnInnovationCreated(
                                                currentUser.getId(),
                                                savedInnovation.getId(),
                                                savedInnovation.getInnovationName(),
                                                savedInnovation.getStatus());
                        } catch (Exception e) {
                                logger.error("Lỗi khi gửi thông báo tạo sáng kiến: {}", e.getMessage(), e);
                        }
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
                response.setTemplates(innovationSignatureService.buildTemplateFormDataResponses(formDataResponses));
                response.setTemplateSignatures(
                                innovationSignatureService.buildTemplateSignatureResponses(signatureResults));
                response.setSubmissionTimeRemainingSeconds(timeRemainingSeconds);

                return response;
        }

        // 5. Lấy Innovation & FormData theo ID (bao gồm FormField đầy đủ)
        public InnovationFormDataResponse getInnovationWithFormDataById(String innovationId) {
                Innovation innovation = innovationRepository.findById(innovationId)
                                .orElseThrow(() -> {
                                        logger.error("Không tìm thấy sáng kiến với ID: {}", innovationId);
                                        return new IdInvalidException(
                                                        "Không tìm thấy sáng kiến với ID: " + innovationId);
                                });

                List<FormData> formDataList = formDataRepository.findByInnovationIdWithRelations(innovationId);

                List<FormDataResponse> formDataResponses = new ArrayList<>();

                for (FormData formData : formDataList) {
                        FormDataResponse formDataResponse = formDataMapper.toFormDataResponse(formData);

                        if (formData.getFormField() != null) {
                                FormField formField = formData.getFormField();

                                if (formField.getFormTemplate() == null) {
                                        formField = formFieldRepository.findByIdWithTemplate(formField.getId())
                                                        .orElse(formField);
                                }
                        } else {
                                logger.warn("FormField is null for FormData ID: {}", formData.getId());
                        }

                        formDataResponses.add(formDataResponse);
                }

                InnovationFormDataResponse response = new InnovationFormDataResponse();
                InnovationResponse innovationResponse = innovationMapper.toInnovationResponse(innovation);
                Long timeRemainingSeconds = getSubmissionTimeRemainingSeconds(innovation);
                innovationResponse.setSubmissionTimeRemainingSeconds(timeRemainingSeconds);
                response.setInnovation(innovationResponse);
                response.setTemplates(innovationSignatureService.buildTemplateFormDataResponses(formDataResponses));
                response.setTemplateSignatures(Collections.emptyList());
                response.setSubmissionTimeRemainingSeconds(timeRemainingSeconds);

                return response;
        }

        // 6. Lấy Innovation & FormData theo ID của user hiện tại (chỉ cho phép xem sáng
        // kiến của chính mình)
        public InnovationFormDataResponse getMyInnovationWithFormDataById(String innovationId) {
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

                List<FormData> formDataList = formDataRepository.findByInnovationIdWithRelations(innovationId);

                List<FormDataResponse> formDataResponses = new ArrayList<>();

                for (FormData formData : formDataList) {
                        FormDataResponse formDataResponse = formDataMapper.toFormDataResponse(formData);

                        if (formData.getFormField() != null) {
                                FormField formField = formData.getFormField();

                                if (formField.getFormTemplate() == null) {
                                        formField = formFieldRepository.findByIdWithTemplate(formField.getId())
                                                        .orElse(formField);
                                }
                        } else {
                                logger.warn("FormField is null for FormData ID: {}", formData.getId());
                        }

                        formDataResponses.add(formDataResponse);
                }

                InnovationFormDataResponse response = new InnovationFormDataResponse();
                InnovationResponse innovationResponse = innovationMapper.toInnovationResponse(innovation);
                Long timeRemainingSeconds = getSubmissionTimeRemainingSeconds(innovation);
                innovationResponse.setSubmissionTimeRemainingSeconds(timeRemainingSeconds);
                response.setInnovation(innovationResponse);
                response.setTemplates(innovationSignatureService.buildTemplateFormDataResponses(formDataResponses));
                response.setTemplateSignatures(Collections.emptyList());
                response.setSubmissionTimeRemainingSeconds(timeRemainingSeconds);

                return response;
        }

        // 8. Lấy chi tiết sáng kiến cho TRUONG_KHOA, QUAN_TRI_VIEN_KHOA và thành viên
        // hội đồng
        public DepartmentInnovationDetailResponse getDepartmentInnovationDetailById(String innovationId) {
                User currentUser = userService.getCurrentUser();

                // Kiểm tra quyền
                boolean hasQuanTriVienKhoaRole = currentUser.getUserRoles().stream()
                                .anyMatch(userRole -> userRole.getRole()
                                                .getRoleName() == UserRoleEnum.QUAN_TRI_VIEN_KHOA);
                boolean hasTruongKhoaRole = currentUser.getUserRoles().stream()
                                .anyMatch(userRole -> userRole.getRole().getRoleName() == UserRoleEnum.TRUONG_KHOA);
                boolean hasCouncilMemberRole = currentUser.getUserRoles().stream()
                                .anyMatch(userRole -> {
                                        UserRoleEnum role = userRole.getRole().getRoleName();
                                        return role == UserRoleEnum.TV_HOI_DONG_KHOA
                                                        || role == UserRoleEnum.TV_HOI_DONG_TRUONG;
                                });

                if (!hasQuanTriVienKhoaRole && !hasTruongKhoaRole && !hasCouncilMemberRole) {
                        throw new IdInvalidException(
                                        "Chỉ QUAN_TRI_VIEN_KHOA, TRUONG_KHOA hoặc thành viên hội đồng mới có quyền xem");
                }

                // Lấy innovation
                Innovation innovation = innovationRepository.findById(innovationId)
                                .orElseThrow(() -> new IdInvalidException("Không tìm thấy sáng kiến"));

                // Kiểm tra department matching - chỉ áp dụng cho QUAN_TRI_VIEN_KHOA và
                // TRUONG_KHOA
                // Thành viên hội đồng có thể xem sáng kiến của mọi khoa
                if (hasQuanTriVienKhoaRole || hasTruongKhoaRole) {
                        if (innovation.getDepartment() == null || currentUser.getDepartment() == null) {
                                throw new IdInvalidException(
                                                "Không thể xác định phòng ban của sáng kiến hoặc người dùng");
                        }

                        // Kiểm tra department matching
                        if (!innovation.getDepartment().getId().equals(currentUser.getDepartment().getId())) {
                                throw new IdInvalidException("Bạn chỉ có thể xem sáng kiến của khoa mình");
                        }
                }

                // Lấy thông tin cơ bản
                User author = innovation.getUser();
                String academicYear = innovation.getInnovationRound() != null
                                ? innovation.getInnovationRound().getAcademicYear()
                                : null;
                String roundName = innovation.getInnovationRound() != null
                                ? innovation.getInnovationRound().getName()
                                : null;

                // Lấy danh sách đồng tác giả
                List<CoInnovation> coInnovations = coInnovationRepository.findByInnovationId(innovationId);
                List<CoAuthorResponse> coAuthors = coInnovations.stream()
                                .map(co -> new CoAuthorResponse(
                                                co.getCoInnovatorFullName(),
                                                co.getCoInnovatorDepartmentName(),
                                                co.getUser() != null ? co.getUser().getEmail() : null))
                                .collect(Collectors.toList());

                // Lấy form data
                List<FormData> formDataList = formDataRepository.findByInnovationIdWithRelations(innovationId);
                List<FormDataResponse> formDataResponses = formDataList.stream()
                                .map(formDataMapper::toFormDataResponse)
                                .collect(Collectors.toList());

                // Lấy danh sách tài liệu đính kèm
                List<Attachment> attachments = attachmentRepository.findByInnovationId(innovationId);

                // Lấy tất cả digital signatures của innovation này
                List<DigitalSignature> digitalSignatures = digitalSignatureRepository
                                .findByInnovationIdWithRelations(innovationId);

                List<AttachmentInfo> attachmentInfos = attachments.stream()
                                .map(attachment -> {
                                        String templateType = null;
                                        boolean isDigitallySigned = false;
                                        String signerName = null;

                                        // Chỉ xử lý templateType và digital signature cho PDF template (có fileSize)
                                        // File đính kèm user upload có fileSize = NULL
                                        if (attachment.getFileSize() != null && attachment.getTemplateId() != null) {
                                                Optional<FormTemplate> formTemplateOpt = formTemplateRepository
                                                                .findById(attachment.getTemplateId());
                                                if (formTemplateOpt.isPresent()) {
                                                        FormTemplate formTemplate = formTemplateOpt.get();
                                                        TemplateTypeEnum templateTypeEnum = formTemplate
                                                                        .getTemplateType();

                                                        if (templateTypeEnum != null) {
                                                                templateType = templateTypeEnum.name();
                                                                // Map TemplateTypeEnum sang DocumentTypeEnum
                                                                DocumentTypeEnum documentType = digitalSignatureService
                                                                                .mapTemplateTypeToDocumentType(
                                                                                                templateTypeEnum);

                                                                // Kiểm tra digital signature cho PDF template
                                                                if (documentType != null) {
                                                                        Optional<DigitalSignature> signatureOpt = digitalSignatures
                                                                                        .stream()
                                                                                        .filter(sig -> sig
                                                                                                        .getDocumentType() == documentType
                                                                                                        && sig.getStatus() == SignatureStatusEnum.SIGNED)
                                                                                        .findFirst();

                                                                        if (signatureOpt.isPresent()) {
                                                                                isDigitallySigned = true;
                                                                                DigitalSignature signature = signatureOpt
                                                                                                .get();
                                                                                if (signature.getUser() != null) {
                                                                                        signerName = signature.getUser()
                                                                                                        .getFullName();
                                                                                }
                                                                        }
                                                                }
                                                        }
                                                }
                                        }

                                        return AttachmentInfo.builder()
                                                        .fileName(attachment.getPathUrl())
                                                        .templateId(attachment.getTemplateId())
                                                        .templateType(templateType)
                                                        .uploadedAt(attachment.getCreatedAt())
                                                        .isDigitallySigned(isDigitallySigned)
                                                        .signerName(signerName)
                                                        .build();
                                })
                                .collect(Collectors.toList());

                // Tạo formData object
                InnovationResponse innovationResponse = innovationMapper.toInnovationResponse(innovation);
                Long timeRemainingSeconds = getSubmissionTimeRemainingSeconds(innovation);
                innovationResponse.setSubmissionTimeRemainingSeconds(timeRemainingSeconds);

                InnovationFormDataResponse formData = new InnovationFormDataResponse();
                formData.setInnovation(innovationResponse);
                formData.setTemplates(innovationSignatureService.buildTemplateFormDataResponses(formDataResponses));
                formData.setTemplateSignatures(Collections.emptyList()); // Empty list như full-detail API
                formData.setSubmissionTimeRemainingSeconds(timeRemainingSeconds);

                return DepartmentInnovationDetailResponse.builder()
                                .innovationId(innovation.getId())
                                .innovationName(innovation.getInnovationName())
                                .authorName(author.getFullName())
                                .authorEmail(author.getEmail())
                                .departmentName(innovation.getDepartment().getDepartmentName())
                                .academicYear(academicYear)
                                .roundName(roundName)
                                .isScore(innovation.getIsScore())
                                .status(innovation.getStatus())
                                .submittedAt(innovation.getCreatedAt())
                                .coAuthors(coAuthors)
                                .attachments(attachmentInfos)
                                .formData(formData)
                                .build();
        }

        // 9. Lấy chi tiết sáng kiến kèm bảng điểm để chấm điểm
        public InnovationScoringDetailResponse getInnovationScoringDetailById(String innovationId) {
                // Lấy innovation detail (reuse existing logic)
                DepartmentInnovationDetailResponse baseDetail = getDepartmentInnovationDetailById(innovationId);

                // Lấy Innovation entity
                Innovation innovation = innovationRepository.findById(innovationId)
                                .orElseThrow(() -> new IdInvalidException("Không tìm thấy sáng kiến"));

                // Kiểm tra InnovationRound
                if (innovation.getInnovationRound() == null) {
                        throw new IdInvalidException(
                                        "Sáng kiến chưa được gán vào đợt sáng kiến nào. Không thể lấy bảng điểm");
                }

                // Lấy InnovationDecision từ InnovationRound
                if (innovation.getInnovationRound().getInnovationDecision() == null) {
                        throw new IdInvalidException(
                                        "Đợt sáng kiến chưa có quyết định đánh giá. Không thể lấy bảng điểm");
                }

                JsonNode scoringCriteria = innovation.getInnovationRound().getInnovationDecision()
                                .getScoringCriteria();

                // Build response
                InnovationScoringDetailResponse response = new InnovationScoringDetailResponse();

                // Copy all fields from baseDetail
                response.setInnovationId(baseDetail.getInnovationId());
                response.setInnovationName(baseDetail.getInnovationName());
                response.setAuthorName(baseDetail.getAuthorName());
                response.setAuthorEmail(baseDetail.getAuthorEmail());
                response.setDepartmentName(baseDetail.getDepartmentName());
                response.setAcademicYear(baseDetail.getAcademicYear());
                response.setIsScore(baseDetail.getIsScore());
                response.setStatus(baseDetail.getStatus());
                response.setSubmittedAt(baseDetail.getSubmittedAt());
                response.setCoAuthors(baseDetail.getCoAuthors());
                response.setAttachments(baseDetail.getAttachments());
                response.setFormData(baseDetail.getFormData());

                // Add scoring criteria
                response.setScoringCriteria(scoringCriteria);
                response.setMaxTotalScore(100);

                return response;
        }

        // 8. Lấy danh sách sáng kiến của khoa với filter chi tiết
        public ResultPaginationDTO getAllDepartmentInnovationsWithDetailedFilter(
                        FilterMyInnovationRequest filterRequest, Pageable pageable) {
                return innovationQueryService.getAllDepartmentInnovationsWithDetailedFilter(filterRequest, pageable);
        }

        // 7. Lấy Innovation & FormData theo ID cho QUAN_TRI_VIEN_KHOA và TRUONG_KHOA
        // (chỉ cho phép xem sáng kiến của phòng ban mình)
        public InnovationFormDataResponse getDepartmentInnovationWithFormDataById(String innovationId) {
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

                List<FormData> formDataList = formDataRepository.findByInnovationIdWithRelations(innovationId);

                List<FormDataResponse> formDataResponses = new ArrayList<>();

                for (FormData formData : formDataList) {
                        FormDataResponse formDataResponse = formDataMapper.toFormDataResponse(formData);

                        if (formData.getFormField() != null) {
                                FormField formField = formData.getFormField();

                                if (formField.getFormTemplate() == null) {
                                        formField = formFieldRepository.findByIdWithTemplate(formField.getId())
                                                        .orElse(formField);
                                }
                        } else {
                                logger.warn("FormField is null for FormData ID: {}", formData.getId());
                        }

                        formDataResponses.add(formDataResponse);
                }

                InnovationFormDataResponse response = new InnovationFormDataResponse();
                InnovationResponse innovationResponse = innovationMapper.toInnovationResponse(innovation);
                Long timeRemainingSeconds = getSubmissionTimeRemainingSeconds(innovation);
                innovationResponse.setSubmissionTimeRemainingSeconds(timeRemainingSeconds);
                response.setInnovation(innovationResponse);
                response.setTemplates(innovationSignatureService.buildTemplateFormDataResponses(formDataResponses));
                response.setTemplateSignatures(Collections.emptyList());
                response.setSubmissionTimeRemainingSeconds(timeRemainingSeconds);

                return response;
        }

        // 10. Lấy chi tiết sáng kiến của user hiện tại bằng ID
        public InnovationDetailResponse getInnovationDetailById(String innovationId) {
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

                User author = innovation.getUser();
                String authorName = author != null ? author.getFullName() : null;
                String authorEmail = author != null ? author.getEmail() : null;
                String departmentName = author != null && author.getDepartment() != null
                                ? author.getDepartment().getDepartmentName()
                                : null;
                String academicYear = innovation.getInnovationRound() != null
                                ? innovation.getInnovationRound().getAcademicYear()
                                : null;

                List<CoInnovation> coInnovations = coInnovationRepository.findByInnovationId(innovationId);
                List<CoAuthorResponse> coAuthors = coInnovations.stream()
                                .map(co -> {
                                        User coUser = co.getUser();
                                        String coEmail = coUser != null ? coUser.getEmail() : null;
                                        String coDepartmentName = coUser != null && coUser.getDepartment() != null
                                                        ? coUser.getDepartment().getDepartmentName()
                                                        : co.getCoInnovatorDepartmentName();
                                        String coFullName = coUser != null ? coUser.getFullName()
                                                        : co.getCoInnovatorFullName();

                                        return new CoAuthorResponse(coFullName, coDepartmentName, coEmail);
                                })
                                .collect(Collectors.toList());

                List<Attachment> attachments = attachmentRepository.findByInnovationId(innovationId);
                Integer attachmentCount = attachments != null ? attachments.size() : 0;

                return InnovationDetailResponse.builder()
                                .innovationId(innovation.getId())
                                .innovationName(innovation.getInnovationName())
                                .authorName(authorName)
                                .academicYear(academicYear)
                                .departmentName(departmentName)
                                .isScore(innovation.getIsScore())
                                .status(innovation.getStatus())
                                .authorEmail(authorEmail)
                                .coAuthors(coAuthors)
                                .attachmentCount(attachmentCount)
                                .build();
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

                Specification<Innovation> combinedSpec = specification != null
                                ? departmentSpec.and(notDraftSpec).and(specification)
                                : departmentSpec.and(notDraftSpec);

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

        // 10. Lấy tất cả sáng kiến với filter cho QUAN_TRI_VIEN_QLKH_HTQT,
        // TV_HOI_DONG_TRUONG, CHU_TICH_HD_TRUONG, QUAN_TRI_VIEN_HE_THONG
        public ResultPaginationDTO getAllInnovationsForAdminRolesWithFilter(FilterAdminInnovationRequest filterRequest,
                        Pageable pageable) {
                return innovationQueryService.getAllInnovationsForAdminRolesWithFilter(filterRequest, pageable);
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
        }

        // 9. Tạo Attachment từ formData để tạo liên kết giữa Innovation và Attachment
        private void createAttachmentsFromFormData(String innovationId) {
                List<FormData> formDataList = formDataRepository.findByInnovationIdWithRelations(innovationId);
                if (formDataList == null || formDataList.isEmpty()) {
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
                        return;
                }

                // Lấy file size từ MinIO nếu chưa có
                if (fileSize == null) {
                        try {
                                if (fileService.fileExists(pathUrl)) {
                                        fileSize = fileService.getFileInfo(pathUrl).size();
                                }
                        } catch (Exception e) {
                                logger.warn("Không thể lấy thông tin file từ MinIO: {}", e.getMessage());
                        }
                }

                Attachment attachment = new Attachment();
                attachment.setInnovation(innovation);
                attachment.setTemplateId(templateId);
                attachment.setPathUrl(pathUrl);

                if (originalFileName != null && !originalFileName.isBlank()) {
                        attachment.setFileName(originalFileName);
                        attachment.setOriginalFileName(originalFileName);
                } else {
                        attachment.setFileName(pathUrl);
                        attachment.setOriginalFileName(pathUrl);
                }

                attachment.setFileSize(fileSize);

                attachmentRepository.save(attachment);
        }

}
