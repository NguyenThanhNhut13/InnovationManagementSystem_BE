package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.dto.responseDTO;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.edu.iuh.fit.innovationmanagementsystem_be.utils.JsonStringDeserializer;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegulationResponse {

    private String id;
    private String clauseNumber;
    private String title;
    @JsonDeserialize(using = JsonStringDeserializer.class)
    private Object content;
    private String innovationDecisionId;
    private String chapterId; // Có thể null nếu không thuộc chương nào
}
