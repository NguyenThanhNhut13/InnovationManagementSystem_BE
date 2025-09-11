package vn.edu.iuh.fit.innovationmanagementsystem_be.service;

import org.springframework.stereotype.Service;
import vn.edu.iuh.fit.innovationmanagementsystem_be.exception.IdInvalidException;

import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.time.LocalDateTime;
import java.util.Base64;

@Service
public class KeyManagementService {

    private static final String ALGORITHM = "RSA";
    private static final int KEY_SIZE = 2048;
    private static final String SIGNATURE_ALGORITHM = "SHA256withRSA";

    /**
     * Tạo cặp khóa mới cho user
     */
    public KeyPair generateKeyPair() {
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(ALGORITHM);
            keyPairGenerator.initialize(KEY_SIZE);
            return keyPairGenerator.generateKeyPair();
        } catch (NoSuchAlgorithmException e) {
            throw new IdInvalidException("Không thể tạo cặp khóa: " + e.getMessage());
        }
    }

    /**
     * Chuyển đổi PrivateKey thành chuỗi Base64
     */
    public String privateKeyToString(PrivateKey privateKey) {
        return Base64.getEncoder().encodeToString(privateKey.getEncoded());
    }

    /**
     * Chuyển đổi PublicKey thành chuỗi Base64
     */
    public String publicKeyToString(PublicKey publicKey) {
        return Base64.getEncoder().encodeToString(publicKey.getEncoded());
    }

    /**
     * Chuyển đổi chuỗi Base64 thành PrivateKey
     */
    public PrivateKey stringToPrivateKey(String privateKeyString) {
        try {
            byte[] keyBytes = Base64.getDecoder().decode(privateKeyString);
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM);
            return keyFactory.generatePrivate(keySpec);
        } catch (Exception e) {
            throw new IdInvalidException("Không thể chuyển đổi private key: " + e.getMessage());
        }
    }

    /**
     * Chuyển đổi chuỗi Base64 thành PublicKey
     */
    public PublicKey stringToPublicKey(String publicKeyString) {
        try {
            byte[] keyBytes = Base64.getDecoder().decode(publicKeyString);
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM);
            return keyFactory.generatePublic(keySpec);
        } catch (Exception e) {
            throw new IdInvalidException("Không thể chuyển đổi public key: " + e.getMessage());
        }
    }

    /**
     * Tạo hash SHA-256 cho file content
     */
    public String generateDocumentHash(byte[] fileContent) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(fileContent);
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IdInvalidException("Không thể tạo hash: " + e.getMessage());
        }
    }

    /**
     * Tạo chữ ký từ document hash bằng private key của user
     */
    public String generateSignature(String documentHash, String privateKeyString) {
        try {
            PrivateKey privateKey = stringToPrivateKey(privateKeyString);
            java.security.Signature signature = java.security.Signature.getInstance(SIGNATURE_ALGORITHM);
            signature.initSign(privateKey);
            signature.update(documentHash.getBytes());
            byte[] signatureBytes = signature.sign();
            return Base64.getEncoder().encodeToString(signatureBytes);
        } catch (Exception e) {
            throw new IdInvalidException("Không thể tạo chữ ký: " + e.getMessage());
        }
    }

    /**
     * Xác thực chữ ký bằng public key của user
     */
    public boolean verifySignature(String documentHash, String signatureHash, String publicKeyString) {
        try {
            PublicKey publicKey = stringToPublicKey(publicKeyString);
            byte[] signatureBytes = Base64.getDecoder().decode(signatureHash);

            java.security.Signature signature = java.security.Signature.getInstance(SIGNATURE_ALGORITHM);
            signature.initVerify(publicKey);
            signature.update(documentHash.getBytes());
            return signature.verify(signatureBytes);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Tạo chứng thư số giả lập (trong thực tế sẽ lấy từ CA)
     */
    public String generateCertificateSerial() {
        return "CERT-" + System.currentTimeMillis() + "-" + (int) (Math.random() * 10000);
    }

    /**
     * Tạo thông tin chứng thư số
     */
    public LocalDateTime getCertificateValidFrom() {
        return LocalDateTime.now();
    }

    /**
     * Tạo thời gian hết hạn chứng thư số (1 năm)
     */
    public LocalDateTime getCertificateValidTo() {
        return LocalDateTime.now().plusYears(1);
    }
}
