package vn.edu.iuh.fit.innovationmanagementsystem_be.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.ReportInnovationDetail;

import java.util.List;

@Repository
public interface ReportInnovationDetailRepository extends JpaRepository<ReportInnovationDetail, String> {

    // Tìm detail theo report
    @Query("SELECT rid FROM ReportInnovationDetail rid WHERE rid.report.id = :reportId ORDER BY rid.displayOrder")
    List<ReportInnovationDetail> findByReportIdOrderByDisplayOrder(@Param("reportId") String reportId);

    // Tìm detail theo innovation
    @Query("SELECT rid FROM ReportInnovationDetail rid WHERE rid.innovation.id = :innovationId")
    List<ReportInnovationDetail> findByInnovationId(@Param("innovationId") String innovationId);

    // Tìm detail theo report và innovation
    @Query("SELECT rid FROM ReportInnovationDetail rid WHERE rid.report.id = :reportId AND rid.innovation.id = :innovationId")
    List<ReportInnovationDetail> findByReportIdAndInnovationId(@Param("reportId") String reportId,
            @Param("innovationId") String innovationId);

    // Tìm detail theo thứ tự hiển thị
    @Query("SELECT rid FROM ReportInnovationDetail rid WHERE rid.displayOrder = :displayOrder")
    List<ReportInnovationDetail> findByDisplayOrder(@Param("displayOrder") Integer displayOrder);

    // Tìm detail theo report và thứ tự hiển thị
    @Query("SELECT rid FROM ReportInnovationDetail rid WHERE rid.report.id = :reportId AND rid.displayOrder = :displayOrder")
    List<ReportInnovationDetail> findByReportIdAndDisplayOrder(@Param("reportId") String reportId,
            @Param("displayOrder") Integer displayOrder);

    // Đếm detail theo report
    @Query("SELECT COUNT(rid) FROM ReportInnovationDetail rid WHERE rid.report.id = :reportId")
    long countByReportId(@Param("reportId") String reportId);

    // Đếm detail theo innovation
    @Query("SELECT COUNT(rid) FROM ReportInnovationDetail rid WHERE rid.innovation.id = :innovationId")
    long countByInnovationId(@Param("innovationId") String innovationId);

    // Tìm detail theo innovation round (thông qua innovation)
    @Query("SELECT rid FROM ReportInnovationDetail rid WHERE rid.innovation.innovationRound.id = :roundId")
    List<ReportInnovationDetail> findByInnovationRoundId(@Param("roundId") String roundId);

    // Tìm detail theo user (thông qua report)
    @Query("SELECT rid FROM ReportInnovationDetail rid WHERE rid.report.user.id = :userId")
    List<ReportInnovationDetail> findByUserId(@Param("userId") String userId);

    // Kiểm tra innovation đã có trong report chưa
    @Query("SELECT COUNT(rid) > 0 FROM ReportInnovationDetail rid WHERE rid.report.id = :reportId AND rid.innovation.id = :innovationId")
    boolean existsByReportIdAndInnovationId(@Param("reportId") String reportId,
            @Param("innovationId") String innovationId);
}

