package vn.edu.iuh.fit.innovationmanagementsystem_be.service;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.CertificateAuthority;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.FormData;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.User;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.UserSignatureProfile;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.FieldTypeEnum;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.InnovationStatusEnum;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.UpdateUserSignatureProfilePathUrlRequest;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.UserSignatureProfileRequest;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.UserSignatureProfileResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.exception.IdInvalidException;
import vn.edu.iuh.fit.innovationmanagementsystem_be.mapper.UserSignatureProfileResponseMapper;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.FormDataRepository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.UserSignatureProfileRepository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.UserRepository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.utils.constants.CAConstans;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.CAStatusEnum;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.math.BigInteger;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
@Transactional
public class UserSignatureProfileService {

    private final UserSignatureProfileRepository userSignatureProfileRepository;
    private final UserRepository userRepository;
    private final KeyManagementService keyManagementService;
    private final HSMEncryptionService hsmEncryptionService;
    private final UserService userService;
    private final FileService fileService;
    private final UserSignatureProfileResponseMapper userSignatureProfileResponseMapper;
    private final ObjectMapper objectMapper;
    private final CertificateAuthorityService certificateAuthorityService;
    private final FormDataRepository formDataRepository;

    private static final List<String> ALLOWED_IMAGE_TYPES = Arrays.asList(
            "image/jpeg", "image/jpg", "image/png", "image/gif", "image/webp", "image/bmp");

    public UserSignatureProfileService(UserSignatureProfileRepository userSignatureProfileRepository,
            UserRepository userRepository,
            KeyManagementService keyManagementService,
            HSMEncryptionService hsmEncryptionService,
            @Lazy UserService userService,
            FileService fileService,
            UserSignatureProfileResponseMapper userSignatureProfileResponseMapper,
            ObjectMapper objectMapper,
            CertificateAuthorityService certificateAuthorityService,
            FormDataRepository formDataRepository) {
        this.userSignatureProfileRepository = userSignatureProfileRepository;
        this.userRepository = userRepository;
        this.keyManagementService = keyManagementService;
        this.hsmEncryptionService = hsmEncryptionService;
        this.userService = userService;
        this.fileService = fileService;
        this.userSignatureProfileResponseMapper = userSignatureProfileResponseMapper;
        this.objectMapper = objectMapper;
        this.certificateAuthorityService = certificateAuthorityService;
        this.formDataRepository = formDataRepository;
    }

