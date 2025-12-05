package vn.edu.iuh.fit.innovationmanagementsystem_be.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.Attachment;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.CertificateAuthority;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.DigitalSignature;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.FormTemplate;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.Innovation;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.User;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.UserSignatureProfile;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.DocumentTypeEnum;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.SignatureStatusEnum;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.TemplateTypeEnum;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.UserRoleEnum;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.DigitalSignatureRequest;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.DigitalSignatureResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.SignatureStatusResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.TemplatePdfResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.TemplatePdfSignerResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.UserDocumentSignatureStatusResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.VerifyDigitalSignatureResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.exception.IdInvalidException;
import vn.edu.iuh.fit.innovationmanagementsystem_be.mapper.DigitalSignatureResponseMapper;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.AttachmentRepository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.DigitalSignatureRepository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.FormTemplateRepository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.InnovationRepository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.DepartmentRepository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.ReportRepository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.UserSignatureProfileRepository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.Report;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.DepartmentDocumentSignRequest;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.DepartmentDocumentSignResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.utils.Utils;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class DigitalSignatureService {

    private final DigitalSignatureRepository digitalSignatureRepository;
    private final InnovationRepository innovationRepository;
    private final UserSignatureProfileRepository userSignatureProfileRepository;
    private final UserService userService;
    private final KeyManagementService keyManagementService;
    private final DigitalSignatureResponseMapper digitalSignatureResponseMapper;
    private final CertificateValidationService certificateValidationService;
    private final CertificateAuthorityService certificateAuthorityService;
    private final HSMEncryptionService hsmEncryptionService;
    private final AttachmentRepository attachmentRepository;
    private final FileService fileService;
    private final FormTemplateRepository formTemplateRepository;
    private final CertificateRevocationService certificateRevocationService;
    private final PdfGeneratorService pdfGeneratorService;
    private final DepartmentRepository departmentRepository;
    private final ReportRepository reportRepository;

    public DigitalSignatureService(DigitalSignatureRepository digitalSignatureRepository,
            InnovationRepository innovationRepository,
            UserSignatureProfileRepository userSignatureProfileRepository,
            UserService userService,
            KeyManagementService keyManagementService,
            DigitalSignatureResponseMapper digitalSignatureResponseMapper,
            CertificateValidationService certificateValidationService,
            CertificateAuthorityService certificateAuthorityService,
            HSMEncryptionService hsmEncryptionService,
            AttachmentRepository attachmentRepository,
            FileService fileService,
            FormTemplateRepository formTemplateRepository,
            CertificateRevocationService certificateRevocationService,
            PdfGeneratorService pdfGeneratorService,
            DepartmentRepository departmentRepository,
            ReportRepository reportRepository) {
        this.digitalSignatureRepository = digitalSignatureRepository;
        this.innovationRepository = innovationRepository;
        this.userSignatureProfileRepository = userSignatureProfileRepository;
        this.userService = userService;
        this.keyManagementService = keyManagementService;
        this.digitalSignatureResponseMapper = digitalSignatureResponseMapper;
        this.certificateValidationService = certificateValidationService;
        this.certificateAuthorityService = certificateAuthorityService;
        this.hsmEncryptionService = hsmEncryptionService;
        this.attachmentRepository = attachmentRepository;
        this.fileService = fileService;
        this.formTemplateRepository = formTemplateRepository;
        this.certificateRevocationService = certificateRevocationService;
        this.pdfGeneratorService = pdfGeneratorService;
        this.departmentRepository = departmentRepository;
        this.reportRepository = reportRepository;
    }

    // 1. Tạo digital signature
    public DigitalSignatureResponse createDigitalSignature(DigitalSignatureRequest request) {

        Innovation innovation = innovationRepository.findById(request.getInnovationId())
                .orElseThrow(
                        () -> new IdInvalidException("Không tìm thấy sáng kiến với ID: " + request.getInnovationId()));

        User currentUser = userService.getCurrentUser();

        // Validate user có quyền ký
        validateSigningPermission(innovation, currentUser, request.getSignedAsRole());

        // Lấy user signature profile
        UserSignatureProfile signatureProfile = this.userSignatureProfileRepository.findByUserId(currentUser.getId())
                .orElseThrow(() -> new IdInvalidException("Người dùng chưa có hồ sơ chữ ký số"));

        // Kiểm tra user status và CRL
        validateUserCanSign(currentUser, signatureProfile);

        // 1. Validate certificate trước khi ký
        if (signatureProfile.getCertificateData() != null) {
            CertificateValidationService.CertificateValidationResult certValidation = certificateValidationService
                    .validateX509Certificate(signatureProfile.getCertificateData());

            if (!certValidation.isValid()) {
                throw new IdInvalidException("Certificate không hợp lệ: " +
                        String.join(", ", certValidation.getErrors()));
            }

            // Check certificate expiration
            CertificateValidationService.CertificateExpirationResult expirationResult = certificateValidationService
                    .checkCertificateExpiration(signatureProfile.getCertificateData());

            if (expirationResult.isExpired()) {
                throw new IdInvalidException("Certificate đã hết hạn");
            }

            // 1.1. Kiểm tra CA nội bộ
            validateInternalCA(signatureProfile);

            validateAndSetupCertificateIfNeeded(signatureProfile, currentUser);
        }

        if (digitalSignatureRepository.existsByInnovationIdAndDocumentTypeAndUserIdAndStatus(
                request.getInnovationId(), request.getDocumentType(), currentUser.getId(),
                SignatureStatusEnum.SIGNED)) {
            throw new IdInvalidException("Bạn đã ký tài liệu này rồi");
        }

        // 2. Xác thực chữ ký trước khi lưu bằng public key của user
        boolean isValidSignature = keyManagementService.verifySignature(
                request.getDocumentHash(),
                request.getSignatureHash(),
                signatureProfile.getPublicKey());

        if (!isValidSignature) {
            throw new IdInvalidException("Chữ ký không hợp lệ");
        }

        // 2.1 Xử lý idempotent theo signature_hash để tránh lỗi trùng khóa DB
        Optional<DigitalSignature> existingByHashOpt = digitalSignatureRepository
                .findBySignatureHash(request.getSignatureHash());

        if (existingByHashOpt.isPresent()) {
            DigitalSignature existingByHash = existingByHashOpt.get();

            boolean sameContext = existingByHash.getInnovation() != null
                    && existingByHash.getInnovation().getId().equals(innovation.getId())
                    && existingByHash.getDocumentType() == request.getDocumentType()
                    && existingByHash.getUser() != null
                    && existingByHash.getUser().getId().equals(currentUser.getId())
                    && existingByHash.getStatus() == SignatureStatusEnum.SIGNED;

            if (sameContext) {
                // Ký lại cùng một tài liệu cho cùng sáng kiến, trả về chữ ký cũ (idempotent)
                return digitalSignatureResponseMapper.toResponse(existingByHash);
            }

            // Nếu signature_hash đã được dùng cho tài liệu khác thì không cho phép
            // throw new IdInvalidException(
            // "Chữ ký số này đã được sử dụng cho tài liệu khác. Vui lòng tạo lại tài liệu
            // và ký lại.");
            throw new IdInvalidException(
                    "Tài liệu này đã được ký số trước đó. Vui lòng kiểm tra lại.");
        }

        // 3. Tạo timestamp cho chữ ký (RFC 3161) - Disabled for academic project
        String timestampToken = null;

        // Tạo digital signature
        DigitalSignature digitalSignature = new DigitalSignature();
        digitalSignature.setDocumentType(request.getDocumentType());
        digitalSignature.setSignedAsRole(request.getSignedAsRole());
        digitalSignature.setSignatureHash(request.getSignatureHash());
        digitalSignature.setDocumentHash(request.getDocumentHash());
        digitalSignature.setStatus(SignatureStatusEnum.SIGNED);
        digitalSignature.setInnovation(innovation);
        digitalSignature.setUser(currentUser);
        digitalSignature.setUserSignatureProfile(signatureProfile);

        // Lưu certificate validation info
        digitalSignature.setTimestampToken(timestampToken);
        digitalSignature.setCertificateValidationStatus("VALID");

        DigitalSignature savedSignature = digitalSignatureRepository.save(digitalSignature);

        return digitalSignatureResponseMapper.toResponse(savedSignature);
    }

    // 2. Lấy signature status của innovation
    public SignatureStatusResponse getSignatureStatus(String innovationId, DocumentTypeEnum documentType) {
        Innovation innovation = innovationRepository.findById(innovationId)
                .orElseThrow(() -> new IdInvalidException("Không tìm thấy sáng kiến với ID: " + innovationId));

        // Lấy tất cả signatures cho document type này
        List<DigitalSignature> signatures = digitalSignatureRepository
                .findByInnovationIdAndDocumentTypeWithRelations(innovationId, documentType);

        // Xác định required signatures dựa trên document type
        List<SignatureStatusResponse.RequiredSignatureInfo> requiredSignatures = getRequiredSignatures(documentType);

        // Kiểm tra signatures nào đã được hoàn thành
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

        // Kiểm tra nếu đã được ký đầy đủ
        boolean isFullySigned = requiredSignatures.stream()
                .allMatch(SignatureStatusResponse.RequiredSignatureInfo::isSigned);

        // Kiểm tra nếu có thể submit (cả 2 forms phải được ký đầy đủ)
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
                .map(digitalSignatureResponseMapper::toResponse)
                .collect(Collectors.toList()));

        return response;
    }

    // 3. Kiểm tra nếu cả 2 forms đã được ký đầy đủ
    public boolean isBothFormsFullySigned(String innovationId) {
        boolean form1Signed = isFormFullySigned(innovationId, DocumentTypeEnum.FORM_1);
        boolean form2Signed = isFormFullySigned(innovationId, DocumentTypeEnum.FORM_2);
        return form1Signed && form2Signed;
    }

    // 4. Kiểm tra nếu một form đã được ký đầy đủ
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
                .map(digitalSignatureResponseMapper::toResponse)
                .collect(Collectors.toList());
    }

    // 6. Kiểm tra trạng thái chữ ký của user hiện tại đối với một tài liệu
    public UserDocumentSignatureStatusResponse getCurrentUserDocumentSignatureStatus(
            String innovationId,
            DocumentTypeEnum documentType,
            UserRoleEnum signedAsRole) {

        Innovation innovation = innovationRepository.findById(innovationId)
                .orElseThrow(() -> new IdInvalidException("Không tìm thấy sáng kiến với ID: " + innovationId));

        User currentUser = userService.getCurrentUser();

        validateSigningPermission(innovation, currentUser, signedAsRole);

        UserSignatureProfile signatureProfile = this.userSignatureProfileRepository.findByUserId(currentUser.getId())
                .orElseThrow(() -> new IdInvalidException("Người dùng chưa có hồ sơ chữ ký số"));

        if (signatureProfile.getCertificateData() != null && !signatureProfile.getCertificateData().trim().isEmpty()) {
            CertificateValidationService.CertificateValidationResult certValidation = certificateValidationService
                    .validateX509Certificate(signatureProfile.getCertificateData());

            if (!certValidation.isValid()) {
                throw new IdInvalidException("Certificate không hợp lệ: " +
                        String.join(", ", certValidation.getErrors()));
            }

            CertificateValidationService.CertificateExpirationResult expirationResult = certificateValidationService
                    .checkCertificateExpiration(signatureProfile.getCertificateData());

            if (expirationResult.isExpired()) {
                throw new IdInvalidException("Certificate đã hết hạn");
            }

            validateAndSetupCertificateIfNeeded(signatureProfile, currentUser);
        }

        List<DigitalSignature> signatures = digitalSignatureRepository
                .findByInnovationIdAndDocumentTypeWithRelations(innovationId, documentType);

        Optional<DigitalSignature> latestSignatureOpt = signatures.stream()
                .filter(sig -> sig.getUser() != null
                        && sig.getUser().getId().equals(currentUser.getId())
                        && sig.getSignedAsRole() == signedAsRole
                        && sig.getStatus() == SignatureStatusEnum.SIGNED)
                .sorted(Comparator.comparing(DigitalSignature::getSignAt).reversed())
                .findFirst();

        if (latestSignatureOpt.isEmpty()) {
            return new UserDocumentSignatureStatusResponse(
                    innovationId,
                    documentType,
                    signedAsRole,
                    false,
                    null,
                    false);
        }

        DigitalSignature latestSignature = latestSignatureOpt.get();

        boolean isValidSignature = keyManagementService.verifySignature(
                latestSignature.getDocumentHash(),
                latestSignature.getSignatureHash(),
                signatureProfile.getPublicKey());

        return new UserDocumentSignatureStatusResponse(
                innovationId,
                documentType,
                signedAsRole,
                latestSignature.getStatus() == SignatureStatusEnum.SIGNED,
                latestSignature.getSignAt(),
                isValidSignature);
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

    // Validate user status và certificate revocation status
    private void validateUserCanSign(User user, UserSignatureProfile profile) {
        // 1. Kiểm tra user status
        if (user.getStatus() != vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.UserStatusEnum.ACTIVE) {
            throw new IdInvalidException(
                    "Tài khoản không được phép ký số. " +
                            "Trạng thái hiện tại: " + user.getStatus() + ". " +
                            "Chỉ tài khoản ACTIVE mới có thể ký tài liệu.");
        }

        // 2. Kiểm tra certificate có bị thu hồi không
        if (profile != null && profile.getCertificateSerial() != null &&
                certificateRevocationService.isCertificateRevoked(profile.getCertificateSerial())) {
            java.util.Optional<vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.CertificateRevocation> revocation = certificateRevocationService
                    .getRevocationInfo(profile.getCertificateSerial());

            String reason = revocation
                    .map(r -> r.getRevocationReason().getDescription())
                    .orElse("không rõ");

            throw new IdInvalidException(
                    "Certificate đã bị thu hồi. Lý do: " + reason + ". " +
                            "Vui lòng liên hệ quản trị viên để được hỗ trợ.");
        }
    }

    private void validateInternalCA(UserSignatureProfile signatureProfile) {
        // Kiểm tra CA từ relationship trước
        if (signatureProfile.getCertificateAuthority() != null) {
            CertificateAuthority ca = signatureProfile.getCertificateAuthority();
            String caId = ca.getId();
            boolean canUse = certificateAuthorityService.canUseCAForSigning(caId);
            if (!canUse) {
                throw new IdInvalidException(
                        "CA nội bộ không thể sử dụng để ký số. CA chưa được xác minh hoặc đã hết hạn");
            }

            // Kiểm tra certificate của user có khớp với CA không
            if (signatureProfile.getCertificateData() != null
                    && !signatureProfile.getCertificateData().trim().isEmpty()) {
                try {
                    CertificateValidationService.CertificateInfo certInfo = certificateValidationService
                            .extractCertificateInfo(signatureProfile.getCertificateData());

                    // Kiểm tra issuer phải khớp với CA issuer (so sánh theo field, không phải
                    // chuỗi)
                    if (!compareDistinguishedNames(certInfo.getIssuer(), ca.getCertificateIssuer())) {
                        throw new IdInvalidException(
                                "Certificate của bạn không khớp với CA nội bộ. Certificate issuer: "
                                        + certInfo.getIssuer() + ", CA issuer: " + ca.getCertificateIssuer());
                    }
                } catch (IdInvalidException e) {
                    throw e;
                } catch (Exception e) {
                    throw new IdInvalidException(
                            "Không thể xác minh certificate có khớp với CA không: " + e.getMessage());
                }
            }
            return;
        }

        // Nếu không có relationship, tìm CA từ certificate issuer
        if (signatureProfile.getCertificateIssuer() != null) {
            // Kiểm tra certificate data có tồn tại không
            if (signatureProfile.getCertificateData() == null
                    || signatureProfile.getCertificateData().trim().isEmpty()) {
                throw new IdInvalidException(
                        "Certificate data không tồn tại. Vui lòng cập nhật certificate trước khi ký số.");
            }

            // Extract certificate serial từ certificate data để tìm CA
            try {
                CertificateValidationService.CertificateInfo certInfo = certificateValidationService
                        .extractCertificateInfo(signatureProfile.getCertificateData());

                vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.CertificateAuthority ca = certificateAuthorityService
                        .findCAByCertificateSerial(certInfo.getSerialNumber());

                if (ca != null) {
                    // Tìm thấy CA, kiểm tra có thể dùng không
                    boolean canUse = certificateAuthorityService.canUseCAForSigning(ca.getId());
                    if (!canUse) {
                        throw new IdInvalidException(
                                "CA nội bộ không thể sử dụng để ký số. CA chưa được xác minh hoặc đã hết hạn");
                    }
                    // Link CA với signature profile để lần sau không cần tìm lại
                    signatureProfile.setCertificateAuthority(ca);
                    userSignatureProfileRepository.save(signatureProfile);
                    return;
                }

                // Nếu không tìm thấy CA trong hệ thống, từ chối ký số
                // Yêu cầu admin thêm CA vào hệ thống và xác minh trước
                throw new IdInvalidException(
                        "Certificate không thuộc CA nội bộ đã được xác minh. " +
                                "Vui lòng liên hệ admin để thêm CA vào hệ thống và xác minh trước khi sử dụng. " +
                                "Certificate issuer: " + certInfo.getIssuer());
            } catch (IdInvalidException e) {
                // Re-throw IdInvalidException để giữ nguyên thông báo lỗi
                throw e;
            } catch (Exception e) {
                // Nếu có lỗi khi extract certificate info, từ chối ký số
                throw new IdInvalidException(
                        "Không thể xác minh CA của certificate. Certificate có thể không hợp lệ hoặc không thuộc CA nội bộ: "
                                +
                                e.getMessage());
            }
        } else {
            // Nếu không có certificate issuer, từ chối ký số
            throw new IdInvalidException(
                    "Certificate không có thông tin issuer. Chỉ chấp nhận certificate từ CA nội bộ đã được xác minh.");
        }
    }

    private void validateAndSetupCertificateIfNeeded(UserSignatureProfile signatureProfile, User currentUser) {
        // Kiểm tra certificate data có tồn tại không
        if (signatureProfile.getCertificateData() == null || signatureProfile.getCertificateData().trim().isEmpty()) {
            throw new IdInvalidException(
                    "Certificate data không tồn tại. Vui lòng cập nhật certificate trước khi ký số.");
        }

        try {
            CertificateValidationService.CertificateValidationResult chainValidation = certificateValidationService
                    .validateX509Certificate(signatureProfile.getCertificateData());
            if (!chainValidation.isValid()) {
                throw new IdInvalidException("Certificate chain không hợp lệ: " +
                        String.join(", ", chainValidation.getErrors()));
            }
        } catch (Exception e) {
            throw new IdInvalidException("Certificate chain không hợp lệ: " + e.getMessage());
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

    /*
     * Helper method: Kiểm tra user có phải là trưởng khoa của phòng ban cụ thể
     * không
     */
    private boolean isDepartmentHeadOf(User user, String departmentId) {
        return user.getUserRoles().stream()
                .anyMatch(userRole -> userRole.getRole().getRoleName() == UserRoleEnum.TRUONG_KHOA
                        && user.getDepartment().getId().equals(departmentId));
    }

    private TemplatePdfSignerResponse toTemplatePdfSignerResponse(DigitalSignature signature) {
        TemplatePdfSignerResponse signerResponse = new TemplatePdfSignerResponse();

        User signer = signature.getUser();
        if (signer != null) {
            signerResponse.setSignerId(signer.getId());
            signerResponse.setSignerFullName(signer.getFullName());
            signerResponse.setSignerPersonnelId(signer.getPersonnelId());
        }

        signerResponse.setSignedAsRole(signature.getSignedAsRole());
        signerResponse.setSignAt(signature.getSignAt());

        boolean verified = false;
        UserSignatureProfile profile = signature.getUserSignatureProfile();
        if (profile != null && profile.getPublicKey() != null) {
            verified = keyManagementService.verifySignature(
                    signature.getDocumentHash(),
                    signature.getSignatureHash(),
                    profile.getPublicKey());
        }

        signerResponse.setVerified(verified);
        return signerResponse;
    }

    /*
     * Method để tạo chữ ký từ document hash bằng private key của user hiện tại
     */
    public String generateSignatureForDocument(String documentHash) {
        User currentUser = userService.getCurrentUser();
        UserSignatureProfile signatureProfile = this.userSignatureProfileRepository.findByUserId(currentUser.getId())
                .orElseThrow(() -> new IdInvalidException("Người dùng chưa có hồ sơ chữ ký số"));

        // Decrypt private key trước khi sử dụng
        String decryptedPrivateKey = hsmEncryptionService.decryptPrivateKey(signatureProfile.getEncryptedPrivateKey());
        return keyManagementService.generateSignature(documentHash, decryptedPrivateKey);
    }

    /*
     * Method để xác thực chữ ký bằng public key của user
     */
    public VerifyDigitalSignatureResponse verifyDocumentSignature(String documentHash, String signatureHash,
            String userId) {
        UserSignatureProfile signatureProfile = userSignatureProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new IdInvalidException("Không tìm thấy hồ sơ chữ ký số của user"));

        boolean isValid = keyManagementService.verifySignature(documentHash, signatureHash,
                signatureProfile.getPublicKey());

        VerifyDigitalSignatureResponse response = new VerifyDigitalSignatureResponse();
        response.setVerified(isValid);

        Optional<DigitalSignature> digitalSignatureOpt = digitalSignatureRepository.findBySignatureHash(signatureHash);
        if (digitalSignatureOpt.isPresent()) {
            DigitalSignature digitalSignature = digitalSignatureOpt.get();

            response.setDocumentType(digitalSignature.getDocumentType());
            response.setSignedAsRole(digitalSignature.getSignedAsRole());
            response.setSignAt(digitalSignature.getSignAt());

            User signer = digitalSignature.getUser();
            if (signer != null) {
                response.setUserId(signer.getId());
                response.setUserFullName(signer.getFullName());
                response.setUserPersonnelId(signer.getPersonnelId());
            }

            Innovation innovation = digitalSignature.getInnovation();
            if (innovation != null) {
                response.setInnovationId(innovation.getId());
                response.setInnovationName(innovation.getInnovationName());
            }
        }

        return response;
    }

    // 3. Lấy PDF của template kèm thông tin ký
    public TemplatePdfResponse getTemplatePdf(String innovationId, String templateId) {
        if (innovationId == null || innovationId.isBlank() || templateId == null || templateId.isBlank()) {
            throw new IdInvalidException("innovationId và templateId không được để trống");
        }

        // Kiểm tra Innovation tồn tại
        innovationRepository.findById(innovationId)
                .orElseThrow(() -> new IdInvalidException("Không tìm thấy sáng kiến với ID: " + innovationId));

        // Kiểm tra FormTemplate tồn tại
        FormTemplate formTemplate = formTemplateRepository.findById(templateId)
                .orElseThrow(() -> new IdInvalidException("Không tìm thấy template với ID: " + templateId));

        // Tìm attachment
        Attachment attachment = attachmentRepository
                .findTopByInnovationIdAndTemplateIdOrderByCreatedAtDesc(innovationId, templateId)
                .orElseThrow(() -> new IdInvalidException(
                        String.format("Không tìm thấy file PDF cho template đã yêu cầu. " +
                                "InnovationId: %s, TemplateId: %s, Template Name: %s. " +
                                "Nguyên nhân có thể: PDF chưa được tạo hoặc đã bị xóa.",
                                innovationId, templateId, formTemplate.getTemplateType().getValue())));

        String pdfUrl = fileService.getPresignedUrl(attachment.getPathUrl(), 3600);
        DocumentTypeEnum documentType = mapTemplateTypeToDocumentType(formTemplate.getTemplateType());

        TemplatePdfResponse response = new TemplatePdfResponse();
        response.setInnovationId(innovationId);
        response.setTemplateId(templateId);
        response.setDocumentType(documentType);
        response.setOriginalFileName(resolveAttachmentOriginalFileName(attachment, formTemplate));
        response.setPdfUrl(pdfUrl);

        // Kiểm tra CA hợp lệ của user hiện tại
        boolean isCAValid = checkCAValidForCurrentUser();
        response.setIsCAValid(isCAValid);

        if (documentType != null) {
            List<DigitalSignature> signatures = digitalSignatureRepository
                    .findByInnovationIdAndDocumentTypeWithRelations(innovationId, documentType);

            List<TemplatePdfSignerResponse> signerResponses = signatures.stream()
                    .filter(sig -> sig.getStatus() == SignatureStatusEnum.SIGNED)
                    .sorted(Comparator.comparing(DigitalSignature::getSignAt))
                    .map(this::toTemplatePdfSignerResponse)
                    .collect(Collectors.toList());

            response.setSigners(signerResponses);
        } else {
            response.setSigners(new ArrayList<>());
        }

        return response;
    }

    /*
     * Method để kiểm tra CA hợp lệ của user hiện tại
     */
    private boolean checkCAValidForCurrentUser() {
        try {
            User currentUser = userService.getCurrentUser();
            Optional<UserSignatureProfile> signatureProfileOpt = userSignatureProfileRepository
                    .findByUserId(currentUser.getId());

            if (signatureProfileOpt.isEmpty()) {
                return false;
            }

            UserSignatureProfile signatureProfile = signatureProfileOpt.get();

            // Kiểm tra CA từ relationship trước
            if (signatureProfile.getCertificateAuthority() != null) {
                String caId = signatureProfile.getCertificateAuthority().getId();
                return certificateAuthorityService.canUseCAForSigning(caId);
            }

            // Nếu không có relationship, tìm CA từ certificate issuer
            if (signatureProfile.getCertificateIssuer() != null
                    && signatureProfile.getCertificateData() != null
                    && !signatureProfile.getCertificateData().trim().isEmpty()) {
                try {
                    CertificateValidationService.CertificateInfo certInfo = certificateValidationService
                            .extractCertificateInfo(signatureProfile.getCertificateData());

                    vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.CertificateAuthority ca = certificateAuthorityService
                            .findCAByCertificateSerial(certInfo.getSerialNumber());

                    if (ca != null) {
                        return certificateAuthorityService.canUseCAForSigning(ca.getId());
                    }

                    return false;
                } catch (Exception e) {
                    return false;
                }
            }

            return false;
        } catch (Exception e) {
            return false;
        }
    }

    public DocumentTypeEnum mapTemplateTypeToDocumentType(TemplateTypeEnum templateType) {
        if (templateType == null) {
            return null;
        }

        switch (templateType) {
            case DON_DE_NGHI:
                return DocumentTypeEnum.FORM_1;
            case BAO_CAO_MO_TA:
                return DocumentTypeEnum.FORM_2;
            case BIEN_BAN_HOP:
                return DocumentTypeEnum.REPORT_MAU_3;
            case TONG_HOP_DE_NGHI:
                return DocumentTypeEnum.REPORT_MAU_4;
            case TONG_HOP_CHAM_DIEM:
                return DocumentTypeEnum.REPORT_MAU_5;
            case PHIEU_DANH_GIA:
                return DocumentTypeEnum.REPORT_MAU_7;
            default:
                return null;
        }
    }

    private String resolveAttachmentOriginalFileName(Attachment attachment, FormTemplate formTemplate) {
        if (attachment.getOriginalFileName() != null && !attachment.getOriginalFileName().isBlank()) {
            return attachment.getOriginalFileName();
        }
        if (formTemplate != null && formTemplate.getTemplateType() != null) {
            return formTemplate.getTemplateType().getValue() + ".pdf";
        }
        return attachment.getFileName();
    }

    /*
     * Method để tạo hash cho file content
     */
    public String generateDocumentHash(byte[] fileContent) {
        return keyManagementService.generateDocumentHash(fileContent);
    }

    /*
     * Method để validate timestamp của digital signature - Disabled for academic
     * project
     */
    public TimestampValidationResult validateSignatureTimestamp(String signatureId) {
        TimestampValidationResult result = new TimestampValidationResult();
        result.setValid(true);
        result.addWarning("Timestamp validation disabled for academic project");
        return result;
    }

    /*
     * Method để re-validate certificate của digital signature
     */
    public CertificateRevalidationResult revalidateSignatureCertificate(String signatureId) {
        try {
            DigitalSignature signature = digitalSignatureRepository.findById(signatureId)
                    .orElseThrow(() -> new IdInvalidException("Không tìm thấy digital signature"));

            UserSignatureProfile profile = signature.getUserSignatureProfile();
            if (profile.getCertificateData() == null || profile.getCertificateData().trim().isEmpty()) {
                CertificateRevalidationResult result = new CertificateRevalidationResult();
                result.setValid(false);
                result.addError("Digital signature không có certificate data");
                return result;
            }

            // Re-validate certificate
            CertificateValidationService.CertificateValidationResult certValidation = certificateValidationService
                    .validateX509Certificate(profile.getCertificateData());

            CertificateValidationService.CertificateExpirationResult expirationResult = certificateValidationService
                    .checkCertificateExpiration(profile.getCertificateData());

            CertificateRevalidationResult result = new CertificateRevalidationResult();
            result.setValid(certValidation.isValid() && !expirationResult.isExpired());
            result.setExpired(expirationResult.isExpired());
            result.setExpiringSoon(expirationResult.isExpiringSoon());
            result.setErrors(certValidation.getErrors());
            result.setWarnings(certValidation.getWarnings());

            // Update certificate status trong signature
            if (result.isValid()) {
                signature.setCertificateValidationStatus("VALID");
            } else if (expirationResult.isExpired()) {
                signature.setCertificateValidationStatus("EXPIRED");
            } else {
                signature.setCertificateValidationStatus("INVALID");
            }

            digitalSignatureRepository.save(signature);

            return result;

        } catch (Exception e) {
            throw new IdInvalidException("Không thể re-validate certificate: " + e.getMessage());
        }
    }

    // Inner classes for results
    public static class TimestampValidationResult {
        private boolean valid;
        private LocalDateTime timestamp;
        private List<String> errors = new ArrayList<>();
        private List<String> warnings = new ArrayList<>();

        // Getters and setters
        public boolean isValid() {
            return valid;
        }

        public void setValid(boolean valid) {
            this.valid = valid;
        }

        public LocalDateTime getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(LocalDateTime timestamp) {
            this.timestamp = timestamp;
        }

        public List<String> getErrors() {
            return errors;
        }

        public List<String> getWarnings() {
            return warnings;
        }

        public void addError(String error) {
            this.errors.add(error);
        }

        public void addWarning(String warning) {
            this.warnings.add(warning);
        }
    }

    public static class CertificateRevalidationResult {
        private boolean valid;
        private boolean expired;
        private boolean expiringSoon;
        private List<String> errors = new ArrayList<>();
        private List<String> warnings = new ArrayList<>();

        // Getters and setters
        public boolean isValid() {
            return valid;
        }

        public void setValid(boolean valid) {
            this.valid = valid;
        }

        public boolean isExpired() {
            return expired;
        }

        public void setExpired(boolean expired) {
            this.expired = expired;
        }

        public boolean isExpiringSoon() {
            return expiringSoon;
        }

        public void setExpiringSoon(boolean expiringSoon) {
            this.expiringSoon = expiringSoon;
        }

        public List<String> getErrors() {
            return errors;
        }

        public void setErrors(List<String> errors) {
            this.errors = errors;
        }

        public List<String> getWarnings() {
            return warnings;
        }

        public void setWarnings(List<String> warnings) {
            this.warnings = warnings;
        }

        public void addError(String error) {
            this.errors.add(error);
        }

        public void addWarning(String warning) {
            this.warnings.add(warning);
        }
    }

    /**
     * So sánh hai Distinguished Name (DN) strings bằng cách parse các field riêng
     * lẻ
     * Xử lý trường hợp thứ tự field khác nhau (ví dụ: C=VN, O=IUH, CN=... vs
     * CN=..., O=IUH, C=VN)
     */
    private boolean compareDistinguishedNames(String dn1, String dn2) {
        if (dn1 == null && dn2 == null) {
            return true;
        }
        if (dn1 == null || dn2 == null) {
            return false;
        }

        // Parse các field từ DN string
        Map<String, String> fields1 = parseDNFields(dn1);
        Map<String, String> fields2 = parseDNFields(dn2);

        // So sánh các field quan trọng: CN, O, C
        return Objects.equals(fields1.get("CN"), fields2.get("CN"))
                && Objects.equals(fields1.get("O"), fields2.get("O"))
                && Objects.equals(fields1.get("C"), fields2.get("C"));
    }

    /**
     * Parse Distinguished Name string thành Map các field
     * Ví dụ: "C=VN, O=IUH, CN=Test" -> {C=VN, O=IUH, CN=Test}
     */
    private Map<String, String> parseDNFields(String dn) {
        Map<String, String> fields = new HashMap<>();
        if (dn == null || dn.trim().isEmpty()) {
            return fields;
        }

        // Split theo dấu phẩy, nhưng cần xử lý trường hợp có dấu phẩy trong giá trị
        String[] parts = dn.split(",\\s*");
        for (String part : parts) {
            part = part.trim();
            int equalsIndex = part.indexOf('=');
            if (equalsIndex > 0) {
                String key = part.substring(0, equalsIndex).trim();
                String value = part.substring(equalsIndex + 1).trim();
                fields.put(key, value);
            }
        }
        return fields;
    }

    // 7. Ký số tài liệu tổng hợp cấp khoa (Mẫu 4 - TONG_HOP_DE_NGHI, Mẫu 5 -
    // TONG_HOP_CHAM_DIEM)
    public DepartmentDocumentSignResponse signDepartmentDocument(DepartmentDocumentSignRequest request) {
        DocumentTypeEnum documentType = request.getDocumentType();
        if (documentType != DocumentTypeEnum.REPORT_MAU_4 && documentType != DocumentTypeEnum.REPORT_MAU_5) {
            throw new IdInvalidException(
                    "Document type phải là REPORT_MAU_4 (Tổng hợp đề nghị) hoặc REPORT_MAU_5 (Tổng hợp chấm điểm)");
        }

        // 2. Kiểm tra department tồn tại
        vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.Department department = departmentRepository
                .findById(request.getDepartmentId())
                .orElseThrow(() -> new IdInvalidException(
                        "Không tìm thấy phòng ban với ID: " + request.getDepartmentId()));

        // 3. Validate user có role TRUONG_KHOA của departmentId đó
        User currentUser = userService.getCurrentUser();
        if (!isDepartmentHeadOf(currentUser, request.getDepartmentId())) {
            throw new IdInvalidException(
                    "Bạn không có quyền ký tài liệu này. Chỉ trưởng khoa của phòng ban " +
                            department.getDepartmentName() + " mới có thể ký.");
        }

        // 4. Lấy signature profile và validate
        UserSignatureProfile signatureProfile = userSignatureProfileRepository.findByUserId(currentUser.getId())
                .orElseThrow(() -> new IdInvalidException("Người dùng chưa có hồ sơ chữ ký số"));

        validateUserCanSign(currentUser, signatureProfile);

        // 5. Validate certificate nếu có
        if (signatureProfile.getCertificateData() != null) {
            CertificateValidationService.CertificateValidationResult certValidation = certificateValidationService
                    .validateX509Certificate(signatureProfile.getCertificateData());

            if (!certValidation.isValid()) {
                throw new IdInvalidException("Certificate không hợp lệ: " +
                        String.join(", ", certValidation.getErrors()));
            }

            CertificateValidationService.CertificateExpirationResult expirationResult = certificateValidationService
                    .checkCertificateExpiration(signatureProfile.getCertificateData());

            if (expirationResult.isExpired()) {
                throw new IdInvalidException("Certificate đã hết hạn");
            }

            validateInternalCA(signatureProfile);
            validateAndSetupCertificateIfNeeded(signatureProfile, currentUser);
        }

        // 6. Decode Base64 HTML
        String htmlContent = Utils.decode(request.getHtmlContentBase64());
        if (htmlContent == null || htmlContent.isBlank()) {
            throw new IdInvalidException("Nội dung HTML sau khi giải mã đang trống.");
        }

        // 7. Convert HTML to PDF và upload lên MinIO
        byte[] pdfBytes = pdfGeneratorService.convertHtmlToPdf(htmlContent);
        String fileName = "department_" + request.getDepartmentId() + "_" + documentType.name() + "_"
                + System.currentTimeMillis() + ".pdf";
        String pdfUrl = fileService.uploadBytes(pdfBytes, fileName, "application/pdf");

        // 8. Generate document hash
        String documentHash = keyManagementService.generateDocumentHash(htmlContent.getBytes(StandardCharsets.UTF_8));

        // 9. Generate signature hash
        String decryptedPrivateKey = hsmEncryptionService.decryptPrivateKey(signatureProfile.getEncryptedPrivateKey());
        String signatureHash = keyManagementService.generateSignature(documentHash, decryptedPrivateKey);

        // 10. Verify signature trước khi lưu
        boolean isValidSignature = keyManagementService.verifySignature(
                documentHash,
                signatureHash,
                signatureProfile.getPublicKey());

        if (!isValidSignature) {
            throw new IdInvalidException("Chữ ký không hợp lệ");
        }

        // 11. Kiểm tra idempotent
        Optional<DigitalSignature> existingByHashOpt = digitalSignatureRepository.findBySignatureHash(signatureHash);
        if (existingByHashOpt.isPresent()) {
            throw new IdInvalidException("Tài liệu này đã được ký số trước đó. Vui lòng kiểm tra lại.");
        }

        // 12. Tạo và lưu DigitalSignature (không liên kết với Innovation cụ thể)
        DigitalSignature digitalSignature = new DigitalSignature();
        digitalSignature.setDocumentType(documentType);
        digitalSignature.setSignedAsRole(UserRoleEnum.TRUONG_KHOA);
        digitalSignature.setSignatureHash(signatureHash);
        digitalSignature.setDocumentHash(documentHash);
        digitalSignature.setStatus(SignatureStatusEnum.SIGNED);
        digitalSignature.setInnovation(null);
        digitalSignature.setUser(currentUser);
        digitalSignature.setUserSignatureProfile(signatureProfile);
        digitalSignature.setCertificateValidationStatus("VALID");

        DigitalSignature savedSignature = digitalSignatureRepository.save(digitalSignature);

        // 13. Tạo hoặc cập nhật Report và lưu đường dẫn PDF
        Report report = reportRepository
                .findByDepartmentIdAndReportType(request.getDepartmentId(), documentType)
                .orElseGet(() -> {
                    Report newReport = new Report();
                    newReport.setDepartmentId(request.getDepartmentId());
                    newReport.setReportType(documentType);
                    return newReport;
                });

        report.setGeneratedPdfPath(pdfUrl);
        report.getDigitalSignatures().add(savedSignature);
        Report savedReport = reportRepository.save(report);

        // Link DigitalSignature với Report
        savedSignature.setReport(savedReport);
        digitalSignatureRepository.save(savedSignature);

        // 14. Build response
        DepartmentDocumentSignResponse response = new DepartmentDocumentSignResponse();
        response.setSignatureId(savedSignature.getId());
        response.setDocumentHash(documentHash);
        response.setSignatureHash(signatureHash);
        response.setDocumentType(documentType);
        response.setDepartmentId(request.getDepartmentId());
        response.setSignerName(currentUser.getFullName());
        response.setSignedAt(savedSignature.getSignAt());
        response.setPdfUrl(fileService.getPresignedUrl(pdfUrl, 3600));

        return response;
    }
}
