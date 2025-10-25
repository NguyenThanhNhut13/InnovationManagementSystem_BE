package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.certificateDTO;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * DTO cho response tạo CSR
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CSRResponseDTO {
    private String csrId;
    private String status;
    private String message;
    private String estimatedProcessingTime;
}
