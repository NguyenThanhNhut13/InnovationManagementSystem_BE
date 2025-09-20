package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FormDataRequest {

    // Fields cho Create/Update single FormData
    @NotBlank(message = "Field value không được để trống")
    private String fieldValue;

    @NotBlank(message = "Form field ID không được để trống")
    private String formFieldId;

    @NotBlank(message = "Innovation ID không được để trống")
    private String innovationId;

    // Fields cho bulk operations
    private List<FormDataItemRequest> formDataItems;

    // Fields cho bulk update
    private String bulkOperation; // UPDATE, DELETE, VALIDATE
    private List<String> formDataIds;

    // Fields cho form status
    private String formStatus; // DRAFT, SUBMITTED, APPROVED, REJECTED
    private String submitComment;

}

@Data
@NoArgsConstructor
@AllArgsConstructor
class FormDataItemRequest {
    @NotBlank(message = "Field value không được để trống")
    private String fieldValue;

    @NotBlank(message = "Form field ID không được để trống")
    private String formFieldId;

    private String dataId; // Cho update operations
}