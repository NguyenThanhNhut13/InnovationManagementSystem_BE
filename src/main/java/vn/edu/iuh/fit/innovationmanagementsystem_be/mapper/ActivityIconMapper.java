package vn.edu.iuh.fit.innovationmanagementsystem_be.mapper;

import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.InnovationStatusEnum;

public class ActivityIconMapper {

    // Lấy loại icon dựa trên InnovationStatusEnum
    public static String getIconType(InnovationStatusEnum status) {
        if (status == null) {
            return "default";
        }

        return switch (status) {
            case DRAFT -> "create";
            case SUBMITTED -> "submit";
            case PENDING_KHOA_REVIEW, PENDING_TRUONG_REVIEW -> "pending";
            case TRUONG_REVIEWED -> "review";
            case KHOA_APPROVED, TRUONG_APPROVED, FINAL_APPROVED -> "approve";
            case KHOA_REJECTED, TRUONG_REJECTED -> "reject";
        };
    }
}
