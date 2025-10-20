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

        Page<Innovation> findByUserIdAndStatus(String userId, InnovationStatusEnum status, Pageable pageable);

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

        @Query("SELECT ir.academicYear, COUNT(i) FROM Innovation i JOIN i.innovationRound ir WHERE i.user.id = :userId AND i.status IN ('DRAFT', 'SUBMITTED', 'PENDING_KHOA_REVIEW', 'KHOA_REVIEWED', 'KHOA_APPROVED', 'PENDING_TRUONG_REVIEW', 'TRUONG_REVIEWED') GROUP BY ir.academicYear ORDER BY ir.academicYear")
        List<Object[]> countSubmittedInnovationsByAcademicYearAndUserId(@Param("userId") String userId);

        @Query("SELECT ir.academicYear, COUNT(i) FROM Innovation i JOIN i.innovationRound ir WHERE i.user.id = :userId AND i.status IN ('TRUONG_APPROVED', 'FINAL_APPROVED') GROUP BY ir.academicYear ORDER BY ir.academicYear")
        List<Object[]> countApprovedInnovationsByAcademicYearAndUserId(@Param("userId") String userId);

        @Query("SELECT ir.academicYear, COUNT(i) FROM Innovation i JOIN i.innovationRound ir WHERE i.user.id = :userId AND i.status IN ('KHOA_REJECTED', 'TRUONG_REJECTED') GROUP BY ir.academicYear ORDER BY ir.academicYear")
        List<Object[]> countRejectedInnovationsByAcademicYearAndUserId(@Param("userId") String userId);

        @Query("SELECT ir.academicYear, COUNT(i) FROM Innovation i JOIN i.innovationRound ir WHERE i.user.id = :userId AND i.status IN ('SUBMITTED', 'DRAFT') GROUP BY ir.academicYear ORDER BY ir.academicYear")
        List<Object[]> countPendingInnovationsByAcademicYearAndUserId(@Param("userId") String userId);
}
