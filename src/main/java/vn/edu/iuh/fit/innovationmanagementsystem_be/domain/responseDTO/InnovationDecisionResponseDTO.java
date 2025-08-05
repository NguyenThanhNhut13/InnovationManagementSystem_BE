package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InnovationDecisionResponseDTO {

    private UUID id;

    private String decisionNumber;

    private Integer yearDecision;

    private UUID innovationRoundId;

    private String innovationRoundName;

    private LocalDateTime createdAt;

    private LocalDateTime updateAt;

    public InnovationDecisionResponseDTO(
            vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.InnovationDecision innovationDecision) {
        this.id = innovationDecision.getId();
        this.decisionNumber = innovationDecision.getDecisionNumber();
        this.yearDecision = innovationDecision.getYearDecision();
        this.createdAt = innovationDecision.getCreatedAt();
        this.updateAt = innovationDecision.getUpdateAt();

        if (innovationDecision.getInnovationRound() != null) {
            this.innovationRoundId = innovationDecision.getInnovationRound().getId();
            this.innovationRoundName = innovationDecision.getInnovationRound().getName();
        }
    }
}