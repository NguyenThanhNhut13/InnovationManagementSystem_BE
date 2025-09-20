package vn.edu.iuh.fit.innovationmanagementsystem_be.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.UserSignatureProfile;

import java.util.Optional;

@Repository
public interface UserSignatureProfileRepository extends JpaRepository<UserSignatureProfile, String> {

    Optional<UserSignatureProfile> findByUserId(String userId);

    boolean existsByUserId(String userId);

    Optional<UserSignatureProfile> findByCertificateSerial(String certificateSerial);
}
