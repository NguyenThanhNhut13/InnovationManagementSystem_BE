package vn.edu.iuh.fit.innovationmanagementsystem_be.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.User;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.UserSignatureProfile;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.UserSignatureProfileRequest;
import vn.edu.iuh.fit.innovationmanagementsystem_be.exception.IdInvalidException;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.UserSignatureProfileRepository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.utils.constants.SignatureConstants;

import java.security.KeyPair;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Service
@Transactional
public class UserSignatureProfileService {

    private final UserSignatureProfileRepository userSignatureProfileRepository;
    private final KeyManagementService keyManagementService;
    private final HSMEncryptionService hsmEncryptionService;
    private final CertificateValidationService certificateValidationService;

    public UserSignatureProfileService(UserSignatureProfileRepository userSignatureProfileRepository,
            KeyManagementService keyManagementService,
            HSMEncryptionService hsmEncryptionService,
            CertificateValidationService certificateValidationService) {
        this.userSignatureProfileRepository = userSignatureProfileRepository;
        this.keyManagementService = keyManagementService;
        this.hsmEncryptionService = hsmEncryptionService;
        this.certificateValidationService = certificateValidationService;
    }

    // 1. Tạo UserSignatureProfile cho user
    public UserSignatureProfile createUserSignatureProfile(UserSignatureProfileRequest request) {
        try {

            if (userSignatureProfileRepository.findByUserId(request.getUserId()).isPresent()) {
                throw new IdInvalidException("User đã có hồ sơ chữ ký số");
            }

            // Tạo cặp khóa mới cho user
            KeyPair keyPair = keyManagementService.generateKeyPair();

            User user = new User();
            user.setId(request.getUserId());

            // Tạo UserSignatureProfile
            UserSignatureProfile signatureProfile = new UserSignatureProfile();
            signatureProfile.setUser(user);
            signatureProfile.setPathUrl(request.getPathUrl());
            // Encrypt private key trước khi lưu
            String privateKeyString = keyManagementService.privateKeyToString(keyPair.getPrivate());
            String encryptedPrivateKey = hsmEncryptionService.encryptPrivateKey(privateKeyString);
            signatureProfile.setEncryptedPrivateKey(encryptedPrivateKey);
            signatureProfile.setPublicKey(keyManagementService.publicKeyToString(keyPair.getPublic()));
            signatureProfile.setCertificateSerial(keyManagementService.generateCertificateSerial());
            signatureProfile.setCertificateIssuer(SignatureConstants.CERTIFICATE_ISSUER);

            return userSignatureProfileRepository.save(signatureProfile);
        } catch (Exception e) {
            throw new IdInvalidException("Không thể tạo hồ sơ chữ ký số cho user: " + e.getMessage());
        }
    }

    // 2. Tạo UserSignatureProfile cho existing user với default path
    public UserSignatureProfile createUserSignatureProfileForExistingUser(String userId) {
        UserSignatureProfileRequest request = new UserSignatureProfileRequest();
        request.setUserId(userId);
        request.setPathUrl(null);

        return createUserSignatureProfile(request);
    }

    // 3. Tạo UserSignatureProfile với certificate từ CA
    public UserSignatureProfile createUserSignatureProfileWithCertificate(String userId, String certificateData,
            String certificateChain) {
        try {
            // Kiểm tra user đã có profile chưa
            if (userSignatureProfileRepository.findByUserId(userId).isPresent()) {
                throw new IdInvalidException("User đã có hồ sơ chữ ký số");
            }

            // Tạo cặp khóa mới
            KeyPair keyPair = keyManagementService.generateKeyPair();

            User user = new User();
            user.setId(userId);

            // Tạo UserSignatureProfile với certificate từ CA
            UserSignatureProfile signatureProfile = new UserSignatureProfile();
            signatureProfile.setUser(user);
            signatureProfile.setPathUrl(null);

            // Encrypt private key
            String privateKeyString = keyManagementService.privateKeyToString(keyPair.getPrivate());
            String encryptedPrivateKey = hsmEncryptionService.encryptPrivateKey(privateKeyString);
            signatureProfile.setEncryptedPrivateKey(encryptedPrivateKey);
            signatureProfile.setPublicKey(keyManagementService.publicKeyToString(keyPair.getPublic()));

            // Set certificate data từ CA
            signatureProfile.setCertificateData(certificateData);
            signatureProfile.setCertificateChain(certificateChain);

            // Extract certificate info
            CertificateValidationService.CertificateInfo certInfo = certificateValidationService
                    .extractCertificateInfo(certificateData);

            signatureProfile.setCertificateSerial(certInfo.getSerialNumber());
            signatureProfile.setCertificateIssuer(certInfo.getIssuer());
            signatureProfile.setCertificateExpiryDate(certInfo.getNotAfter().toInstant()
                    .atZone(ZoneId.systemDefault()).toLocalDateTime());
            signatureProfile.setCertificateStatus("VALID");
            signatureProfile.setLastCertificateValidation(LocalDateTime.now());

            return userSignatureProfileRepository.save(signatureProfile);

        } catch (Exception e) {
            throw new IdInvalidException("Không thể tạo hồ sơ chữ ký số với certificate: " + e.getMessage());
        }
    }

    // 4. Cập nhật certificate cho user đã có profile
    public UserSignatureProfile updateUserCertificate(String userId, String certificateData, String certificateChain) {
        try {
            UserSignatureProfile profile = userSignatureProfileRepository.findByUserId(userId)
                    .orElseThrow(() -> new IdInvalidException("Không tìm thấy user signature profile"));

            // Extract certificate info
            CertificateValidationService.CertificateInfo certInfo = certificateValidationService
                    .extractCertificateInfo(certificateData);

            // Cập nhật certificate data
            profile.setCertificateData(certificateData);
            profile.setCertificateChain(certificateChain);
            profile.setCertificateSerial(certInfo.getSerialNumber());
            profile.setCertificateIssuer(certInfo.getIssuer());
            profile.setCertificateExpiryDate(certInfo.getNotAfter().toInstant()
                    .atZone(ZoneId.systemDefault()).toLocalDateTime());
            profile.setCertificateStatus("VALID");
            profile.setLastCertificateValidation(LocalDateTime.now());

            return userSignatureProfileRepository.save(profile);

        } catch (Exception e) {
            throw new IdInvalidException("Không thể cập nhật certificate: " + e.getMessage());
        }
    }

    // 5. Kiểm tra user có certificate hợp lệ không
    public boolean hasValidCertificate(String userId) {
        try {
            UserSignatureProfile profile = userSignatureProfileRepository.findByUserId(userId)
                    .orElseThrow(() -> new IdInvalidException("Không tìm thấy user signature profile"));

            if (profile.getCertificateData() == null) {
                return false;
            }

            // Validate certificate
            CertificateValidationService.CertificateValidationResult validationResult = certificateValidationService
                    .validateX509Certificate(profile.getCertificateData());

            // Check expiration
            CertificateValidationService.CertificateExpirationResult expirationResult = certificateValidationService
                    .checkCertificateExpiration(profile.getCertificateData());

            return validationResult.isValid() && !expirationResult.isExpired();

        } catch (Exception e) {
            return false;
        }
    }
}
