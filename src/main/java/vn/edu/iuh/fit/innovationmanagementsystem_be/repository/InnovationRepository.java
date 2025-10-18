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
}
