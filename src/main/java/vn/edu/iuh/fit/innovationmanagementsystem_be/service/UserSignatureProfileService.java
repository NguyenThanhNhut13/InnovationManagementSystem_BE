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
            signatureProfile.setPrivateKey(keyManagementService.privateKeyToString(keyPair.getPrivate()));
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
}
