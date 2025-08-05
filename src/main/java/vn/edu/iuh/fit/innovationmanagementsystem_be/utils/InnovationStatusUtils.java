package vn.edu.iuh.fit.innovationmanagementsystem_be.utils;

import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.Innovation.InnovationStatus;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class InnovationStatusUtils {

    // Workflow transitions - các bước chuyển đổi trạng thái hợp lệ
    private static final Map<InnovationStatus, Set<InnovationStatus>> VALID_TRANSITIONS = new HashMap<>();

    static {
        VALID_TRANSITIONS.put(InnovationStatus.DRAFT, Set.of(InnovationStatus.SUBMITTED));
        VALID_TRANSITIONS.put(InnovationStatus.SUBMITTED,
                Set.of(InnovationStatus.PENDING_KHOA_REVIEW, InnovationStatus.RETURNED_TO_SUBMITTER));
        VALID_TRANSITIONS.put(InnovationStatus.PENDING_KHOA_REVIEW,
                Set.of(InnovationStatus.KHOA_REVIEWED, InnovationStatus.RETURNED_TO_SUBMITTER));
        VALID_TRANSITIONS.put(InnovationStatus.RETURNED_TO_SUBMITTER, Set.of(InnovationStatus.SUBMITTED));
        VALID_TRANSITIONS.put(InnovationStatus.KHOA_REVIEWED,
                Set.of(InnovationStatus.KHOA_APPROVED, InnovationStatus.KHOA_REJECTED));
        VALID_TRANSITIONS.put(InnovationStatus.KHOA_APPROVED, Set.of(InnovationStatus.PENDING_TRUONG_REVIEW));
        VALID_TRANSITIONS.put(InnovationStatus.KHOA_REJECTED, Set.of(InnovationStatus.RETURNED_TO_SUBMITTER));
        VALID_TRANSITIONS.put(InnovationStatus.PENDING_TRUONG_REVIEW,
                Set.of(InnovationStatus.TRUONG_REVIEWED, InnovationStatus.RETURNED_TO_SUBMITTER));
        VALID_TRANSITIONS.put(InnovationStatus.TRUONG_REVIEWED,
                Set.of(InnovationStatus.TRUONG_APPROVED, InnovationStatus.TRUONG_REJECTED));
        VALID_TRANSITIONS.put(InnovationStatus.TRUONG_APPROVED, Set.of(InnovationStatus.FINAL_APPROVED));
        VALID_TRANSITIONS.put(InnovationStatus.TRUONG_REJECTED, Set.of(InnovationStatus.RETURNED_TO_SUBMITTER));
        VALID_TRANSITIONS.put(InnovationStatus.FINAL_APPROVED, Set.of()); // Final state
    }

    // Status groups for different roles
    public static final List<InnovationStatus> DRAFT_STATUSES = Arrays.asList(
            InnovationStatus.DRAFT);

    public static final List<InnovationStatus> SUBMITTED_STATUSES = Arrays.asList(
            InnovationStatus.SUBMITTED,
            InnovationStatus.PENDING_KHOA_REVIEW,
            InnovationStatus.RETURNED_TO_SUBMITTER);

    public static final List<InnovationStatus> KHOA_REVIEW_STATUSES = Arrays.asList(
            InnovationStatus.PENDING_KHOA_REVIEW,
            InnovationStatus.KHOA_REVIEWED,
            InnovationStatus.KHOA_APPROVED,
            InnovationStatus.KHOA_REJECTED);

    public static final List<InnovationStatus> TRUONG_REVIEW_STATUSES = Arrays.asList(
            InnovationStatus.PENDING_TRUONG_REVIEW,
            InnovationStatus.TRUONG_REVIEWED,
            InnovationStatus.TRUONG_APPROVED,
            InnovationStatus.TRUONG_REJECTED);

    public static final List<InnovationStatus> FINAL_STATUSES = Arrays.asList(
            InnovationStatus.FINAL_APPROVED);

    /**
     * Kiểm tra xem việc chuyển đổi trạng thái có hợp lệ không
     */
    public static boolean isValidTransition(InnovationStatus currentStatus, InnovationStatus newStatus) {
        Set<InnovationStatus> validNextStatuses = VALID_TRANSITIONS.get(currentStatus);
        return validNextStatuses != null && validNextStatuses.contains(newStatus);
    }

    /**
     * Lấy danh sách trạng thái tiếp theo hợp lệ
     */
    public static Set<InnovationStatus> getValidNextStatuses(InnovationStatus currentStatus) {
        return VALID_TRANSITIONS.getOrDefault(currentStatus, Set.of());
    }

    /**
     * Kiểm tra xem trạng thái có phải là trạng thái cuối cùng không
     */
    public static boolean isFinalStatus(InnovationStatus status) {
        return status == InnovationStatus.FINAL_APPROVED;
    }

    /**
     * Kiểm tra xem trạng thái có thể chỉnh sửa không
     */
    public static boolean isEditable(InnovationStatus status) {
        return status == InnovationStatus.DRAFT || status == InnovationStatus.RETURNED_TO_SUBMITTER;
    }

    /**
     * Lấy mô tả trạng thái bằng tiếng Việt
     */
    public static String getStatusDescription(InnovationStatus status) {
        return switch (status) {
            case DRAFT -> "Bản nháp";
            case SUBMITTED -> "Đã nộp";
            case PENDING_KHOA_REVIEW -> "Chờ Khoa duyệt";
            case RETURNED_TO_SUBMITTER -> "Trả về người nộp";
            case KHOA_REVIEWED -> "Khoa đã duyệt";
            case KHOA_APPROVED -> "Khoa phê duyệt";
            case KHOA_REJECTED -> "Khoa từ chối";
            case PENDING_TRUONG_REVIEW -> "Chờ Trường duyệt";
            case TRUONG_REVIEWED -> "Trường đã duyệt";
            case TRUONG_APPROVED -> "Trường phê duyệt";
            case TRUONG_REJECTED -> "Trường từ chối";
            case FINAL_APPROVED -> "Phê duyệt cuối cùng";
        };
    }

    /**
     * Lấy màu sắc cho trạng thái (cho UI)
     */
    public static String getStatusColor(InnovationStatus status) {
        return switch (status) {
            case DRAFT -> "#6c757d"; // Gray
            case SUBMITTED -> "#17a2b8"; // Info
            case PENDING_KHOA_REVIEW -> "#ffc107"; // Warning
            case RETURNED_TO_SUBMITTER -> "#dc3545"; // Danger
            case KHOA_REVIEWED -> "#28a745"; // Success
            case KHOA_APPROVED -> "#28a745"; // Success
            case KHOA_REJECTED -> "#dc3545"; // Danger
            case PENDING_TRUONG_REVIEW -> "#ffc107"; // Warning
            case TRUONG_REVIEWED -> "#28a745"; // Success
            case TRUONG_APPROVED -> "#28a745"; // Success
            case TRUONG_REJECTED -> "#dc3545"; // Danger
            case FINAL_APPROVED -> "#007bff"; // Primary
        };
    }
}