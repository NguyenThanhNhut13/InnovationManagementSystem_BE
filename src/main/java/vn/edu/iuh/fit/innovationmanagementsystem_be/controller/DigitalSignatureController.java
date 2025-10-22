// package vn.edu.iuh.fit.innovationmanagementsystem_be.controller;

// import io.swagger.v3.oas.annotations.Operation;
// import io.swagger.v3.oas.annotations.Parameter;
// import io.swagger.v3.oas.annotations.media.Content;
// import io.swagger.v3.oas.annotations.media.Schema;
// import io.swagger.v3.oas.annotations.responses.ApiResponse;
// import io.swagger.v3.oas.annotations.responses.ApiResponses;
// import io.swagger.v3.oas.annotations.security.SecurityRequirement;
// import io.swagger.v3.oas.annotations.tags.Tag;
// import org.springframework.http.ResponseEntity;
// import org.springframework.web.bind.annotation.*;
// import
// vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.DocumentTypeEnum;
// import
// vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.DigitalSignatureRequest;
// import
// vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.DigitalSignatureResponse;
// import
// vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.SignatureStatusResponse;
// import
// vn.edu.iuh.fit.innovationmanagementsystem_be.service.DigitalSignatureService;
// import vn.edu.iuh.fit.innovationmanagementsystem_be.service.UserService;
// import
// vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.UserSignatureProfile;
// import
// vn.edu.iuh.fit.innovationmanagementsystem_be.utils.annotation.ApiMessage;

// import jakarta.validation.Valid;
// import java.util.List;
// import java.util.Map;

// @RestController
// @RequestMapping("/api/v1/digital-signatures")
// @Tag(name = "Digital Signature", description = "Digital signature management
// APIs")
// @SecurityRequirement(name = "Bearer Authentication")
// public class DigitalSignatureController {

// private final DigitalSignatureService digitalSignatureService;
// private final UserService userService;

// public DigitalSignatureController(DigitalSignatureService
// digitalSignatureService, UserService userService) {
// this.digitalSignatureService = digitalSignatureService;
// this.userService = userService;
// }

// // 1. Tạo chữ ký số
// @PostMapping
// @ApiMessage("Tạo chữ ký số thành công")
// @Operation(summary = "Create Digital Signature", description = "Create a new
// digital signature")
// @ApiResponses(value = {
// @ApiResponse(responseCode = "200", description = "Digital signature created
// successfully", content = @Content(schema = @Schema(implementation =
// DigitalSignatureResponse.class))),
// @ApiResponse(responseCode = "400", description = "Invalid request data")
// })
// public ResponseEntity<DigitalSignatureResponse> createDigitalSignature(
// @Parameter(description = "Digital signature creation request", required =
// true) @Valid @RequestBody DigitalSignatureRequest request) {
// DigitalSignatureResponse response =
// digitalSignatureService.createDigitalSignature(request);
// return ResponseEntity.ok(response);
// }

// // 2. Lấy trạng thái chữ ký của innovation
// @GetMapping("/innovation/{innovationId}/status")
// @ApiMessage("Lấy trạng thái chữ ký thành công")
// @Operation(summary = "Get Signature Status", description = "Get signature
// status of innovation")
// @ApiResponses(value = {
// @ApiResponse(responseCode = "200", description = "Signature status retrieved
// successfully", content = @Content(schema = @Schema(implementation =
// SignatureStatusResponse.class))),
// @ApiResponse(responseCode = "400", description = "Invalid request data")
// })
// public ResponseEntity<SignatureStatusResponse> getSignatureStatus(
// @Parameter(description = "Innovation ID", required = true) @PathVariable
// String innovationId,
// @Parameter(description = "Document type", required = true) @RequestParam
// DocumentTypeEnum documentType) {
// SignatureStatusResponse response =
// digitalSignatureService.getSignatureStatus(innovationId,
// documentType);
// return ResponseEntity.ok(response);
// }

// // 3. Lấy trạng thái chữ ký của cả 2 mẫu
// @GetMapping("/innovation/{innovationId}/status/all")
// @ApiMessage("Lấy trạng thái chữ ký của cả 2 mẫu thành công")
// @Operation(summary = "Get All Signature Status", description = "Get all
// signature status of innovation")
// @ApiResponses(value = {
// @ApiResponse(responseCode = "200", description = "All signature status
// retrieved successfully", content = @Content(schema = @Schema(implementation =
// List.class))),
// @ApiResponse(responseCode = "400", description = "Invalid request data")
// })
// public ResponseEntity<List<SignatureStatusResponse>> getAllSignatureStatus(
// @Parameter(description = "Innovation ID", required = true) @PathVariable
// String innovationId) {
// List<SignatureStatusResponse> responses = List.of(
// digitalSignatureService.getSignatureStatus(innovationId,
// DocumentTypeEnum.FORM_1),
// digitalSignatureService.getSignatureStatus(innovationId,
// DocumentTypeEnum.FORM_2));
// return ResponseEntity.ok(responses);
// }

