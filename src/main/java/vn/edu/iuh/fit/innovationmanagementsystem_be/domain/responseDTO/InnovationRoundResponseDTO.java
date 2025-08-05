package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.InnovationRound;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.FormTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InnovationRoundResponseDTO {

    private UUID id;

    private String name;

    private LocalDate startDate;

    private LocalDate endDate;

    private InnovationRound.InnovationRoundStatus status;

    private List<UUID> formTemplateIds;

    private List<String> formTemplateNames;

    private Integer innovationCount;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private String createdBy;

    private String updatedBy;

    public InnovationRoundResponseDTO(InnovationRound innovationRound) {
        this.id = innovationRound.getId();
        this.name = innovationRound.getName();
        this.startDate = innovationRound.getStartDate();
        this.endDate = innovationRound.getEndDate();
        this.status = innovationRound.getStatus();
        this.createdAt = innovationRound.getCreatedAt();
        this.updatedAt = innovationRound.getUpdatedAt();
        this.createdBy = innovationRound.getCreatedBy();
        this.updatedBy = innovationRound.getUpdatedBy();

        if (innovationRound.getFormTemplates() != null && !innovationRound.getFormTemplates().isEmpty()) {
            this.formTemplateIds = innovationRound.getFormTemplates().stream()
                    .map(FormTemplate::getId)
                    .collect(java.util.stream.Collectors.toList());
            this.formTemplateNames = innovationRound.getFormTemplates().stream()
                    .map(FormTemplate::getName)
                    .collect(java.util.stream.Collectors.toList());
        }

        // Set innovation count
        if (innovationRound.getInnovations() != null) {
            this.innovationCount = innovationRound.getInnovations().size();
        } else {
            this.innovationCount = 0;
        }
    }
}