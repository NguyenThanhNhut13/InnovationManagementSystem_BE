package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.InnovationStatusEnum;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MyInnovationResponse {

    private String innovationId;
    private String innovationName;
    private String authorName;
    private String academicYear;
    private String innovationRoundName;
    private InnovationStatusEnum status;
    private Long submissionTimeRemainingSeconds;
    private Boolean isCoAuthor;
    private Boolean isScore;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
