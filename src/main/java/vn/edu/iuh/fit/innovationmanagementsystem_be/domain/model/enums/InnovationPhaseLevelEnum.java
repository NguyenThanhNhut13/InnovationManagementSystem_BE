package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums;

import lombok.Getter;

@Getter
public enum InnovationPhaseLevelEnum {
    SCHOOL("SCHOOL"), DEPARTMENT("DEPARTMENT");

    private final String value;

    InnovationPhaseLevelEnum(String value) {
        this.value = value;
    }


}
