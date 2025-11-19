package vn.edu.iuh.fit.innovationmanagementsystem_be.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.CreateCertificateAuthorityRequest;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.CertificateAuthorityResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.service.CertificateAuthorityService;
import vn.edu.iuh.fit.innovationmanagementsystem_be.utils.annotation.ApiMessage;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Certificate Authority", description = "Certificate Authority management APIs")
@SecurityRequirement(name = "Bearer Authentication")
public class CertificateAuthorityController {

    private final CertificateAuthorityService certificateAuthorityService;

    public CertificateAuthorityController(CertificateAuthorityService certificateAuthorityService) {
        this.certificateAuthorityService = certificateAuthorityService;
    }

    // 1. Tạo CA mới
    @PostMapping("/certificate-authorities")
    @ApiMessage("Tạo CA thành công")
    @Operation(summary = "Create Certificate Authority", description = "Create a new internal Certificate Authority")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "CA created successfully", content = @Content(schema = @Schema(implementation = CertificateAuthorityResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid certificate data"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<CertificateAuthorityResponse> createCertificateAuthority(
            @RequestBody CreateCertificateAuthorityRequest request) {
        CertificateAuthorityResponse response = certificateAuthorityService.createCertificateAuthority(request);
        return ResponseEntity.ok(response);
    }

    // 2. Xác minh CA
    @PostMapping("/certificate-authorities/{caId}/verify")
    @ApiMessage("Xác minh CA thành công")
    @Operation(summary = "Verify Certificate Authority", description = "Verify a Certificate Authority")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "CA verified successfully", content = @Content(schema = @Schema(implementation = CertificateAuthorityResponse.class))),
            @ApiResponse(responseCode = "400", description = "CA cannot be verified"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "CA not found")
    })
    public ResponseEntity<CertificateAuthorityResponse> verifyCertificateAuthority(
            @Parameter(description = "CA ID", required = true) @PathVariable String caId) {
        CertificateAuthorityResponse response = certificateAuthorityService.verifyCertificateAuthority(caId);
        return ResponseEntity.ok(response);
    }

    // 3. Thu hồi CA
    @PostMapping("/certificate-authorities/{caId}/revoke")
    @ApiMessage("Thu hồi CA thành công")
    @Operation(summary = "Revoke Certificate Authority", description = "Revoke a Certificate Authority")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "CA revoked successfully", content = @Content(schema = @Schema(implementation = CertificateAuthorityResponse.class))),
            @ApiResponse(responseCode = "400", description = "CA cannot be revoked"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "CA not found")
    })
    public ResponseEntity<CertificateAuthorityResponse> revokeCertificateAuthority(
            @Parameter(description = "CA ID", required = true) @PathVariable String caId) {
        CertificateAuthorityResponse response = certificateAuthorityService.revokeCertificateAuthority(caId);
        return ResponseEntity.ok(response);
    }

    // 4. Kiểm tra trạng thái CA
    @GetMapping("/certificate-authorities/{caId}/status")
    @ApiMessage("Lấy trạng thái CA thành công")
    @Operation(summary = "Check CA Status", description = "Check the status of a Certificate Authority")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "CA status retrieved successfully", content = @Content(schema = @Schema(implementation = CertificateAuthorityResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "CA not found")
    })
    public ResponseEntity<CertificateAuthorityResponse> checkCAStatus(
            @Parameter(description = "CA ID", required = true) @PathVariable String caId) {
        CertificateAuthorityResponse response = certificateAuthorityService.checkCAStatus(caId);
        return ResponseEntity.ok(response);
    }

    // 5. Lấy tất cả CA
    @GetMapping("/certificate-authorities")
    @ApiMessage("Lấy danh sách CA thành công")
    @Operation(summary = "Get All Certificate Authorities", description = "Get all Certificate Authorities")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "CAs retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<List<CertificateAuthorityResponse>> getAllCAs() {
        List<CertificateAuthorityResponse> responses = certificateAuthorityService.getAllCAs();
        return ResponseEntity.ok(responses);
    }

    // 6. Lấy CA theo ID
    @GetMapping("/certificate-authorities/{caId}")
    @ApiMessage("Lấy CA thành công")
    @Operation(summary = "Get Certificate Authority by ID", description = "Get a Certificate Authority by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "CA retrieved successfully", content = @Content(schema = @Schema(implementation = CertificateAuthorityResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "CA not found")
    })
    public ResponseEntity<CertificateAuthorityResponse> getCAById(
            @Parameter(description = "CA ID", required = true) @PathVariable String caId) {
        CertificateAuthorityResponse response = certificateAuthorityService.getCAById(caId);
        return ResponseEntity.ok(response);
    }

    // 7. Lấy CA đã được xác minh
    @GetMapping("/certificate-authorities/verified")
    @ApiMessage("Lấy danh sách CA đã xác minh thành công")
    @Operation(summary = "Get Verified Certificate Authorities", description = "Get all verified Certificate Authorities")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Verified CAs retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<List<CertificateAuthorityResponse>> getVerifiedCAs() {
        List<CertificateAuthorityResponse> responses = certificateAuthorityService.getVerifiedCAs();
        return ResponseEntity.ok(responses);
    }
}
