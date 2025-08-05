package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FormTemplateResponseDTO {

    private UUID id;

    private String name;

    private String description;

    private String templateContent;

    private LocalDateTime createAt;

    private LocalDateTime updateAt;

    private String createdBy;

    private String updatedBy;

    private UUID innovationRoundId;

    private String innovationRoundName;

    public FormTemplateResponseDTO(
            vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.FormTemplate formTemplate) {
        this.id = formTemplate.getId();
        this.name = formTemplate.getName();
        this.description = formTemplate.getDescription();
        this.templateContent = formTemplate.getTemplateContent();
        this.createAt = formTemplate.getCreateAt();
        this.updateAt = formTemplate.getUpdateAt();
        this.createdBy = formTemplate.getCreatedBy();
        this.updatedBy = formTemplate.getUpdatedBy();

        if (formTemplate.getInnovationRound() != null) {
            this.innovationRoundId = formTemplate.getInnovationRound().getId();
            this.innovationRoundName = formTemplate.getInnovationRound().getName();
        }
    }
}