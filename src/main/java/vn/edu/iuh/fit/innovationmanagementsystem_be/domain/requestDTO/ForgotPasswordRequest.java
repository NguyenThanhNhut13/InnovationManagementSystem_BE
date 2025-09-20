/*
 * @ (#) ForgotPasswordRequest.java       1.0     20/09/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/*
 * @description:
 * @author: Nguyen Thanh Nhut
 * @date: 20/09/2025
 * @version:    1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ForgotPasswordRequest {
    @NotBlank(message = "Mã nhân sự không được để trống")
    private String personnelId;
}
