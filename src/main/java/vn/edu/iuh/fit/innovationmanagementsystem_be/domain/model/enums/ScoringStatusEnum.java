package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum ScoringStatusEnum {
    ALL("ALL"),           // Tất cả
    PENDING("PENDING"),   // Chưa chấm
    SCORED("SCORED");     // Đã chấm

    private final String value;

    ScoringStatusEnum(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}

