package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.ArrayList;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InnovationFormDataResponse {

    // private List<FormDataResponse> formDataList;

    private List<TemplateFormDataResponse> templates = new ArrayList<>();

    private List<TemplateSignatureResponse> templateSignatures = new ArrayList<>();

    // Số giây đã trễ từ deadline (dương nếu đã quá deadline, 0 nếu chưa quá
    // deadline, null nếu không có deadline)
    private Long submissionTimeRemainingSeconds;

}
