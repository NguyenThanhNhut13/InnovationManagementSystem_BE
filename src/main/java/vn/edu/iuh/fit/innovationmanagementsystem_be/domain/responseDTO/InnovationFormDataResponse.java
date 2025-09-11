package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InnovationFormDataResponse {

    private InnovationResponse innovation;

    private List<FormDataResponse> formDataList;

    // Thêm documentHash để client có thể tạo chữ ký
    private String documentHash;

}
