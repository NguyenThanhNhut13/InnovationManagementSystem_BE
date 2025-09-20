package vn.edu.iuh.fit.innovationmanagementsystem_be.repository;

import org.springframework.data.jpa.repository.JpaRepository;
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

        List<DigitalSignature> findByInnovationIdAndDocumentType(String innovationId, DocumentTypeEnum documentType);

        List<DigitalSignature> findByInnovationIdAndDocumentTypeAndSignedAsRole(
                        String innovationId,
                        DocumentTypeEnum documentType,
                        UserRoleEnum signedAsRole);

        List<DigitalSignature> findByInnovationIdAndDocumentTypeAndStatus(
                        String innovationId,
                        DocumentTypeEnum documentType,
                        SignatureStatusEnum status);

        List<DigitalSignature> findByUserIdAndDocumentType(String userId, DocumentTypeEnum documentType);

        boolean existsByInnovationIdAndDocumentTypeAndUserIdAndStatus(
                        String innovationId,
                        DocumentTypeEnum documentType,
                        String userId,
                        SignatureStatusEnum status);

        // Tìm chữ ký theo innovation với eager loading
        @Query("SELECT ds FROM DigitalSignature ds " +
                        "LEFT JOIN FETCH ds.user u " +
                        "LEFT JOIN FETCH ds.userSignatureProfile usp " +
                        "WHERE ds.innovation.id = :innovationId")
        List<DigitalSignature> findByInnovationIdWithRelations(@Param("innovationId") String innovationId);

        // Tìm chữ ký theo innovation và document type với eager loading
        @Query("SELECT ds FROM DigitalSignature ds " +
                        "LEFT JOIN FETCH ds.user u " +
                        "LEFT JOIN FETCH ds.userSignatureProfile usp " +
                        "LEFT JOIN FETCH ds.innovation i " +
                        "WHERE ds.innovation.id = :innovationId AND ds.documentType = :documentType")
        List<DigitalSignature> findByInnovationIdAndDocumentTypeWithRelations(
                        @Param("innovationId") String innovationId,
                        @Param("documentType") DocumentTypeEnum documentType);

        // Đếm số chữ ký cần thiết cho một document type
        @Query("SELECT COUNT(ds) FROM DigitalSignature ds " +
                        "WHERE ds.innovation.id = :innovationId " +
                        "AND ds.documentType = :documentType " +
                        "AND ds.status = :status")
        long countByInnovationIdAndDocumentTypeAndStatus(
                        @Param("innovationId") String innovationId,
                        @Param("documentType") DocumentTypeEnum documentType,
                        @Param("status") SignatureStatusEnum status);

        Optional<DigitalSignature> findBySignatureHash(String signatureHash);

        List<DigitalSignature> findByDocumentHash(String documentHash);

        boolean existsByInnovationIdAndDocumentTypeAndSignedAsRoleAndStatus(
                        String innovationId,
                        DocumentTypeEnum documentType,
                        UserRoleEnum signedAsRole,
                        SignatureStatusEnum status);
}
