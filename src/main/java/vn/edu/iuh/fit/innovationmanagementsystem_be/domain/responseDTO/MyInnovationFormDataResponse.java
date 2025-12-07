package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO;

import java.util.List;
import java.util.ArrayList;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MyInnovationFormDataResponse {

    private InnovationBasicInfo innovation;
    
    private List<MyTemplateFormDataResponse> templates = new ArrayList<>();

    private List<FormSignatureResponse> templateSignatures = new ArrayList<>();

    private Long submissionTimeRemainingSeconds;

}

