package vn.edu.iuh.fit.innovationmanagementsystem_be.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.Council;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.ReviewLevelEnum;

import java.util.List;
import java.util.Optional;

@Repository
public interface CouncilRepository extends JpaRepository<Council, String>, JpaSpecificationExecutor<Council> {

    Optional<Council> findByNameIgnoreCase(String name);

    List<Council> findByReviewCouncilLevel(ReviewLevelEnum level);

    // Tìm council theo round và department (cho faculty level) - chỉ fetch councilMembers để tránh MultipleBagFetchException
    @Query("SELECT DISTINCT c FROM Council c " +
            "LEFT JOIN FETCH c.councilMembers cm " +
            "LEFT JOIN FETCH cm.user " +
            "WHERE c.innovationRound.id = :roundId AND c.reviewCouncilLevel = :level AND c.department.id = :departmentId")
    Optional<Council> findByRoundIdAndLevelAndDepartmentId(
            @Param("roundId") String roundId,
            @Param("level") ReviewLevelEnum level,
            @Param("departmentId") String departmentId
    );

    // Tìm council theo round (cho school level - không có department) - chỉ fetch councilMembers để tránh MultipleBagFetchException
    @Query("SELECT DISTINCT c FROM Council c " +
            "LEFT JOIN FETCH c.councilMembers cm " +
            "LEFT JOIN FETCH cm.user " +
            "WHERE c.innovationRound.id = :roundId AND c.reviewCouncilLevel = :level AND c.department IS NULL")
    Optional<Council> findByRoundIdAndLevelAndNoDepartment(
            @Param("roundId") String roundId,
            @Param("level") ReviewLevelEnum level
    );
}
