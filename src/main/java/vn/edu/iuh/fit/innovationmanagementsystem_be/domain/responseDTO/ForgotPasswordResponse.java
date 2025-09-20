/*
 * @ (#) ForgotPasswordResponse.java       1.0     20/09/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO;
/*
 * @description:
 * @author: Nguyen Thanh Nhut
 * @date: 20/09/2025
 * @version:    1.0
 */

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ForgotPasswordResponse {
    private String message;
    private String email;
    private Long expiresIn;
}
