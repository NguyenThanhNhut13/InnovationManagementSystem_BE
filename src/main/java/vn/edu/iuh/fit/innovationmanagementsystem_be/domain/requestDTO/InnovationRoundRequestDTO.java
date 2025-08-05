package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.InnovationRound;

import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InnovationRoundRequestDTO {

    private String name;

    private LocalDate startDate;

    private LocalDate endDate;

    private InnovationRound.InnovationRoundStatus status;

    private UUID formTemplateId;

    private String createdBy;

    private String updatedBy;

    // Validation annotations
    @jakarta.validation.constraints.NotBlank(message = "Round name is required")
    @jakarta.validation.constraints.Size(min = 3, max = 255, message = "Round name must be between 3 and 255 characters")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @jakarta.validation.constraints.NotNull(message = "Start date is required")
    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    @jakarta.validation.constraints.NotNull(message = "End date is required")
    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }
}