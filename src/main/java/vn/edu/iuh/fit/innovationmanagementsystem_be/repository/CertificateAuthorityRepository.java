package vn.edu.iuh.fit.innovationmanagementsystem_be.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.CertificateAuthority;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.CAStatusEnum;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CertificateAuthorityRepository extends JpaRepository<CertificateAuthority, String> {

    Optional<CertificateAuthority> findByCertificateSerial(String certificateSerial);

    List<CertificateAuthority> findByStatus(CAStatusEnum status);

    @Query("SELECT ca FROM CertificateAuthority ca WHERE ca.status = :status AND ca.validTo >= :now")
    List<CertificateAuthority> findActiveCAs(@Param("status") CAStatusEnum status, @Param("now") LocalDateTime now);

    @Query("SELECT ca FROM CertificateAuthority ca WHERE ca.status = :status AND ca.validTo < :now")
    List<CertificateAuthority> findExpiredCAs(@Param("status") CAStatusEnum status, @Param("now") LocalDateTime now);

    boolean existsByCertificateSerial(String certificateSerial);
}
