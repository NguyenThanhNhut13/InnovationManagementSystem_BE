package vn.edu.iuh.fit.innovationmanagementsystem_be.service;

import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.KeyUsage;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import vn.edu.iuh.fit.innovationmanagementsystem_be.exception.IdInvalidException;

import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.Security;
import java.security.cert.X509Certificate;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("CertificateValidationService Unit Tests")
class CertificateValidationServiceTest {

    private CertificateValidationService certificateValidationService;

    @BeforeAll
    static void setUpBouncyCastle() {
        Security.addProvider(new BouncyCastleProvider());
    }

    @BeforeEach
    void setUp() {
        certificateValidationService = new CertificateValidationService();
    }

    // ==================== Helper Methods ====================

    private X509Certificate generateSelfSignedCertificate(KeyPair keyPair, String cn, int validDays) throws Exception {
        Instant now = Instant.now();
        Date notBefore = Date.from(now);
        Date notAfter = Date.from(now.plus(validDays, ChronoUnit.DAYS));

        X500Name issuer = new X500Name("CN=" + cn + ", O=Test Org, C=VN");
        BigInteger serial = BigInteger.valueOf(System.currentTimeMillis());

        X509v3CertificateBuilder certBuilder = new JcaX509v3CertificateBuilder(
                issuer,
                serial,
                notBefore,
                notAfter,
                issuer,
                keyPair.getPublic());

        // Add extensions
        certBuilder.addExtension(Extension.basicConstraints, true, new BasicConstraints(true));
        certBuilder.addExtension(Extension.keyUsage, true,
                new KeyUsage(KeyUsage.digitalSignature | KeyUsage.keyCertSign));

        ContentSigner signer = new JcaContentSignerBuilder("SHA256WithRSA")
                .setProvider("BC")
                .build(keyPair.getPrivate());

        X509CertificateHolder certHolder = certBuilder.build(signer);
        return new JcaX509CertificateConverter()
                .setProvider("BC")
                .getCertificate(certHolder);
    }

    private X509Certificate generateExpiredCertificate(KeyPair keyPair) throws Exception {
        Instant past = Instant.now().minus(60, ChronoUnit.DAYS);
        Date notBefore = Date.from(past);
        Date notAfter = Date.from(past.plus(30, ChronoUnit.DAYS));

        X500Name issuer = new X500Name("CN=Expired Cert, O=Test Org, C=VN");
        BigInteger serial = BigInteger.valueOf(System.currentTimeMillis());

        X509v3CertificateBuilder certBuilder = new JcaX509v3CertificateBuilder(
                issuer,
                serial,
                notBefore,
                notAfter,
                issuer,
                keyPair.getPublic());

        certBuilder.addExtension(Extension.basicConstraints, true, new BasicConstraints(true));
        certBuilder.addExtension(Extension.keyUsage, true,
                new KeyUsage(KeyUsage.digitalSignature | KeyUsage.keyCertSign));

        ContentSigner signer = new JcaContentSignerBuilder("SHA256WithRSA")
                .setProvider("BC")
                .build(keyPair.getPrivate());

        X509CertificateHolder certHolder = certBuilder.build(signer);
        return new JcaX509CertificateConverter()
                .setProvider("BC")
                .getCertificate(certHolder);
    }

    private String certificateToBase64(X509Certificate cert) throws Exception {
        return Base64.getEncoder().encodeToString(cert.getEncoded());
    }

    // ==================== validateX509Certificate Tests ====================

    @Test
    @DisplayName("1. validateX509Certificate - Valid certificate should pass validation")
    void testValidateX509Certificate_ValidCert_Success() throws Exception {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        KeyPair keyPair = keyGen.generateKeyPair();

        X509Certificate cert = generateSelfSignedCertificate(keyPair, "Valid Cert", 365);
        String certBase64 = certificateToBase64(cert);

        CertificateValidationService.CertificateValidationResult result = certificateValidationService
                .validateX509Certificate(certBase64);

        assertNotNull(result);
        assertTrue(result.isValid(), "Valid certificate should pass validation");
        assertTrue(result.getErrors().isEmpty(), "No errors expected");
    }

    @Test
    @DisplayName("2. validateX509Certificate - Expired certificate should have errors")
    void testValidateX509Certificate_ExpiredCert_ReturnsErrors() throws Exception {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        KeyPair keyPair = keyGen.generateKeyPair();

        X509Certificate expiredCert = generateExpiredCertificate(keyPair);
        String certBase64 = certificateToBase64(expiredCert);

        CertificateValidationService.CertificateValidationResult result = certificateValidationService
                .validateX509Certificate(certBase64);

        assertNotNull(result);
        assertFalse(result.isValid(), "Expired certificate should not be valid");
        assertFalse(result.getErrors().isEmpty(), "Should have expiration error");
    }

