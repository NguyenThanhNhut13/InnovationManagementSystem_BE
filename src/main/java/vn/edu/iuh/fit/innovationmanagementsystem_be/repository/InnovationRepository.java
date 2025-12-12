package vn.edu.iuh.fit.innovationmanagementsystem_be.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.Innovation;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.InnovationStatusEnum;

@Repository
public interface InnovationRepository extends JpaRepository<Innovation, String>, JpaSpecificationExecutor<Innovation> {

        Page<Innovation> findByUserId(String userId, Pageable pageable);

        // Thống kê innovation cho giảng viên
        long countByUserId(String userId);

        @Query("SELECT COUNT(i) FROM Innovation i WHERE i.user.id = :userId AND i.status = :status")
        long countByUserIdAndStatus(@Param("userId") String userId, @Param("status") InnovationStatusEnum status);

        @Query("SELECT COUNT(i) FROM Innovation i WHERE i.user.id = :userId AND i.status IN :statuses")
        long countByUserIdAndStatusIn(@Param("userId") String userId,
                        @Param("statuses") List<InnovationStatusEnum> statuses);

        @Query("SELECT COUNT(i) FROM Innovation i WHERE i.user.id = :userId AND i.innovationRound.id = :roundId AND i.status = :status")
        long countByUserIdAndRoundIdAndStatus(@Param("userId") String userId, @Param("roundId") String roundId,
                        @Param("status") InnovationStatusEnum status);

        // Thống kê phần trăm kết quả sáng kiến đã nộp
        @Query("SELECT COUNT(i) FROM Innovation i WHERE i.user.id = :userId AND i.status IN ('SUBMITTED', 'DRAFT')")
        long countPendingInnovationsByUserId(@Param("userId") String userId);

        // Thống kê sáng kiến theo năm học
        @Query("SELECT ir.academicYear, COUNT(i) FROM Innovation i JOIN i.innovationRound ir WHERE i.user.id = :userId GROUP BY ir.academicYear ORDER BY ir.academicYear")
        List<Object[]> countInnovationsByAcademicYearAndUserId(@Param("userId") String userId);

        @Query("SELECT ir.academicYear, COUNT(i) FROM Innovation i JOIN i.innovationRound ir WHERE i.user.id = :userId AND i.status IN ('DRAFT', 'SUBMITTED', 'PENDING_KHOA_REVIEW', 'KHOA_APPROVED', 'PENDING_TRUONG_REVIEW') GROUP BY ir.academicYear ORDER BY ir.academicYear")
        List<Object[]> countSubmittedInnovationsByAcademicYearAndUserId(@Param("userId") String userId);

        @Query("SELECT ir.academicYear, COUNT(i) FROM Innovation i JOIN i.innovationRound ir WHERE i.user.id = :userId AND i.status IN ('TRUONG_APPROVED', 'FINAL_APPROVED') GROUP BY ir.academicYear ORDER BY ir.academicYear")
        List<Object[]> countApprovedInnovationsByAcademicYearAndUserId(@Param("userId") String userId);

        @Query("SELECT ir.academicYear, COUNT(i) FROM Innovation i JOIN i.innovationRound ir WHERE i.user.id = :userId AND i.status IN ('KHOA_REJECTED', 'TRUONG_REJECTED') GROUP BY ir.academicYear ORDER BY ir.academicYear")
        List<Object[]> countRejectedInnovationsByAcademicYearAndUserId(@Param("userId") String userId);

        @Query("SELECT ir.academicYear, COUNT(i) FROM Innovation i JOIN i.innovationRound ir WHERE i.user.id = :userId AND i.status IN ('SUBMITTED', 'DRAFT') GROUP BY ir.academicYear ORDER BY ir.academicYear")
        List<Object[]> countPendingInnovationsByAcademicYearAndUserId(@Param("userId") String userId);

        // Lấy danh sách innovations theo roundId và status
        @Query("SELECT i FROM Innovation i WHERE i.innovationRound.id = :roundId AND i.status = :status")
        List<Innovation> findByRoundIdAndStatus(
                        @Param("roundId") String roundId,
                        @Param("status") InnovationStatusEnum status);

