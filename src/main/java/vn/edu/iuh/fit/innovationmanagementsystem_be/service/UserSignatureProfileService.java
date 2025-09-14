package vn.edu.iuh.fit.innovationmanagementsystem_be.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.User;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.UserSignatureProfile;
import vn.edu.iuh.fit.innovationmanagementsystem_be.exception.IdInvalidException;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.UserSignatureProfileRepository;

import java.security.KeyPair;

@Service
@Transactional
public class UserSignatureProfileService {

    private final UserSignatureProfileRepository userSignatureProfileRepository;
    private final KeyManagementService keyManagementService;

    public UserSignatureProfileService(UserSignatureProfileRepository userSignatureProfileRepository,
            KeyManagementService keyManagementService) {
        this.userSignatureProfileRepository = userSignatureProfileRepository;
        this.keyManagementService = keyManagementService;
    }

    // 1. Find UserSignatureProfile by current user (business logic)
    public UserSignatureProfile findByCurrentUser(User currentUser) {
        return userSignatureProfileRepository.findByUserId(currentUser.getId())
                .orElseThrow(() -> new IdInvalidException("Người dùng chưa có hồ sơ chữ ký số"));
    }

    // 2. Create UserSignatureProfile for user
    public UserSignatureProfile createUserSignatureProfile(User user) {
        try {
            // Tạo cặp khóa mới cho user
            KeyPair keyPair = keyManagementService.generateKeyPair();

            // Tạo UserSignatureProfile
            UserSignatureProfile signatureProfile = new UserSignatureProfile();
            signatureProfile.setUser(user);
            signatureProfile.setPrivateKey(keyManagementService.privateKeyToString(keyPair.getPrivate()));
            signatureProfile.setPublicKey(keyManagementService.publicKeyToString(keyPair.getPublic()));
            signatureProfile.setCertificateSerial(keyManagementService.generateCertificateSerial());
            signatureProfile.setCertificateIssuer("IUH Innovation Management System");
            signatureProfile.setCertificateValidFrom(keyManagementService.getCertificateValidFrom());
            signatureProfile.setCertificateValidTo(keyManagementService.getCertificateValidTo());
            signatureProfile.setPathUrl("/signatures/" + user.getId());

            return userSignatureProfileRepository.save(signatureProfile);
        } catch (Exception e) {
            throw new IdInvalidException("Không thể tạo hồ sơ chữ ký số cho user: " + e.getMessage());
        }
    }

    // 3. Create UserSignatureProfile for existing user
    public UserSignatureProfile createUserSignatureProfileForExistingUser(String userId) {
        if (userSignatureProfileRepository.findByUserId(userId).isPresent()) {
            throw new IdInvalidException("User đã có hồ sơ chữ ký số");
        }

        User user = new User();
        user.setId(userId);

        return createUserSignatureProfile(user);
    }
}
