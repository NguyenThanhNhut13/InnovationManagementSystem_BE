package vn.edu.iuh.fit.innovationmanagementsystem_be.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.CertificateRevocation;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.RevocationReasonEnum;

import java.util.List;
import java.util.Optional;

@Repository
public interface CertificateRevocationRepository extends JpaRepository<CertificateRevocation, String> {

    boolean existsByCertificateSerial(String certificateSerial);

    Optional<CertificateRevocation> findByCertificateSerial(String certificateSerial);

    List<CertificateRevocation> findByUserId(String userId);

    List<CertificateRevocation> findByRevocationReason(RevocationReasonEnum reason);

    void deleteByCertificateSerial(String certificateSerial);
}
