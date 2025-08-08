package vn.edu.iuh.fit.innovationmanagementsystem_be.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.DigitalSignature;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.DocumentTypeEnum;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.RoleEnum;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.SignatureStatusEnum;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface DigitalSignatureRepository extends JpaRepository<DigitalSignature, String> {

        // Tìm signature theo innovation
        @Query("SELECT ds FROM DigitalSignature ds WHERE ds.innovation.id = :innovationId")
        List<DigitalSignature> findByInnovationId(@Param("innovationId") String innovationId);

        // Tìm signature theo user
        @Query("SELECT ds FROM DigitalSignature ds WHERE ds.user.id = :userId")
        List<DigitalSignature> findByUserId(@Param("userId") String userId);

        // Tìm signature theo user signature profile
        @Query("SELECT ds FROM DigitalSignature ds WHERE ds.userSignatureProfile.id = :profileId")
        List<DigitalSignature> findByUserSignatureProfileId(@Param("profileId") String profileId);

        // Tìm signature theo report
        @Query("SELECT ds FROM DigitalSignature ds WHERE ds.report.id = :reportId")
        List<DigitalSignature> findByReportId(@Param("reportId") String reportId);

        // Tìm signature theo loại tài liệu
        List<DigitalSignature> findByDocumentType(DocumentTypeEnum documentType);

        // Tìm signature theo vai trò ký
        List<DigitalSignature> findBySignedAsRole(RoleEnum signedAsRole);

        // Tìm signature theo thời gian ký
        @Query("SELECT ds FROM DigitalSignature ds WHERE ds.signAt BETWEEN :startDate AND :endDate")
        List<DigitalSignature> findBySignAtBetween(@Param("startDate") LocalDateTime startDate,
                        @Param("endDate") LocalDateTime endDate);

        // Đếm signature theo user
        @Query("SELECT COUNT(ds) FROM DigitalSignature ds WHERE ds.user.id = :userId")
        long countByUserId(@Param("userId") String userId);

        // Kiểm tra signature hash đã tồn tại chưa
        boolean existsBySignatureHash(String signatureHash);

        // Tìm signature theo innovation round (thông qua innovation)
        @Query("SELECT ds FROM DigitalSignature ds WHERE ds.innovation.innovationRound.id = :roundId")
        List<DigitalSignature> findByInnovationRoundId(@Param("roundId") String roundId);

        // Tìm signature theo department (thông qua user)
        @Query("SELECT ds FROM DigitalSignature ds WHERE ds.user.department.id = :departmentId")
        List<DigitalSignature> findByDepartmentId(@Param("departmentId") String departmentId);
}
