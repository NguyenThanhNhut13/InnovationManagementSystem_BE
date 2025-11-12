package vn.edu.iuh.fit.innovationmanagementsystem_be.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.UserSignatureProfile;

import java.util.Optional;

@Repository
public interface UserSignatureProfileRepository extends JpaRepository<UserSignatureProfile, String> {

    Optional<UserSignatureProfile> findByUserId(String userId);

    @Query("SELECT usp FROM UserSignatureProfile usp " +
            "LEFT JOIN FETCH usp.user u " +
            "WHERE usp.user.id = :userId")
    Optional<UserSignatureProfile> findByUserIdWithUser(@Param("userId") String userId);

    boolean existsByUserId(String userId);

    Optional<UserSignatureProfile> findByCertificateSerial(String certificateSerial);
}
