package vn.edu.iuh.fit.innovationmanagementsystem_be.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.CertificateRevocation;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.User;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.UserSignatureProfile;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.RevocationReasonEnum;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.UserStatusEnum;
import vn.edu.iuh.fit.innovationmanagementsystem_be.exception.IdInvalidException;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.CertificateRevocationRepository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.UserRepository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.UserSignatureProfileRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class CertificateRevocationService {

    private static final Logger logger = LoggerFactory.getLogger(CertificateRevocationService.class);

    private final CertificateRevocationRepository revocationRepository;
    private final UserSignatureProfileRepository profileRepository;
    private final UserRepository userRepository;

    public CertificateRevocationService(
            CertificateRevocationRepository revocationRepository,
            UserSignatureProfileRepository profileRepository,
            UserRepository userRepository) {
        this.revocationRepository = revocationRepository;
        this.profileRepository = profileRepository;
        this.userRepository = userRepository;
    }

    // 1. Thu hồi certificate khi user không còn hiệu lực
    public void revokeCertificateByUserId(String userId, RevocationReasonEnum reason) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IdInvalidException("User không tồn tại"));

        Optional<UserSignatureProfile> profileOpt = profileRepository.findByUserId(userId);
        if (profileOpt.isEmpty()) {
            logger.info("User {} không có signature profile, không cần thu hồi", userId);
            return;
        }

        UserSignatureProfile profile = profileOpt.get();
        if (profile.getCertificateSerial() == null || profile.getCertificateSerial().isBlank()) {
            logger.info("User {} chưa có certificate serial, không cần thu hồi", userId);
            return;
        }

        // Kiểm tra đã thu hồi chưa
        if (revocationRepository.existsByCertificateSerial(profile.getCertificateSerial())) {
            logger.info("Certificate {} đã được thu hồi trước đó", profile.getCertificateSerial());
            return;
        }

        // Tạo bản ghi thu hồi
        CertificateRevocation revocation = new CertificateRevocation();
        revocation.setCertificateSerial(profile.getCertificateSerial());
        revocation.setUser(user);
        revocation.setRevocationDate(LocalDateTime.now());
        revocation.setRevocationReason(reason);
        revocation.setRevokedBy(getCurrentUserId());
        revocation.setNotes("Auto-revoked by system on user status change");

        revocationRepository.save(revocation);

        logger.info("Revoked certificate {} for user {} - Reason: {}",
                profile.getCertificateSerial(), userId, reason);
    }

    // 2. Thu hồi certificate khi đổi status user
    public void revokeOnStatusChange(User user, UserStatusEnum newStatus) {
        if (newStatus == UserStatusEnum.ACTIVE) {
            return; // Không thu hồi nếu chuyển sang ACTIVE
        }

        RevocationReasonEnum reason = switch (newStatus) {
            case INACTIVE -> RevocationReasonEnum.USER_INACTIVE;
            case SUSPENDED -> RevocationReasonEnum.USER_SUSPENDED;
            default -> RevocationReasonEnum.CESSATION_OF_OPERATION;
        };

        try {
            revokeCertificateByUserId(user.getId(), reason);
        } catch (Exception e) {
            logger.warn("Failed to revoke certificate for user {}: {}", user.getId(), e.getMessage());
        }
    }

    // 3. Kiểm tra certificate có bị thu hồi không
    @Transactional(readOnly = true)
    public boolean isCertificateRevoked(String certificateSerial) {
        if (certificateSerial == null || certificateSerial.isBlank()) {
            return false;
        }
        return revocationRepository.existsByCertificateSerial(certificateSerial);
    }

    // 4. Lấy thông tin thu hồi
    @Transactional(readOnly = true)
    public Optional<CertificateRevocation> getRevocationInfo(String certificateSerial) {
        if (certificateSerial == null || certificateSerial.isBlank()) {
            return Optional.empty();
        }
        return revocationRepository.findByCertificateSerial(certificateSerial);
    }

    // 5. Khôi phục certificate (khi user được active lại)
    public void restoreCertificate(String userId) {
        Optional<UserSignatureProfile> profileOpt = profileRepository.findByUserId(userId);
        if (profileOpt.isEmpty()) {
            logger.info("User {} không có signature profile, không cần khôi phục", userId);
            return;
        }

        UserSignatureProfile profile = profileOpt.get();
        if (profile.getCertificateSerial() == null || profile.getCertificateSerial().isBlank()) {
            logger.info("User {} chưa có certificate serial, không cần khôi phục", userId);
            return;
        }

        // Xóa bản ghi thu hồi nếu có
        Optional<CertificateRevocation> revocationOpt = revocationRepository
                .findByCertificateSerial(profile.getCertificateSerial());

        if (revocationOpt.isPresent()) {
            revocationRepository.delete(revocationOpt.get());
            logger.info("Restored certificate {} for user {}", profile.getCertificateSerial(), userId);
        } else {
            logger.info("Certificate {} chưa bị thu hồi, không cần khôi phục", profile.getCertificateSerial());
        }
    }

    // 6. Lấy danh sách certificate đã thu hồi của user
    @Transactional(readOnly = true)
    public List<CertificateRevocation> getUserRevokedCertificates(String userId) {
        return revocationRepository.findByUserId(userId);
    }

    // 7. Lấy tất cả certificate đã thu hồi (cho admin)
    @Transactional(readOnly = true)
    public List<CertificateRevocation> getAllRevokedCertificates() {
        return revocationRepository.findAll();
    }

    // Helper: Lấy current user ID (luôn return SYSTEM để tránh circular dependency)
    private String getCurrentUserId() {
        return "SYSTEM";
    }
}
