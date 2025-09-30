/*
 * @ (#) CloudinaryService.java       1.0     30/09/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.innovationmanagementsystem_be.service;
/*
 * @description:
 * @author: Nguyen Thanh Nhut
 * @date: 30/09/2025
 * @version:    1.0
 */

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.UploadFileResponse;

@Service
public interface CloudinaryService {
    UploadFileResponse uploadFile(MultipartFile file);
    void deleteFile(String publicId);
}
