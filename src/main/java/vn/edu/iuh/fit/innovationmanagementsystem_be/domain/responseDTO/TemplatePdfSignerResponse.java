package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.UserRoleEnum;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TemplatePdfSignerResponse {

    private String signerId;
    private String signerFullName;
    private String signerPersonnelId;
    private UserRoleEnum signedAsRole;
    private LocalDateTime signAt;
    private boolean verified; // Chữ ký số có khớp với tài liệu gốc không (document integrity)
    private boolean isCertificateValid; // Chứng thư số của người ký có còn hiệu lực không
}
