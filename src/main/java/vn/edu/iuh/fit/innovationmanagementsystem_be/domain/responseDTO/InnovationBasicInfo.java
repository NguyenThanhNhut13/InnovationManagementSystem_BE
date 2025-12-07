package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

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
    private String authorName;
    private LocalDateTime updatedAt;
    private Boolean isCoAuthor;
}

