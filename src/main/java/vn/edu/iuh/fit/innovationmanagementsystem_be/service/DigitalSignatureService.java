package vn.edu.iuh.fit.innovationmanagementsystem_be.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.DigitalSignature;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.Innovation;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.User;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.UserSignatureProfile;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.DocumentTypeEnum;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.SignatureStatusEnum;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.UserRoleEnum;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.DigitalSignatureRequest;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.DigitalSignatureResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.SignatureStatusResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.exception.IdInvalidException;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.DigitalSignatureRepository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.InnovationRepository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.UserSignatureProfileRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class DigitalSignatureService {

    private final DigitalSignatureRepository digitalSignatureRepository;
    private final InnovationRepository innovationRepository;
    private final UserSignatureProfileRepository userSignatureProfileRepository;
    private final UserSignatureProfileService userSignatureProfileService;
    private final UserService userService;
    private final KeyManagementService keyManagementService;

    public DigitalSignatureService(DigitalSignatureRepository digitalSignatureRepository,
            InnovationRepository innovationRepository,
            UserSignatureProfileRepository userSignatureProfileRepository,
            UserSignatureProfileService userSignatureProfileService,
            UserService userService,
            KeyManagementService keyManagementService) {
        this.digitalSignatureRepository = digitalSignatureRepository;
        this.innovationRepository = innovationRepository;
        this.userSignatureProfileRepository = userSignatureProfileRepository;
        this.userSignatureProfileService = userSignatureProfileService;
        this.userService = userService;
        this.keyManagementService = keyManagementService;
    }

    // 1. Tạo chữ ký số
    public DigitalSignatureResponse createDigitalSignature(DigitalSignatureRequest request) {
        // Validate innovation exists
        Innovation innovation = innovationRepository.findById(request.getInnovationId())
                .orElseThrow(
                        () -> new IdInvalidException("Không tìm thấy sáng kiến với ID: " + request.getInnovationId()));

        // Get current user
        User currentUser = userService.getCurrentUser();

        // Validate user has permission to sign
        validateSigningPermission(innovation, currentUser, request.getSignedAsRole());

        // Get user signature profile
        UserSignatureProfile signatureProfile = userSignatureProfileService.findByCurrentUser(currentUser);

        // Check if already signed
        if (digitalSignatureRepository.existsByInnovationIdAndDocumentTypeAndUserIdAndStatus(
                request.getInnovationId(), request.getDocumentType(), currentUser.getId(),
                SignatureStatusEnum.SIGNED)) {
            throw new IdInvalidException("Bạn đã ký tài liệu này rồi");
        }

        // Xác thực chữ ký trước khi lưu bằng public key của user
        boolean isValidSignature = keyManagementService.verifySignature(
                request.getDocumentHash(),
                request.getSignatureHash(),
                signatureProfile.getPublicKey());

        if (!isValidSignature) {
            throw new IdInvalidException("Chữ ký không hợp lệ");
        }

        // Create digital signature
        DigitalSignature digitalSignature = new DigitalSignature();
        digitalSignature.setDocumentType(request.getDocumentType());
        digitalSignature.setSignedAsRole(request.getSignedAsRole());
        digitalSignature.setSignatureHash(request.getSignatureHash());
        digitalSignature.setDocumentHash(request.getDocumentHash());
        digitalSignature.setStatus(SignatureStatusEnum.SIGNED);
        digitalSignature.setInnovation(innovation);
        digitalSignature.setUser(currentUser);
        digitalSignature.setUserSignatureProfile(signatureProfile);

        DigitalSignature savedSignature = digitalSignatureRepository.save(digitalSignature);

        return mapToResponse(savedSignature);
    }

    // 2. Lấy trạng thái chữ ký của innovation
    public SignatureStatusResponse getSignatureStatus(String innovationId, DocumentTypeEnum documentType) {
        Innovation innovation = innovationRepository.findById(innovationId)
                .orElseThrow(() -> new IdInvalidException("Không tìm thấy sáng kiến với ID: " + innovationId));

        // Get all signatures for this document type
        List<DigitalSignature> signatures = digitalSignatureRepository
                .findByInnovationIdAndDocumentTypeWithRelations(innovationId, documentType);

        // Determine required signatures based on document type
        List<SignatureStatusResponse.RequiredSignatureInfo> requiredSignatures = getRequiredSignatures(documentType);

        // Check which signatures are completed
        for (SignatureStatusResponse.RequiredSignatureInfo required : requiredSignatures) {
            UserRoleEnum role = UserRoleEnum.valueOf(required.getRoleCode());
            boolean isSigned = signatures.stream()
                    .anyMatch(sig -> sig.getSignedAsRole() == role && sig.getStatus() == SignatureStatusEnum.SIGNED);

            required.setSigned(isSigned);

            if (isSigned) {
                DigitalSignature signedSig = signatures.stream()
                        .filter(sig -> sig.getSignedAsRole() == role && sig.getStatus() == SignatureStatusEnum.SIGNED)
                        .findFirst().orElse(null);

                if (signedSig != null) {
                    required.setSignedBy(signedSig.getUser().getFullName());
                    required.setSignedAt(signedSig.getSignAt().toString());
                }
            }
        }

        // Check if fully signed
        boolean isFullySigned = requiredSignatures.stream()
                .allMatch(SignatureStatusResponse.RequiredSignatureInfo::isSigned);

        // Check if can submit (both forms must be fully signed)
        boolean canSubmit = isFullySigned && isBothFormsFullySigned(innovationId);

        SignatureStatusResponse response = new SignatureStatusResponse();
        response.setInnovationId(innovationId);
        response.setInnovationName(innovation.getInnovationName());
        response.setDocumentType(documentType);
        response.setFullySigned(isFullySigned);
        response.setCanSubmit(canSubmit);
        response.setRequiredSignatures(requiredSignatures);
        response.setCompletedSignatures(signatures.stream()
                .filter(sig -> sig.getStatus() == SignatureStatusEnum.SIGNED)
                .map(this::mapToResponse)
                .collect(Collectors.toList()));

        return response;
    }

    // 3. Kiểm tra xem cả 2 mẫu đã được ký đủ chưa
    public boolean isBothFormsFullySigned(String innovationId) {
        boolean form1Signed = isFormFullySigned(innovationId, DocumentTypeEnum.FORM_1);
        boolean form2Signed = isFormFullySigned(innovationId, DocumentTypeEnum.FORM_2);
        return form1Signed && form2Signed;
    }

    // 4. Kiểm tra xem một mẫu đã được ký đủ chưa
    public boolean isFormFullySigned(String innovationId, DocumentTypeEnum documentType) {
        List<SignatureStatusResponse.RequiredSignatureInfo> requiredSignatures = getRequiredSignatures(documentType);

        for (SignatureStatusResponse.RequiredSignatureInfo required : requiredSignatures) {
            UserRoleEnum role = UserRoleEnum.valueOf(required.getRoleCode());
            boolean isSigned = digitalSignatureRepository.existsByInnovationIdAndDocumentTypeAndSignedAsRoleAndStatus(
                    innovationId, documentType, role, SignatureStatusEnum.SIGNED);

            if (!isSigned) {
                return false;
            }
        }

        return true;
    }

    // 5. Lấy danh sách chữ ký của innovation
    public List<DigitalSignatureResponse> getInnovationSignatures(String innovationId) {
        List<DigitalSignature> signatures = digitalSignatureRepository.findByInnovationIdWithRelations(innovationId);
        return signatures.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // Helper methods
    private void validateSigningPermission(Innovation innovation, User currentUser, UserRoleEnum signedAsRole) {
        // Kiểm tra xem user có phải là tác giả của sáng kiến không
        boolean isAuthor = innovation.getUser().getId().equals(currentUser.getId());

        // Kiểm tra xem user có role tương ứng không
        boolean hasRole = currentUser.getUserRoles().stream()
                .anyMatch(userRole -> userRole.getRole().getRoleName() == signedAsRole);

        if (signedAsRole == UserRoleEnum.GIANG_VIEN) {
            // Chỉ tác giả sáng kiến mới được ký với vai trò GIANG_VIEN
            if (!isAuthor) {
                throw new IdInvalidException("Chỉ tác giả sáng kiến mới có thể ký với vai trò Giảng viên");
            }
            // Kiểm tra user có role GIANG_VIEN không (nếu tác giả có nhiều role)
            if (!hasRole) {
                throw new IdInvalidException("Bạn không có quyền ký với vai trò Giảng viên");
            }
        }

        if (signedAsRole == UserRoleEnum.TRUONG_KHOA) {
            // Kiểm tra user có role TRUONG_KHOA không
            if (!hasRole) {
                throw new IdInvalidException("Bạn không có quyền ký với vai trò Trưởng khoa");
            }

            // Kiểm tra user có phải trưởng khoa của cùng phòng ban với sáng kiến không
            if (!isDepartmentHeadOf(currentUser, innovation.getDepartment().getId())) {
                throw new IdInvalidException(
                        "Chỉ trưởng khoa của chính phòng ban mà sáng kiến thuộc về mới có thể ký với vai trò Trưởng khoa. "
                                +
                                "Phòng ban của sáng kiến: " + innovation.getDepartment().getId() +
                                ", Phòng ban của bạn: " + currentUser.getDepartment().getId());
            }
        }
    }

    private List<SignatureStatusResponse.RequiredSignatureInfo> getRequiredSignatures(DocumentTypeEnum documentType) {
        List<SignatureStatusResponse.RequiredSignatureInfo> required = new ArrayList<>();

        if (documentType == DocumentTypeEnum.FORM_1) {
            // Mẫu 1 chỉ cần chữ ký của tác giả (với vai trò GIANG_VIEN)
            SignatureStatusResponse.RequiredSignatureInfo author = new SignatureStatusResponse.RequiredSignatureInfo();
            author.setRoleName("Tác giả sáng kiến (với vai trò Giảng viên)");
            author.setRoleCode("GIANG_VIEN");
            author.setSigned(false);
            required.add(author);

        } else if (documentType == DocumentTypeEnum.FORM_2) {
            // Mẫu 2 cần chữ ký của tác giả (với vai trò GIANG_VIEN) và trưởng khoa của
            // chính phòng ban
            SignatureStatusResponse.RequiredSignatureInfo author = new SignatureStatusResponse.RequiredSignatureInfo();
            author.setRoleName("Tác giả sáng kiến (với vai trò Giảng viên)");
            author.setRoleCode("GIANG_VIEN");
            author.setSigned(false);
            required.add(author);

            SignatureStatusResponse.RequiredSignatureInfo departmentHead = new SignatureStatusResponse.RequiredSignatureInfo();
            departmentHead.setRoleName("Trưởng khoa (cùng phòng ban)");
            departmentHead.setRoleCode("TRUONG_KHOA");
            departmentHead.setSigned(false);
            required.add(departmentHead);
        }

        return required;
    }

    private DigitalSignatureResponse mapToResponse(DigitalSignature signature) {
        DigitalSignatureResponse response = new DigitalSignatureResponse();
        response.setId(signature.getId());
        response.setDocumentType(signature.getDocumentType());
        response.setSignedAsRole(signature.getSignedAsRole());
        response.setSignAt(signature.getSignAt());
        response.setSignatureHash(signature.getSignatureHash());
        response.setDocumentHash(signature.getDocumentHash());
        response.setStatus(signature.getStatus());

        // User information
        response.setUserId(signature.getUser().getId());
        response.setUserFullName(signature.getUser().getFullName());
        response.setUserPersonnelId(signature.getUser().getPersonnelId());

        // Innovation information
        response.setInnovationId(signature.getInnovation().getId());
        response.setInnovationName(signature.getInnovation().getInnovationName());

        // Certificate information
        if (signature.getUserSignatureProfile() != null) {
            response.setCertificateSerial(signature.getUserSignatureProfile().getCertificateSerial());
            response.setCertificateIssuer(signature.getUserSignatureProfile().getCertificateIssuer());
            response.setCertificateValidFrom(signature.getUserSignatureProfile().getCertificateValidFrom());
            response.setCertificateValidTo(signature.getUserSignatureProfile().getCertificateValidTo());
        }

        return response;
    }

    // Helper method: Kiểm tra user có phải là trưởng khoa của phòng ban cụ thể
    // không
    private boolean isDepartmentHeadOf(User user, String departmentId) {
        return user.getUserRoles().stream()
                .anyMatch(userRole -> userRole.getRole().getRoleName() == UserRoleEnum.TRUONG_KHOA
                        && user.getDepartment().getId().equals(departmentId));
    }

    // Method để tạo chữ ký từ document hash bằng private key của user hiện tại
    public String generateSignatureForDocument(String documentHash) {
        User currentUser = userService.getCurrentUser();
        UserSignatureProfile signatureProfile = userSignatureProfileService.findByCurrentUser(currentUser);

        return keyManagementService.generateSignature(documentHash, signatureProfile.getPrivateKey());
    }

    // Method để xác thực chữ ký bằng public key của user
    public boolean verifyDocumentSignature(String documentHash, String signatureHash, String userId) {
        UserSignatureProfile signatureProfile = userSignatureProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new IdInvalidException("Không tìm thấy hồ sơ chữ ký số của user"));

        return keyManagementService.verifySignature(documentHash, signatureHash, signatureProfile.getPublicKey());
    }

    // Method để tạo hash cho file content
    public String generateDocumentHash(byte[] fileContent) {
        return keyManagementService.generateDocumentHash(fileContent);
    }
}
