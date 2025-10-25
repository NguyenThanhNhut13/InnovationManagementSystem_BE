package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.InnovationStatusEnum;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InnovationFormDataRequest {

    @NotBlank(message = "Template ID không được để trống")
    private String templateId;

    @NotEmpty(message = "Danh sách form data không được để trống")
    private List<FormDataItemRequest> formDataItems;

    @Pattern(regexp = "^(DRAFT|SUBMITTED)$", message = "Action type chỉ được là DRAFT hoặc SUBMITTED")
    private String actionType = InnovationStatusEnum.DRAFT.name();

    private String innovationName;
    private String innovationPhaseId;
    private Boolean isScore;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FormDataItemRequest {
        @NotBlank(message = "Field value không được để trống")
        private String fieldValue;

        @NotBlank(message = "Form field ID không được để trống")
        private String formFieldId;

        private String dataId; // Cho update operations
    }
}