    @Test
    @DisplayName("3. validateX509Certificate - Null input should throw exception")
    void testValidateX509Certificate_NullInput_ThrowsException() {
        assertThrows(IdInvalidException.class, () -> {
            certificateValidationService.validateX509Certificate(null);
        });
    }

    @Test
    @DisplayName("4. validateX509Certificate - Empty input should throw exception")
    void testValidateX509Certificate_EmptyInput_ThrowsException() {
        assertThrows(IdInvalidException.class, () -> {
            certificateValidationService.validateX509Certificate("");
        });
    }

    @Test
    @DisplayName("5. validateX509Certificate - Invalid Base64 should throw exception")
    void testValidateX509Certificate_InvalidFormat_ThrowsException() {
        assertThrows(Exception.class, () -> {
            certificateValidationService.validateX509Certificate("not-valid-base64!!!");
        });
    }

    // ==================== checkCertificateExpiration Tests ====================

    @Test
    @DisplayName("6. checkCertificateExpiration - Valid certificate should not be expired")
    void testCheckCertificateExpiration_NotExpired_Success() throws Exception {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        KeyPair keyPair = keyGen.generateKeyPair();

        X509Certificate cert = generateSelfSignedCertificate(keyPair, "Long Valid Cert", 365);
        String certBase64 = certificateToBase64(cert);

        CertificateValidationService.CertificateExpirationResult result = certificateValidationService
                .checkCertificateExpiration(certBase64);

        assertNotNull(result);
        assertFalse(result.isExpired(), "Certificate should not be expired");
        assertFalse(result.isExpiringSoon(), "Certificate should not be expiring soon");
        assertTrue(result.getDaysUntilExpiry() > 30, "Should have more than 30 days until expiry");
    }

    @Test
    @DisplayName("7. checkCertificateExpiration - Certificate expiring soon should have warning")
    void testCheckCertificateExpiration_ExpiringSoon_Warning() throws Exception {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        KeyPair keyPair = keyGen.generateKeyPair();

        X509Certificate cert = generateSelfSignedCertificate(keyPair, "Soon Expiring", 15);
        String certBase64 = certificateToBase64(cert);

        CertificateValidationService.CertificateExpirationResult result = certificateValidationService
                .checkCertificateExpiration(certBase64);

        assertNotNull(result);
        assertFalse(result.isExpired(), "Certificate should not be expired yet");
        assertTrue(result.isExpiringSoon(), "Certificate should be expiring soon");
        assertFalse(result.getWarnings().isEmpty(), "Should have warning about expiring soon");
    }

    @Test
    @DisplayName("8. checkCertificateExpiration - Expired certificate should have error")
    void testCheckCertificateExpiration_Expired_Error() throws Exception {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        KeyPair keyPair = keyGen.generateKeyPair();

        X509Certificate expiredCert = generateExpiredCertificate(keyPair);
        String certBase64 = certificateToBase64(expiredCert);

        CertificateValidationService.CertificateExpirationResult result = certificateValidationService
                .checkCertificateExpiration(certBase64);

        assertNotNull(result);
        assertTrue(result.isExpired(), "Certificate should be expired");
        assertFalse(result.getErrors().isEmpty(), "Should have expiration error");
    }

    // ==================== extractCertificateInfo Tests ====================

    @Test
    @DisplayName("9. extractCertificateInfo - Should extract all certificate details")
    void testExtractCertificateInfo_Success() throws Exception {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        KeyPair keyPair = keyGen.generateKeyPair();

        X509Certificate cert = generateSelfSignedCertificate(keyPair, "Test Subject", 365);
        String certBase64 = certificateToBase64(cert);

        CertificateValidationService.CertificateInfo info = certificateValidationService
                .extractCertificateInfo(certBase64);

        assertNotNull(info);
        assertNotNull(info.getVersion());
        assertNotNull(info.getSerialNumber());
        assertNotNull(info.getIssuer());
        assertNotNull(info.getSubject());
        assertNotNull(info.getNotBefore());
        assertNotNull(info.getNotAfter());
        assertNotNull(info.getPublicKey());
        assertTrue(info.getSubject().contains("Test Subject"), "Subject should contain provided CN");
    }

    @Test
    @DisplayName("10. extractCertificateInfo - Null input should throw exception")
    void testExtractCertificateInfo_NullInput_ThrowsException() {
        assertThrows(IdInvalidException.class, () -> {
            certificateValidationService.extractCertificateInfo(null);
        });
    }

    @Test
    @DisplayName("11. extractCertificateInfo - Text description should throw exception")
    void testExtractCertificateInfo_TextDescription_ThrowsException() {
        String fakeData = Base64.getEncoder().encodeToString(
                "Certificate Data for test user".getBytes());

        IdInvalidException exception = assertThrows(IdInvalidException.class, () -> {
            certificateValidationService.extractCertificateInfo(fakeData);
        });

        assertTrue(exception.getMessage().contains("text description"));
    }
}