    // 1. Tạo UserSignatureProfile cho user
    public UserSignatureProfile createUserSignatureProfile(UserSignatureProfileRequest request) {
        try {

            if (userSignatureProfileRepository.findByUserId(request.getUserId()).isPresent()) {
                throw new IdInvalidException("User đã có hồ sơ chữ ký số");
            }

            // Lấy CA đang hoạt động trước để lấy thông tin issuer
            CertificateAuthority ca = null;
            if (request.getCertificateAuthorityId() != null && !request.getCertificateAuthorityId().isEmpty()) {
                ca = certificateAuthorityService.findCAById(request.getCertificateAuthorityId());
            }

            if (ca == null) {
                ca = certificateAuthorityService.getActiveCA();
            }

            // Lấy thông tin user đầy đủ
            User user = userRepository.findById(request.getUserId())
                    .orElseThrow(() -> new IdInvalidException("Không tìm thấy user với ID: " + request.getUserId()));

            // Tạo cặp khóa mới cho user
            KeyPair keyPair = keyManagementService.generateKeyPair();
            String publicKeyBase64 = keyManagementService.publicKeyToString(keyPair.getPublic());

            // Tạo UserSignatureProfile
            UserSignatureProfile signatureProfile = new UserSignatureProfile();
            signatureProfile.setUser(user);
            signatureProfile.setPathUrl(request.getPathUrl());
            // Encrypt private key trước khi lưu
            String privateKeyString = keyManagementService.privateKeyToString(keyPair.getPrivate());
            String encryptedPrivateKey = hsmEncryptionService.encryptPrivateKey(privateKeyString);
            signatureProfile.setEncryptedPrivateKey(encryptedPrivateKey);
            signatureProfile.setPublicKey(publicKeyBase64);

            // Thiết lập thời gian hiệu lực certificate
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime validFrom = now;
            LocalDateTime validTo = now.plusMonths(1);

            // Lấy thông tin certificate từ CA nếu có, nếu không thì dùng constants
            String certificateIssuer;
            String certificateSerial;
            if (ca != null) {
                certificateIssuer = ca.getCertificateIssuer();
                signatureProfile.setCertificateAuthority(ca);
                // Tạo serial dựa trên CA serial để đảm bảo liên kết
                String caSerial = ca.getCertificateSerial();
                if (caSerial != null && !caSerial.isEmpty()) {
                    // Lấy prefix từ CA serial (ví dụ: "CA-ADMIN-QUINTON-1763521463" ->
                    // "CA-ADMIN-QUINTON")
                    // Tìm vị trí số cuối cùng (timestamp) và lấy phần trước đó
                    String prefix = caSerial;
                    int lastDashIndex = caSerial.lastIndexOf('-');
                    if (lastDashIndex > 0) {
                        // Kiểm tra phần sau dấu "-" cuối cùng có phải là số không
                        String lastPart = caSerial.substring(lastDashIndex + 1);
                        if (lastPart.matches("\\d+")) {
                            // Nếu là số (timestamp), lấy phần trước dấu "-" cuối cùng
                            prefix = caSerial.substring(0, lastDashIndex);
                        }
                    }
                    certificateSerial = prefix + "-USER-" + System.currentTimeMillis() + "-"
                            + (int) (Math.random() * 10000);
                } else {
                    certificateSerial = keyManagementService.generateCertificateSerial();
                }
            } else {
                // Fallback về constants nếu không có CA
                certificateIssuer = CAConstans.certificateIssuer;
                certificateSerial = keyManagementService.generateCertificateSerial();
            }

            // Tạo certificate subject từ thông tin user
            String certificateSubject = String.format("CN=%s, O=IUH, C=VN", user.getFullName());

            // Tạo certificate data (X.509 certificate thực sự)
            String certificateData = createCertificateData(keyPair.getPublic(), keyPair.getPrivate(), certificateSerial,
                    certificateIssuer, certificateSubject, validFrom, validTo);

            // Set các trường certificate
            signatureProfile.setCertificateVersion(3); // X.509 v3
            signatureProfile.setCertificateSerial(certificateSerial);
            signatureProfile.setCertificateIssuer(certificateIssuer);
            signatureProfile.setCertificateSubject(certificateSubject);
            signatureProfile.setCertificateValidFrom(validFrom);
            signatureProfile.setCertificateExpiryDate(validTo);
            signatureProfile.setCertificateData(certificateData);
            signatureProfile.setCertificateStatus(CAStatusEnum.VERIFIED);

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

            // Kiểm tra xem chữ ký có đang được sử dụng trong các innovation có status
            // SUBMITTED không
            if (isSignatureInUse(pathUrlToDelete)) {
                throw new IdInvalidException(
                        "Không thể xóa chữ ký này vì đang được sử dụng trong các sáng kiến đã nộp (status SUBMITTED)");
            }

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

    // Helper method: Kiểm tra xem chữ ký có đang được sử dụng trong các innovation
    // có status SUBMITTED không
    private boolean isSignatureInUse(String pathUrl) {
        List<FormData> signatureFormDataList = formDataRepository
                .findSignatureFormDataWithSubmittedInnovations(FieldTypeEnum.SIGNATURE,
                        InnovationStatusEnum.SUBMITTED);

        for (FormData formData : signatureFormDataList) {
            JsonNode fieldValue = formData.getFieldValue();
            if (fieldValue == null) {
                continue;
            }

            // Kiểm tra nếu fieldValue là string và bằng pathUrl
            if (fieldValue.isTextual() && pathUrl.equals(fieldValue.asText())) {
                return true;
            }

            // Kiểm tra nếu fieldValue là object có chứa "value" và value là string bằng
            // pathUrl
            if (fieldValue.isObject() && fieldValue.has("value")) {
                JsonNode valueNode = fieldValue.get("value");
                if (valueNode != null && valueNode.isTextual() && pathUrl.equals(valueNode.asText())) {
                    return true;
                }
            }

            // Kiểm tra nếu fieldValue là array chứa pathUrl
            if (fieldValue.isArray()) {
                for (JsonNode arrayItem : fieldValue) {
                    if (arrayItem.isTextual() && pathUrl.equals(arrayItem.asText())) {
                        return true;
                    }
                    // Kiểm tra nested object trong array
                    if (arrayItem.isObject() && arrayItem.has("value")) {
                        JsonNode nestedValue = arrayItem.get("value");
                        if (nestedValue != null && nestedValue.isTextual()
                                && pathUrl.equals(nestedValue.asText())) {
                            return true;
                        }
                    }
                }
            }
        }

        return false;
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

    // Helper method: Tạo certificate data (tương tự như CASeeder)
    private String createCertificateData(PublicKey publicKey, PrivateKey privateKey, String serial, String issuer,
            String subject, LocalDateTime validFrom, LocalDateTime validTo) {
        try {
            // Chuyển đổi LocalDateTime sang Date
            Date notBefore = Date.from(validFrom.atZone(ZoneId.systemDefault()).toInstant());
            Date notAfter = Date.from(validTo.atZone(ZoneId.systemDefault()).toInstant());

            // Tạo X500Name cho issuer và subject
            X500Name issuerName = new X500Name(issuer);
            X500Name subjectName = new X500Name(subject);

            // Tạo serial number từ string
            BigInteger serialNumber = new BigInteger(serial.getBytes());

            // Tạo X.509 v3 certificate builder
            X509v3CertificateBuilder certBuilder = new JcaX509v3CertificateBuilder(
                    issuerName,
                    serialNumber,
                    notBefore,
                    notAfter,
                    subjectName,
                    publicKey);

            // Tạo content signer với SHA256withRSA
            ContentSigner contentSigner = new JcaContentSignerBuilder("SHA256withRSA")
                    .build(privateKey);

            // Build và sign certificate
            X509Certificate certificate = new JcaX509CertificateConverter()
                    .getCertificate(certBuilder.build(contentSigner));

            // Encode certificate sang Base64
            byte[] certBytes = certificate.getEncoded();
            return Base64.getEncoder().encodeToString(certBytes);

        } catch (Exception e) {
            throw new IdInvalidException("Không thể tạo X.509 certificate: " + e.getMessage());
        }
    }
}
