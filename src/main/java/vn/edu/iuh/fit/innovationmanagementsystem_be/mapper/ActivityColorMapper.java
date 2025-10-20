package vn.edu.iuh.fit.innovationmanagementsystem_be.mapper;

import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.InnovationStatusEnum;

public class ActivityColorMapper {

    /**
     * Lấy màu icon dựa trên InnovationStatusEnum
     * 
     * @param status InnovationStatusEnum
     * @return String color
     */
    public static String getIconColor(InnovationStatusEnum status) {
        if (status == null) {
            return "gray";
        }

        return switch (status) {
            case DRAFT -> "blue";
            case SUBMITTED -> "orange";
            case PENDING_KHOA_REVIEW, PENDING_TRUONG_REVIEW -> "yellow";
            case KHOA_REVIEWED, TRUONG_REVIEWED -> "purple";
            case KHOA_APPROVED, TRUONG_APPROVED, FINAL_APPROVED -> "green";
            case KHOA_REJECTED, TRUONG_REJECTED -> "red";
        };
    }
}
