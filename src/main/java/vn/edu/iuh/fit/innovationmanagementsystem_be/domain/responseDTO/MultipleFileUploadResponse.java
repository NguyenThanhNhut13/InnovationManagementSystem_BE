package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MultipleFileUploadResponse {
    private int uploadedCount;
    private long totalSize;
    private List<String> fileNames;
    private List<FileUploadResponse> files;
    private LocalDateTime uploadedAt;
}
