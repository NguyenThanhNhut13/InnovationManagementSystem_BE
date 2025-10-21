package vn.edu.iuh.fit.innovationmanagementsystem_be.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.ActivityLog;

@Repository
public interface ActivityLogRepository extends JpaRepository<ActivityLog, String> {

    Page<ActivityLog> findByUserIdOrderByCreatedAtDesc(String userId, Pageable pageable);

    @Query("SELECT a FROM ActivityLog a WHERE a.userId = :userId ORDER BY a.createdAt DESC")
    Page<ActivityLog> findRecentActivitiesForDashboard(@Param("userId") String userId, Pageable pageable);

    // Đếm số hoạt động chưa đọc
    Long countByUserIdAndIsReadFalse(String userId);

    // Đếm tổng số hoạt động của user
    Long countByUserId(String userId);

    // Đánh dấu tất cả hoạt động là đã đọc
    @Query("UPDATE ActivityLog a SET a.isRead = true WHERE a.userId = :userId")
    void markAllAsReadByUserId(@Param("userId") String userId);
}
