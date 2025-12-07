package vn.edu.iuh.fit.innovationmanagementsystem_be.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.DigitalSignature;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.DocumentTypeEnum;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.SignatureStatusEnum;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.UserRoleEnum;

import java.util.List;
import java.util.Optional;

@Repository
public interface DigitalSignatureRepository extends JpaRepository<DigitalSignature, String> {

        boolean existsByInnovationIdAndDocumentTypeAndUserIdAndStatus(
                        String innovationId,
                        DocumentTypeEnum documentType,
                        String userId,
                        SignatureStatusEnum status);

        @Query("SELECT ds FROM DigitalSignature ds " +
                        "LEFT JOIN FETCH ds.user u " +
                        "LEFT JOIN FETCH ds.userSignatureProfile usp " +
                        "WHERE ds.innovation.id = :innovationId")
        List<DigitalSignature> findByInnovationIdWithRelations(@Param("innovationId") String innovationId);

        @Query("SELECT ds FROM DigitalSignature ds " +
                        "LEFT JOIN FETCH ds.user u " +
                        "LEFT JOIN FETCH ds.userSignatureProfile usp " +
                        "LEFT JOIN FETCH ds.innovation i " +
                        "WHERE ds.innovation.id = :innovationId AND ds.documentType = :documentType")
        List<DigitalSignature> findByInnovationIdAndDocumentTypeWithRelations(
                        @Param("innovationId") String innovationId,
                        @Param("documentType") DocumentTypeEnum documentType);

        boolean existsByInnovationIdAndDocumentTypeAndSignedAsRoleAndStatus(
                        String innovationId,
                        DocumentTypeEnum documentType,
                        UserRoleEnum signedAsRole,
                        SignatureStatusEnum status);

        Optional<DigitalSignature> findBySignatureHash(String signatureHash);

        @Modifying
        @Query("DELETE FROM DigitalSignature ds WHERE ds.innovation.id = :innovationId")
        void deleteByInnovationId(@Param("innovationId") String innovationId);

        @Query("SELECT CASE WHEN COUNT(ds) > 0 THEN true ELSE false END FROM DigitalSignature ds " +
                        "WHERE ds.report.departmentId = :departmentId " +
                        "AND ds.documentType = :documentType " +
                        "AND ds.user.id = :userId " +
                        "AND ds.status = :status")
        boolean existsByReportDepartmentIdAndDocumentTypeAndUserIdAndStatus(
                        @Param("departmentId") String departmentId,
                        @Param("documentType") DocumentTypeEnum documentType,
                        @Param("userId") String userId,
                        @Param("status") SignatureStatusEnum status);

        @Query("SELECT CASE WHEN COUNT(ds) > 0 THEN true ELSE false END FROM DigitalSignature ds " +
                        "WHERE ds.report.id = :reportId " +
                        "AND ds.signedAsRole = :signedAsRole " +
                        "AND ds.status = :status")
        boolean existsByReportIdAndSignedAsRoleAndStatus(
                        @Param("reportId") String reportId,
                        @Param("signedAsRole") UserRoleEnum signedAsRole,
                        @Param("status") SignatureStatusEnum status);

        @Query("SELECT ds FROM DigitalSignature ds " +
                        "LEFT JOIN FETCH ds.user u " +
                        "LEFT JOIN FETCH ds.userSignatureProfile usp " +
                        "WHERE ds.report.id = :reportId " +
                        "AND ds.signedAsRole = :signedAsRole " +
                        "AND ds.status = :status " +
                        "ORDER BY ds.signAt DESC")
        List<DigitalSignature> findByReportIdAndSignedAsRoleAndStatusWithRelations(
                        @Param("reportId") String reportId,
                        @Param("signedAsRole") UserRoleEnum signedAsRole,
                        @Param("status") SignatureStatusEnum status);

        /**
         * Tìm các departmentId có TRUONG_KHOA đã ký đủ 3 loại report (MAU_3, MAU_4,
         * MAU_5)
         * Chỉ chấp nhận TRUONG_KHOA ký (không phải TV_HOI_DONG_KHOA)
         * 
         * @return Danh sách departmentId đã hoàn tất ký báo cáo cấp khoa
         */
        @Query("SELECT r.departmentId FROM DigitalSignature ds " +
                        "JOIN ds.report r " +
                        "WHERE ds.signedAsRole = vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.UserRoleEnum.TRUONG_KHOA "
                        +
                        "AND ds.status = vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.SignatureStatusEnum.SIGNED "
                        +
                        "AND r.reportType IN (vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.DocumentTypeEnum.REPORT_MAU_3, "
                        +
                        "vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.DocumentTypeEnum.REPORT_MAU_4, "
                        +
                        "vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.DocumentTypeEnum.REPORT_MAU_5) "
                        +
                        "GROUP BY r.departmentId " +
                        "HAVING COUNT(DISTINCT r.reportType) = 3")
        List<String> findDepartmentIdsWithAllReportsSigned();
}
