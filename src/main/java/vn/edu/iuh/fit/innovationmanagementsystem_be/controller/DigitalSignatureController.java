package vn.edu.iuh.fit.innovationmanagementsystem_be.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.DocumentTypeEnum;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.UserRoleEnum;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.DepartmentDocumentSignRequest;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.SignExistingReportRequest;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.VerifyDigitalSignatureRequest;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.DepartmentDocumentSignResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.UserDocumentSignatureStatusResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.TemplatePdfResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.VerifyDigitalSignatureResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.BatchSignInnovationsRequest;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.BatchSignInnovationsResponse;
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
        @GetMapping("/digital-signatures/check")
        @ApiMessage("Kiểm tra trạng thái chữ ký của người dùng hiện tại thành công")
        @Operation(summary = "Check current user signature status for a document", description = "Nhận query parameters, validate quyền ký, validate certificate (nếu có) và verify chữ ký bằng public key của user hiện tại")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Trả về trạng thái ký và thời gian ký (nếu có)", content = @Content(schema = @Schema(implementation = UserDocumentSignatureStatusResponse.class))),
                        @ApiResponse(responseCode = "400", description = "Yêu cầu không hợp lệ"),
                        @ApiResponse(responseCode = "401", description = "Không được phép truy cập"),
                        @ApiResponse(responseCode = "404", description = "Không tìm thấy dữ liệu liên quan")
        })
        public ResponseEntity<UserDocumentSignatureStatusResponse> checkCurrentUserSignatureStatus(
                        @RequestParam String innovationId,
                        @RequestParam String documentType,
                        @RequestParam String signedAsRole) {

                DocumentTypeEnum documentTypeEnum = DocumentTypeEnum.valueOf(documentType);
                UserRoleEnum signedAsRoleEnum = UserRoleEnum.valueOf(signedAsRole);

                UserDocumentSignatureStatusResponse result = digitalSignatureService
                                .getCurrentUserDocumentSignatureStatus(
                                                innovationId,
                                                documentTypeEnum,
                                                signedAsRoleEnum);
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

        // 4. Ký số tài liệu tổng hợp cấp khoa (Mẫu 3, Mẫu 4, Mẫu 5)
        @PostMapping("/department-documents/sign")
        @PreAuthorize("hasAnyRole('TRUONG_KHOA', 'TV_HOI_DONG_TRUONG', 'TV_HOI_DONG_KHOA')")
        @ApiMessage("Xử lý tài liệu cấp khoa thành công")
        @Operation(summary = "Sign department summary document", description = "Ký số tài liệu tổng hợp cấp khoa (Mẫu 4 - Tổng hợp đề nghị, Mẫu 5 - Tổng hợp chấm điểm). Yêu cầu role TRUONG_KHOA của phòng ban tương ứng.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Trả về thông tin chữ ký và URL PDF", content = @Content(schema = @Schema(implementation = DepartmentDocumentSignResponse.class))),
                        @ApiResponse(responseCode = "400", description = "Document type không hợp lệ hoặc thiếu dữ liệu"),
                        @ApiResponse(responseCode = "401", description = "Không được phép truy cập"),
                        @ApiResponse(responseCode = "403", description = "Không có quyền ký (không phải trưởng khoa của phòng ban)"),
                        @ApiResponse(responseCode = "404", description = "Không tìm thấy phòng ban")
        })
        public ResponseEntity<DepartmentDocumentSignResponse> signDepartmentDocument(
                        @Valid @RequestBody DepartmentDocumentSignRequest request) {

                DepartmentDocumentSignResponse response = digitalSignatureService.signDepartmentDocument(request);
                return ResponseEntity.ok(response);
        }

        // 5. Ký số Report đã tồn tại (sau khi lưu với isSign = false)
        // - lúc đầu tạo report nhưng chưa ký
        @PostMapping("/department-documents/{reportId}/sign")
        @PreAuthorize("hasAnyRole('TRUONG_KHOA', 'TV_HOI_DONG_TRUONG', 'TV_HOI_DONG_KHOA')")
        @ApiMessage("Ký số báo cáo thành công")
        @Operation(summary = "Sign existing report", description = "Ký số Report đã lưu trước đó (khi tạo với isSign = false)")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Ký số thành công", content = @Content(schema = @Schema(implementation = DepartmentDocumentSignResponse.class))),
                        @ApiResponse(responseCode = "400", description = "Không thể ký"),
                        @ApiResponse(responseCode = "403", description = "Không có quyền ký"),
                        @ApiResponse(responseCode = "404", description = "Không tìm thấy Report")
        })
        public ResponseEntity<DepartmentDocumentSignResponse> signExistingReport(
                        @PathVariable String reportId,
                        @RequestBody(required = false) SignExistingReportRequest request) {

                DepartmentDocumentSignResponse response = digitalSignatureService.signExistingReport(reportId, request);
                return ResponseEntity.ok(response);
        }

        // 6. Ký nhiều sáng kiến cùng lúc (cho TRUONG_KHOA)
        @PostMapping("/digital-signatures/batch-sign-innovations")
        @PreAuthorize("hasRole('TRUONG_KHOA')")
        @ApiMessage("Ký số nhiều sáng kiến thành công")
        @Operation(summary = "Batch sign innovations as department head", description = "Cho phép TRUONG_KHOA ký nhiều sáng kiến cùng lúc trên mẫu 2 (Báo cáo mô tả). Chỉ xử lý sáng kiến ở trạng thái SUBMITTED và đã được tác giả ký.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Trả về kết quả ký cho từng sáng kiến", content = @Content(schema = @Schema(implementation = BatchSignInnovationsResponse.class))),
                        @ApiResponse(responseCode = "400", description = "Danh sách sáng kiến trống"),
                        @ApiResponse(responseCode = "401", description = "Không được phép truy cập"),
                        @ApiResponse(responseCode = "403", description = "Không có quyền ký (không phải TRUONG_KHOA)")
        })
        public ResponseEntity<BatchSignInnovationsResponse> batchSignInnovations(
                        @Valid @RequestBody BatchSignInnovationsRequest request) {

                BatchSignInnovationsResponse response = digitalSignatureService
                                .batchSignInnovationsAsDepartmentHead(request);
                return ResponseEntity.ok(response);
        }
}
