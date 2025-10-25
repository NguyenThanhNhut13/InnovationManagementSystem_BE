package vn.edu.iuh.fit.innovationmanagementsystem_be.service.certificateService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

/**
 * Service để quản lý cấu hình CA
 */
@Service
public class CAConfigurationService {

    @Value("${ca.root.certificate}")
    private String caRootCertificate;

    @Value("${ca.certificate.validity.years}")
    private int certificateValidityYears;

    @Value("${ca.csr.processing.time.days}")
    private String csrProcessingTimeDays;

    @Value("${ca.blacklisted.users}")
    private String blacklistedUsers;

    @Autowired
    private ResourceLoader resourceLoader;

    /**
     * Lấy CA Root Certificate
     */
    public String getCARootCertificate() {
        return caRootCertificate;
    }

    /**
     * Lấy CA Root Certificate từ file PEM
     */
    public String getCARootCertificateFromPem() {
        try {
            Resource resource = resourceLoader.getResource("classpath:ca-root-cert.pem");
            if (resource.exists()) {
                return StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
            }
        } catch (IOException e) {
            // Fallback to properties
        }
        return caRootCertificate;
    }

    /**
     * Lấy thời gian hiệu lực certificate (năm)
     */
    public int getCertificateValidityYears() {
        return certificateValidityYears;
    }

    /**
     * Lấy thời gian xử lý CSR
     */
    public String getCSRProcessingTimeDays() {
        return csrProcessingTimeDays;
    }

    /**
     * Lấy danh sách user bị blacklist
     */
    public List<String> getBlacklistedUsers() {
        if (blacklistedUsers == null || blacklistedUsers.trim().isEmpty()) {
            return Arrays.asList("BLACKLISTED_USER_1", "BLACKLISTED_USER_2");
        }
        return Arrays.asList(blacklistedUsers.split(","));
    }

    /**
     * Kiểm tra user có trong blacklist không
     */
    public boolean isUserBlacklisted(String userId) {
        return getBlacklistedUsers().contains(userId);
    }
}
