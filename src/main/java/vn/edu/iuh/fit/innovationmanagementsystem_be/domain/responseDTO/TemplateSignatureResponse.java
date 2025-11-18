package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.DocumentTypeEnum;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.TemplateTypeEnum;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TemplateSignatureResponse {

    private String templateId;

    private TemplateTypeEnum templateType;

    private DocumentTypeEnum documentType;

    private String documentHash;

    private String signatureHash;
}
