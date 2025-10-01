package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums;

import lombok.Getter;

@Getter
public enum InnovationPhaseTypeEnum {
    SUBMISSION("SUBMISSION"), SCORING("SCORING"), ANNOUNCEMENT("ANNOUNCEMENT");
    private final String value;

    InnovationPhaseTypeEnum(String value) {
        this.value = value;
    }

}
