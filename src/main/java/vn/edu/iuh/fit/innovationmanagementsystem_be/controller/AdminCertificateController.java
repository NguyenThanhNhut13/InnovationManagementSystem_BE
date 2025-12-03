package vn.edu.iuh.fit.innovationmanagementsystem_be.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.CertificateRevocation;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.RevokeCertificateRequest;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.UpdateUserStatusRequest;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.CertificateStatusResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.service.CertificateRevocationService;
import vn.edu.iuh.fit.innovationmanagementsystem_be.service.UserService;
import vn.edu.iuh.fit.innovationmanagementsystem_be.utils.annotation.ApiMessage;

import java.util.Optional;

@RestController
@RequestMapping("/api/v1/admin/certificates")
@Tag(name = "Admin - Certificate Management", description = "APIs quản lý thu hồi và khôi phục certificate (Admin only)")
@PreAuthorize("hasAnyRole('QUAN_TRI_VIEN_HE_THONG')")
public class AdminCertificateController {

        private final CertificateRevocationService certificateRevocationService;
        private final UserService userService;

        public AdminCertificateController(
                        CertificateRevocationService certificateRevocationService,
                        UserService userService) {
                this.certificateRevocationService = certificateRevocationService;
                this.userService = userService;
        }

        // 1. Kiểm tra certificate có bị thu hồi không
        @GetMapping("/{certificateSerial}/status")
        @ApiMessage("Kiểm tra trạng thái certificate thành công")
        @Operation(summary = "Check certificate revocation status", description = "Kiểm tra xem certificate có bị thu hồi hay không")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Success"),
                        @ApiResponse(responseCode = "404", description = "Certificate not found")
        })
        public ResponseEntity<CertificateStatusResponse> checkCertificateStatus(
                        @Parameter(description = "Certificate Serial Number", required = true) @PathVariable String certificateSerial) {

                boolean isRevoked = certificateRevocationService.isCertificateRevoked(certificateSerial);
                Optional<CertificateRevocation> revocationOpt = certificateRevocationService
                                .getRevocationInfo(certificateSerial);

                CertificateStatusResponse response = new CertificateStatusResponse();
                response.setCertificateSerial(certificateSerial);
                response.setRevoked(isRevoked);

                if (revocationOpt.isPresent()) {
                        CertificateRevocation revocation = revocationOpt.get();
                        response.setRevocationDate(revocation.getRevocationDate());
                        response.setRevocationReason(revocation.getRevocationReason());
                        response.setRevokedBy(revocation.getRevokedBy());
                        response.setNotes(revocation.getNotes());
                }

                return ResponseEntity.ok(response);
        }

        // 2. Thu hồi certificate thủ công
        @PostMapping("/revoke")
        @ApiMessage("Thu hồi certificate thành công")
        @Operation(summary = "Manually revoke certificate", description = "Admin thu hồi certificate của user thủ công")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Certificate revoked successfully"),
                        @ApiResponse(responseCode = "400", description = "Invalid request"),
                        @ApiResponse(responseCode = "403", description = "Forbidden")
        })
        public void revokeCertificate(@RequestBody RevokeCertificateRequest request) {
                certificateRevocationService.revokeCertificateByUserId(
                                request.getUserId(),
                                request.getReason());
        }

        // 3. Khôi phục certificate
        @PostMapping("/restore/{userId}")
        @ApiMessage("Khôi phục certificate thành công")
        @Operation(summary = "Restore revoked certificate", description = "Khôi phục certificate đã bị thu hồi (xóa khỏi CRL)")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Certificate restored successfully"),
                        @ApiResponse(responseCode = "404", description = "User or certificate not found"),
                        @ApiResponse(responseCode = "403", description = "Forbidden")
        })
        public void restoreCertificate(
                        @Parameter(description = "User ID", required = true) @PathVariable String userId) {
                certificateRevocationService.restoreCertificate(userId);
        }

        // 4. Cập nhật user status (trigger auto revocation/restoration)
        @PutMapping("/users/{userId}/status")
        @ApiMessage("Cập nhật trạng thái user thành công")
        @Operation(summary = "Update user status", description = "Cập nhật status của user. Tự động thu hồi/khôi phục certificate khi cần.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Status updated successfully"),
                        @ApiResponse(responseCode = "400", description = "Invalid status"),
                        @ApiResponse(responseCode = "403", description = "Forbidden")
        })
        public void updateUserStatus(
                        @Parameter(description = "User ID", required = true) @PathVariable String userId,
                        @RequestBody UpdateUserStatusRequest request) {
                userService.updateUserStatus(userId, request.getStatus());
        }
}
