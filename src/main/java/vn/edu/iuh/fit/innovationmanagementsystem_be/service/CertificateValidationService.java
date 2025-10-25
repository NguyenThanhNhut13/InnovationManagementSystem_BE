package vn.edu.iuh.fit.innovationmanagementsystem_be.service;

import org.springframework.stereotype.Service;
import vn.edu.iuh.fit.innovationmanagementsystem_be.exception.IdInvalidException;

import java.io.ByteArrayInputStream;
import java.security.KeyStore;
import java.security.cert.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

@Service
public class CertificateValidationService {

    /**
     * X.509 Certificate Validation
     * Kiểm tra tính hợp lệ của certificate
     */
    public CertificateValidationResult validateX509Certificate(String certificateData) {
        try {
            // Decode certificate từ Base64
            byte[] certBytes = Base64.getDecoder().decode(certificateData);
            CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
            X509Certificate certificate = (X509Certificate) certFactory.generateCertificate(
                    new ByteArrayInputStream(certBytes));

            CertificateValidationResult result = new CertificateValidationResult();
            result.setCertificate(certificate);
            result.setValid(true);

            // 1. Kiểm tra thời gian hết hạn
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime notBefore = certificate.getNotBefore().toInstant()
                    .atZone(ZoneId.systemDefault()).toLocalDateTime();
            LocalDateTime notAfter = certificate.getNotAfter().toInstant()
                    .atZone(ZoneId.systemDefault()).toLocalDateTime();

            if (now.isBefore(notBefore)) {
                result.setValid(false);
                result.addError("Certificate chưa có hiệu lực. Có hiệu lực từ: " + notBefore);
            }

            if (now.isAfter(notAfter)) {
                result.setValid(false);
                result.addError("Certificate đã hết hạn. Hết hạn vào: " + notAfter);
            }

            // 2. Kiểm tra signature của certificate
            try {
                certificate.verify(certificate.getPublicKey());
            } catch (Exception e) {
                result.setValid(false);
                result.addError("Certificate signature không hợp lệ: " + e.getMessage());
            }

            // 3. Kiểm tra key usage
            boolean[] keyUsage = certificate.getKeyUsage();
            if (keyUsage != null && keyUsage.length > 0) {
                // Kiểm tra Digital Signature bit (bit 0)
                if (!keyUsage[0]) {
                    result.setValid(false);
                    result.addError("Certificate không được phép sử dụng cho Digital Signature");
                }
            }

            // 4. Kiểm tra Extended Key Usage (nếu có)
            try {
                List<String> extendedKeyUsage = certificate.getExtendedKeyUsage();
                if (extendedKeyUsage != null && !extendedKeyUsage.isEmpty()) {
                    if (!extendedKeyUsage.contains("1.3.6.1.5.5.7.3.4")) { // emailProtection
                        result.addWarning("Certificate không có Extended Key Usage cho email protection");
                    }
                }
            } catch (Exception e) {
                // Extended Key Usage không bắt buộc
            }

            return result;

        } catch (Exception e) {
            throw new IdInvalidException("Không thể validate certificate: " + e.getMessage());
        }
    }

    /**
     * Certificate Chain Verification
     * Kiểm tra chuỗi certificate từ root CA
     */
    public CertificateChainValidationResult validateCertificateChain(
            String certificateData,
            List<String> intermediateCerts,
            String rootCert) {

        try {
            CertificateFactory certFactory = CertificateFactory.getInstance("X.509");

            // Tạo certificate chain
            List<X509Certificate> chain = new ArrayList<>();

            // Add end entity certificate
            byte[] certBytes = Base64.getDecoder().decode(certificateData);
            X509Certificate endCert = (X509Certificate) certFactory.generateCertificate(
                    new ByteArrayInputStream(certBytes));
            chain.add(endCert);

            // Add intermediate certificates
            for (String intermediateCert : intermediateCerts) {
                byte[] intermediateBytes = Base64.getDecoder().decode(intermediateCert);
                X509Certificate intermediate = (X509Certificate) certFactory.generateCertificate(
                        new ByteArrayInputStream(intermediateBytes));
                chain.add(intermediate);
            }

            // Add root certificate
            byte[] rootBytes = Base64.getDecoder().decode(rootCert);
            X509Certificate root = (X509Certificate) certFactory.generateCertificate(
                    new ByteArrayInputStream(rootBytes));
            chain.add(root);

            CertificateChainValidationResult result = new CertificateChainValidationResult();
            result.setValid(true);

            // Verify certificate chain
            try {
                // Tạo trust store với root certificate
                KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
                trustStore.load(null, null);
                trustStore.setCertificateEntry("root", root);

                // Tạo certificate path validator
                CertPathValidator validator = CertPathValidator.getInstance("PKIX");
                PKIXParameters params = new PKIXParameters(trustStore);
                params.setRevocationEnabled(false); // Tạm thời disable CRL checking

                // Tạo certificate path
                CertPath certPath = certFactory.generateCertPath(chain);

                // Validate certificate path
                PKIXCertPathValidatorResult validationResult = (PKIXCertPathValidatorResult) validator
                        .validate(certPath, params);

                result.setValid(true);
                result.setTrustAnchor(validationResult.getTrustAnchor());

            } catch (Exception e) {
                result.setValid(false);
                result.addError("Certificate chain validation failed: " + e.getMessage());
            }

            return result;

        } catch (Exception e) {
            throw new IdInvalidException("Không thể validate certificate chain: " + e.getMessage());
        }
    }

