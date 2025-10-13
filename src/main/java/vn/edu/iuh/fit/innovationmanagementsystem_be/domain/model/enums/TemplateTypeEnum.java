package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums;

public enum TemplateTypeEnum {
    DON_DE_NGHI("DON_DE_NGHI"),
    BAO_CAO_MO_TA("BAO_CAO_MO_TA"),
    BIEN_BAN_HOP("BIEN_BAN_HOP"),
    TONG_HOP_DE_NGHI("TONG_HOP_DE_NGHI"),
    TONG_HOP_CHAM_DIEM("TONG_HOP_CHAM_DIEM"),
    PHIEU_DANH_GIA("PHIEU_DANH_GIA"),
    BIEN_BAN_HOI_DONG("BIEN_BAN_HOI_DONG");

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
