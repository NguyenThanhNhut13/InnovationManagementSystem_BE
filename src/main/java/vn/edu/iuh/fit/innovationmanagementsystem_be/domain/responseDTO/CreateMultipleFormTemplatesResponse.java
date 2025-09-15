package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateMultipleFormTemplatesResponse {

    private String innovationPhaseId;
    private String innovationPhaseName;
    private int totalCreated;
    private List<FormTemplateResponse> formTemplates;

    public CreateMultipleFormTemplatesResponse(String innovationPhaseId, String innovationPhaseName,
            List<FormTemplateResponse> formTemplates) {
        this.innovationPhaseId = innovationPhaseId;
        this.innovationPhaseName = innovationPhaseName;
        this.totalCreated = formTemplates.size();
        this.formTemplates = formTemplates;
    }
}
