package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.AttachmentTypeEnum;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.DocumentTypeEnum;

@Data
public class AttachmentRequest {

    @NotBlank(message = "Đường dẫn tệp không được để trống")
    private String pathUrl;

    @NotNull(message = "Loại tệp đính kèm không được để trống")
    private AttachmentTypeEnum type;

    @NotBlank(message = "Tên tệp không được để trống")
    private String fileName;

    @NotNull(message = "Dung lượng tệp không được để trống")
    @PositiveOrZero(message = "Dung lượng tệp phải lớn hơn hoặc bằng 0")
    private Long fileSize;

    private String templateId;

    private DocumentTypeEnum documentType;

    @NotBlank(message = "Innovation ID không được để trống")
    private String innovationId;
}
