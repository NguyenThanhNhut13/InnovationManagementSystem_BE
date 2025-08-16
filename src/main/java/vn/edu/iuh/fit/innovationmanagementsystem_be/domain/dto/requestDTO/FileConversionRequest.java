package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.dto.requestDTO;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class FileConversionRequest {
    private MultipartFile file;
    private String outputFormat; // "html" hoặc các format khác trong tương lai
}
