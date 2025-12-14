package vn.edu.iuh.fit.innovationmanagementsystem_be.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import vn.edu.iuh.fit.innovationmanagementsystem_be.exception.IdInvalidException;

import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("KeyManagementService Unit Tests")
class KeyManagementServiceTest {

    private KeyManagementService keyManagementService;

    @BeforeEach
    void setUp() {
        keyManagementService = new KeyManagementService();
    }

    // ==================== generateKeyPair Tests ====================

    @Test
    @DisplayName("1. generateKeyPair - Should generate valid RSA 2048-bit key pair")
    void testGenerateKeyPair_Success() {
        KeyPair keyPair = keyManagementService.generateKeyPair();

        assertNotNull(keyPair);
        assertNotNull(keyPair.getPrivate());
        assertNotNull(keyPair.getPublic());
        assertEquals("RSA", keyPair.getPrivate().getAlgorithm());
        assertEquals("RSA", keyPair.getPublic().getAlgorithm());
    }

    @Test
    @DisplayName("2. generateKeyPair - Should generate unique key pairs each time")
    void testGenerateKeyPair_UniqueEachTime() {
        KeyPair keyPair1 = keyManagementService.generateKeyPair();
        KeyPair keyPair2 = keyManagementService.generateKeyPair();

        assertNotEquals(
                Base64.getEncoder().encodeToString(keyPair1.getPrivate().getEncoded()),
                Base64.getEncoder().encodeToString(keyPair2.getPrivate().getEncoded()));
    }

    // ==================== Key Conversion Tests ====================

    @Test
    @DisplayName("3. privateKeyToString & stringToPrivateKey - Round trip conversion")
    void testPrivateKeyToString_And_StringToPrivateKey_RoundTrip() {
        KeyPair keyPair = keyManagementService.generateKeyPair();
        PrivateKey originalPrivateKey = keyPair.getPrivate();

        String privateKeyString = keyManagementService.privateKeyToString(originalPrivateKey);
        PrivateKey convertedPrivateKey = keyManagementService.stringToPrivateKey(privateKeyString);

        assertArrayEquals(originalPrivateKey.getEncoded(), convertedPrivateKey.getEncoded());
    }

    @Test
    @DisplayName("4. publicKeyToString & stringToPublicKey - Round trip conversion")
    void testPublicKeyToString_And_StringToPublicKey_RoundTrip() {
        KeyPair keyPair = keyManagementService.generateKeyPair();
        PublicKey originalPublicKey = keyPair.getPublic();

        String publicKeyString = keyManagementService.publicKeyToString(originalPublicKey);
        PublicKey convertedPublicKey = keyManagementService.stringToPublicKey(publicKeyString);

        assertArrayEquals(originalPublicKey.getEncoded(), convertedPublicKey.getEncoded());
    }

    @Test
    @DisplayName("5. stringToPrivateKey - Should throw exception for invalid input")
    void testStringToPrivateKey_InvalidInput_ThrowsException() {
        String invalidPrivateKey = "invalid-key-data";

        assertThrows(IdInvalidException.class, () -> {
            keyManagementService.stringToPrivateKey(invalidPrivateKey);
        });
    }

    @Test
    @DisplayName("6. stringToPublicKey - Should throw exception for invalid input")
    void testStringToPublicKey_InvalidInput_ThrowsException() {
        String invalidPublicKey = "invalid-key-data";

        assertThrows(IdInvalidException.class, () -> {
            keyManagementService.stringToPublicKey(invalidPublicKey);
        });
    }

    // ==================== generateDocumentHash Tests ====================

    @Test
    @DisplayName("7. generateDocumentHash - Same content should produce same hash")
    void testGenerateDocumentHash_SameContent_SameHash() {
        byte[] content = "Document content for testing".getBytes(StandardCharsets.UTF_8);

        String hash1 = keyManagementService.generateDocumentHash(content);
        String hash2 = keyManagementService.generateDocumentHash(content);

        assertEquals(hash1, hash2);
    }

    @Test
    @DisplayName("8. generateDocumentHash - Different content should produce different hash")
    void testGenerateDocumentHash_DifferentContent_DifferentHash() {
        byte[] content1 = "Document content 1".getBytes(StandardCharsets.UTF_8);
        byte[] content2 = "Document content 2".getBytes(StandardCharsets.UTF_8);

        String hash1 = keyManagementService.generateDocumentHash(content1);
        String hash2 = keyManagementService.generateDocumentHash(content2);

        assertNotEquals(hash1, hash2);
    }

