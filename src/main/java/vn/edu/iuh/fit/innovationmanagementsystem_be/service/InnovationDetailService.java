package vn.edu.iuh.fit.innovationmanagementsystem_be.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.*;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.ReviewLevelEnum;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.*;
import vn.edu.iuh.fit.innovationmanagementsystem_be.exception.IdInvalidException;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.*;

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
        private final ReviewCommentRepository reviewCommentRepository;
        private final DigitalSignatureRepository digitalSignatureRepository;

        // 1. Main method
        public InnovationDetailForGiangVienResponse getMyInnovationDetailForGiangVien(String innovationId) {
                Innovation innovation = validateMyInnovation(innovationId);

                return InnovationDetailForGiangVienResponse.builder()
                                .overview(buildOverviewData(innovation))
                                .coAuthors(buildCoAuthorsList(innovation))
                                .statistics(buildStatistics(innovation))
                                .formData(innovationService.getMyInnovationWithFormDataById(innovationId))
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
                                .roundName("Đợt " + (innovation.getInnovationRound() != null
                                                ? innovation.getInnovationRound().getId()
                                                : ""))
                                .isScored(innovation.getCouncils() != null && !innovation.getCouncils().isEmpty())
                                .build();
        }

        private List<CoAuthorInfo> buildCoAuthorsList(Innovation innovation) {
                List<CoInnovation> coInnovations = coInnovationRepository.findByInnovationId(innovation.getId());
                return coInnovations.stream()
                                .map(co -> CoAuthorInfo.builder()
                                                .fullName(co.getCoInnovatorFullName())
                                                .departmentName(co.getCoInnovatorDepartmentName())
                                                .email(co.getContactInfo() != null ? co.getContactInfo() : "")
                                                .build())
                                .collect(Collectors.toList());
        }

        private StatisticsData buildStatistics(Innovation innovation) {
                List<Attachment> attachments = attachmentRepository.findByInnovationId(innovation.getId());
                List<ReviewComment> reviewComments = reviewCommentRepository.findByInnovationId(innovation.getId());
                return StatisticsData.builder()
                                .attachmentCount(attachments.size())
                                .reviewCommentCount(reviewComments.size())
                                .build();
        }

        private List<AttachmentInfo> buildAttachmentsList(Innovation innovation) {
                List<Attachment> attachments = attachmentRepository.findByInnovationId(innovation.getId());
                List<DigitalSignature> signatures = digitalSignatureRepository
                                .findByInnovationIdWithRelations(innovation.getId());

                return attachments.stream()
                                .map(attachment -> {
                                        boolean hasSig = !signatures.isEmpty();
                                        String signerName = hasSig && !signatures.isEmpty()
                                                        && signatures.get(0).getUser() != null
                                                                        ? signatures.get(0).getUser().getFullName()
                                                                        : null;

                                        return AttachmentInfo.builder()
                                                        .fileName(attachment.getFileName())
                                                        .uploadedAt(attachment.getCreatedAt())
                                                        .isDigitallySigned(hasSig)
                                                        .signerName(signerName)
                                                        .build();
                                })
                                .collect(Collectors.toList());
        }

        private List<ReviewCommentInfo> buildReviewCommentsList(Innovation innovation) {
                List<ReviewComment> reviewComments = reviewCommentRepository.findByInnovationId(innovation.getId());
                return reviewComments.stream()
                                .map(comment -> {
                                        User reviewer = comment.getCouncilMember() != null
                                                        && comment.getCouncilMember().getUser() != null
                                                                        ? comment.getCouncilMember().getUser()
                                                                        : null;
                                        return ReviewCommentInfo.builder()
                                                        .reviewerName(reviewer != null ? reviewer.getFullName()
                                                                        : "Unknown")
                                                        .reviewerRole("Thành viên hội đồng")
                                                        .level(comment.getReviewsLevel() == ReviewLevelEnum.TRUONG
                                                                        ? "Cấp Trường"
                                                                        : "Cấp Khoa")
                                                        .createdAt(comment.getCreatedAt())
                                                        .content(comment.getComment())
                                                        .build();
                                })
                                .collect(Collectors.toList());
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
                                .stepName("Trưởng Khoa phê duyệt")
                                .description("Đã được Trưởng Khoa phê duyệt")
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

                List<ReviewComment> comments = reviewCommentRepository.findByInnovationId(innovation.getId());
                if (!comments.isEmpty()) {
                        ReviewComment firstComment = comments.get(0);
                        history.add(ActivityHistoryInfo.builder()
                                        .actionName("Đánh giá hội đồng")
                                        .fromStatus("Chờ khoa đánh giá")
                                        .toStatus("Khoa đã đánh giá")
                                        .actorName("Hội đồng cấp Khoa")
                                        .actorRole("TV_HOI_DONG_KHOA")
                                        .timestamp(firstComment.getCreatedAt())
                                        .notes("Hội đồng đã hoàn thành đánh giá")
                                        .build());
                }

                history.sort((a, b) -> a.getTimestamp().compareTo(b.getTimestamp()));
                return history;
        }
}
