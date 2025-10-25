package vn.edu.iuh.fit.innovationmanagementsystem_be.service.certificateService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.certificateDTO.CSRRequestDTO;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.certificateDTO.CSRResponseDTO;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.certificateDTO.CertificateIssuanceResultDTO;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.certificateDTO.CertificateRevocationResultDTO;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.certificateDTO.CertificateStatusResultDTO;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.certificateDTO.IdentityVerificationRequestDTO;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.certificateDTO.IdentityVerificationResultDTO;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.certificateDTO.IssuedCertificateDTO;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.UserSignatureProfile;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.UserSignatureProfileRepository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.service.CertificateValidationService;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.UserRepository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.exception.IdInvalidException;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service chính cho Certificate Authority
 * Clean code - chỉ chứa business logic
 */
@Service
public class CertificateAuthorityService {

    private static final Logger logger = LoggerFactory.getLogger(CertificateAuthorityService.class);

    // Danh sách CSR đang chờ xử lý
    private final Map<String, CSRRequestDTO> pendingCSRs = new ConcurrentHashMap<>();

    // Danh sách certificate đã cấp
    private final Map<String, IssuedCertificateDTO> issuedCertificates = new ConcurrentHashMap<>();

    @Autowired
    private UserSignatureProfileRepository userSignatureProfileRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CertificateValidationService certificateValidationService;

    @Autowired
    private CAConfigurationService caConfigurationService;

    /**
     * 1. User tạo Certificate Signing Request (CSR)
     */
    public CSRResponseDTO createCertificateSigningRequest(String userId, CSRRequestDTO request) {
        try {
            logger.info("User {} tạo CSR", userId);

            // Validate user
            userRepository.findById(userId)
                    .orElseThrow(() -> new IdInvalidException("User không tồn tại"));

            // Validate user signature profile
            UserSignatureProfile profile = userSignatureProfileRepository.findByUserId(userId)
                    .orElseThrow(() -> new IdInvalidException("User chưa có hồ sơ chữ ký số"));

            if (profile.getCertificateData() != null) {
                throw new IdInvalidException("User đã có certificate");
            }

            // Tạo CSR ID
            String csrId = "CSR-" + System.currentTimeMillis() + "-" + userId;

            // Lưu CSR request
            CSRRequestDTO csrRequest = new CSRRequestDTO();
            csrRequest.setCsrId(csrId);
            csrRequest.setUserId(userId);
            csrRequest.setPublicKey(profile.getPublicKey());
            csrRequest.setSubjectDN(request.getSubjectDN());
            csrRequest.setEmail(request.getEmail());
            csrRequest.setPhoneNumber(request.getPhoneNumber());
            csrRequest.setOrganization(request.getOrganization());
            csrRequest.setStatus("PENDING");
            csrRequest.setCreatedAt(LocalDateTime.now());
            csrRequest.setValidationDocuments(request.getValidationDocuments());

            pendingCSRs.put(csrId, csrRequest);

            // Tạo response
            CSRResponseDTO response = new CSRResponseDTO();
            response.setCsrId(csrId);
            response.setStatus("PENDING");
            response.setMessage("CSR đã được tạo và đang chờ xác minh");
            response.setEstimatedProcessingTime(caConfigurationService.getCSRProcessingTimeDays());

            logger.info("CSR {} đã được tạo cho user {}", csrId, userId);

            return response;

        } catch (Exception e) {
            logger.error("Lỗi khi tạo CSR: {}", e.getMessage());
            throw new IdInvalidException("Không thể tạo CSR: " + e.getMessage());
        }
    }

