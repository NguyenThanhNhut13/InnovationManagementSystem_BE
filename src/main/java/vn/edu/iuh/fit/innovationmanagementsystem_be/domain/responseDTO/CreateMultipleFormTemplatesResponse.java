package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateMultipleFormTemplatesResponse {

    private String innovationRoundId;
    private String innovationRoundName;
    private int totalCreated;
    private List<FormTemplateResponse> formTemplates;

    public CreateMultipleFormTemplatesResponse(String innovationRoundId, String innovationRoundName,
            List<FormTemplateResponse> formTemplates) {
        this.innovationRoundId = innovationRoundId;
        this.innovationRoundName = innovationRoundName;
        this.totalCreated = formTemplates.size();
        this.formTemplates = formTemplates;
    }
}