// // 4. Lấy danh sách chữ ký của innovation
// @GetMapping("/innovation/{innovationId}")
// @ApiMessage("Lấy danh sách chữ ký thành công")
// @Operation(summary = "Get Innovation Signatures", description = "Get all
// signatures of innovation")
// @ApiResponses(value = {
// @ApiResponse(responseCode = "200", description = "Innovation signatures
// retrieved successfully", content = @Content(schema = @Schema(implementation =
// List.class))),
// @ApiResponse(responseCode = "400", description = "Invalid request data")
// })
// public ResponseEntity<List<DigitalSignatureResponse>>
// getInnovationSignatures(
// @Parameter(description = "Innovation ID", required = true) @PathVariable
// String innovationId) {
// List<DigitalSignatureResponse> responses = digitalSignatureService
// .getInnovationSignatures(innovationId);
// return ResponseEntity.ok(responses);
// }

// // 5. Kiểm tra xem có thể SUBMITTED không
// @GetMapping("/innovation/{innovationId}/can-submit")
// @ApiMessage("Kiểm tra khả năng SUBMITTED thành công")
// @Operation(summary = "Can Submit Innovation", description = "Check if
// innovation can be submitted")
// @ApiResponses(value = {
// @ApiResponse(responseCode = "200", description = "Innovation can be
// submitted", content = @Content(schema = @Schema(implementation =
// Boolean.class))),
// @ApiResponse(responseCode = "400", description = "Invalid request data")
// })
// public ResponseEntity<Boolean> canSubmitInnovation(
// @Parameter(description = "Innovation ID", required = true) @PathVariable
// String innovationId) {
// boolean canSubmit =
// digitalSignatureService.isBothFormsFullySigned(innovationId);
// return ResponseEntity.ok(canSubmit);
// }

// // 6. Tạo signature cho document hash
// @PostMapping("/generate-signature")
// @ApiMessage("Tạo chữ ký từ document hash thành công")
// @Operation(summary = "Generate Signature for Document Hash", description =
// "Generate signature for document hash")
// @ApiResponses(value = {
// @ApiResponse(responseCode = "200", description = "Signature generated
// successfully", content = @Content(schema = @Schema(implementation =
// String.class))),
// @ApiResponse(responseCode = "400", description = "Invalid request data")
// })
// public ResponseEntity<String> generateSignature(
// @Parameter(description = "Document hash", required = true) @RequestBody
// String documentHash) {
// return
// ResponseEntity.ok(digitalSignatureService.generateSignatureForDocument(documentHash));
// }

// // 7. Verify document signature
// @PostMapping("/verify-signature")
// @ApiMessage("Xác thực chữ ký thành công")
// @Operation(summary = "Verify Document Signature", description = "Verify
// document signature")
// @ApiResponses(value = {
// @ApiResponse(responseCode = "200", description = "Document signature verified
// successfully", content = @Content(schema = @Schema(implementation =
// Boolean.class))),
// @ApiResponse(responseCode = "400", description = "Invalid request data")
// })
// public ResponseEntity<Boolean> verifySignature(
// @Parameter(description = "Verification request with documentHash,
// signatureHash, and userId", required = true) @RequestBody Map<String, String>
// request) {
// String documentHash = request.get("documentHash");
// String signatureHash = request.get("signatureHash");
// String userId = request.get("userId");
// return ResponseEntity.ok(
// digitalSignatureService.verifyDocumentSignature(documentHash, signatureHash,
// userId));
// }

// // 8. Tạo document hash cho file content
// @PostMapping("/generate-hash")
// @ApiMessage("Tạo hash cho file thành công")
// @Operation(summary = "Generate Document Hash", description = "Generate
// document hash from file content")
// @ApiResponses(value = {
// @ApiResponse(responseCode = "200", description = "Document hash generated
// successfully", content = @Content(schema = @Schema(implementation =
// String.class))),
// @ApiResponse(responseCode = "400", description = "Invalid request data")
// })
// public ResponseEntity<String> generateDocumentHash(
// @Parameter(description = "File content as byte array", required = true)
// @RequestBody byte[] fileContent) {
// return
// ResponseEntity.ok(digitalSignatureService.generateDocumentHash(fileContent));
// }

// // 9. Tạo UserSignatureProfile cho user hiện có
// @PostMapping("/create-signature-profile/{userId}")
// @ApiMessage("Tạo hồ sơ chữ ký số thành công")
// @Operation(summary = "Create User Signature Profile", description = "Create a
// new user signature profile")
// @ApiResponses(value = {
// @ApiResponse(responseCode = "200", description = "User signature profile
// created successfully", content = @Content(schema = @Schema(implementation =
// UserSignatureProfile.class))),
// @ApiResponse(responseCode = "400", description = "Invalid request data")
// })
// public ResponseEntity<UserSignatureProfile> createUserSignatureProfile(
// @Parameter(description = "User ID", required = true) @PathVariable String
// userId) {
// UserSignatureProfile profile =
// userService.createUserSignatureProfileForExistingUser(userId);
// return ResponseEntity.ok(profile);
// }
// }
