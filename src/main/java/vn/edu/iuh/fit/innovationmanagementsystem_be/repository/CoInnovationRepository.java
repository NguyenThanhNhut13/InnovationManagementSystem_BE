package vn.edu.iuh.fit.innovationmanagementsystem_be.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.CoInnovation;

import java.util.List;

@Repository
public interface CoInnovationRepository extends JpaRepository<CoInnovation, String> {

        // Tìm co-innovation theo innovation
        @Query("SELECT ci FROM CoInnovation ci WHERE ci.innovation.id = :innovationId")
        List<CoInnovation> findByInnovationId(@Param("innovationId") String innovationId);

        // Tìm co-innovation theo user
        @Query("SELECT ci FROM CoInnovation ci WHERE ci.user.id = :userId")
        List<CoInnovation> findByUserId(@Param("userId") String userId);

        // Tìm co-innovation theo tên đồng tác giả
        List<CoInnovation> findByCoInnovatorFullNameContaining(String coInnovatorFullName);

        // Tìm co-innovation theo tên khoa
        List<CoInnovation> findByCoInnovatorDepartmentNameContaining(String departmentName);

        // Đếm co-innovation theo user
        @Query("SELECT COUNT(ci) FROM CoInnovation ci WHERE ci.user.id = :userId")
        long countByUserId(@Param("userId") String userId);

        // Kiểm tra user đã là co-innovation của innovation chưa
        @Query("SELECT COUNT(ci) > 0 FROM CoInnovation ci WHERE ci.innovation.id = :innovationId AND ci.user.id = :userId")
        boolean existsByInnovationIdAndUserId(@Param("innovationId") String innovationId,
                        @Param("userId") String userId);
}
