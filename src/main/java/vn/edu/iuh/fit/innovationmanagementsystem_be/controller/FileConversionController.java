package vn.edu.iuh.fit.innovationmanagementsystem_be.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.dto.responseDTO.FileConversionResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.service.FileConversionService;
import vn.edu.iuh.fit.innovationmanagementsystem_be.utils.annotation.ApiMessage;

@RestController
@RequestMapping("/api/v1/file-conversion")
public class FileConversionController {

    private final FileConversionService fileConversionService;

    public FileConversionController(FileConversionService fileConversionService) {
        this.fileConversionService = fileConversionService;
    }

    @PostMapping(value = "/docx-to-html", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ApiMessage("Chuyển đổi file thành công")
    public ResponseEntity<FileConversionResponse> convertDocxToHtml(
            @RequestParam("file") MultipartFile file) {

        try {
            FileConversionResponse response = fileConversionService.convertDocxToHtml(file);

            if ("SUCCESS".equals(response.getConversionStatus())) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.badRequest().body(response);
            }

        } catch (Exception e) {
            FileConversionResponse errorResponse = FileConversionResponse.builder()
                    .htmlContent(null)
                    .originalFileName(file.getOriginalFilename())
                    .fileSize("0 B")
                    .conversionStatus("ERROR")
                    .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}
