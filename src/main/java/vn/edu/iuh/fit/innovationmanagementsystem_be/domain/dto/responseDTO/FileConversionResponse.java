package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.dto.responseDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileConversionResponse {
    private String htmlContent;
    private String originalFileName;
    private String fileSize;
    private String conversionStatus;
}
