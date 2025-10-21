package vn.edu.iuh.fit.innovationmanagementsystem_be.service;

import org.springframework.stereotype.Service;
import vn.edu.iuh.fit.innovationmanagementsystem_be.exception.IdInvalidException;

import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Service
public class KeyManagementService {

    private static final String ALGORITHM = "RSA";
    private static final int KEY_SIZE = 2048;
    private static final String SIGNATURE_ALGORITHM = "SHA256withRSA";

    // 1. Tạo cặp khóa
    public KeyPair generateKeyPair() {
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(ALGORITHM);
            keyPairGenerator.initialize(KEY_SIZE);
            return keyPairGenerator.generateKeyPair();
        } catch (NoSuchAlgorithmException e) {
            throw new IdInvalidException("Không thể tạo cặp khóa: " + e.getMessage());
        }
    }

    // 2. Chuyển PrivateKey sang Base64
    public String privateKeyToString(PrivateKey privateKey) {
        return Base64.getEncoder().encodeToString(privateKey.getEncoded());
    }

    // 3. Chuyển PublicKey sang Base64
    public String publicKeyToString(PublicKey publicKey) {
        return Base64.getEncoder().encodeToString(publicKey.getEncoded());
    }

    // 4. Chuyển Base64 sang PrivateKey
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

    // 5. Chuyển Base64 sang PublicKey
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

    // 6. Tạo hash SHA-256 cho nội dung file
    public String generateDocumentHash(byte[] fileContent) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(fileContent);
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IdInvalidException("Không thể tạo hash: " + e.getMessage());
        }
    }

    // 7. Tạo chữ ký từ hash của file
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

    // 8. Xác thực chữ ký
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

    // 9. Tạo serial certificate
    public String generateCertificateSerial() {
        return "CERT-" + System.currentTimeMillis() + "-" + (int) (Math.random() * 10000);
    }

}
