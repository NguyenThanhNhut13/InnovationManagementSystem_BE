package vn.edu.iuh.fit.innovationmanagementsystem_be.controller;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/convert")
public class DocToHtmlController {

    @PostMapping(value = "/doc-to-html", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> convertDocToHtml(@RequestParam("file") MultipartFile file) {
        File tempFile = null;
        File htmlFile = null;
        try {
            // 1. Lưu file tạm
            tempFile = File.createTempFile("upload-", "-" + file.getOriginalFilename());
            file.transferTo(tempFile);

            // 2. Thư mục output
            File outputDir = new File(System.getProperty("java.io.tmpdir"));

            // 3. Gọi LibreOffice để convert DOC/DOCX → HTML
            ProcessBuilder pb = new ProcessBuilder(
                    "C:\\Program Files\\LibreOffice\\program\\soffice.exe",
                    "--headless",
                    // "--convert-to", "html:HTML (StarWriter)",
                    "--convert-to", "html:XHTML Writer File:UTF8",
                    "--outdir", outputDir.getAbsolutePath(),
                    tempFile.getAbsolutePath());
            pb.redirectErrorStream(true);

            Process process = pb.start();

            // Log stdout/stderr của LibreOffice
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println("[soffice] " + line);
                }
            }

            int exitCode = process.waitFor();
            System.out.println("LibreOffice exit code = " + exitCode);

            // 4. Tìm file HTML đã sinh ra
            String tempName = tempFile.getName();
            String htmlName = tempName.replaceAll("\\.[^.]+$", "") + ".html";
            htmlFile = new File(outputDir, htmlName);

            if (!htmlFile.exists()) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Convert thất bại! Không tìm thấy file: " + htmlFile.getAbsolutePath());
            }

            // 5. Đọc nội dung HTML
            String html = new String(Files.readAllBytes(htmlFile.toPath()), StandardCharsets.UTF_8);

            // 6. Trả về nội dung HTML
            return ResponseEntity.ok()
                    .contentType(MediaType.TEXT_HTML)
                    .body(html);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi khi convert: " + e.getMessage());
        } finally {
            // 7. Dọn dẹp file tạm
            if (tempFile != null && tempFile.exists()) {
                boolean deleted = tempFile.delete();
                System.out.println("Deleted tempFile: " + deleted);
            }
            if (htmlFile != null && htmlFile.exists()) {
                boolean deleted = htmlFile.delete();
                System.out.println("Deleted htmlFile: " + deleted);
            }
        }
    }

}
