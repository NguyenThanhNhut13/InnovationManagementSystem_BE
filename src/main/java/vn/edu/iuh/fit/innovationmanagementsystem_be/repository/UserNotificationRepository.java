package vn.edu.iuh.fit.innovationmanagementsystem_be.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.UserNotification;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserNotificationRepository extends JpaRepository<UserNotification, String> {

    Page<UserNotification> findByUserIdOrderByCreatedAtDesc(String userId, Pageable pageable);

    @Query("SELECT un FROM UserNotification un WHERE un.user.id = :userId AND un.isRead = :isRead ORDER BY un.createdAt DESC")
    Page<UserNotification> findByUserIdAndIsRead(@Param("userId") String userId, @Param("isRead") Boolean isRead,
            Pageable pageable);

    @Query("SELECT COUNT(un) FROM UserNotification un WHERE un.user.id = :userId AND un.isRead = false")
    Long countUnreadByUserId(@Param("userId") String userId);

    @Modifying
    @Query("UPDATE UserNotification un SET un.isRead = true, un.readAt = CURRENT_TIMESTAMP WHERE un.user.id = :userId AND un.isRead = false")
    int markAllAsReadByUserId(@Param("userId") String userId);

    @Modifying
    @Query("UPDATE UserNotification un SET un.isRead = true, un.readAt = CURRENT_TIMESTAMP WHERE un.id = :id AND un.user.id = :userId")
    int markAsRead(@Param("id") String id, @Param("userId") String userId);

    Optional<UserNotification> findByIdAndUserId(String id, String userId);

    @Query("SELECT un FROM UserNotification un WHERE un.user.id = :userId AND un.notification.id = :notificationId")
    Optional<UserNotification> findByUserIdAndNotificationId(@Param("userId") String userId,
            @Param("notificationId") String notificationId);
}
