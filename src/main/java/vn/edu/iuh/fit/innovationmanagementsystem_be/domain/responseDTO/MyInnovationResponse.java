package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.InnovationStatusEnum;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MyInnovationResponse {

    private Integer stt;
    private String innovationName;
    private String academicYear;
    private String innovationRoundName;
    private InnovationStatusEnum status;
    private Long submissionTimeRemainingSeconds;
    private Boolean isCoAuthor;
}
