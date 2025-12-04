package vn.edu.iuh.fit.innovationmanagementsystem_be.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.*;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.DocumentTypeEnum;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.SignatureStatusEnum;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.TemplateTypeEnum;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.UserRoleEnum;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.*;
import vn.edu.iuh.fit.innovationmanagementsystem_be.exception.IdInvalidException;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.*;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class InnovationDetailService {

        private final InnovationRepository innovationRepository;
        private final InnovationService innovationService;
        private final UserService userService;
        private final CoInnovationRepository coInnovationRepository;
        private final AttachmentRepository attachmentRepository;
        // private final ReviewCommentRepository reviewCommentRepository;
        private final ReviewScoreRepository reviewScoreRepository;
        private final FormTemplateRepository formTemplateRepository;
        private final DigitalSignatureRepository digitalSignatureRepository;

        // 1. Main method
        public InnovationDetailForGiangVienResponse getMyInnovationDetailForGiangVien(String innovationId) {
                Innovation innovation = validateMyInnovation(innovationId);

                return InnovationDetailForGiangVienResponse.builder()
                                .overview(buildOverviewData(innovation))
                                .coAuthors(buildCoAuthorsList(innovation))
                                .statistics(buildStatistics(innovation))
                                .formData(innovationService.getMyInnovationFormDataForDetail(innovationId))
                                .attachments(buildAttachmentsList(innovation))
                                .reviewComments(buildReviewCommentsList(innovation))
                                .workflowSteps(buildWorkflowSteps(innovation))
                                .activityHistory(buildActivityHistory(innovation))
                                .build();
        }

        private Innovation validateMyInnovation(String innovationId) {
                String currentUserId = userService.getCurrentUserId();
                Innovation innovation = innovationRepository.findById(innovationId)
                                .orElseThrow(() -> new IdInvalidException(
                                                "Không tìm thấy sáng kiến với ID: " + innovationId));

                if (innovation.getUser() == null || !innovation.getUser().getId().equals(currentUserId)) {
                        throw new IdInvalidException("Bạn không có quyền xem sáng kiến này");
                }
                return innovation;
        }

        private OverviewData buildOverviewData(Innovation innovation) {
                User author = innovation.getUser();
                return OverviewData.builder()
                                .innovationId(innovation.getId())
                                .innovationName(innovation.getInnovationName())
                                .authorName(author != null ? author.getFullName() : null)
                                .departmentName(author != null && author.getDepartment() != null
                                                ? author.getDepartment().getDepartmentName()
                                                : null)
                                .academicYear(innovation.getInnovationRound() != null
                                                ? innovation.getInnovationRound().getAcademicYear()
                                                : null)
                                .status(innovation.getStatus() != null ? innovation.getStatus().name() : null)
                                .roundName(innovation.getInnovationRound() != null
                                                ? innovation.getInnovationRound().getName()
                                                : null)
                                .isScored(innovation.getCouncils() != null && !innovation.getCouncils().isEmpty())
                                .build();
        }

        private List<CoAuthorInfo> buildCoAuthorsList(Innovation innovation) {
                List<CoInnovation> coInnovations = coInnovationRepository.findByInnovationId(innovation.getId());
                return coInnovations.stream()
                                .map(co -> {
                                        User coUser = co.getUser();
                                        String email = coUser != null && coUser.getEmail() != null
                                                        ? coUser.getEmail()
                                                        : co.getContactInfo();
                                        return CoAuthorInfo.builder()
                                                        .fullName(co.getCoInnovatorFullName())
                                                        .departmentName(co.getCoInnovatorDepartmentName())
                                                        .email(email)
                                                        .build();
                                })
                                .collect(Collectors.toList());
        }

        private StatisticsData buildStatistics(Innovation innovation) {
                List<Attachment> attachments = attachmentRepository.findByInnovationId(innovation.getId());
                long reviewCommentCount = reviewScoreRepository.countByInnovationId(innovation.getId());
                return StatisticsData.builder()
                                .attachmentCount(attachments.size())
                                .reviewCommentCount((int) reviewCommentCount)
                                .build();
        }

        private List<AttachmentInfo> buildAttachmentsList(Innovation innovation) {
                List<Attachment> attachments = attachmentRepository.findByInnovationId(innovation.getId());
                String innovationId = innovation.getId();

                return attachments.stream()
                                .map(attachment -> {
                                        boolean isDigitallySigned = false;
                                        String signerName = null;
                                        String templateType = null;

                                        if (attachment.getTemplateId() != null
                                                        && isGeneratedTemplateFile(attachment, innovationId)) {
                                                Optional<FormTemplate> formTemplateOpt = formTemplateRepository
                                                                .findById(attachment.getTemplateId());
                                                if (formTemplateOpt.isPresent()) {
                                                        FormTemplate formTemplate = formTemplateOpt.get();
                                                        TemplateTypeEnum templateTypeEnum = formTemplate
                                                                        .getTemplateType();

                                                        if (templateTypeEnum != null) {
                                                                templateType = templateTypeEnum.name();
                                                        }

                                                        if (templateTypeEnum == TemplateTypeEnum.DON_DE_NGHI
                                                                        || templateTypeEnum == TemplateTypeEnum.BAO_CAO_MO_TA) {
                                                                DocumentTypeEnum documentType = mapTemplateTypeToDocumentType(
                                                                                templateTypeEnum);

                                                                if (documentType != null) {
                                                                        List<DigitalSignature> signatures = digitalSignatureRepository
                                                                                        .findByInnovationIdAndDocumentTypeWithRelations(
                                                                                                        innovationId,
                                                                                                        documentType);

                                                                        Optional<DigitalSignature> signedSignature = signatures
                                                                                        .stream()
                                                                                        .filter(sig -> sig
                                                                                                        .getStatus() == SignatureStatusEnum.SIGNED
                                                                                                        || sig.getStatus() == SignatureStatusEnum.VERIFIED)
                                                                                        .findFirst();

                                                                        if (signedSignature.isPresent()) {
                                                                                isDigitallySigned = true;
                                                                                User signer = signedSignature.get()
                                                                                                .getUser();
                                                                                signerName = signer != null
                                                                                                ? signer.getFullName()
                                                                                                : null;
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
        }

        private boolean isGeneratedTemplateFile(Attachment attachment, String innovationId) {
                if (attachment.getFileName() == null || attachment.getTemplateId() == null) {
                        return false;
                }
                String expectedFileName = innovationId + "_" + attachment.getTemplateId() + ".pdf";
                return attachment.getFileName().equals(expectedFileName);
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

        private List<ReviewCommentInfo> buildReviewCommentsList(Innovation innovation) {
                List<ReviewScore> reviewScores = reviewScoreRepository.findByInnovationId(innovation.getId());
                return reviewScores.stream()
                                .map(reviewScore -> {
                                        User reviewer = reviewScore.getReviewer();
                                        String reviewerName = reviewer != null ? reviewer.getFullName() : "Unknown";
                                        String level = determineReviewLevel(reviewer);
                                        LocalDateTime createdAt = reviewScore.getReviewedAt() != null
                                                        ? reviewScore.getReviewedAt()
                                                        : reviewScore.getCreatedAt();
                                        String content = reviewScore.getDetailedComments() != null
                                                        ? reviewScore.getDetailedComments()
                                                        : "";

                                        return ReviewCommentInfo.builder()
                                                        .reviewerName(reviewerName)
                                                        .reviewerRole("Thành viên hội đồng")
                                                        .level(level)
                                                        .createdAt(createdAt)
                                                        .content(content)
                                                        .build();
                                })
                                .collect(Collectors.toList());
        }

        private String determineReviewLevel(User reviewer) {
                if (reviewer == null) {
                        return "Cấp Khoa";
                }

                if (reviewer.getUserRoles() != null) {
                        boolean hasTruongRole = reviewer.getUserRoles().stream()
                                        .anyMatch(userRole -> userRole.getRole() != null
                                                        && userRole.getRole()
                                                                        .getRoleName() == UserRoleEnum.TV_HOI_DONG_TRUONG);
                        if (hasTruongRole) {
                                return "Cấp Trường";
                        }
                }

                return "Cấp Khoa";
        }

        private List<WorkflowStepInfo> buildWorkflowSteps(Innovation innovation) {
                List<WorkflowStepInfo> steps = new ArrayList<>();
                steps.add(WorkflowStepInfo.builder()
                                .stepName("Nộp hồ sơ")
                                .description("Giảng viên nộp hồ sơ sáng kiến")
                                .completedAt(innovation.getCreatedAt())
                                .isCompleted(true)
                                .isCurrent(false)
                                .build());

                boolean isSubmitted = innovation.getStatus() != null &&
                                (innovation.getStatus().name().equals("SUBMITTED") ||
                                                innovation.getStatus().name().equals("UNDER_REVIEW") ||
                                                innovation.getStatus().name().equals("APPROVED"));

                steps.add(WorkflowStepInfo.builder()
                                .stepName("Thư ký Khoa sơ duyệt")
                                .description("Hồ sơ đã được sơ duyệt và chấp nhận")
                                .completedAt(isSubmitted ? innovation.getUpdatedAt() : null)
                                .isCompleted(isSubmitted)
                                .isCurrent(!isSubmitted)
                                .build());

                boolean isUnderReview = innovation.getStatus() != null &&
                                (innovation.getStatus().name().equals("UNDER_REVIEW") ||
                                                innovation.getStatus().name().equals("APPROVED"));

                steps.add(WorkflowStepInfo.builder()
                                .stepName("Hội đồng Khoa đánh giá")
                                .description("Đã được hội đồng cấp Khoa đánh giá và thông qua")
                                .completedAt(isUnderReview ? innovation.getUpdatedAt() : null)
                                .isCompleted(isUnderReview)
                                .isCurrent(isSubmitted && !isUnderReview)
                                .build());

                boolean isApproved = innovation.getStatus() != null && innovation.getStatus().name().equals("APPROVED");

                steps.add(WorkflowStepInfo.builder()
                                .stepName("Trường phê duyệt")
                                .description("Đã được Trường phê duyệt")
                                .completedAt(isApproved ? innovation.getUpdatedAt() : null)
                                .isCompleted(isApproved)
                                .isCurrent(isUnderReview && !isApproved)
                                .build());

                return steps;
        }

        private List<ActivityHistoryInfo> buildActivityHistory(Innovation innovation) {
                List<ActivityHistoryInfo> history = new ArrayList<>();
                User author = innovation.getUser();

                history.add(ActivityHistoryInfo.builder()
                                .actionName("Nộp hồ sơ")
                                .fromStatus("Bản nháp")
                                .toStatus("Đã nộp")
                                .actorName(author != null ? author.getFullName() : "Unknown")
                                .actorRole("GIANG_VIEN")
                                .timestamp(innovation.getCreatedAt())
                                .notes("Giảng viên nộp hồ sơ sáng kiến lần đầu")
                                .build());

                if (innovation.getStatus() != null && !innovation.getStatus().name().equals("DRAFT")) {
                        history.add(ActivityHistoryInfo.builder()
                                        .actionName("Sơ duyệt hồ sơ")
                                        .fromStatus("Đã nộp")
                                        .toStatus("Chờ khoa đánh giá")
                                        .actorName("Thư ký Khoa")
                                        .actorRole("QUAN_TRI_VIEN_KHOA")
                                        .timestamp(innovation.getUpdatedAt())
                                        .notes("Hồ sơ đầy đủ, chuyển sang giai đoạn đánh giá")
                                        .build());
                }

                history.sort((a, b) -> a.getTimestamp().compareTo(b.getTimestamp()));
                return history;
        }
}
