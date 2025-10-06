package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TableConfigRequest {

    @NotNull(message = "Columns không được để trống")
    @Valid
    private List<TableColumnRequest> columns;

    private Integer minRows = 1;

    private Integer maxRows = 100;

    private Boolean allowAddRows = true;

    private Boolean allowDeleteRows = true;
}
