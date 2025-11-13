package vn.edu.iuh.fit.innovationmanagementsystem_be.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.Notification;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.NotificationTypeEnum;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, String> {

    Page<Notification> findByDepartmentIdOrderByCreatedAtDesc(String departmentId, Pageable pageable);

    Page<Notification> findByTargetRoleOrderByCreatedAtDesc(String targetRole, Pageable pageable);

    @Query("SELECT n FROM Notification n WHERE n.type = :type AND n.createdAt >= :fromDate ORDER BY n.createdAt DESC")
    List<Notification> findByTypeAndCreatedAtAfter(@Param("type") NotificationTypeEnum type,
            @Param("fromDate") LocalDateTime fromDate);

    @Query("SELECT n FROM Notification n WHERE (n.department.id = :departmentId OR n.targetRole = :targetRole) ORDER BY n.createdAt DESC")
    Page<Notification> findByDepartmentOrRole(@Param("departmentId") String departmentId,
            @Param("targetRole") String targetRole, Pageable pageable);
}