    /**
     * 2. CA xác minh danh tính user (mô phỏng quy trình thực tế)
     */
    public IdentityVerificationResultDTO verifyUserIdentity(String csrId, IdentityVerificationRequestDTO request) {
        try {
            logger.info("Bắt đầu xác minh danh tính cho CSR {}", csrId);

            CSRRequestDTO csr = pendingCSRs.get(csrId);
            if (csr == null) {
                throw new IdInvalidException("CSR không tồn tại");
            }

            IdentityVerificationResultDTO result = new IdentityVerificationResultDTO();
            result.setCsrId(csrId);
            result.setUserId(csr.getUserId());

            // Mô phỏng quy trình xác minh thực tế
            List<String> verificationSteps = new ArrayList<>();
            List<String> errors = new ArrayList<>();

            // 1. Xác minh thông tin cá nhân
            if (verifyPersonalInformation(csr, request)) {
                verificationSteps.add("✓ Xác minh thông tin cá nhân");
            } else {
                errors.add("Thông tin cá nhân không khớp");
            }

            // 2. Xác minh tài liệu
            if (verifyDocuments(csr, request)) {
                verificationSteps.add("✓ Xác minh tài liệu");
            } else {
                errors.add("Tài liệu không hợp lệ");
            }

            // 3. Xác minh email
            if (verifyEmail(csr, request)) {
                verificationSteps.add("✓ Xác minh email");
            } else {
                errors.add("Email không được xác minh");
            }

            // 4. Xác minh số điện thoại
            if (verifyPhoneNumber(csr, request)) {
                verificationSteps.add("✓ Xác minh số điện thoại");
            } else {
                errors.add("Số điện thoại không được xác minh");
            }

            // 5. Kiểm tra blacklist
            if (caConfigurationService.isUserBlacklisted(csr.getUserId())) {
                errors.add("User trong danh sách đen");
            } else {
                verificationSteps.add("✓ Kiểm tra danh sách đen");
            }

            result.setVerificationSteps(verificationSteps);
            result.setErrors(errors);

            if (errors.isEmpty()) {
                result.setStatus("VERIFIED");
                result.setMessage("Xác minh danh tính thành công");
                csr.setStatus("VERIFIED");
                csr.setVerifiedAt(LocalDateTime.now());
            } else {
                result.setStatus("FAILED");
                result.setMessage("Xác minh danh tính thất bại");
                csr.setStatus("FAILED");
            }

            logger.info("Kết quả xác minh cho CSR {}: {}", csrId, result.getStatus());

            return result;

        } catch (Exception e) {
            logger.error("Lỗi khi xác minh danh tính: {}", e.getMessage());
            throw new IdInvalidException("Không thể xác minh danh tính: " + e.getMessage());
        }
    }

    /**
     * 3. CA cấp certificate X.509
     */
    public CertificateIssuanceResultDTO issueCertificate(String csrId) {
        try {
            logger.info("Bắt đầu cấp certificate cho CSR {}", csrId);

            CSRRequestDTO csr = pendingCSRs.get(csrId);
            if (csr == null) {
                throw new IdInvalidException("CSR không tồn tại");
            }

            if (!"VERIFIED".equals(csr.getStatus())) {
                throw new IdInvalidException("CSR chưa được xác minh");
            }

            // Tạo X.509 certificate
            String certificateData = createX509Certificate(csr);

            // Tạo certificate chain
            String certificateChain = createCertificateChain(certificateData);

            // Lưu certificate đã cấp
            IssuedCertificateDTO issuedCert = new IssuedCertificateDTO();
            issuedCert.setCertificateId("CERT-" + System.currentTimeMillis());
            issuedCert.setUserId(csr.getUserId());
            issuedCert.setCsrId(csrId);
            issuedCert.setCertificateData(certificateData);
            issuedCert.setCertificateChain(certificateChain);
            issuedCert.setIssuedAt(LocalDateTime.now());
            issuedCert
                    .setExpiresAt(LocalDateTime.now().plusYears(caConfigurationService.getCertificateValidityYears()));
            issuedCert.setStatus("ACTIVE");

            issuedCertificates.put(issuedCert.getCertificateId(), issuedCert);

            // Cập nhật user signature profile
            updateUserSignatureProfile(csr.getUserId(), certificateData, certificateChain);

            // Xóa CSR khỏi danh sách pending
            pendingCSRs.remove(csrId);

            CertificateIssuanceResultDTO result = new CertificateIssuanceResultDTO();
            result.setCertificateId(issuedCert.getCertificateId());
            result.setStatus("ISSUED");
            result.setMessage("Certificate đã được cấp thành công");
            result.setCertificateData(certificateData);
            result.setCertificateChain(certificateChain);
            result.setExpiresAt(issuedCert.getExpiresAt());

            logger.info("Certificate {} đã được cấp cho user {}", issuedCert.getCertificateId(), csr.getUserId());

            return result;

        } catch (Exception e) {
            logger.error("Lỗi khi cấp certificate: {}", e.getMessage());
            throw new IdInvalidException("Không thể cấp certificate: " + e.getMessage());
        }
    }

