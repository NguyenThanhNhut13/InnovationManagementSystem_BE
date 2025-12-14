package vn.edu.iuh.fit.innovationmanagementsystem_be.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import vn.edu.iuh.fit.innovationmanagementsystem_be.exception.IdInvalidException;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("HSMEncryptionService Unit Tests")
class HSMEncryptionServiceTest {

    private HSMEncryptionService hsmEncryptionService;

    private static final String VALID_MASTER_KEY = "dGhpcyBpcyBhIDMyIGJ5dGUga2V5IQ=="; // 32 bytes base64

    @BeforeEach
    void setUp() {
        hsmEncryptionService = new HSMEncryptionService();
        // Inject valid 256-bit master key (32 bytes)
        String masterKey256Bit = java.util.Base64.getEncoder().encodeToString(
                "this is a 32 byte key!!!!!!!!!!!".substring(0, 32).getBytes());
        ReflectionTestUtils.setField(hsmEncryptionService, "masterKey", masterKey256Bit);
    }

    // ==================== encryptPrivateKey Tests ====================

    @Test
    @DisplayName("1. encryptPrivateKey - Should encrypt successfully and return Base64 string")
    void testEncryptPrivateKey_Success() {
        String privateKey = "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQC...";

        String encrypted = hsmEncryptionService.encryptPrivateKey(privateKey);

        assertNotNull(encrypted);
        assertFalse(encrypted.isEmpty());
        assertNotEquals(privateKey, encrypted);
        // Verify it's valid Base64
        assertDoesNotThrow(() -> java.util.Base64.getDecoder().decode(encrypted));
    }

    @Test
    @DisplayName("2. encryptPrivateKey - Should produce different results each time due to random IV")
    void testEncryptPrivateKey_DifferentResultsEachTime() {
        String privateKey = "test-private-key-content";

        String encrypted1 = hsmEncryptionService.encryptPrivateKey(privateKey);
        String encrypted2 = hsmEncryptionService.encryptPrivateKey(privateKey);

        assertNotEquals(encrypted1, encrypted2, "Each encryption should produce different results due to random IV");
    }

    // ==================== decryptPrivateKey Tests ====================

    @Test
    @DisplayName("3. decryptPrivateKey - Should decrypt successfully")
    void testDecryptPrivateKey_Success() {
        String originalPrivateKey = "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQC...";

        String encrypted = hsmEncryptionService.encryptPrivateKey(originalPrivateKey);
        String decrypted = hsmEncryptionService.decryptPrivateKey(encrypted);

        assertEquals(originalPrivateKey, decrypted);
    }

    @Test
    @DisplayName("4. Encrypt-Decrypt Round Trip - Should return original value")
    void testEncryptDecrypt_RoundTrip() {
        String[] testCases = {
                "simple-key",
                "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQC...",
                "Khóa riêng tư với ký tự tiếng Việt",
                "Special chars: !@#$%^&*()_+-=[]{}|;':\",./<>?"
        };

        for (String original : testCases) {
            String encrypted = hsmEncryptionService.encryptPrivateKey(original);
            String decrypted = hsmEncryptionService.decryptPrivateKey(encrypted);
            assertEquals(original, decrypted, "Round trip should preserve original value for: " + original);
        }
    }

    @Test
    @DisplayName("5. decryptPrivateKey - Should throw exception for invalid input")
    void testDecryptPrivateKey_InvalidInput_ThrowsException() {
        String invalidEncryptedData = "invalid-base64-data!!!";

        assertThrows(Exception.class, () -> {
            hsmEncryptionService.decryptPrivateKey(invalidEncryptedData);
        });
    }

    // ==================== generateMasterKey Tests ====================

    @Test
    @DisplayName("6. generateMasterKey - Should return 256-bit key (32 bytes after decode)")
    void testGenerateMasterKey_Returns256BitKey() {
        String masterKey = hsmEncryptionService.generateMasterKey();

        assertNotNull(masterKey);
        byte[] decoded = java.util.Base64.getDecoder().decode(masterKey);
        assertEquals(32, decoded.length, "Master key should be 256-bit (32 bytes)");
    }

    @Test
    @DisplayName("7. generateMasterKey - Should generate unique keys each time")
    void testGenerateMasterKey_UniqueEachTime() {
        String key1 = hsmEncryptionService.generateMasterKey();
        String key2 = hsmEncryptionService.generateMasterKey();

        assertNotEquals(key1, key2, "Each generated master key should be unique");
    }

    // ==================== hashMasterKey Tests ====================

    @Test
    @DisplayName("8. hashMasterKey - Should return consistent hash for same input")
    void testHashMasterKey_ReturnsConsistentHash() {
        String masterKey = "test-master-key";

        String hash1 = hsmEncryptionService.hashMasterKey(masterKey);
        String hash2 = hsmEncryptionService.hashMasterKey(masterKey);

        assertEquals(hash1, hash2, "Same input should produce same hash");
    }

    @Test
    @DisplayName("9. hashMasterKey - Should return different hashes for different inputs")
    void testHashMasterKey_DifferentInputsDifferentHashes() {
        String hash1 = hsmEncryptionService.hashMasterKey("master-key-1");
        String hash2 = hsmEncryptionService.hashMasterKey("master-key-2");

        assertNotEquals(hash1, hash2, "Different inputs should produce different hashes");
    }

    @Test
    @DisplayName("10. hashMasterKey - Should return SHA-256 hash (32 bytes)")
    void testHashMasterKey_ReturnsSHA256Hash() {
        String hash = hsmEncryptionService.hashMasterKey("any-master-key");

        byte[] decoded = java.util.Base64.getDecoder().decode(hash);
        assertEquals(32, decoded.length, "SHA-256 hash should be 32 bytes");
    }
}
