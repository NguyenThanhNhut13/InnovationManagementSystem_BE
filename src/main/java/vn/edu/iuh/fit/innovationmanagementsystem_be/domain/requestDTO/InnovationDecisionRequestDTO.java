package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InnovationDecisionRequestDTO {

    private String decisionNumber;

    private Integer yearDecision;

    // Validation annotations
    @jakarta.validation.constraints.NotBlank(message = "Decision number is required")
    public String getDecisionNumber() {
        return decisionNumber;
    }

    public void setDecisionNumber(String decisionNumber) {
        this.decisionNumber = decisionNumber;
    }

    @jakarta.validation.constraints.NotNull(message = "Year decision is required")
    public Integer getYearDecision() {
        return yearDecision;
    }

    public void setYearDecision(Integer yearDecision) {
        this.yearDecision = yearDecision;
    }
}