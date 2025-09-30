/*
 * @ (#) CloudinaryServiceImpl.java       1.0     30/09/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.innovationmanagementsystem_be.service.impl;
/*
 * @description:
 * @author: Nguyen Thanh Nhut
 * @date: 30/09/2025
 * @version:    1.0
 */

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.UploadFileResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.exception.NotFoundException;
import vn.edu.iuh.fit.innovationmanagementsystem_be.service.CloudinaryService;

import java.io.IOException;
import java.util.*;

@Service
public class CloudinaryServiceImpl implements CloudinaryService {

    private final Cloudinary cloudinary;

    public CloudinaryServiceImpl(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    @Override
    public UploadFileResponse uploadFile(MultipartFile file) {
        try {
            Map<?, ?> uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.emptyMap());
            String publicId = (String) uploadResult.get("public_id");
            String imageUrl = (String) uploadResult.get("secure_url");

            return UploadFileResponse.builder()
                    .publicId(publicId)
                    .imageUrl(imageUrl)
                    .build();
        } catch (Exception e) {
            throw new RuntimeException("Image upload failed", e);
        }
    }

    @Override
    public void deleteFile(String publicId) {
        if (publicId == null || publicId.isEmpty()) {
            throw new NotFoundException("Not found public id!");
        }
        try {
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
        } catch (IOException e) {
            throw new RuntimeException("Failed to delete image", e);
        }
    }

}
