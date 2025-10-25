package vn.edu.iuh.fit.innovationmanagementsystem_be.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.certificateDTO.CSRRequestDTO;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.certificateDTO.CSRResponseDTO;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.certificateDTO.CertificateIssuanceResultDTO;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.certificateDTO.CertificateRevocationResultDTO;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.certificateDTO.CertificateStatusResultDTO;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.certificateDTO.IdentityVerificationRequestDTO;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.certificateDTO.IdentityVerificationResultDTO;
import vn.edu.iuh.fit.innovationmanagementsystem_be.service.UserService;
import vn.edu.iuh.fit.innovationmanagementsystem_be.service.certificateService.CertificateAuthorityService;

/**
 * Controller cho Certificate Authority
 * Clean code - chỉ chứa API endpoints
 */
@RestController
@RequestMapping("/api/ca")
@CrossOrigin(origins = "*")
public class CertificateAuthorityController {

    @Autowired
    private CertificateAuthorityService certificateAuthorityService;

    @Autowired
    private UserService userService;

    /**
     * 1. User tạo CSR
     */
    @PostMapping("/csr/create")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> createCSR(@RequestBody CSRRequestDTO request) {
        try {
            String currentUserId = userService.getCurrentUser().getId();
            CSRResponseDTO response = certificateAuthorityService
                    .createCertificateSigningRequest(currentUserId, request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi: " + e.getMessage());
        }
    }

    /**
     * 2. Admin xác minh danh tính
     */
    @PostMapping("/csr/{csrId}/verify")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> verifyIdentity(
            @PathVariable String csrId,
            @RequestBody IdentityVerificationRequestDTO request) {
        try {
            IdentityVerificationResultDTO result = certificateAuthorityService
                    .verifyUserIdentity(csrId, request);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi: " + e.getMessage());
        }
    }

    /**
     * 3. Admin cấp certificate
     */
    @PostMapping("/csr/{csrId}/issue")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> issueCertificate(@PathVariable String csrId) {
        try {
            CertificateIssuanceResultDTO result = certificateAuthorityService
                    .issueCertificate(csrId);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi: " + e.getMessage());
        }
    }

    /**
     * 4. Thu hồi certificate
     */
    @PostMapping("/certificate/{certificateId}/revoke")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> revokeCertificate(
            @PathVariable String certificateId,
            @RequestParam String reason) {
        try {
            CertificateRevocationResultDTO result = certificateAuthorityService
                    .revokeCertificate(certificateId, reason);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi: " + e.getMessage());
        }
    }

    /**
     * 5. Kiểm tra trạng thái certificate
     */
    @GetMapping("/certificate/{certificateId}/status")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> checkCertificateStatus(@PathVariable String certificateId) {
        try {
            CertificateStatusResultDTO result = certificateAuthorityService
                    .checkCertificateStatus(certificateId);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi: " + e.getMessage());
        }
    }

    /**
     * 6. Lấy danh sách CSR đang chờ xử lý (Admin)
     */
    @GetMapping("/csr/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getPendingCSRs() {
        try {
            return ResponseEntity.ok(certificateAuthorityService.getPendingCSRs());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi: " + e.getMessage());
        }
    }

    /**
     * 7. Lấy danh sách certificate đã cấp (Admin)
     */
    @GetMapping("/certificates/issued")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getIssuedCertificates() {
        try {
            return ResponseEntity.ok(certificateAuthorityService.getIssuedCertificates());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi: " + e.getMessage());
        }
    }

    /**
     * 8. Lấy thông tin CSR theo ID
     */
    @GetMapping("/csr/{csrId}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> getCSRInfo(@PathVariable String csrId) {
        try {
            // Tìm CSR trong danh sách pending
            CSRRequestDTO csr = certificateAuthorityService.getPendingCSRs()
                    .stream()
                    .filter(c -> c.getCsrId().equals(csrId))
                    .findFirst()
                    .orElse(null);

            if (csr == null) {
                return ResponseEntity.notFound().build();
            }

            return ResponseEntity.ok(csr);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi: " + e.getMessage());
        }
    }
}