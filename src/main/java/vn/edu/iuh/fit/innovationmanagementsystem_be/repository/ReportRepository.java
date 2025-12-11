package vn.edu.iuh.fit.innovationmanagementsystem_be.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.Report;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.DocumentTypeEnum;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface ReportRepository extends JpaRepository<Report, String> {

    Optional<Report> findByDepartmentIdAndReportType(
            String departmentId,
            DocumentTypeEnum reportType);

    /**
     * Tìm report theo councilId và reportType (cho cấp trường)
     */
    Optional<Report> findByCouncilIdAndReportType(
            String councilId,
            DocumentTypeEnum reportType);

    /**
     * Đếm số report có status cụ thể theo departmentId và danh sách reportType
     */
    long countByDepartmentIdAndStatusAndReportTypeIn(
            String departmentId,
            vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.ReportStatusEnum status,
            List<DocumentTypeEnum> reportTypes);

    /**
     * Đếm số report có status cụ thể theo departmentId, roundId (thông qua
     * template) và danh sách reportType
     */
    @Query("SELECT COUNT(r) FROM Report r " +
            "JOIN FormTemplate ft ON ft.id = r.templateId " +
            "WHERE r.departmentId = :departmentId " +
            "AND r.status = :status " +
            "AND r.reportType IN :reportTypes " +
            "AND ft.innovationRound.id = :roundId")
    long countByDepartmentIdAndStatusAndReportTypeInAndRoundId(
            @Param("departmentId") String departmentId,
            @Param("status") vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.ReportStatusEnum status,
            @Param("reportTypes") List<DocumentTypeEnum> reportTypes,
            @Param("roundId") String roundId);
}
