package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO;

import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.AttachmentTypeEnum;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.DocumentTypeEnum;

@Data
public class UpdateAttachmentRequest {

    private String pathUrl;

    private AttachmentTypeEnum type;

    private String fileName;

    @PositiveOrZero(message = "Dung lượng tệp phải lớn hơn hoặc bằng 0")
    private Long fileSize;

    private String templateId;

    private DocumentTypeEnum documentType;
}
