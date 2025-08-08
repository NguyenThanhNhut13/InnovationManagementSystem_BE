package vn.edu.iuh.fit.innovationmanagementsystem_be.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.Council;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.ReviewLevelEnum;

import java.util.List;

@Repository
public interface CouncilRepository extends JpaRepository<Council, String> {

    // Tìm council theo tên
    List<Council> findByNameContaining(String name);

    // Tìm council theo level
    List<Council> findByReviewCouncilLevel(ReviewLevelEnum level);

    // Tìm council theo tên và level
    @Query("SELECT c FROM Council c WHERE c.name LIKE %:name% AND c.reviewCouncilLevel = :level")
    List<Council> findByNameContainingAndReviewCouncilLevel(@Param("name") String name,
            @Param("level") ReviewLevelEnum level);

    // Đếm council theo level
    long countByReviewCouncilLevel(ReviewLevelEnum level);

    // Kiểm tra tên council đã tồn tại chưa
    boolean existsByName(String name);

    // Tìm council theo năm (thông qua council members)
    @Query("SELECT DISTINCT c FROM Council c JOIN c.councilMembers cm WHERE cm.user.department.id = :departmentId")
    List<Council> findByDepartmentId(@Param("departmentId") String departmentId);
}

