package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CoInnovationResponseDTO {

    private UUID id;
    private UUID userId;
    private String userName;
    private String fullName;
    private String email;
    private UUID innovationId;
    private String innovationName;
}