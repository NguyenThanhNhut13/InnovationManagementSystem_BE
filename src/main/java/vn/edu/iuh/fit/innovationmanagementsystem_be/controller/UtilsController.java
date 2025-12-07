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
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Value;
import jakarta.validation.Valid;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.Base64DecodeRequest;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.Base64EncodeRequest;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.Base64EncodeResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.FileExistsResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.FileInfoResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.FileUploadResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.MultipleFileUploadResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.exception.IdInvalidException;
import vn.edu.iuh.fit.innovationmanagementsystem_be.service.Base64Service;
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
@RequestMapping("/api/v1")
@Tag(name = "Utils", description = "Utility APIs for file management and other utilities")
@SecurityRequirement(name = "Bearer Authentication")
public class UtilsController {

    private final FileService fileService;
    private final Base64Service base64Service;

    @Value("${CONVERTAPI_TOKEN:}")
    private String convertApiToken;

    public UtilsController(FileService fileService, Base64Service base64Service) {
        this.fileService = fileService;
        this.base64Service = base64Service;
    }

    // 1. Upload một file to MinIO
    @PostMapping(value = "/utils/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ApiMessage("Upload file thành công")
    @Operation(summary = "Upload Single File", description = "Upload a single file to MinIO storage")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "File uploaded successfully", content = @Content(schema = @Schema(implementation = FileUploadResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid file or file too large")
    })
    public ResponseEntity<FileUploadResponse> uploadFile(
            @Parameter(description = "File to upload", required = true) @RequestParam("file") MultipartFile file)
            throws Exception {

        System.out.println("file " + file.getOriginalFilename());

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

    // 2. Upload nhiều file to MinIO
    @PostMapping("/utils/upload-multiple")
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

    // 3. Download file từ MinIO
    @GetMapping("/utils/download/{fileName}")
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

    // 4. Lấy thông tin file & không download
    @GetMapping("/utils/info/{fileName}")
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

    // 5. Xóa file từ MinIO
    @DeleteMapping("/utils/delete/{fileName}")
    @ApiMessage("Xóa file thành công")
    public ResponseEntity<Void> deleteFile(@PathVariable String fileName) throws Exception {

        if (!fileService.fileExists(fileName)) {
            return ResponseEntity.notFound().build();
        }

        fileService.deleteFile(fileName);

        return ResponseEntity.noContent().build();
    }

    // 6. Check if file exists
    @GetMapping("/utils/exists/{fileName}")
    @ApiMessage("Kiểm tra file tồn tại thành công")
    public ResponseEntity<FileExistsResponse> checkFileExists(@PathVariable String fileName) {
        boolean exists = fileService.fileExists(fileName);

        FileExistsResponse response = new FileExistsResponse(fileName, exists);

        return ResponseEntity.ok(response);
    }

    // 7. Convert DOC/DOCX file to HTML using LibreOffice container
    @PostMapping(value = "/utils/doc-to-html", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ApiMessage("Convert DOC/DOCX sang HTML thành công")
    public ResponseEntity<String> convertDocToHtml(@RequestParam("file") MultipartFile file) {
        File tempFile = null;
        File htmlFile = null;
        try {
            // Lưu file tạm
            tempFile = File.createTempFile("upload-", "-" + file.getOriginalFilename());
            file.transferTo(tempFile);

            // Thư mục output
            File outputDir = new File(System.getProperty("java.io.tmpdir"));

            // Copy file vào container
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

            // Gọi LibreOffice container để convert DOC/DOCX → HTML
            ProcessBuilder pb = new ProcessBuilder(
                    "docker", "exec", "libreoffice",
                    "libreoffice",
                    "--headless",
                    "--convert-to", "html:XHTML Writer File:UTF8",
                    "--outdir", "/tmp",
                    "/tmp/" + tempFile.getName());
            pb.redirectErrorStream(true);

            Process process = pb.start();

            // Đọc output & debug
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

            // Tìm file HTML đã sinh ra trong container
            String tempName = tempFile.getName();
            String htmlName = tempName.replaceAll("\\.[^.]+$", "") + ".html";

            // Copy file từ container về host
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

            // Đọc nội dung HTML
            String html = new String(Files.readAllBytes(htmlFile.toPath()), StandardCharsets.UTF_8);

            return ResponseEntity.ok()
                    .contentType(MediaType.TEXT_HTML)
                    .body(html);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi khi convert: " + e.getMessage());
        } finally {
            // Dọn dẹp file tạm
            if (tempFile != null && tempFile.exists()) {
                tempFile.delete();
            }
            if (htmlFile != null && htmlFile.exists()) {
                htmlFile.delete();
            }

            // Dọn dẹp file trong container
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
    @GetMapping("/utils/ping")
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

    @GetMapping("/utils/view/{fileName}")
    public ResponseEntity<RestResponse<String>> getIframeUrl(@PathVariable String fileName) {
        try {
            String url = fileService.getPresignedUrl(fileName, 60 * 5);
            RestResponse<String> response = RestResponse.<String>builder()
                    .statusCode(200)
                    .message("Success")
                    .data(url)
                    .build();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            RestResponse<String> errorResponse = RestResponse.<String>builder()
                    .statusCode(500)
                    .message("Internal Server Error: " + e.getMessage())
                    .data(null)
                    .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(errorResponse);
        }
    }

    // 9. ConvertAPI: DOC/DOCX -> HTML
    @PostMapping(value = "/utils/convert-word-to-html", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Convert Word to HTML via ConvertAPI", description = "Proxy qua backend để ẩn token. Trả về base64 'FileData' từ ConvertAPI.")
    public ResponseEntity<?> convertWordToHtmlViaThirdParty(
            @RequestParam("file") MultipartFile file) {
        File tempFile = null;
        try {
            if (file == null || file.isEmpty()) {
                throw new IdInvalidException("Vui lòng chọn file DOC/DOCX");
            }

            if (convertApiToken == null || convertApiToken.isBlank()) {
                throw new IdInvalidException("Thiếu CONVERTAPI_TOKEN trong môi trường/.env");
            }

            // Lưu file tạm để gửi multipart qua RestTemplate
            tempFile = File.createTempFile("convert-", "-" + file.getOriginalFilename());
            file.transferTo(tempFile);

            // Xác định URL dựa trên extension của file
            String originalFilename = file.getOriginalFilename();
            String fileExtension = getFileExtension(originalFilename);
            String url;
            
            if (fileExtension != null) {
                String ext = fileExtension.toLowerCase();
                if (ext.equals(".docx")) {
                    url = "https://v2.convertapi.com/convert/docx/to/html";
                } else if (ext.equals(".doc")) {
                    url = "https://v2.convertapi.com/convert/doc/to/html";
                } else {
                    throw new IdInvalidException("File không phải định dạng DOC hoặc DOCX. Định dạng hiện tại: " + fileExtension);
                }
            } else {
                throw new IdInvalidException("Không thể xác định định dạng file từ tên file: " + originalFilename);
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            headers.setBearerAuth(convertApiToken);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", new FileSystemResource(tempFile));

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            // Tăng timeout upload file lớn
            SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
            requestFactory.setConnectTimeout(30_000);
            requestFactory.setReadTimeout(120_000);

            RestTemplate restTemplate = new RestTemplate(requestFactory);
            ResponseEntity<String> thirdPartyResp = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    requestEntity,
                    String.class);

            return ResponseEntity.status(thirdPartyResp.getStatusCode())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(thirdPartyResp.getBody());

        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(java.util.Map.of(
                            "statusCode", 500,
                            "message", "Lỗi proxy ConvertAPI: " + ex.getMessage()));
        } finally {
            if (tempFile != null && tempFile.exists()) {
                tempFile.delete();
            }
        }
    }

    // 10. Encode text to Base64
    @PostMapping("/utils/base64/encode")
    @ApiMessage("Encode text to Base64 thành công")
    @Operation(summary = "Encode text to Base64", description = "Nhận plain text và trả về chuỗi Base64 để test.")
    public ResponseEntity<Base64EncodeResponse> encodeBase64(
            @Valid @RequestBody Base64EncodeRequest request) {

        String encoded = base64Service.encode(request.getPlainText());
        Base64EncodeResponse response = new Base64EncodeResponse(encoded);
        return ResponseEntity.ok(response);
    }

    // 11. Decode Base64 to text
    @PostMapping("/utils/base64/decode")
    @ApiMessage("Decode Base64 thành text thành công")
    @Operation(summary = "Decode Base64 to HTML", description = "Nhận chuỗi Base64 và trả về nội dung HTML để hiển thị.")
    public ResponseEntity<String> decodeBase64(
            @Valid @RequestBody Base64DecodeRequest request) {

        String decoded = base64Service.decode(request.getBase64Text());
        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_HTML)
                .body(decoded);
    }

    // 12. Test MinIO connection
    @GetMapping("/utils/test-minio")
    @ApiMessage("Kiểm tra kết nối MinIO thành công")
    @Operation(summary = "Test MinIO Connection", description = "Kiểm tra kết nối MinIO và hiển thị thông tin bucket")
    public ResponseEntity<RestResponse<String>> testMinioConnection() {
        try {
            fileService.testMinioConnection();
            RestResponse<String> response = RestResponse.<String>builder()
                    .statusCode(200)
                    .message("Kết nối MinIO thành công. Xem log để biết chi tiết.")
                    .data("MinIO connection OK")
                    .build();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            RestResponse<String> errorResponse = RestResponse.<String>builder()
                    .statusCode(500)
                    .message("Lỗi kết nối MinIO: " + e.getMessage())
                    .data(null)
                    .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(errorResponse);
        }
    }

    /**
     * Get file extension from filename
     */
    private String getFileExtension(String filename) {
        if (filename == null || filename.isEmpty()) {
            return null;
        }
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex == -1 || lastDotIndex == filename.length() - 1) {
            return null;
        }
        return filename.substring(lastDotIndex);
    }

}