    @Test
    @DisplayName("9. generateDocumentHash - Should return SHA-256 hash (32 bytes)")
    void testGenerateDocumentHash_ReturnsSHA256Hash() {
        byte[] content = "Test content".getBytes(StandardCharsets.UTF_8);

        String hash = keyManagementService.generateDocumentHash(content);
        byte[] decodedHash = Base64.getDecoder().decode(hash);

        assertEquals(32, decodedHash.length, "SHA-256 hash should be 32 bytes");
    }

    // ==================== Digital Signature Tests ====================

    @Test
    @DisplayName("10. generateSignature & verifySignature - Valid signature should verify successfully")
    void testGenerateSignature_And_VerifySignature_Success() {
        KeyPair keyPair = keyManagementService.generateKeyPair();
        String privateKeyString = keyManagementService.privateKeyToString(keyPair.getPrivate());
        String publicKeyString = keyManagementService.publicKeyToString(keyPair.getPublic());

        byte[] documentContent = "Important document content".getBytes(StandardCharsets.UTF_8);
        String documentHash = keyManagementService.generateDocumentHash(documentContent);

        String signature = keyManagementService.generateSignature(documentHash, privateKeyString);
        boolean isValid = keyManagementService.verifySignature(documentHash, signature, publicKeyString);

        assertTrue(isValid, "Valid signature should be verified successfully");
    }

    @Test
    @DisplayName("11. verifySignature - Wrong public key should return false")
    void testVerifySignature_WrongPublicKey_ReturnsFalse() {
        KeyPair keyPair1 = keyManagementService.generateKeyPair();
        KeyPair keyPair2 = keyManagementService.generateKeyPair();

        String privateKeyString1 = keyManagementService.privateKeyToString(keyPair1.getPrivate());
        String publicKeyString2 = keyManagementService.publicKeyToString(keyPair2.getPublic());

        byte[] documentContent = "Document content".getBytes(StandardCharsets.UTF_8);
        String documentHash = keyManagementService.generateDocumentHash(documentContent);
        String signature = keyManagementService.generateSignature(documentHash, privateKeyString1);

        boolean isValid = keyManagementService.verifySignature(documentHash, signature, publicKeyString2);

        assertFalse(isValid, "Wrong public key should fail verification");
    }

    @Test
    @DisplayName("12. verifySignature - Tampered document should return false")
    void testVerifySignature_TamperedDocument_ReturnsFalse() {
        KeyPair keyPair = keyManagementService.generateKeyPair();
        String privateKeyString = keyManagementService.privateKeyToString(keyPair.getPrivate());
        String publicKeyString = keyManagementService.publicKeyToString(keyPair.getPublic());

        byte[] originalContent = "Original document content".getBytes(StandardCharsets.UTF_8);
        byte[] tamperedContent = "Tampered document content".getBytes(StandardCharsets.UTF_8);

        String originalHash = keyManagementService.generateDocumentHash(originalContent);
        String tamperedHash = keyManagementService.generateDocumentHash(tamperedContent);
        String signature = keyManagementService.generateSignature(originalHash, privateKeyString);

        boolean isValid = keyManagementService.verifySignature(tamperedHash, signature, publicKeyString);

        assertFalse(isValid, "Tampered document should fail verification");
    }

    @Test
    @DisplayName("13. verifySignature - Invalid signature should return false")
    void testVerifySignature_InvalidSignature_ReturnsFalse() {
        KeyPair keyPair = keyManagementService.generateKeyPair();
        String publicKeyString = keyManagementService.publicKeyToString(keyPair.getPublic());

        byte[] documentContent = "Document content".getBytes(StandardCharsets.UTF_8);
        String documentHash = keyManagementService.generateDocumentHash(documentContent);
        String invalidSignature = Base64.getEncoder().encodeToString("invalid-signature".getBytes());

        boolean isValid = keyManagementService.verifySignature(documentHash, invalidSignature, publicKeyString);

        assertFalse(isValid, "Invalid signature should fail verification");
    }

    // ==================== generateCertificateSerial Tests ====================

    @Test
    @DisplayName("14. generateCertificateSerial - Should return unique serial each time")
    void testGenerateCertificateSerial_UniqueValues() {
        String serial1 = keyManagementService.generateCertificateSerial();
        String serial2 = keyManagementService.generateCertificateSerial();

        assertNotEquals(serial1, serial2, "Each serial should be unique");
    }

    @Test
    @DisplayName("15. generateCertificateSerial - Should have correct format CERT-{timestamp}-{random}")
    void testGenerateCertificateSerial_CorrectFormat() {
        String serial = keyManagementService.generateCertificateSerial();

        assertTrue(serial.startsWith("CERT-"), "Serial should start with CERT-");
        assertTrue(serial.matches("CERT-\\d+-\\d+"), "Serial should match format CERT-{timestamp}-{random}");
    }
}
