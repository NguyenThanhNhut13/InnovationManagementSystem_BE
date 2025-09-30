/*
 * @ (#) CreateInnovationRoundRequest.java       1.0     30/09/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO;
/*
 * @description:
 * @author: Nguyen Thanh Nhut
 * @date: 30/09/2025
 * @version:    1.0
 */

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateInnovationRoundRequest {

    @NotBlank(message ="Tên đợt không được để trống")
    private String name;

    @NotBlank(message ="Ngày bắt đầu không được để trống")
    private String startDate;

    @NotBlank(message ="Ngày kết thúc không được để trống")
    private String endDate;

    @NotBlank(message ="Ngày bắt đầu không được để trống")
    private String status;

    private String description;

    @NotBlank(message = "Năm học không được để trống")
    private String academicYear;

    private InnovationDecisionRequest innovationDecision;
}
