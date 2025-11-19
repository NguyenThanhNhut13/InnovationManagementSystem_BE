package vn.edu.iuh.fit.innovationmanagementsystem_be.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.CertificateAuthority;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.UserSignatureProfile;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.CAStatusEnum;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.CertificateAuthorityRepository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.UserSignatureProfileRepository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service tự động cập nhật trạng thái của Certificate
 * VERIFIED -> EXPIRED khi hết hạn
 */
@Service
@Slf4j
public class CertificateStatusSchedulerService {

    private final CertificateAuthorityRepository certificateAuthorityRepository;
    private final UserSignatureProfileRepository userSignatureProfileRepository;

    public CertificateStatusSchedulerService(
            CertificateAuthorityRepository certificateAuthorityRepository,
            UserSignatureProfileRepository userSignatureProfileRepository) {
        this.certificateAuthorityRepository = certificateAuthorityRepository;
        this.userSignatureProfileRepository = userSignatureProfileRepository;
    }

    /**
     * Chạy mỗi ngày lúc 00:06 để cập nhật trạng thái certificate
     * Chạy sau các scheduled task khác (00:01-00:05) để đảm bảo thứ tự
     * Cron format: giây phút giờ ngày tháng thứ
     */
    @Scheduled(cron = "0 6 0 * * ?")
    @Transactional
    public void updateCertificateStatuses() {
        log.info("Bắt đầu kiểm tra và cập nhật trạng thái các Certificate...");

        LocalDateTime now = LocalDateTime.now();

        int caUpdated = updateCertificateAuthorities(now);
        int profileUpdated = updateUserSignatureProfiles(now);

        log.info("Hoàn thành cập nhật trạng thái Certificate. CA: {}, UserSignatureProfile: {}",
                caUpdated, profileUpdated);
    }

    /**
     * Cập nhật các CertificateAuthority từ VERIFIED sang EXPIRED
     */
    private int updateCertificateAuthorities(LocalDateTime now) {
        List<CertificateAuthority> expiredCAs = certificateAuthorityRepository
                .findExpiredCAs(CAStatusEnum.VERIFIED, now);

        for (CertificateAuthority ca : expiredCAs) {
            if (ca.getStatus() == CAStatusEnum.VERIFIED && ca.getValidTo().isBefore(now)) {
                ca.setStatus(CAStatusEnum.EXPIRED);
                certificateAuthorityRepository.save(ca);
                log.info("Cập nhật CertificateAuthority '{}' (ID: {}, Serial: {}) từ VERIFIED sang EXPIRED",
                        ca.getName(), ca.getId(), ca.getCertificateSerial());
            }
        }

        return expiredCAs.size();
    }

    /**
     * Cập nhật các UserSignatureProfile từ VERIFIED sang EXPIRED
     */
    private int updateUserSignatureProfiles(LocalDateTime now) {
        List<UserSignatureProfile> expiredProfiles = userSignatureProfileRepository
                .findVerifiedProfilesExpired(CAStatusEnum.VERIFIED, now);

        for (UserSignatureProfile profile : expiredProfiles) {
            if (profile.getCertificateStatus() == CAStatusEnum.VERIFIED
                    && profile.getCertificateExpiryDate() != null
                    && profile.getCertificateExpiryDate().isBefore(now)) {
                profile.setCertificateStatus(CAStatusEnum.EXPIRED);
                userSignatureProfileRepository.save(profile);
                log.info("Cập nhật UserSignatureProfile (ID: {}, User: {}, Serial: {}) từ VERIFIED sang EXPIRED",
                        profile.getId(),
                        profile.getUser() != null ? profile.getUser().getId() : "N/A",
                        profile.getCertificateSerial());
            }
        }

        return expiredProfiles.size();
    }
}
