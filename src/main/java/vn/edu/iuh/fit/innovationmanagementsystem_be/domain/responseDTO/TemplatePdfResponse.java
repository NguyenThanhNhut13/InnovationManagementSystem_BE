package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.DocumentTypeEnum;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TemplatePdfResponse {

    private String innovationId;
    private String templateId;
    private String originalFileName;
    private DocumentTypeEnum documentType;
    private String pdfUrl;
    private List<TemplatePdfSignerResponse> signers = new ArrayList<>();
    private Boolean isCAValid;
}
