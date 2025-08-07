package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.CouncilMember;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CouncilMemberResponseDTO {

    private UUID id;

    private UUID councilId;
    private String councilName;
    private String reviewCouncilLevel;

    private UUID userId;
    private String userName;
    private String userFullName;
    private String userEmail;

    // Constructor to convert from CouncilMember entity
    public CouncilMemberResponseDTO(CouncilMember councilMember) {
        this.id = councilMember.getId();

        // Set council information if available
        if (councilMember.getCouncil() != null) {
            this.councilId = councilMember.getCouncil().getId();
            this.councilName = councilMember.getCouncil().getName();
            this.reviewCouncilLevel = councilMember.getCouncil().getReviewCouncilLevel().name();
        }

        // Set user information if available
        if (councilMember.getUser() != null) {
            this.userId = councilMember.getUser().getId();
            this.userName = councilMember.getUser().getUserName();
            this.userFullName = councilMember.getUser().getFullName();
            this.userEmail = councilMember.getUser().getEmail();
        }
    }
}