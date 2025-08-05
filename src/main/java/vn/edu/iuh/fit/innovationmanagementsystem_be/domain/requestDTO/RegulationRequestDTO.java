package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegulationRequestDTO {

    private String clauseNumber;

    private String title;

    private String content;

    private UUID decisionId;

    // Validation annotations
    @jakarta.validation.constraints.NotBlank(message = "Clause number is required")
    public String getClauseNumber() {
        return clauseNumber;
    }

    public void setClauseNumber(String clauseNumber) {
        this.clauseNumber = clauseNumber;
    }

    @jakarta.validation.constraints.NotBlank(message = "Title is required")
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @jakarta.validation.constraints.NotNull(message = "Decision ID is required")
    public UUID getDecisionId() {
        return decisionId;
    }

    public void setDecisionId(UUID decisionId) {
        this.decisionId = decisionId;
    }
}