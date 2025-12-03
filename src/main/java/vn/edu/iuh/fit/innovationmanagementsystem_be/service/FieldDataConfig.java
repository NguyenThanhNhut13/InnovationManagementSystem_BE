package vn.edu.iuh.fit.innovationmanagementsystem_be.service;

import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.FieldTypeEnum;

import java.util.ArrayList;
import java.util.List;

/**
 * Configuration class for fields that need dynamic data
 */
public class FieldDataConfig {
    String fieldKey;
    FieldTypeEnum fieldType;
    List<ChildFieldConfig> childConfigs;
    List<TableColumnConfig> tableColumns;

    public FieldDataConfig(String fieldKey, FieldTypeEnum fieldType) {
        this.fieldKey = fieldKey;
        this.fieldType = fieldType;
        this.childConfigs = new ArrayList<>();
        this.tableColumns = new ArrayList<>();
    }
}

