package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums;

public enum TemplateTypeEnum {
    DON_DE_NGHI("Đơn đề nghị"),
    BAO_CAO_MO_TA("Báo cáo mô tả"),
    BIEN_BAN_HOP("Biên bản cuộc họp"),
    TONG_HOP_DE_NGHI("Tổng hợp đề nghị"),
    TONG_HOP_CHAM_DIEM("Tổng hợp chấm điểm"),
    PHIEU_DANH_GIA("Phiếu đánh giá"),
    BIEN_BAN_HOI_DONG("Biên bản hội đồng");

    private final String value;

    TemplateTypeEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return value;
    }
}
