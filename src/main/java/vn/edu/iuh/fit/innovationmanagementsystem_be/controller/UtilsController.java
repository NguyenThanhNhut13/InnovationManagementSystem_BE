package vn.edu.iuh.fit.innovationmanagementsystem_be.controller;

import io.minio.StatObjectResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.FileExistsResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.FileInfoResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.FileUploadResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.MultipleFileUploadResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.exception.IdInvalidException;
import vn.edu.iuh.fit.innovationmanagementsystem_be.service.FileService;
import vn.edu.iuh.fit.innovationmanagementsystem_be.utils.RestResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.utils.annotation.ApiMessage;

import java.io.File;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/v1/utils")
@Tag(name = "Utils", description = "Utility APIs for file management and other utilities")
@SecurityRequirement(name = "Bearer Authentication")
public class UtilsController {

    private final FileService fileService;

    public UtilsController(FileService fileService) {
        this.fileService = fileService;
    }

    // 1. Upload single file to MinIO
    @PostMapping(
            value = "/upload",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    @ApiMessage("Upload file thành công")
    @Operation(summary = "Upload Single File", description = "Upload a single file to MinIO storage")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "File uploaded successfully", content = @Content(schema = @Schema(implementation = FileUploadResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid file or file too large")
    })
    public ResponseEntity<FileUploadResponse> uploadFile(
            @Parameter(description = "File to upload", required = true) @RequestParam("file") MultipartFile file)
            throws Exception {

        System.out.println("file "+ file.getOriginalFilename());

//        if (file.isEmpty()) {
//            throw new IdInvalidException("File is empty");
//        }

        if (file.getOriginalFilename() == null || file.getOriginalFilename().trim().isEmpty()) {
            throw new IdInvalidException("File name is required");
        }

        if (file.getSize() > 10 * 1024 * 1024) {
            throw new IdInvalidException("File size exceeds 10MB limit");
        }

        String fileName = fileService.uploadFile(file);

        FileUploadResponse response = new FileUploadResponse(
                fileName,
                file.getOriginalFilename(),
                file.getSize(),
                file.getContentType(),
                LocalDateTime.now());

        return ResponseEntity.ok(response);
    }

    // 2. Upload multiple files to MinIO
    @PostMapping("/upload-multiple")
    @ApiMessage("Upload nhiều file thành công")
    @Operation(summary = "Upload Multiple Files", description = "Upload multiple files to MinIO storage")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Files uploaded successfully", content = @Content(schema = @Schema(implementation = MultipleFileUploadResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid files or total size too large")
    })
    public ResponseEntity<MultipleFileUploadResponse> uploadMultipleFiles(
            @Parameter(description = "Files to upload", required = true) @RequestParam("files") List<MultipartFile> files)
            throws Exception {

        if (files == null || files.isEmpty()) {
            throw new IdInvalidException("No files provided");
        }

        for (MultipartFile file : files) {
            if (file.isEmpty()) {
                throw new IdInvalidException("One or more files are empty");
            }
        }

        for (MultipartFile file : files) {
            if (file.getSize() > 10 * 1024 * 1024) {
                throw new IdInvalidException("File '" + file.getOriginalFilename() + "' exceeds 10MB limit");
            }
        }

        long totalSize = files.stream().mapToLong(MultipartFile::getSize).sum();
        if (totalSize > 50 * 1024 * 1024) {
            throw new IdInvalidException("Total request size exceeds 50MB limit");
        }

        List<String> uploadedFileNames = fileService.uploadMultipleFiles(files);

        List<FileUploadResponse> fileResponses = new ArrayList<>();
        for (int i = 0; i < files.size(); i++) {
            FileUploadResponse fileResponse = new FileUploadResponse(
                    uploadedFileNames.get(i),
                    files.get(i).getOriginalFilename(),
                    files.get(i).getSize(),
                    files.get(i).getContentType(),
                    LocalDateTime.now());
            fileResponses.add(fileResponse);
        }

        MultipleFileUploadResponse response = new MultipleFileUploadResponse(
                uploadedFileNames.size(),
                totalSize,
                uploadedFileNames,
                fileResponses,
                LocalDateTime.now());

        return ResponseEntity.ok(response);
    }

    // 3. Download file from MinIO
    @GetMapping("/download/{fileName}")
    @ApiMessage("Download file thành công")
    @Operation(summary = "Download File", description = "Download a file from MinIO storage")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "File downloaded successfully"),
            @ApiResponse(responseCode = "404", description = "File not found")
    })
    public ResponseEntity<InputStreamResource> downloadFile(
            @Parameter(description = "File name to download", required = true) @PathVariable String fileName)
            throws Exception {

        if (!fileService.fileExists(fileName)) {
            return ResponseEntity.notFound().build();
        }

        StatObjectResponse fileInfo = fileService.getFileInfo(fileName);
        InputStream fileStream = fileService.downloadFile(fileName);

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"");
        headers.add(HttpHeaders.CONTENT_TYPE, fileInfo.contentType());
        headers.add(HttpHeaders.CONTENT_LENGTH, String.valueOf(fileInfo.size()));

        InputStreamResource resource = new InputStreamResource(fileStream);

        return ResponseEntity.ok()
                .headers(headers)
                .body(resource);
    }

    // 4. Get file info without downloading
    @GetMapping("/info/{fileName}")
    @ApiMessage("Lấy thông tin file thành công")
    public ResponseEntity<FileInfoResponse> getFileInfo(@PathVariable String fileName) throws Exception {

        if (!fileService.fileExists(fileName)) {
            return ResponseEntity.notFound().build();
        }

        StatObjectResponse fileInfo = fileService.getFileInfo(fileName);

        FileInfoResponse response = new FileInfoResponse(
                fileName,
                fileInfo.size(),
                fileInfo.contentType(),
                fileInfo.lastModified().toLocalDateTime(),
                fileInfo.etag());

        return ResponseEntity.ok(response);
    }

    // 5. Delete file from MinIO
    @DeleteMapping("/delete/{fileName}")
    @ApiMessage("Xóa file thành công")
    public ResponseEntity<Void> deleteFile(@PathVariable String fileName) throws Exception {

        if (!fileService.fileExists(fileName)) {
            return ResponseEntity.notFound().build();
        }

        fileService.deleteFile(fileName);

        return ResponseEntity.noContent().build();
    }

    // 6. Check if file exists
    @GetMapping("/exists/{fileName}")
    @ApiMessage("Kiểm tra file tồn tại thành công")
    public ResponseEntity<FileExistsResponse> checkFileExists(@PathVariable String fileName) {
        boolean exists = fileService.fileExists(fileName);

        FileExistsResponse response = new FileExistsResponse(fileName, exists);

        return ResponseEntity.ok(response);
    }

    // 7. Convert DOC/DOCX file to HTML using LibreOffice container
    @PostMapping(value = "/doc-to-html", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ApiMessage("Convert DOC/DOCX sang HTML thành công")
    public ResponseEntity<String> convertDocToHtml(@RequestParam("file") MultipartFile file) {
        File tempFile = null;
        File htmlFile = null;
        try {
            // 1. Lưu file tạm
            tempFile = File.createTempFile("upload-", "-" + file.getOriginalFilename());
            file.transferTo(tempFile);

            // 2. Thư mục output
            File outputDir = new File(System.getProperty("java.io.tmpdir"));

            // 3. Copy file vào container trước
            ProcessBuilder copyInPb = new ProcessBuilder(
                    "docker", "cp",
                    tempFile.getAbsolutePath(),
                    "libreoffice:/tmp/" + tempFile.getName());
            copyInPb.redirectErrorStream(true);

            Process copyInProcess = copyInPb.start();
            int copyInExitCode = copyInProcess.waitFor();

            if (copyInExitCode != 0) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Failed to copy file to container");
            }

            // 4. Gọi LibreOffice container để convert DOC/DOCX → HTML
            ProcessBuilder pb = new ProcessBuilder(
                    "docker", "exec", "libreoffice",
                    "libreoffice",
                    "--headless",
                    "--convert-to", "html:XHTML Writer File:UTF8",
                    "--outdir", "/tmp",
                    "/tmp/" + tempFile.getName());
            pb.redirectErrorStream(true);

            Process process = pb.start();

            // Đọc output để debug nếu cần
            try (var reader = new java.io.BufferedReader(new java.io.InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println("LibreOffice output: " + line);
                }

            }

            int exitCode = process.waitFor();
            if (exitCode != 0) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("LibreOffice conversion failed with exit code: " + exitCode);
            }

            // 5. Tìm file HTML đã sinh ra trong container
            String tempName = tempFile.getName();
            String htmlName = tempName.replaceAll("\\.[^.]+$", "") + ".html";

            // 6. Copy file từ container về host
            ProcessBuilder copyPb = new ProcessBuilder(
                    "docker", "cp",
                    "libreoffice:/tmp/" + htmlName,
                    outputDir.getAbsolutePath() + "/" + htmlName);
            copyPb.redirectErrorStream(true);

            Process copyProcess = copyPb.start();
            int copyExitCode = copyProcess.waitFor();

            if (copyExitCode != 0) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Failed to copy converted file from container");
            }

            htmlFile = new File(outputDir, htmlName);

            if (!htmlFile.exists()) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Convert thất bại! Không tìm thấy file: " + htmlFile.getAbsolutePath());
            }

            // 7. Đọc nội dung HTML
            String html = new String(Files.readAllBytes(htmlFile.toPath()), StandardCharsets.UTF_8);

            // 8. Trả về nội dung HTML
            return ResponseEntity.ok()
                    .contentType(MediaType.TEXT_HTML)
                    .body(html);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi khi convert: " + e.getMessage());
        } finally {
            // 9. Dọn dẹp file tạm
            if (tempFile != null && tempFile.exists()) {
                tempFile.delete();
            }
            if (htmlFile != null && htmlFile.exists()) {
                htmlFile.delete();
            }

            // 10. Dọn dẹp file trong container
            try {
                String tempName = tempFile != null ? tempFile.getName() : "";
                String htmlName = tempName.replaceAll("\\.[^.]+$", "") + ".html";

                ProcessBuilder cleanupPb = new ProcessBuilder(
                        "docker", "exec", "libreoffice",
                        "rm", "-f", "/tmp/" + tempName, "/tmp/" + htmlName);
                cleanupPb.start();
            } catch (Exception e) {
                System.err.println("Failed to cleanup container files: " + e.getMessage());
            }
        }
    }

    // 8. Ping endpoint for Uptime
    @GetMapping("/ping")
    @ApiMessage("pong")
    @Operation(summary = "Ping", description = "Endpoint rất nhẹ để kiểm tra sức khỏe service và tránh sleep")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Service online")
    })
    public ResponseEntity<java.util.Map<String, Object>> ping() {
        var body = new java.util.HashMap<String, Object>();
        body.put("status", "ok");
        body.put("service", "innovation-management-system");
        body.put("timestamp", java.time.Instant.now().toString());
        return ResponseEntity.ok(body);
    }

    @GetMapping("/view/{fileName}")
    public ResponseEntity<RestResponse<String>> getIframeUrl(@PathVariable String fileName) {
        try {
            String url = fileService.getPresignedUrl(fileName, 60 * 5);
            RestResponse<String> response = RestResponse.<String>builder()
                    .statusCode(200)
                    .error(null)
                    .message("Success")
                    .data(url)
                    .build();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            RestResponse<String> errorResponse = RestResponse.<String>builder()
                    .statusCode(500)
                    .error("Internal Server Error")
                    .message(e.getMessage())
                    .data(null)
                    .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(errorResponse);
        }
    }

}