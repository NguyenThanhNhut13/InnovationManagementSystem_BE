package vn.edu.iuh.fit.innovationmanagementsystem_be.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.Report;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.DocumentTypeEnum;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ReportRepository extends JpaRepository<Report, String> {

        // Tìm report theo năm áp dụng
        List<Report> findByApplicableYear(Integer applicableYear);

        // Tìm report theo loại
        List<Report> findByReportType(DocumentTypeEnum reportType);

        // Tìm report theo user
        @Query("SELECT r FROM Report r WHERE r.user.id = :userId")
        List<Report> findByUserId(@Param("userId") String userId);

        // Tìm report theo thời gian tạo
        @Query("SELECT r FROM Report r WHERE r.generatedDate BETWEEN :startDate AND :endDate")
        List<Report> findByGeneratedDateBetween(@Param("startDate") LocalDateTime startDate,
                        @Param("endDate") LocalDateTime endDate);

        // Tìm report theo năm và loại
        @Query("SELECT r FROM Report r WHERE r.applicableYear = :year AND r.reportType = :type")
        List<Report> findByApplicableYearAndReportType(@Param("year") Integer year,
                        @Param("type") DocumentTypeEnum type);

        // Tìm report theo user và năm
        @Query("SELECT r FROM Report r WHERE r.user.id = :userId AND r.applicableYear = :year")
        List<Report> findByUserIdAndApplicableYear(@Param("userId") String userId,
                        @Param("year") Integer year);

        // Đếm report theo năm
        long countByApplicableYear(Integer applicableYear);

        // Đếm report theo loại
        long countByReportType(DocumentTypeEnum reportType);

        // Đếm report theo user
        @Query("SELECT COUNT(r) FROM Report r WHERE r.user.id = :userId")
        long countByUserId(@Param("userId") String userId);
}