    /**
     * Certificate Expiration Checking
     * Kiểm tra thời gian hết hạn của certificate
     */
    public CertificateExpirationResult checkCertificateExpiration(String certificateData) {
        try {
            byte[] certBytes = Base64.getDecoder().decode(certificateData);
            CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
            X509Certificate certificate = (X509Certificate) certFactory.generateCertificate(
                    new ByteArrayInputStream(certBytes));

            CertificateExpirationResult result = new CertificateExpirationResult();
            result.setCertificate(certificate);

            LocalDateTime now = LocalDateTime.now();
            LocalDateTime notAfter = certificate.getNotAfter().toInstant()
                    .atZone(ZoneId.systemDefault()).toLocalDateTime();

            long daysUntilExpiry = java.time.Duration.between(now, notAfter).toDays();

            result.setDaysUntilExpiry(daysUntilExpiry);
            result.setExpired(daysUntilExpiry <= 0);
            result.setExpiringSoon(daysUntilExpiry <= 30); // Cảnh báo nếu còn 30 ngày

            if (result.isExpired()) {
                result.addError("Certificate đã hết hạn");
            } else if (result.isExpiringSoon()) {
                result.addWarning("Certificate sẽ hết hạn trong " + daysUntilExpiry + " ngày");
            }

            return result;

        } catch (Exception e) {
            throw new IdInvalidException("Không thể kiểm tra thời gian hết hạn: " + e.getMessage());
        }
    }

    /**
     * Extract certificate information
     */
    public CertificateInfo extractCertificateInfo(String certificateData) {
        try {
            byte[] certBytes = Base64.getDecoder().decode(certificateData);
            CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
            X509Certificate certificate = (X509Certificate) certFactory.generateCertificate(
                    new ByteArrayInputStream(certBytes));

            CertificateInfo info = new CertificateInfo();
            info.setSubject(certificate.getSubjectX500Principal().toString());
            info.setIssuer(certificate.getIssuerX500Principal().toString());
            info.setSerialNumber(certificate.getSerialNumber().toString());
            info.setNotBefore(certificate.getNotBefore());
            info.setNotAfter(certificate.getNotAfter());
            info.setPublicKey(certificate.getPublicKey());

            return info;

        } catch (Exception e) {
            throw new IdInvalidException("Không thể extract certificate info: " + e.getMessage());
        }
    }

    // Inner classes for results
    public static class CertificateValidationResult {
        private X509Certificate certificate;
        private boolean valid;
        private List<String> errors = new ArrayList<>();
        private List<String> warnings = new ArrayList<>();

        // Getters and setters
        public X509Certificate getCertificate() {
            return certificate;
        }

        public void setCertificate(X509Certificate certificate) {
            this.certificate = certificate;
        }

        public boolean isValid() {
            return valid;
        }

        public void setValid(boolean valid) {
            this.valid = valid;
        }

        public List<String> getErrors() {
            return errors;
        }

        public List<String> getWarnings() {
            return warnings;
        }

        public void addError(String error) {
            this.errors.add(error);
        }

        public void addWarning(String warning) {
            this.warnings.add(warning);
        }
    }

    public static class CertificateChainValidationResult {
        private boolean valid;
        private TrustAnchor trustAnchor;
        private List<String> errors = new ArrayList<>();

        // Getters and setters
        public boolean isValid() {
            return valid;
        }

        public void setValid(boolean valid) {
            this.valid = valid;
        }

        public TrustAnchor getTrustAnchor() {
            return trustAnchor;
        }

        public void setTrustAnchor(TrustAnchor trustAnchor) {
            this.trustAnchor = trustAnchor;
        }

        public List<String> getErrors() {
            return errors;
        }

        public void addError(String error) {
            this.errors.add(error);
        }
    }

    public static class CertificateExpirationResult {
        private X509Certificate certificate;
        private long daysUntilExpiry;
        private boolean expired;
        private boolean expiringSoon;
        private List<String> errors = new ArrayList<>();
        private List<String> warnings = new ArrayList<>();

        // Getters and setters
        public X509Certificate getCertificate() {
            return certificate;
        }

        public void setCertificate(X509Certificate certificate) {
            this.certificate = certificate;
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

        public List<String> getErrors() {
            return errors;
        }

        public List<String> getWarnings() {
            return warnings;
        }

        public void addError(String error) {
            this.errors.add(error);
        }

        public void addWarning(String warning) {
            this.warnings.add(warning);
        }
    }

    public static class CertificateInfo {
        private String subject;
        private String issuer;
        private String serialNumber;
        private Date notBefore;
        private Date notAfter;
        private java.security.PublicKey publicKey;

        // Getters and setters
        public String getSubject() {
            return subject;
        }

        public void setSubject(String subject) {
            this.subject = subject;
        }

        public String getIssuer() {
            return issuer;
        }

        public void setIssuer(String issuer) {
            this.issuer = issuer;
        }

        public String getSerialNumber() {
            return serialNumber;
        }

        public void setSerialNumber(String serialNumber) {
            this.serialNumber = serialNumber;
        }

        public Date getNotBefore() {
            return notBefore;
        }

        public void setNotBefore(Date notBefore) {
            this.notBefore = notBefore;
        }

        public Date getNotAfter() {
            return notAfter;
        }

        public void setNotAfter(Date notAfter) {
            this.notAfter = notAfter;
        }

        public java.security.PublicKey getPublicKey() {
            return publicKey;
        }

        public void setPublicKey(java.security.PublicKey publicKey) {
            this.publicKey = publicKey;
        }
    }
}
