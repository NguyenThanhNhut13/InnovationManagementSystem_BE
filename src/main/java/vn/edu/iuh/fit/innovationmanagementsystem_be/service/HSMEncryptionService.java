package vn.edu.iuh.fit.innovationmanagementsystem_be.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import vn.edu.iuh.fit.innovationmanagementsystem_be.exception.IdInvalidException;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

@Service
public class HSMEncryptionService {

    @Value("${hsm.master.key}")
    private String masterKey;

    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int GCM_IV_LENGTH = 12;
    private static final int GCM_TAG_LENGTH = 16;

    /**
     * Mã hóa private key trước khi lưu vào database
     * Sử dụng AES-GCM encryption với master key
     */
    public String encryptPrivateKey(String privateKey) {
        try {
            SecretKey secretKey = getSecretKey();

            // Tạo IV ngẫu nhiên
            byte[] iv = new byte[GCM_IV_LENGTH];
            new SecureRandom().nextBytes(iv);

            // Khởi tạo cipher
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, gcmSpec);

            // Mã hóa private key
            byte[] encryptedBytes = cipher.doFinal(privateKey.getBytes(StandardCharsets.UTF_8));

            // Kết hợp IV + encrypted data
            byte[] combined = new byte[GCM_IV_LENGTH + encryptedBytes.length];
            System.arraycopy(iv, 0, combined, 0, GCM_IV_LENGTH);
            System.arraycopy(encryptedBytes, 0, combined, GCM_IV_LENGTH, encryptedBytes.length);

            return Base64.getEncoder().encodeToString(combined);
        } catch (Exception e) {
            throw new IdInvalidException("Không thể mã hóa private key: " + e.getMessage());
        }
    }

    /**
     * Giải mã private key từ database
     */
    public String decryptPrivateKey(String encryptedPrivateKey) {
        try {
            SecretKey secretKey = getSecretKey();

            // Decode base64
            byte[] combined = Base64.getDecoder().decode(encryptedPrivateKey);

            // Tách IV và encrypted data
            byte[] iv = new byte[GCM_IV_LENGTH];
            byte[] encryptedBytes = new byte[combined.length - GCM_IV_LENGTH];
            System.arraycopy(combined, 0, iv, 0, GCM_IV_LENGTH);
            System.arraycopy(combined, GCM_IV_LENGTH, encryptedBytes, 0, encryptedBytes.length);

            // Khởi tạo cipher
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, gcmSpec);

            // Giải mã
            byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
            return new String(decryptedBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new IdInvalidException("Không thể giải mã private key: " + e.getMessage());
        }
    }

    /**
     * Tạo master key mới (chỉ dùng cho setup ban đầu)
     */
    public String generateMasterKey() {
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance(ALGORITHM);
            keyGenerator.init(256); // 256-bit key
            SecretKey key = keyGenerator.generateKey();
            return Base64.getEncoder().encodeToString(key.getEncoded());
        } catch (Exception e) {
            throw new IdInvalidException("Không thể tạo master key: " + e.getMessage());
        }
    }

    /**
     * Hash master key để lưu trữ an toàn
     */
    public String hashMasterKey(String masterKey) {
        try {
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(masterKey.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            throw new IdInvalidException("Không thể hash master key: " + e.getMessage());
        }
    }

    private SecretKey getSecretKey() {
        try {
            byte[] keyBytes = Base64.getDecoder().decode(masterKey);
            int length = keyBytes.length;
            if (length != 16 && length != 24 && length != 32) {
                throw new IdInvalidException(
                        "Master key phải có độ dài 128/192/256-bit sau khi decode, hiện tại: " + (length * 8) + " bit");
            }
            return new SecretKeySpec(keyBytes, ALGORITHM);
        } catch (IllegalArgumentException e) {
            throw new IdInvalidException("Master key không hợp lệ (không thể decode Base64)");
        }
    }
}