    /**
     * 4. Thu hồi certificate
     */
    public CertificateRevocationResultDTO revokeCertificate(String certificateId, String reason) {
        try {
            logger.info("Bắt đầu thu hồi certificate {}", certificateId);

            IssuedCertificateDTO cert = issuedCertificates.get(certificateId);
            if (cert == null) {
                throw new IdInvalidException("Certificate không tồn tại");
            }

            if ("REVOKED".equals(cert.getStatus())) {
                throw new IdInvalidException("Certificate đã bị thu hồi");
            }

            // Cập nhật trạng thái
            cert.setStatus("REVOKED");
            cert.setRevokedAt(LocalDateTime.now());
            cert.setRevocationReason(reason);

            // Cập nhật user signature profile
            UserSignatureProfile profile = userSignatureProfileRepository.findByUserId(cert.getUserId())
                    .orElseThrow(() -> new IdInvalidException("Không tìm thấy user signature profile"));

            profile.setCertificateStatus("REVOKED");
            userSignatureProfileRepository.save(profile);

            CertificateRevocationResultDTO result = new CertificateRevocationResultDTO();
            result.setCertificateId(certificateId);
            result.setStatus("REVOKED");
            result.setMessage("Certificate đã bị thu hồi");
            result.setRevokedAt(cert.getRevokedAt());
            result.setReason(reason);

            logger.info("Certificate {} đã bị thu hồi", certificateId);

            return result;

        } catch (Exception e) {
            logger.error("Lỗi khi thu hồi certificate: {}", e.getMessage());
            throw new IdInvalidException("Không thể thu hồi certificate: " + e.getMessage());
        }
    }

    /**
     * 5. Kiểm tra trạng thái certificate
     */
    public CertificateStatusResultDTO checkCertificateStatus(String certificateId) {
        try {
            IssuedCertificateDTO cert = issuedCertificates.get(certificateId);
            if (cert == null) {
                throw new IdInvalidException("Certificate không tồn tại");
            }

            CertificateStatusResultDTO result = new CertificateStatusResultDTO();
            result.setCertificateId(certificateId);
            result.setStatus(cert.getStatus());
            result.setIssuedAt(cert.getIssuedAt());
            result.setExpiresAt(cert.getExpiresAt());
            result.setRevokedAt(cert.getRevokedAt());
            result.setRevocationReason(cert.getRevocationReason());

            // Kiểm tra hết hạn
            if (LocalDateTime.now().isAfter(cert.getExpiresAt())) {
                result.setStatus("EXPIRED");
            }

            return result;

        } catch (Exception e) {
            logger.error("Lỗi khi kiểm tra trạng thái certificate: {}", e.getMessage());
            throw new IdInvalidException("Không thể kiểm tra trạng thái certificate: " + e.getMessage());
        }
    }

    /**
     * 6. Lấy danh sách CSR đang chờ xử lý
     */
    public List<CSRRequestDTO> getPendingCSRs() {
        return new ArrayList<>(pendingCSRs.values());
    }

    /**
     * 7. Lấy danh sách certificate đã cấp
     */
    public List<IssuedCertificateDTO> getIssuedCertificates() {
        return new ArrayList<>(issuedCertificates.values());
    }

    // ========== PRIVATE METHODS ==========

    private boolean verifyPersonalInformation(CSRRequestDTO csr, IdentityVerificationRequestDTO request) {
        // Mô phỏng xác minh thông tin cá nhân
        return request.getPersonalInfo() != null &&
                request.getPersonalInfo().getFullName() != null &&
                request.getPersonalInfo().getDateOfBirth() != null &&
                request.getPersonalInfo().getNationalId() != null;
    }

