/*
 * @ (#) FileController.java       1.0     30/09/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.innovationmanagementsystem_be.controller;
/*
 * @description:
 * @author: Nguyen Thanh Nhut
 * @date: 30/09/2025
 * @version:    1.0
 */

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.UploadFileResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.service.CloudinaryService;
import vn.edu.iuh.fit.innovationmanagementsystem_be.utils.annotation.ApiMessage;


@RestController
@RequestMapping("/files")
public class FileController {
    private final CloudinaryService cloudinaryService;

    public FileController(CloudinaryService cloudinaryService) {
        this.cloudinaryService = cloudinaryService;
    }

    @ApiMessage("Upload file thành công")
    @Operation(summary = "Upload file", description = "Upload file")
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Upload file successful"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "400", description = "Invalid request data")
    })
    @PostMapping("/upload")
    public ResponseEntity<UploadFileResponse> uploadImage(@RequestParam("file") MultipartFile file) {
        UploadFileResponse response = cloudinaryService.uploadFile(file);
        return ResponseEntity.ok(response);
    }

    @ApiMessage("Xóa file thành công")
    @Operation(summary = "Delete file", description = "Delete file")
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Delete file successful"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "400", description = "Invalid request data")
    })
    @DeleteMapping("/{publicId}")
    public ResponseEntity<Void> deleteImage(@PathVariable String publicId) {
        cloudinaryService.deleteFile(publicId);
        return ResponseEntity.ok().build();
    }


}

