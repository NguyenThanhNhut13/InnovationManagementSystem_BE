package vn.edu.iuh.fit.innovationmanagementsystem_be.service;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.User;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.UserSignatureProfile;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.UpdateUserSignatureProfilePathUrlRequest;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.UserSignatureProfileRequest;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.UserSignatureProfileResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.exception.IdInvalidException;
import vn.edu.iuh.fit.innovationmanagementsystem_be.mapper.UserSignatureProfileResponseMapper;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.UserSignatureProfileRepository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.utils.constants.SignatureConstants;

import java.security.KeyPair;
import java.util.Arrays;
import java.util.List;

@Service
@Transactional
public class UserSignatureProfileService {

    private final UserSignatureProfileRepository userSignatureProfileRepository;
    private final KeyManagementService keyManagementService;
    private final HSMEncryptionService hsmEncryptionService;
    private final UserService userService;
    private final FileService fileService;
    private final UserSignatureProfileResponseMapper userSignatureProfileResponseMapper;

    private static final List<String> ALLOWED_IMAGE_TYPES = Arrays.asList(
            "image/jpeg", "image/jpg", "image/png", "image/gif", "image/webp", "image/bmp");

    public UserSignatureProfileService(UserSignatureProfileRepository userSignatureProfileRepository,
            KeyManagementService keyManagementService,
            HSMEncryptionService hsmEncryptionService,
            @Lazy UserService userService,
            FileService fileService,
            UserSignatureProfileResponseMapper userSignatureProfileResponseMapper) {
        this.userSignatureProfileRepository = userSignatureProfileRepository;
        this.keyManagementService = keyManagementService;
        this.hsmEncryptionService = hsmEncryptionService;
        this.userService = userService;
        this.fileService = fileService;
        this.userSignatureProfileResponseMapper = userSignatureProfileResponseMapper;
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

    // 3. Cập nhật pathUrl của UserSignatureProfile cho user đang login
    public UserSignatureProfile updatePathUrlForCurrentUser(UpdateUserSignatureProfilePathUrlRequest request) {
        String currentUserId = userService.getCurrentUserId();

        UserSignatureProfile signatureProfile = userSignatureProfileRepository.findByUserId(currentUserId)
                .orElseThrow(() -> new IdInvalidException("Không tìm thấy hồ sơ chữ ký số cho user hiện tại"));

        signatureProfile.setPathUrl(request.getPathUrl().trim());

        return userSignatureProfileRepository.save(signatureProfile);
    }

    // 4. Upload ảnh chữ ký và cập nhật pathUrl của UserSignatureProfile
    public UserSignatureProfileResponse uploadSignatureImageForCurrentUser(MultipartFile file) {
        try {
            if (file == null || file.isEmpty()) {
                throw new IdInvalidException("File ảnh không được để trống");
            }

            String contentType = file.getContentType();
            if (contentType == null || !ALLOWED_IMAGE_TYPES.contains(contentType.toLowerCase())) {
                throw new IdInvalidException("File phải là ảnh (JPEG, PNG, GIF, WEBP, BMP)");
            }

            if (file.getSize() > 10 * 1024 * 1024) {
                throw new IdInvalidException("Kích thước file không được vượt quá 10MB");
            }

            String currentUserId = userService.getCurrentUserId();

            UserSignatureProfile signatureProfile = userSignatureProfileRepository.findByUserIdWithUser(currentUserId)
                    .orElseThrow(() -> new IdInvalidException("Không tìm thấy hồ sơ chữ ký số cho user hiện tại"));

            String oldPathUrl = signatureProfile.getPathUrl();

            String uploadedFileName = fileService.uploadFile(file);

            signatureProfile.setPathUrl(uploadedFileName);

            UserSignatureProfile savedProfile = userSignatureProfileRepository.save(signatureProfile);

            if (oldPathUrl != null && !oldPathUrl.trim().isEmpty()) {
                try {
                    fileService.deleteFile(oldPathUrl);
                } catch (Exception e) {
                    // Log error nhưng không throw exception vì file mới đã upload thành công
                    System.err.println("Không thể xóa file cũ: " + oldPathUrl + " - " + e.getMessage());
                }
            }

            return userSignatureProfileResponseMapper.toUserSignatureProfileResponse(savedProfile);
        } catch (IdInvalidException e) {
            throw new IdInvalidException("Không thể upload ảnh chữ ký: " + e.getMessage());
        } catch (Exception e) {
            throw new IdInvalidException("Không thể upload ảnh chữ ký: " + e.getMessage());
        }
    }
}
