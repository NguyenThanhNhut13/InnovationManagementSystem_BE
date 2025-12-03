package vn.edu.iuh.fit.innovationmanagementsystem_be.service;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.List;

/**
 * Configuration class for table columns that need dynamic data
 */
public class TableColumnConfig {
    String columnKey;
    String columnType;
    String sourceFieldKey; // Cho INNOVATION_DATA và USER_DATA
    JsonNode referenceConfig; // Cho REFERENCE
    List<String> targetTemplateIds; // Cho CONTRIBUTED (thay vì formTemplateId)

    public TableColumnConfig(String columnKey, String columnType) {
        this.columnKey = columnKey;
        this.columnType = columnType;
    }
}

