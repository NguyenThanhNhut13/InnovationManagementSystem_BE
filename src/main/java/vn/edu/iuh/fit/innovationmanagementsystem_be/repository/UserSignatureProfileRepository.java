package vn.edu.iuh.fit.innovationmanagementsystem_be.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.UserSignatureProfile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserSignatureProfileRepository extends JpaRepository<UserSignatureProfile, String> {

        // Tìm profile theo user
        @Query("SELECT usp FROM UserSignatureProfile usp WHERE usp.user.id = :userId")
        List<UserSignatureProfile> findByUserId(@Param("userId") String userId);

        // Tìm profile hết hạn
        @Query("SELECT usp FROM UserSignatureProfile usp WHERE usp.certificateValidTo < :currentDate")
        List<UserSignatureProfile> findExpiredProfiles(@Param("currentDate") LocalDateTime currentDate);

        // Đếm profile theo user
        @Query("SELECT COUNT(usp) FROM UserSignatureProfile usp WHERE usp.user.id = :userId")
        long countByUserId(@Param("userId") String userId);

        // Kiểm tra certificate serial đã tồn tại chưa
        boolean existsByCertificateSerial(String certificateSerial);

        // Kiểm tra user đã có profile chưa
        @Query("SELECT COUNT(usp) > 0 FROM UserSignatureProfile usp WHERE usp.user.id = :userId")
        boolean existsByUserId(@Param("userId") String userId);

        // Tìm profile theo department (thông qua user)
        @Query("SELECT usp FROM UserSignatureProfile usp WHERE usp.user.department.id = :departmentId")
        List<UserSignatureProfile> findByDepartmentId(@Param("departmentId") String departmentId);
}
