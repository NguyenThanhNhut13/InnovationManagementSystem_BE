package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegulationResponseDTO {

    private UUID id;

    private String clauseNumber;

    private String title;

    private String content;

    private UUID decisionId;

    private String decisionNumber;

    private LocalDateTime createdAt;

    private LocalDateTime updateAt;

    public RegulationResponseDTO(vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.Regulation regulation) {
        this.id = regulation.getId();
        this.clauseNumber = regulation.getClauseNumber();
        this.title = regulation.getTitle();
        this.content = regulation.getContent();
        this.createdAt = regulation.getCreatedAt();
        this.updateAt = regulation.getUpdateAt();

        if (regulation.getInnovationDecision() != null) {
            this.decisionId = regulation.getInnovationDecision().getId();
            this.decisionNumber = regulation.getInnovationDecision().getDecisionNumber();
        }
    }
}