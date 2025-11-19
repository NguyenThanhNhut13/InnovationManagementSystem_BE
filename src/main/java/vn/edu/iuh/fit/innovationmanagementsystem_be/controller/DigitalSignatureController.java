package vn.edu.iuh.fit.innovationmanagementsystem_be.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.VerifyDigitalSignatureRequest;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.UserDocumentSignatureStatusRequest;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.UserDocumentSignatureStatusResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.TemplatePdfResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.VerifyDigitalSignatureResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.service.DigitalSignatureService;
import vn.edu.iuh.fit.innovationmanagementsystem_be.utils.annotation.ApiMessage;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Digital Signature", description = "Digital signature checking APIs")
@SecurityRequirement(name = "Bearer Authentication")
public class DigitalSignatureController {

        private final DigitalSignatureService digitalSignatureService;

        public DigitalSignatureController(DigitalSignatureService digitalSignatureService) {
                this.digitalSignatureService = digitalSignatureService;
        }

        // 1. Kiểm tra trạng thái chữ ký của user hiện tại đối với một tài liệu
        @PostMapping("/digital-signatures/check")
        @ApiMessage("Kiểm tra trạng thái chữ ký của người dùng hiện tại thành công")
        @Operation(summary = "Check current user signature status for a document", description = "Nhận JSON request, validate quyền ký, validate certificate (nếu có) và verify chữ ký bằng public key của user hiện tại")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Trả về trạng thái ký và thời gian ký (nếu có)", content = @Content(schema = @Schema(implementation = UserDocumentSignatureStatusResponse.class))),
                        @ApiResponse(responseCode = "400", description = "Yêu cầu không hợp lệ"),
                        @ApiResponse(responseCode = "401", description = "Không được phép truy cập"),
                        @ApiResponse(responseCode = "404", description = "Không tìm thấy dữ liệu liên quan")
        })
        public ResponseEntity<UserDocumentSignatureStatusResponse> checkCurrentUserSignatureStatus(
                        @RequestBody UserDocumentSignatureStatusRequest request) {

                UserDocumentSignatureStatusResponse result = digitalSignatureService
                                .getCurrentUserDocumentSignatureStatus(
                                                request.getInnovationId(),
                                                request.getDocumentType(),
                                                request.getSignedAsRole());
                return ResponseEntity.ok(result);
        }

        // 2. Xác thực chữ ký dựa trên document hash, signature hash và user ID
        @PostMapping("/digital-signatures/verify")
        @ApiMessage("Xác thực chữ ký số thành công")
        @Operation(summary = "Verify digital signature", description = "Nhận JSON gồm document hash, signature hash và user ID, sử dụng public key lưu trong hồ sơ chữ ký số để xác thực chữ ký")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Trả về trạng thái xác thực chữ ký", content = @Content(schema = @Schema(implementation = VerifyDigitalSignatureResponse.class))),
                        @ApiResponse(responseCode = "400", description = "Yêu cầu không hợp lệ"),
                        @ApiResponse(responseCode = "401", description = "Không được phép truy cập"),
                        @ApiResponse(responseCode = "404", description = "Không tìm thấy dữ liệu liên quan")
        })
        public ResponseEntity<VerifyDigitalSignatureResponse> verifyDigitalSignature(
                        @RequestBody VerifyDigitalSignatureRequest request) {

                VerifyDigitalSignatureResponse response = digitalSignatureService.verifyDocumentSignature(
                                request.getDocumentHash(),
                                request.getSignatureHash(),
                                request.getUserId());

                return ResponseEntity.ok(response);
        }

        // 3. Lấy PDF của template theo templateId
        @GetMapping("/digital-signatures/templates/{templateId}/pdf")
        @ApiMessage("Lấy PDF template thành công")
        @Operation(summary = "Get template PDF with signer info", description = "Trả về URL PDF đã upload kèm danh sách người ký, role, thời gian ký và trạng thái xác thực chữ ký")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Trả về thông tin PDF và chữ ký", content = @Content(schema = @Schema(implementation = TemplatePdfResponse.class))),
                        @ApiResponse(responseCode = "400", description = "Yêu cầu không hợp lệ"),
                        @ApiResponse(responseCode = "401", description = "Không được phép truy cập"),
                        @ApiResponse(responseCode = "404", description = "Không tìm thấy dữ liệu liên quan")
        })
        public ResponseEntity<TemplatePdfResponse> getTemplatePdfByTemplateId(
                        @PathVariable String templateId,
                        @RequestParam String innovationId) {

                TemplatePdfResponse response = digitalSignatureService.getTemplatePdf(innovationId, templateId);
                return ResponseEntity.ok(response);
        }
}
