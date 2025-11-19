package vn.edu.iuh.fit.innovationmanagementsystem_be.utils.dataSeeder;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.CertificateAuthority;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.CertificateAuthorityRepository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.service.CertificateAuthorityService;
import vn.edu.iuh.fit.innovationmanagementsystem_be.service.KeyManagementService;

import java.security.KeyPair;
import java.time.LocalDateTime;
import java.util.Base64;

@Component
@RequiredArgsConstructor
@Slf4j
public class CASeeder implements DatabaseSeeder {

    private final CertificateAuthorityRepository certificateAuthorityRepository;
    private final CertificateAuthorityService certificateAuthorityService;
    private final KeyManagementService keyManagementService;

    @Override
    public void seed() {
        if (!isEnabled()) {
            log.info("{} seeding bị tắt.", getSeederName());
            return;
        }

        if (!isForce() && isDataExists()) {
            log.info("Dữ liệu {} đã tồn tại, bỏ qua seeding.", getConfigPrefix());
            return;
        }

        if (isForce()) {
            log.info("Force seeding: Xóa dữ liệu cũ và tạo mới...");
            certificateAuthorityRepository.deleteAll();
        }

        CertificateAuthority adminCA = createAdminCA();
        log.info("Đã seed thành công CA Admin: {} với ID: {}", adminCA.getName(), adminCA.getId());
    }

    @Override
    public int getOrder() {
        return 0;
    }

    private boolean isDataExists() {
        return certificateAuthorityRepository.count() > 0;
    }

    private CertificateAuthority createAdminCA() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime validFrom = now;
        LocalDateTime validTo = now.plusYears(1);

        KeyPair keyPair = keyManagementService.generateKeyPair();
        String publicKeyBase64 = keyManagementService.publicKeyToString(keyPair.getPublic());

        String certificateSerial = "CA-ADMIN-" + System.currentTimeMillis();
        String certificateIssuer = "CN=Innovation Management System CA, O=IUH, C=VN";
        String certificateSubject = "CN=Innovation Management System Admin CA, O=IUH, C=VN";

        String certificateData = createSimpleCertificateData(publicKeyBase64, certificateSerial,
                certificateIssuer, certificateSubject, validFrom, validTo);

        return certificateAuthorityService.createCADirectly(
                "CA Admin - Innovation Management System",
                certificateData,
                certificateSerial,
                certificateIssuer,
                certificateSubject,
                validFrom,
                validTo,
                "CA Admin được tạo tự động khi hệ thống khởi động lần đầu. Thời hạn 1 năm từ ngày tạo.");
    }

    private String createSimpleCertificateData(String publicKeyBase64, String serial,
            String issuer, String subject, LocalDateTime validFrom, LocalDateTime validTo) {
        try {
            String certInfo = String.format(
                    "Certificate Data for CA Admin\nSerial: %s\nIssuer: %s\nSubject: %s\nValid From: %s\nValid To: %s\nPublic Key: %s",
                    serial, issuer, subject, validFrom, validTo, publicKeyBase64);

            return Base64.getEncoder().encodeToString(certInfo.getBytes());
        } catch (Exception e) {
            log.error("Lỗi khi tạo certificate data: {}", e.getMessage());
            throw new RuntimeException("Không thể tạo certificate data: " + e.getMessage());
        }
    }
}
