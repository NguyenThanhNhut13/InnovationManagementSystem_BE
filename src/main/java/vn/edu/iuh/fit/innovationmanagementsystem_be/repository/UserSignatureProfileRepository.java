package vn.edu.iuh.fit.innovationmanagementsystem_be.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.UserSignatureProfile;

import java.util.Optional;

@Repository
public interface UserSignatureProfileRepository extends JpaRepository<UserSignatureProfile, String> {

    // Tìm hồ sơ chữ ký theo user ID
    Optional<UserSignatureProfile> findByUserId(String userId);

    // Kiểm tra user đã có hồ sơ chữ ký chưa
    boolean existsByUserId(String userId);

    // Tìm hồ sơ chữ ký theo certificate serial
    Optional<UserSignatureProfile> findByCertificateSerial(String certificateSerial);
}
