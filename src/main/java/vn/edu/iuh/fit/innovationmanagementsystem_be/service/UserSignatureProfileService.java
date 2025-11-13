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
import java.util.ArrayList;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
@Transactional
public class UserSignatureProfileService {

    private final UserSignatureProfileRepository userSignatureProfileRepository;
    private final KeyManagementService keyManagementService;
    private final HSMEncryptionService hsmEncryptionService;
    private final UserService userService;
    private final FileService fileService;
    private final UserSignatureProfileResponseMapper userSignatureProfileResponseMapper;
    private final ObjectMapper objectMapper;

    private static final List<String> ALLOWED_IMAGE_TYPES = Arrays.asList(
            "image/jpeg", "image/jpg", "image/png", "image/gif", "image/webp", "image/bmp");

    public UserSignatureProfileService(UserSignatureProfileRepository userSignatureProfileRepository,
            KeyManagementService keyManagementService,
            HSMEncryptionService hsmEncryptionService,
            @Lazy UserService userService,
            FileService fileService,
            UserSignatureProfileResponseMapper userSignatureProfileResponseMapper,
            ObjectMapper objectMapper) {
        this.userSignatureProfileRepository = userSignatureProfileRepository;
        this.keyManagementService = keyManagementService;
        this.hsmEncryptionService = hsmEncryptionService;
        this.userService = userService;
        this.fileService = fileService;
        this.userSignatureProfileResponseMapper = userSignatureProfileResponseMapper;
        this.objectMapper = objectMapper;
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

    // 4. Upload ảnh chữ ký và thêm vào danh sách pathUrls của UserSignatureProfile
    public UserSignatureProfileResponse uploadSignatureImageForCurrentUser(MultipartFile files) {
        try {
            if (files == null || files.isEmpty()) {
                throw new IdInvalidException("File ảnh không được để trống");
            }

            String contentType = files.getContentType();
            if (contentType == null || !ALLOWED_IMAGE_TYPES.contains(contentType.toLowerCase())) {
                throw new IdInvalidException("File phải là ảnh (JPEG, PNG, GIF, WEBP, BMP)");
            }

            if (files.getSize() > 10 * 1024 * 1024) {
                throw new IdInvalidException("Kích thước file không được vượt quá 10MB");
            }

            String currentUserId = userService.getCurrentUserId();

            UserSignatureProfile signatureProfile = userSignatureProfileRepository.findByUserIdWithUser(currentUserId)
                    .orElseThrow(() -> new IdInvalidException("Không tìm thấy hồ sơ chữ ký số cho user hiện tại"));

            String uploadedFileName = fileService.uploadFile(files);

            List<String> pathUrls = parsePathUrlsFromString(signatureProfile.getPathUrl());
            pathUrls.add(uploadedFileName);

            String updatedPathUrlJson = objectMapper.writeValueAsString(pathUrls);
            signatureProfile.setPathUrl(updatedPathUrlJson);

            UserSignatureProfile savedProfile = userSignatureProfileRepository.save(signatureProfile);

            return mapToResponseWithPathUrls(savedProfile);
        } catch (IdInvalidException e) {
            throw new IdInvalidException("Không thể upload ảnh chữ ký: " + e.getMessage());
        } catch (Exception e) {
            throw new IdInvalidException("Không thể upload ảnh chữ ký: " + e.getMessage());
        }
    }

    // 5. Lấy thông tin chữ ký của user hiện tại
    public UserSignatureProfileResponse getCurrentUserSignatureProfile() {
        String currentUserId = userService.getCurrentUserId();

        UserSignatureProfile signatureProfile = userSignatureProfileRepository.findByUserIdWithUser(currentUserId)
                .orElseThrow(() -> new IdInvalidException("Không tìm thấy hồ sơ chữ ký số cho user hiện tại"));

        return mapToResponseWithPathUrls(signatureProfile);
    }

    // 6. Xóa một chữ ký cụ thể theo index
    public UserSignatureProfileResponse deleteSignatureImageByIndex(int index) {
        try {
            String currentUserId = userService.getCurrentUserId();

            UserSignatureProfile signatureProfile = userSignatureProfileRepository.findByUserIdWithUser(currentUserId)
                    .orElseThrow(() -> new IdInvalidException("Không tìm thấy hồ sơ chữ ký số cho user hiện tại"));

            List<String> pathUrls = parsePathUrlsFromString(signatureProfile.getPathUrl());

            if (index < 0 || index >= pathUrls.size()) {
                throw new IdInvalidException("Index không hợp lệ. Chỉ số phải từ 0 đến " + (pathUrls.size() - 1));
            }

            String pathUrlToDelete = pathUrls.get(index);
            pathUrls.remove(index);

            try {
                fileService.deleteFile(pathUrlToDelete);
            } catch (Exception e) {
                System.err.println("Không thể xóa file: " + pathUrlToDelete + " - " + e.getMessage());
            }

            String updatedPathUrlJson = objectMapper.writeValueAsString(pathUrls);
            signatureProfile.setPathUrl(updatedPathUrlJson);

            UserSignatureProfile savedProfile = userSignatureProfileRepository.save(signatureProfile);

            return mapToResponseWithPathUrls(savedProfile);
        } catch (IdInvalidException e) {
            throw new IdInvalidException("Không thể xóa ảnh chữ ký: " + e.getMessage());
        } catch (Exception e) {
            throw new IdInvalidException("Không thể xóa ảnh chữ ký: " + e.getMessage());
        }
    }

    // Helper method: Parse pathUrl từ JSON string thành List<String>
    private List<String> parsePathUrlsFromString(String pathUrl) {
        if (pathUrl == null || pathUrl.trim().isEmpty()) {
            return new ArrayList<>();
        }

        try {
            return objectMapper.readValue(pathUrl, new TypeReference<List<String>>() {
            });
        } catch (Exception e) {
            try {
                List<String> result = new ArrayList<>();
                result.add(pathUrl);
                return result;
            } catch (Exception ex) {
                return new ArrayList<>();
            }
        }
    }

    // Helper method: Map UserSignatureProfile to Response với pathUrls
    private UserSignatureProfileResponse mapToResponseWithPathUrls(UserSignatureProfile signatureProfile) {
        UserSignatureProfileResponse response = userSignatureProfileResponseMapper
                .toUserSignatureProfileResponse(signatureProfile);
        List<String> pathUrls = parsePathUrlsFromString(signatureProfile.getPathUrl());
        response.setPathUrls(pathUrls);
        return response;
    }
}
