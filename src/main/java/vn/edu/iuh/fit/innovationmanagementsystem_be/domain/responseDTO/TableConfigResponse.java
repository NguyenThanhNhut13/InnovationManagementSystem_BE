package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TableConfigResponse {

    private String id;
    private List<TableColumnResponse> columns;
    private Integer minRows;
    private Integer maxRows;
    private Boolean allowAddRows;
    private Boolean allowDeleteRows;
}
