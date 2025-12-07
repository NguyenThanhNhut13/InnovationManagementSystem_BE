package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SignExistingReportRequest {

    // Optional: Nếu có thì update reportData
    private Map<String, Object> reportData;

    // Optional: Nếu có thì tạo PDF mới từ HTML này
    private String htmlContentBase64;
}
