package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums;

public enum TemplateTypeEnum {
    DON_DE_NGHI("DON_DE_NGHI"), // Đơn đề nghị công nhận sáng kiến
    BAO_CAO_MO_TA("BAO_CAO_MO_TA"), // Báo cáo mô tả sáng kiến
    BIEN_BAN_HOP("BIEN_BAN_HOP"), // Biên bản họp đơn vị đánh giá
    TONG_HOP_DE_NGHI("TONG_HOP_DE_NGHI"), // Tổng hợp đề nghị công nhận
    TONG_HOP_CHAM_DIEM("TONG_HOP_CHAM_DIEM"), // Tổng hợp có chấm điểm
    PHIEU_DANH_GIA("PHIEU_DANH_GIA"), // Phiếu đánh giá sáng kiến
    BIEN_BAN_HOI_DONG("BIEN_BAN_HOI_DONG"); // Biên bản họp hội đồng sáng kiến Trường

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
