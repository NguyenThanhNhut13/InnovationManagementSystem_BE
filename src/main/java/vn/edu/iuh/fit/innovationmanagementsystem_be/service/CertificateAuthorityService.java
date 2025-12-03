package vn.edu.iuh.fit.innovationmanagementsystem_be.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.CertificateAuthority;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.CAStatusEnum;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.CreateCertificateAuthorityRequest;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.CertificateAuthorityResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.exception.IdInvalidException;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.CertificateAuthorityRepository;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class CertificateAuthorityService {

    private static final Logger logger = LoggerFactory.getLogger(CertificateAuthorityService.class);

    @Autowired
    private CertificateAuthorityRepository certificateAuthorityRepository;

    @Autowired
    private CertificateValidationService certificateValidationService;

    @Autowired
    @Lazy
    private UserService userService;

    // 1. Tạo CA mới
    public CertificateAuthorityResponse createCertificateAuthority(CreateCertificateAuthorityRequest request) {
        try {
            // Validate certificate
            CertificateValidationService.CertificateValidationResult validationResult = certificateValidationService
                    .validateX509Certificate(request.getCertificateData());

            if (!validationResult.isValid()) {
                throw new IdInvalidException("Certificate không hợp lệ: " +
                        String.join(", ", validationResult.getErrors()));
            }

            // Extract certificate info
            CertificateValidationService.CertificateInfo certInfo = certificateValidationService
                    .extractCertificateInfo(request.getCertificateData());

            // Check if certificate serial already exists
            if (certificateAuthorityRepository.existsByCertificateSerial(certInfo.getSerialNumber())) {
                throw new IdInvalidException("CA với serial number này đã tồn tại");
            }

            // Create CA entity
            CertificateAuthority ca = new CertificateAuthority();
            ca.setName(request.getName());
            ca.setCertificateData(request.getCertificateData());
            ca.setCertificateSerial(certInfo.getSerialNumber());
            ca.setCertificateIssuer(certInfo.getIssuer());
            ca.setCertificateSubject(certInfo.getSubject());
            ca.setValidFrom(certInfo.getNotBefore().toInstant()
                    .atZone(ZoneId.systemDefault()).toLocalDateTime());
            ca.setValidTo(certInfo.getNotAfter().toInstant()
                    .atZone(ZoneId.systemDefault()).toLocalDateTime());
            ca.setStatus(CAStatusEnum.PENDING);
            ca.setDescription(request.getDescription());

            ca = certificateAuthorityRepository.save(ca);

            return toResponse(ca);

        } catch (Exception e) {
            throw new IdInvalidException("Không thể tạo CA: " + e.getMessage());
        }
    }

    // 2. Xác minh CA
    public CertificateAuthorityResponse verifyCertificateAuthority(String caId) {
        try {
            CertificateAuthority ca = certificateAuthorityRepository.findById(caId)
                    .orElseThrow(() -> new IdInvalidException("Không tìm thấy CA với ID: " + caId));

            if (ca.getStatus() == CAStatusEnum.VERIFIED) {
                throw new IdInvalidException("CA đã được xác minh rồi");
            }

            if (ca.getStatus() == CAStatusEnum.REVOKED) {
                throw new IdInvalidException("CA đã bị thu hồi, không thể xác minh");
            }

            // Validate certificate again
            CertificateValidationService.CertificateValidationResult validationResult = certificateValidationService
                    .validateX509Certificate(ca.getCertificateData());

            if (!validationResult.isValid()) {
                throw new IdInvalidException("Certificate không hợp lệ: " +
                        String.join(", ", validationResult.getErrors()));
            }

            // Update status
            ca.setStatus(CAStatusEnum.VERIFIED);
            ca.setVerifiedAt(LocalDateTime.now());
            ca.setVerifiedBy(userService.getCurrentUser().getId());

            ca = certificateAuthorityRepository.save(ca);

            return toResponse(ca);

        } catch (Exception e) {
            throw new IdInvalidException("Không thể xác minh CA: " + e.getMessage());
        }
    }

    // 3. Thu hồi CA
    public CertificateAuthorityResponse revokeCertificateAuthority(String caId) {
        try {
            CertificateAuthority ca = certificateAuthorityRepository.findById(caId)
                    .orElseThrow(() -> new IdInvalidException("Không tìm thấy CA với ID: " + caId));

            if (ca.getStatus() == CAStatusEnum.REVOKED) {
                throw new IdInvalidException("CA đã bị thu hồi rồi");
            }

            ca.setStatus(CAStatusEnum.REVOKED);
            ca = certificateAuthorityRepository.save(ca);

            return toResponse(ca);

        } catch (Exception e) {
            throw new IdInvalidException("Không thể thu hồi CA: " + e.getMessage());
        }
    }

    // 4. Kiểm tra trạng thái CA
    public CertificateAuthorityResponse checkCAStatus(String caId) {
        try {
            CertificateAuthority ca = certificateAuthorityRepository.findById(caId)
                    .orElseThrow(() -> new IdInvalidException("Không tìm thấy CA với ID: " + caId));

            // Check expiration and update status if needed
            updateCAStatusIfExpired(ca);

            return toResponse(ca);

        } catch (Exception e) {
            throw new IdInvalidException("Không thể kiểm tra trạng thái CA: " + e.getMessage());
        }
    }

    // 5. Lấy tất cả CA
    public List<CertificateAuthorityResponse> getAllCAs() {
        try {
            List<CertificateAuthority> cas = certificateAuthorityRepository.findAll();

            // Update status for all CAs
            cas.forEach(this::updateCAStatusIfExpired);

            return cas.stream()
                    .map(this::toResponse)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            throw new IdInvalidException("Không thể lấy danh sách CA: " + e.getMessage());
        }
    }

    // 6. Lấy CA theo ID (Response)
    public CertificateAuthorityResponse getCAById(String caId) {
        try {
            CertificateAuthority ca = certificateAuthorityRepository.findById(caId)
                    .orElseThrow(() -> new IdInvalidException("Không tìm thấy CA với ID: " + caId));

            updateCAStatusIfExpired(ca);

            return toResponse(ca);

        } catch (Exception e) {
            throw new IdInvalidException("Không thể lấy CA: " + e.getMessage());
        }
    }

    // 6.1. Lấy CA entity theo ID
    public CertificateAuthority findCAById(String caId) {
        return certificateAuthorityRepository.findById(caId)
                .orElse(null);
    }

    // 7. Lấy CA đã được xác minh
    public List<CertificateAuthorityResponse> getVerifiedCAs() {
        try {
            List<CertificateAuthority> cas = certificateAuthorityRepository.findByStatus(CAStatusEnum.VERIFIED);

            cas.forEach(this::updateCAStatusIfExpired);

            return cas.stream()
                    .map(this::toResponse)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            throw new IdInvalidException("Không thể lấy danh sách CA đã xác minh: " + e.getMessage());
        }
    }

    // 8. Kiểm tra CA có thể dùng để ký số không
    public boolean canUseCAForSigning(String caId) {
        try {
            CertificateAuthority ca = certificateAuthorityRepository.findById(caId)
                    .orElseThrow(() -> new IdInvalidException("Không tìm thấy CA với ID: " + caId));

            updateCAStatusIfExpired(ca);

            // CA phải được xác minh
            if (ca.getStatus() != CAStatusEnum.VERIFIED) {
                return false;
            }

            // Nếu CA hết hạn, không cho phép ký
            if (ca.getStatus() == CAStatusEnum.EXPIRED) {
                return false;
            }

            // CA còn hạn và đã được xác minh
            return true;

        } catch (Exception e) {
            logger.error("Lỗi khi kiểm tra CA có thể dùng để ký số: " + e.getMessage());
            return false;
        }
    }

    // 9. Kiểm tra CA từ certificate serial
    public CertificateAuthority findCAByCertificateSerial(String certificateSerial) {
        return certificateAuthorityRepository.findByCertificateSerial(certificateSerial)
                .orElse(null);
    }

    // 10. Lấy CA đang hoạt động (VERIFIED và chưa hết hạn)
    public CertificateAuthority getActiveCA() {
        LocalDateTime now = LocalDateTime.now();
        List<CertificateAuthority> activeCAs = certificateAuthorityRepository
                .findActiveCAs(CAStatusEnum.VERIFIED, now);

        if (activeCAs.isEmpty()) {
            return null;
        }

        return activeCAs.get(0);
    }

    // 11. Tạo CA trực tiếp (dùng cho seeder)
    public CertificateAuthority createCADirectly(String name, String certificateData,
            String certificateSerial, String certificateIssuer, String certificateSubject,
            LocalDateTime validFrom, LocalDateTime validTo, String description) {
        CertificateAuthority ca = new CertificateAuthority();
        ca.setName(name);
        ca.setCertificateData(certificateData);
        ca.setCertificateSerial(certificateSerial);
        ca.setCertificateIssuer(certificateIssuer);
        ca.setCertificateSubject(certificateSubject);
        ca.setValidFrom(validFrom);
        ca.setValidTo(validTo);
        ca.setStatus(CAStatusEnum.VERIFIED);
        ca.setVerifiedAt(LocalDateTime.now());
        ca.setVerifiedBy("SYSTEM");
        ca.setDescription(description);

        return certificateAuthorityRepository.save(ca);
    }

    // Helper method: Cập nhật trạng thái CA nếu hết hạn
    private void updateCAStatusIfExpired(CertificateAuthority ca) {
        LocalDateTime now = LocalDateTime.now();

        if (ca.getValidTo().isBefore(now) && ca.getStatus() != CAStatusEnum.REVOKED) {
            if (ca.getStatus() == CAStatusEnum.VERIFIED) {
                ca.setStatus(CAStatusEnum.EXPIRED);
                certificateAuthorityRepository.save(ca);
            }
        }
    }

    // Helper method: Convert entity to response
    private CertificateAuthorityResponse toResponse(CertificateAuthority ca) {
        CertificateAuthorityResponse response = new CertificateAuthorityResponse();
        response.setId(ca.getId());
        response.setName(ca.getName());
        response.setCertificateSerial(ca.getCertificateSerial());
        response.setCertificateIssuer(ca.getCertificateIssuer());
        response.setCertificateSubject(ca.getCertificateSubject());
        response.setValidFrom(ca.getValidFrom());
        response.setValidTo(ca.getValidTo());
        response.setStatus(ca.getStatus());
        response.setVerifiedAt(ca.getVerifiedAt());
        response.setVerifiedBy(ca.getVerifiedBy());
        response.setDescription(ca.getDescription());
        response.setCreatedAt(ca.getCreatedAt());
        response.setUpdatedAt(ca.getUpdatedAt());

        // Calculate expiration info
        LocalDateTime now = LocalDateTime.now();
        response.setExpired(ca.getValidTo().isBefore(now));
        long daysUntilExpiry = java.time.Duration.between(now, ca.getValidTo()).toDays();
        response.setDaysUntilExpiry(daysUntilExpiry);

        return response;
    }
}
