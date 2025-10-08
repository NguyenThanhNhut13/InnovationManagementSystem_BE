package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.FieldTypeEnum;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.TemplateTypeEnum;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.TargetRoleCode;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateTemplateWithFieldsRequest {

    @NotBlank(message = "Template content không được để trống")
    private String templateContent;

    @NotNull(message = "Template type không được để trống")
    private TemplateTypeEnum templateType;

    @NotNull(message = "Target role không được để trống")
    private TargetRoleCode targetRole;

    @NotBlank(message = "Round ID không được để trống")
    private String roundId;

    @NotEmpty(message = "Danh sách fields không được để trống")
    @Valid
    private List<FieldData> fields;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FieldData {

        @NotBlank(message = "Field key không được để trống")
        private String fieldKey;

        @NotBlank(message = "Field label không được để trống")
        private String label;

        @NotNull(message = "Field type không được để trống")
        private FieldTypeEnum type;

        @NotNull(message = "Required status không được để trống")
        private Boolean required = false;

        private String placeholder;

        private TableConfigData tableConfig;

        private Boolean repeatable = false;
        private List<FieldData> children;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TableConfigData {

        @NotEmpty(message = "Danh sách columns không được để trống")
        @Valid
        private List<ColumnData> columns;

        private Integer minRows = 1;
        private Integer maxRows = 100;
        private Boolean allowAddRows = true;
        private Boolean allowDeleteRows = true;

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        public static class ColumnData {

            @NotBlank(message = "Column ID không được để trống")
            private String id;

            @NotBlank(message = "Column key không được để trống")
            private String key;

            @NotBlank(message = "Column label không được để trống")
            private String label;

            @NotNull(message = "Column type không được để trống")
            private FieldTypeEnum type;

            @NotNull(message = "Column required status không được để trống")
            private Boolean required = false;
        }
    }
}
