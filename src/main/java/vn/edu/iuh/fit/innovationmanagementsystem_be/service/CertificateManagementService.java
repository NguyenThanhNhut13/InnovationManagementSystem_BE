package vn.edu.iuh.fit.innovationmanagementsystem_be.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.UserSignatureProfile;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.CAStatusEnum;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.UserSignatureProfileRepository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.exception.IdInvalidException;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CertificateManagementService {

    private static final Logger logger = LoggerFactory.getLogger(CertificateManagementService.class);

    @Autowired
    private UserSignatureProfileRepository userSignatureProfileRepository;

    @Autowired
    private CertificateValidationService certificateValidationService;

    @Autowired
    private HSMEncryptionService hsmEncryptionService;

    /**
     * Setup certificate cho user signature profile
     */
    public UserSignatureProfile setupUserCertificate(String userId, String certificateData,
            String privateKey) {

        try {
            // 1. Validate certificate
            CertificateValidationService.CertificateValidationResult validationResult = certificateValidationService
                    .validateX509Certificate(certificateData);

            if (!validationResult.isValid()) {
                throw new IdInvalidException("Certificate không hợp lệ: " +
                        String.join(", ", validationResult.getErrors()));
            }

            // 2. Extract certificate info
            CertificateValidationService.CertificateInfo certInfo = certificateValidationService
                    .extractCertificateInfo(certificateData);

            // 3. Check certificate expiration
            CertificateValidationService.CertificateExpirationResult expirationResult = certificateValidationService
                    .checkCertificateExpiration(certificateData);

            // 4. Encrypt private key
            String encryptedPrivateKey = hsmEncryptionService.encryptPrivateKey(privateKey);

            // 5. Create or update user signature profile
            UserSignatureProfile profile = userSignatureProfileRepository.findByUserId(userId)
                    .orElse(new UserSignatureProfile());

            profile.setUser(new vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.User());
            profile.getUser().setId(userId);
            profile.setEncryptedPrivateKey(encryptedPrivateKey);
            profile.setCertificateData(certificateData);
            profile.setCertificateVersion(certInfo.getVersion());
            profile.setCertificateSerial(certInfo.getSerialNumber());
            profile.setCertificateIssuer(certInfo.getIssuer());
            profile.setCertificateSubject(certInfo.getSubject());
            profile.setCertificateValidFrom(certInfo.getNotBefore()
                    .toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime());
            profile.setCertificateExpiryDate(certInfo.getNotAfter()
                    .toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime());
            profile.setCertificateStatus(expirationResult.isExpired() ? CAStatusEnum.EXPIRED : CAStatusEnum.VERIFIED);
            profile.setLastCertificateValidation(LocalDateTime.now());

            return userSignatureProfileRepository.save(profile);

        } catch (Exception e) {
            throw new IdInvalidException("Không thể setup certificate: " + e.getMessage());
        }
    }

    /**
     * Validate và update certificate status
     */
    public UserSignatureProfile validateAndUpdateCertificate(String userId) {
        try {
            UserSignatureProfile profile = userSignatureProfileRepository.findByUserId(userId)
                    .orElseThrow(() -> new IdInvalidException("Không tìm thấy user signature profile"));

            if (profile.getCertificateData() == null) {
                throw new IdInvalidException("User chưa có certificate");
            }

            // Validate certificate
            CertificateValidationService.CertificateValidationResult validationResult = certificateValidationService
                    .validateX509Certificate(profile.getCertificateData());

            // Check expiration
            CertificateValidationService.CertificateExpirationResult expirationResult = certificateValidationService
                    .checkCertificateExpiration(profile.getCertificateData());

            // Update status
            if (!validationResult.isValid()) {
                profile.setCertificateStatus(CAStatusEnum.REVOKED);
            } else if (expirationResult.isExpired()) {
                profile.setCertificateStatus(CAStatusEnum.EXPIRED);
            } else {
                profile.setCertificateStatus(CAStatusEnum.VERIFIED);
            }

            profile.setLastCertificateValidation(LocalDateTime.now());

            return userSignatureProfileRepository.save(profile);

        } catch (Exception e) {
            throw new IdInvalidException("Không thể validate certificate: " + e.getMessage());
        }
    }

    /**
     * Get decrypted private key cho user
     */
    public String getDecryptedPrivateKey(String userId) {
        try {
            UserSignatureProfile profile = userSignatureProfileRepository.findByUserId(userId)
                    .orElseThrow(() -> new IdInvalidException("Không tìm thấy user signature profile"));

            if (profile.getEncryptedPrivateKey() == null) {
                throw new IdInvalidException("User chưa có private key");
            }

            // Validate certificate trước khi decrypt
            if (profile.getCertificateStatus() != CAStatusEnum.VERIFIED) {
                throw new IdInvalidException("Certificate không hợp lệ hoặc đã hết hạn");
            }

            return hsmEncryptionService.decryptPrivateKey(profile.getEncryptedPrivateKey());

        } catch (Exception e) {
            throw new IdInvalidException("Không thể lấy private key: " + e.getMessage());
        }
    }

    /**
     * Setup Timestamp Authority cho user - Disabled for academic project
     */
    public UserSignatureProfile setupTimestampAuthority(String userId, String tsaUrl, String tsaCertificate) {
        try {
            UserSignatureProfile profile = userSignatureProfileRepository.findByUserId(userId)
                    .orElseThrow(() -> new IdInvalidException("Không tìm thấy user signature profile"));

            // TSA functionality disabled for academic project
            // In a real system, this would validate and store TSA configuration
            logger.info("TSA setup requested for user {} - functionality disabled for academic project", userId);

            return profile;

        } catch (Exception e) {
            throw new IdInvalidException("Không thể setup timestamp authority: " + e.getMessage());
        }
    }

    /**
     * Get certificate info cho user
     */
    public CertificateInfoResponse getCertificateInfo(String userId) {
        try {
            UserSignatureProfile profile = userSignatureProfileRepository.findByUserId(userId)
                    .orElseThrow(() -> new IdInvalidException("Không tìm thấy user signature profile"));

            if (profile.getCertificateData() == null) {
                throw new IdInvalidException("User chưa có certificate");
            }

            CertificateValidationService.CertificateInfo certInfo = certificateValidationService
                    .extractCertificateInfo(profile.getCertificateData());

            CertificateValidationService.CertificateExpirationResult expirationResult = certificateValidationService
                    .checkCertificateExpiration(profile.getCertificateData());

            CertificateInfoResponse response = new CertificateInfoResponse();
            response.setVersion(certInfo.getVersion());
            response.setSerialNumber(certInfo.getSerialNumber());
            response.setIssuer(certInfo.getIssuer());
            response.setSubject(certInfo.getSubject());
            response.setNotBefore(certInfo.getNotBefore());
            response.setNotAfter(certInfo.getNotAfter());
            response.setStatus(profile.getCertificateStatus() != null ? profile.getCertificateStatus().name() : null);
            response.setDaysUntilExpiry(expirationResult.getDaysUntilExpiry());
            response.setExpired(expirationResult.isExpired());
            response.setExpiringSoon(expirationResult.isExpiringSoon());
            response.setLastValidation(profile.getLastCertificateValidation());

            return response;

        } catch (Exception e) {
            throw new IdInvalidException("Không thể lấy certificate info: " + e.getMessage());
        }
    }

    /**
     * Bulk validate certificates cho tất cả users
     */
    public List<CertificateValidationSummary> validateAllCertificates() {
        try {
            List<UserSignatureProfile> profiles = userSignatureProfileRepository.findAll();
            return profiles.stream()
                    .filter(profile -> profile.getCertificateData() != null)
                    .map(profile -> {
                        try {
                            CertificateValidationService.CertificateValidationResult validation = certificateValidationService
                                    .validateX509Certificate(profile.getCertificateData());

                            CertificateValidationService.CertificateExpirationResult expiration = certificateValidationService
                                    .checkCertificateExpiration(profile.getCertificateData());

                            CertificateValidationSummary summary = new CertificateValidationSummary();
                            summary.setUserId(profile.getUser().getId());
                            summary.setCertificateSerial(profile.getCertificateSerial());
                            summary.setValid(validation.isValid());
                            summary.setExpired(expiration.isExpired());
                            summary.setExpiringSoon(expiration.isExpiringSoon());
                            summary.setErrors(validation.getErrors());
                            summary.setWarnings(validation.getWarnings());

                            return summary;
                        } catch (Exception e) {
                            CertificateValidationSummary summary = new CertificateValidationSummary();
                            summary.setUserId(profile.getUser().getId());
                            summary.setValid(false);
                            summary.addError("Validation failed: " + e.getMessage());
                            return summary;
                        }
                    })
                    .toList();

        } catch (Exception e) {
            throw new IdInvalidException("Không thể validate certificates: " + e.getMessage());
        }
    }

    // Response classes
    public static class CertificateInfoResponse {
        private Integer version;
        private String serialNumber;
        private String issuer;
        private String subject;
        private java.util.Date notBefore;
        private java.util.Date notAfter;
        private String status;
        private long daysUntilExpiry;
        private boolean expired;
        private boolean expiringSoon;
        private LocalDateTime lastValidation;

        public Integer getVersion() {
            return version;
        }

        public void setVersion(Integer version) {
            this.version = version;
        }

        public String getSerialNumber() {
            return serialNumber;
        }

        public void setSerialNumber(String serialNumber) {
            this.serialNumber = serialNumber;
        }

        public String getIssuer() {
            return issuer;
        }

        public void setIssuer(String issuer) {
            this.issuer = issuer;
        }

        public String getSubject() {
            return subject;
        }

        public void setSubject(String subject) {
            this.subject = subject;
        }

        public java.util.Date getNotBefore() {
            return notBefore;
        }

        public void setNotBefore(java.util.Date notBefore) {
            this.notBefore = notBefore;
        }

        public java.util.Date getNotAfter() {
            return notAfter;
        }

        public void setNotAfter(java.util.Date notAfter) {
            this.notAfter = notAfter;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public long getDaysUntilExpiry() {
            return daysUntilExpiry;
        }

        public void setDaysUntilExpiry(long daysUntilExpiry) {
            this.daysUntilExpiry = daysUntilExpiry;
        }

        public boolean isExpired() {
            return expired;
        }

        public void setExpired(boolean expired) {
            this.expired = expired;
        }

        public boolean isExpiringSoon() {
            return expiringSoon;
        }

        public void setExpiringSoon(boolean expiringSoon) {
            this.expiringSoon = expiringSoon;
        }

        public LocalDateTime getLastValidation() {
            return lastValidation;
        }

        public void setLastValidation(LocalDateTime lastValidation) {
            this.lastValidation = lastValidation;
        }
    }

    public static class CertificateValidationSummary {
        private String userId;
        private String certificateSerial;
        private boolean valid;
        private boolean expired;
        private boolean expiringSoon;
        private List<String> errors = new java.util.ArrayList<>();
        private List<String> warnings = new java.util.ArrayList<>();

        // Getters and setters
        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        public String getCertificateSerial() {
            return certificateSerial;
        }

        public void setCertificateSerial(String certificateSerial) {
            this.certificateSerial = certificateSerial;
        }

        public boolean isValid() {
            return valid;
        }

        public void setValid(boolean valid) {
            this.valid = valid;
        }

        public boolean isExpired() {
            return expired;
        }

        public void setExpired(boolean expired) {
            this.expired = expired;
        }

        public boolean isExpiringSoon() {
            return expiringSoon;
        }

        public void setExpiringSoon(boolean expiringSoon) {
            this.expiringSoon = expiringSoon;
        }

        public List<String> getErrors() {
            return errors;
        }

        public void setErrors(List<String> errors) {
            this.errors = errors;
        }

        public List<String> getWarnings() {
            return warnings;
        }

        public void setWarnings(List<String> warnings) {
            this.warnings = warnings;
        }

        public void addError(String error) {
            this.errors.add(error);
        }

        public void addWarning(String warning) {
            this.warnings.add(warning);
        }
    }
}