        // Lấy innovations của council với user và department đã được fetch (để tránh
        // LazyInitializationException)
        @Query("SELECT DISTINCT i FROM Innovation i " +
                        "LEFT JOIN FETCH i.user " +
                        "LEFT JOIN FETCH i.department " +
                        "JOIN i.councils c " +
                        "WHERE c.id = :councilId")
        List<Innovation> findByCouncilIdWithUserAndDepartment(@Param("councilId") String councilId);

        /**
         * Tìm innovations theo departmentId, roundId và status
         * (để cập nhật status khi phase SCORING bắt đầu)
         */
        @Query("SELECT i FROM Innovation i " +
                        "WHERE i.department.id = :departmentId " +
                        "AND i.innovationRound.id = :roundId " +
                        "AND i.status = :status")
        List<Innovation> findByDepartmentIdAndRoundIdAndStatus(
                        @Param("departmentId") String departmentId,
                        @Param("roundId") String roundId,
                        @Param("status") InnovationStatusEnum status);

        /**
         * Lấy innovations của department đã được gán vào council
         * (để TRUONG_KHOA ký Mẫu 2)
         */
        @Query("SELECT DISTINCT i FROM Innovation i " +
                        "LEFT JOIN FETCH i.user " +
                        "LEFT JOIN FETCH i.department " +
                        "JOIN i.councils c " +
                        "WHERE i.department.id = :departmentId")
        List<Innovation> findByDepartmentIdWithCouncils(@Param("departmentId") String departmentId);

        /**
         * Lấy danh sách sáng kiến SUBMITTED của department đã được GIANG_VIEN ký mẫu 2
         * (bao gồm cả đã được TRUONG_KHOA ký và chưa được TRUONG_KHOA ký)
         * (cho API list của TRUONG_KHOA để filter theo trạng thái ký)
         */
        @Query("SELECT DISTINCT i FROM Innovation i " +
                        "LEFT JOIN FETCH i.user " +
                        "LEFT JOIN FETCH i.department " +
                        "LEFT JOIN FETCH i.innovationRound " +
                        "WHERE i.department.id = :departmentId " +
                        "AND i.status = vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.InnovationStatusEnum.SUBMITTED "
                        +
                        "AND EXISTS (" +
                        "    SELECT 1 FROM DigitalSignature ds " +
                        "    WHERE ds.innovation = i " +
                        "    AND ds.documentType = vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.DocumentTypeEnum.FORM_2 "
                        +
                        "    AND ds.signedAsRole = vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.UserRoleEnum.GIANG_VIEN "
                        +
                        "    AND ds.status = vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.SignatureStatusEnum.SIGNED"
                        +
                        ")")
        List<Innovation> findInnovationsPendingDepartmentHeadSignature(@Param("departmentId") String departmentId);

        /**
         * Tìm innovations KHOA_APPROVED theo danh sách departmentIds và roundId
         * (để lấy sáng kiến từ các khoa đã hoàn tất quy trình cấp khoa)
         * 
         * @param departmentIds Danh sách departmentId đã hoàn tất ký báo cáo
         * @param roundId       ID của innovation round hiện tại
         * @param pageable      Thông tin phân trang
         * @return Page chứa innovations đã được khoa phê duyệt
         */
        @Query("SELECT i FROM Innovation i " +
                        "LEFT JOIN FETCH i.user u " +
                        "LEFT JOIN FETCH i.department d " +
                        "WHERE i.department.id IN :departmentIds " +
                        "AND i.innovationRound.id = :roundId " +
                        "AND i.status = 'KHOA_APPROVED'")
        List<Innovation> findByDepartmentIdsAndRoundIdAndStatusKhoaApproved(
                        @Param("departmentIds") List<String> departmentIds,
                        @Param("roundId") String roundId);

        @Query("SELECT DISTINCT i FROM Innovation i " +
                        "LEFT JOIN FETCH i.user " +
                        "LEFT JOIN FETCH i.department " +
                        "LEFT JOIN FETCH i.innovationRound " +
                        "WHERE i.status NOT IN ('DRAFT', 'KHOA_REJECTED', 'TRUONG_REJECTED')")
        List<Innovation> findAllWithDetails();

        /**
         * Lấy innovation theo ID với formDataList (cho similarity check)
         */
        @Query("SELECT i FROM Innovation i " +
                        "LEFT JOIN FETCH i.formDataList fd " +
                        "LEFT JOIN FETCH fd.formField " +
                        "WHERE i.id = :innovationId")
        Innovation findByIdWithFormData(@Param("innovationId") String innovationId);
}
