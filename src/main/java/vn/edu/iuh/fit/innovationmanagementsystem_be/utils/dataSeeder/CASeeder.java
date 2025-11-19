package vn.edu.iuh.fit.innovationmanagementsystem_be.utils.dataSeeder;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.CertificateAuthority;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.CertificateAuthorityRepository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.service.CertificateAuthorityService;
import vn.edu.iuh.fit.innovationmanagementsystem_be.service.KeyManagementService;
import vn.edu.iuh.fit.innovationmanagementsystem_be.utils.constants.CAConstans;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Base64;
import java.util.Date;
import java.math.BigInteger;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;

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

        String certificateSerial = CAConstans.certificateSerial + System.currentTimeMillis();
        String certificateIssuer = CAConstans.certificateIssuer;
        String certificateSubject = CAConstans.certificateSubject;

        String certificateData = createX509Certificate(keyPair.getPublic(), keyPair.getPrivate(), certificateSerial,
                certificateIssuer, certificateSubject, validFrom, validTo);

        return certificateAuthorityService.createCADirectly(
                CAConstans.certificateName,
                certificateData,
                certificateSerial,
                certificateIssuer,
                certificateSubject,
                validFrom,
                validTo,
                "CA Admin được tạo tự động khi hệ thống khởi động lần đầu. Thời hạn 1 năm từ ngày tạo.");
    }

    private String createX509Certificate(PublicKey publicKey, PrivateKey privateKey, String serial,
            String issuer, String subject, LocalDateTime validFrom, LocalDateTime validTo) {
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
            log.error("Lỗi khi tạo X.509 certificate: {}", e.getMessage());
            throw new RuntimeException("Không thể tạo X.509 certificate: " + e.getMessage());
        }
    }
}