    private boolean verifyDocuments(CSRRequestDTO csr, IdentityVerificationRequestDTO request) {
        // Mô phỏng xác minh tài liệu
        return request.getDocuments() != null &&
                !request.getDocuments().isEmpty() &&
                request.getDocuments().contains("CMND") &&
                request.getDocuments().contains("CCCD");
    }

    private boolean verifyEmail(CSRRequestDTO csr, IdentityVerificationRequestDTO request) {
        // Mô phỏng xác minh email
        return request.getEmailVerificationCode() != null &&
                request.getEmailVerificationCode().equals("VERIFIED");
    }

    private boolean verifyPhoneNumber(CSRRequestDTO csr, IdentityVerificationRequestDTO request) {
        // Mô phỏng xác minh số điện thoại
        return request.getPhoneVerificationCode() != null &&
                request.getPhoneVerificationCode().equals("VERIFIED");
    }

    private String createX509Certificate(CSRRequestDTO csr) {
        try {
            // Mô phỏng tạo X.509 certificate
            // Trong thực tế, đây sẽ là quá trình phức tạp với BouncyCastle hoặc tương tự

            // Tạo certificate data mô phỏng
            String certificateData = "-----BEGIN CERTIFICATE-----\n" +
                    "MIICdTCCAd4CAQAwDQYJKoZIhvcNAQELBQAwXjELMAkGA1UEBhMCVk4xEzARBgNV\n" +
                    "BAgMCkFubmFtIFByb3YxGTAXBgNVBAoMEElOVk5PVkFUSU9OIFNVUFBMMRkwFwYD\n" +
                    "VQQDDBBJbnZvdmF0aW9uIENBIENlcnQwHhcNMjQwMTAxMDAwMDAwWhcNMjUwMTAx\n" +
                    "MDAwMDAwWjBeMQswCQYDVQQGEwJWTjETMBEGA1UECAwKQW5uYW0gUHJvdjEZMBcG\n" +
                    "A1UECgwQSU5WTk9WQVRJT04gU1VQUFwxGTAXBgNVBAMMEEludm92YXRpb24gQ0Eg\n" +
                    "Q2VydDCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEBALQ...\n" +
                    "-----END CERTIFICATE-----";

            return Base64.getEncoder().encodeToString(certificateData.getBytes());

        } catch (Exception e) {
            throw new IdInvalidException("Không thể tạo certificate: " + e.getMessage());
        }
    }

    private String createCertificateChain(String certificateData) {
        try {
            // Mô phỏng tạo certificate chain
            Map<String, String> chain = new HashMap<>();
            chain.put("endEntity", certificateData);
            chain.put("intermediate", caConfigurationService.getCARootCertificate());
            chain.put("root", caConfigurationService.getCARootCertificate());

            return new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(chain);
        } catch (Exception e) {
            throw new IdInvalidException("Không thể tạo certificate chain: " + e.getMessage());
        }
    }

    private void updateUserSignatureProfile(String userId, String certificateData, String certificateChain) {
        try {
            UserSignatureProfile profile = userSignatureProfileRepository.findByUserId(userId)
                    .orElseThrow(() -> new IdInvalidException("Không tìm thấy user signature profile"));

            // Extract certificate info
            CertificateValidationService.CertificateInfo certInfo = certificateValidationService
                    .extractCertificateInfo(certificateData);

            profile.setCertificateData(certificateData);
            profile.setCertificateChain(certificateChain);
            profile.setCertificateSerial(certInfo.getSerialNumber());
            profile.setCertificateIssuer(certInfo.getIssuer());
            profile.setCertificateExpiryDate(certInfo.getNotAfter().toInstant()
                    .atZone(ZoneId.systemDefault()).toLocalDateTime());
            profile.setCertificateStatus("VALID");
            profile.setLastCertificateValidation(LocalDateTime.now());

            userSignatureProfileRepository.save(profile);

        } catch (Exception e) {
            throw new IdInvalidException("Không thể cập nhật user signature profile: " + e.getMessage());
        }
    }
}