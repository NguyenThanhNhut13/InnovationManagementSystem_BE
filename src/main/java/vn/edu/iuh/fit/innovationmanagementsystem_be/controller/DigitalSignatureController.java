package vn.edu.iuh.fit.innovationmanagementsystem_be.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.DocumentTypeEnum;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.DigitalSignatureRequest;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.DigitalSignatureResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.SignatureStatusResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.service.DigitalSignatureService;
import vn.edu.iuh.fit.innovationmanagementsystem_be.service.UserService;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.UserSignatureProfile;
import vn.edu.iuh.fit.innovationmanagementsystem_be.utils.annotation.ApiMessage;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/digital-signatures")
public class DigitalSignatureController {

    private final DigitalSignatureService digitalSignatureService;
    private final UserService userService;

    public DigitalSignatureController(DigitalSignatureService digitalSignatureService, UserService userService) {
        this.digitalSignatureService = digitalSignatureService;
        this.userService = userService;
    }

    // 1. Tạo chữ ký số
    @PostMapping
    @ApiMessage("Tạo chữ ký số thành công")
    public ResponseEntity<DigitalSignatureResponse> createDigitalSignature(
            @Valid @RequestBody DigitalSignatureRequest request) {
        DigitalSignatureResponse response = digitalSignatureService.createDigitalSignature(request);
        return ResponseEntity.ok(response);
    }

    // 2. Lấy trạng thái chữ ký của innovation
    @GetMapping("/innovation/{innovationId}/status")
    @ApiMessage("Lấy trạng thái chữ ký thành công")
    public ResponseEntity<SignatureStatusResponse> getSignatureStatus(
            @PathVariable String innovationId,
            @RequestParam DocumentTypeEnum documentType) {
        SignatureStatusResponse response = digitalSignatureService.getSignatureStatus(innovationId, documentType);
        return ResponseEntity.ok(response);
    }

    // 3. Lấy trạng thái chữ ký của cả 2 mẫu
    @GetMapping("/innovation/{innovationId}/status/all")
    @ApiMessage("Lấy trạng thái chữ ký của cả 2 mẫu thành công")
    public ResponseEntity<List<SignatureStatusResponse>> getAllSignatureStatus(
            @PathVariable String innovationId) {
        List<SignatureStatusResponse> responses = List.of(
                digitalSignatureService.getSignatureStatus(innovationId, DocumentTypeEnum.FORM_1),
                digitalSignatureService.getSignatureStatus(innovationId, DocumentTypeEnum.FORM_2));
        return ResponseEntity.ok(responses);
    }

    // 4. Lấy danh sách chữ ký của innovation
    @GetMapping("/innovation/{innovationId}")
    @ApiMessage("Lấy danh sách chữ ký thành công")
    public ResponseEntity<List<DigitalSignatureResponse>> getInnovationSignatures(
            @PathVariable String innovationId) {
        List<DigitalSignatureResponse> responses = digitalSignatureService.getInnovationSignatures(innovationId);
        return ResponseEntity.ok(responses);
    }

    // 5. Kiểm tra xem có thể SUBMITTED không
    @GetMapping("/innovation/{innovationId}/can-submit")
    @ApiMessage("Kiểm tra khả năng SUBMITTED thành công")
    public ResponseEntity<Boolean> canSubmitInnovation(@PathVariable String innovationId) {
        boolean canSubmit = digitalSignatureService.isBothFormsFullySigned(innovationId);
        return ResponseEntity.ok(canSubmit);
    }

    // 6. Generate signature for document hash
    @PostMapping("/generate-signature")
    @ApiMessage("Tạo chữ ký từ document hash thành công")
    public ResponseEntity<String> generateSignature(@RequestBody String documentHash) {
        return ResponseEntity.ok(digitalSignatureService.generateSignatureForDocument(documentHash));
    }

    // 7. Verify document signature
    @PostMapping("/verify-signature")
    @ApiMessage("Xác thực chữ ký thành công")
    public ResponseEntity<Boolean> verifySignature(@RequestBody Map<String, String> request) {
        String documentHash = request.get("documentHash");
        String signatureHash = request.get("signatureHash");
        String userId = request.get("userId");
        return ResponseEntity.ok(digitalSignatureService.verifyDocumentSignature(documentHash, signatureHash, userId));
    }

    // 8. Generate document hash from file content
    @PostMapping("/generate-hash")
    @ApiMessage("Tạo hash cho file thành công")
    public ResponseEntity<String> generateDocumentHash(@RequestBody byte[] fileContent) {
        return ResponseEntity.ok(digitalSignatureService.generateDocumentHash(fileContent));
    }

    // 9. Tạo UserSignatureProfile cho user hiện có
    @PostMapping("/create-signature-profile/{userId}")
    @ApiMessage("Tạo hồ sơ chữ ký số thành công")
    public ResponseEntity<UserSignatureProfile> createUserSignatureProfile(@PathVariable String userId) {
        UserSignatureProfile profile = userService.createUserSignatureProfileForExistingUser(userId);
        return ResponseEntity.ok(profile);
    }
}
