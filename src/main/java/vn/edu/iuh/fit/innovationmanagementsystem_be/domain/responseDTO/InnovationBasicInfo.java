package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InnovationBasicInfo {
    private String id;
    private String innovationName;
    private String status;
    private Boolean isScore;
    private String basisText;
    private Long submissionTimeRemainingSeconds;
}

