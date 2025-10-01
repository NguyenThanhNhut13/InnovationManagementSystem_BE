/*
 * @ (#) UploadFileResponse.java       1.0     30/09/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO;


import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UploadFileResponse {
    private String publicId;
    private String imageUrl;
}
