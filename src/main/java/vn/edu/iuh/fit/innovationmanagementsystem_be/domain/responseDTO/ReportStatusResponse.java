package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.ReportStatusEnum;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReportStatusResponse {
    private ReportStatusEnum status; // DRAFT, SUBMITTED_TO_DEPARTMENT, SUBMITTED_TO_SCHOOL
    private boolean isSigned; // Đã có DigitalSignature với status SIGNED
}

