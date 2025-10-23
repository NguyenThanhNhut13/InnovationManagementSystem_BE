package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.FieldTypeEnum;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.UserRoleEnum;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TableConfigData {

    private List<ColumnData> columns;

    private Integer minRows = 1;
    private Integer maxRows = 100;
    private Boolean allowAddRows = true;
    private Boolean allowDeleteRows = true;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ColumnData {

        private String id;

        @NotBlank(message = "Column key không được để trống")
        private String key;

        @NotBlank(message = "Column label không được để trống")
        private String label;

        @NotNull(message = "Column type không được để trống")
        private FieldTypeEnum type;

        @NotNull(message = "Column required status không được để trống")
        private Boolean required = false;

        private List<String> options;

        private JsonNode referenceConfig;

        private JsonNode userDataConfig;

        private UserRoleEnum signingRole;
    }
}
