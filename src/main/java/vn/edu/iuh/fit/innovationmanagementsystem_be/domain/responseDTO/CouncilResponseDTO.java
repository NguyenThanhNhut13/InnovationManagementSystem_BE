package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.Council;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.ReviewLevelEnum;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CouncilResponseDTO {

    private UUID id;

    private String name;

    private ReviewLevelEnum reviewCouncilLevel;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private List<CouncilMemberResponseDTO> councilMembers;

    // Constructor to convert from Council entity
    public CouncilResponseDTO(Council council) {
        this.id = council.getId();
        this.name = council.getName();
        this.reviewCouncilLevel = council.getReviewCouncilLevel();
        this.createdAt = council.getCreatedAt();
        this.updatedAt = council.getUpdatedAt();

        // Convert council members if they exist
        if (council.getCouncilMembers() != null) {
            this.councilMembers = council.getCouncilMembers().stream()
                    .map(CouncilMemberResponseDTO::new)
                    .toList();
        }
    }
}