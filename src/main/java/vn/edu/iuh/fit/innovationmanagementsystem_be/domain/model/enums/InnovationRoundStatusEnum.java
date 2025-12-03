package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums;

import lombok.Getter;

@Getter
public enum InnovationRoundStatusEnum {
    DRAFT("DRAFT"), OPEN("OPEN"), CLOSED("CLOSED"), COMPLETED("COMPLETED");

    private final String value;

    InnovationRoundStatusEnum(String value) {
        this.value = value;
    }
}
