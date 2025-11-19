package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.InnovationStatusEnum;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FilterMyInnovationRequest {
    private String searchText;
    private InnovationStatusEnum status;
    private String innovationRoundId;
    private Boolean isScore;
}
