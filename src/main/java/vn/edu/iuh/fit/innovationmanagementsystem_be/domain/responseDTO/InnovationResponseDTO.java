package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.Innovation;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InnovationResponseDTO {

    private UUID id;

    private UUID userId;
    private String userName;
    private String userFullName;
    private String userEmail;

    private Boolean isScore;

    private String innovationName;

    private UUID departmentId;
    private String departmentName;
    private String departmentCode;

    private UUID innovationRoundId;
    private String innovationRoundName;

    private Innovation.InnovationStatus status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private String createdBy;

    private String updatedBy;

    private List<AttachmentResponseDTO> attachments;

    // Constructor to convert from Innovation entity
    public InnovationResponseDTO(Innovation innovation) {
        this.id = innovation.getId();
        this.isScore = innovation.getIsScore();
        this.innovationName = innovation.getInnovationName();
        this.status = innovation.getStatus();
        this.createdAt = innovation.getCreatedAt();
        this.updatedAt = innovation.getUpdatedAt();
        this.createdBy = innovation.getCreatedBy();
        this.updatedBy = innovation.getUpdatedBy();

        // Set user information if available
        if (innovation.getUser() != null) {
            this.userId = innovation.getUser().getId();
            this.userName = innovation.getUser().getUserName();
            this.userFullName = innovation.getUser().getFullName();
            this.userEmail = innovation.getUser().getEmail();
        }

        // Set department information if available
        if (innovation.getDepartment() != null) {
            this.departmentId = innovation.getDepartment().getId();
            this.departmentName = innovation.getDepartment().getDepartmentName();
            this.departmentCode = innovation.getDepartment().getDepartmentCode();
        }

        // Set innovation round information if available
        if (innovation.getInnovationRound() != null) {
            this.innovationRoundId = innovation.getInnovationRound().getId();
            this.innovationRoundName = innovation.getInnovationRound().getName();
        }

        // Convert attachments if they exist
        if (innovation.getAttachments() != null) {
            this.attachments = innovation.getAttachments().stream()
                    .map(AttachmentResponseDTO::new)
                    .toList();
        }
    }
}