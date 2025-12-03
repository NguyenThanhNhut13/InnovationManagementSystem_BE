package vn.edu.iuh.fit.innovationmanagementsystem_be.service;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.List;

/**
 * Configuration class for child fields within SECTION fields
 */
public class ChildFieldConfig {
    String fieldKey;
    String fieldType;
    String sourceFieldKey; // Cho INNOVATION_DATA và USER_DATA
    JsonNode referenceConfig; // Cho REFERENCE
    List<String> targetTemplateIds; // Cho CONTRIBUTED (thay vì formTemplateId)

    public ChildFieldConfig(String fieldKey, String fieldType) {
        this.fieldKey = fieldKey;
        this.fieldType = fieldType;
    }
}

