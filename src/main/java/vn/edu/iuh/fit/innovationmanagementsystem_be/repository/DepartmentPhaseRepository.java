package vn.edu.iuh.fit.innovationmanagementsystem_be.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.DepartmentPhase;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.InnovationPhaseTypeEnum;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.PhaseStatusEnum;

import java.util.List;
import java.util.Optional;

@Repository
public interface DepartmentPhaseRepository
                extends JpaRepository<DepartmentPhase, String>, JpaSpecificationExecutor<DepartmentPhase> {

        Optional<DepartmentPhase> findById(String id);

        List<DepartmentPhase> findByDepartmentId(String departmentId);

        List<DepartmentPhase> findByInnovationPhaseId(String innovationPhaseId);

        List<DepartmentPhase> findByDepartmentIdAndInnovationPhaseId(String departmentId, String innovationPhaseId);

        List<DepartmentPhase> findByPhaseStatus(PhaseStatusEnum phaseStatus);

        List<DepartmentPhase> findByDepartmentIdAndPhaseStatus(String departmentId, PhaseStatusEnum phaseStatus);

        Optional<DepartmentPhase> findByDepartmentIdAndInnovationRoundIdAndPhaseType(String departmentId,
                        String innovationRoundId, InnovationPhaseTypeEnum phaseType);

        List<DepartmentPhase> findByDepartmentIdAndInnovationRoundId(String departmentId, String innovationRoundId);

        List<DepartmentPhase> findByInnovationRoundId(String innovationRoundId);

        /**
         * Tìm các department phase có trạng thái SCHEDULED và đã đến ngày bắt đầu
         */
        @org.springframework.data.jpa.repository.Query("SELECT dp FROM DepartmentPhase dp " +
                        "WHERE dp.phaseStatus = 'SCHEDULED' " +
                        "AND dp.phaseStartDate <= :currentDate")
        List<DepartmentPhase> findScheduledDepartmentPhasesReadyToStart(
                        @org.springframework.data.repository.query.Param("currentDate") java.time.LocalDate currentDate);

        /**
         * Tìm các department phase có trạng thái ACTIVE và đã qua ngày kết thúc
         */
        @org.springframework.data.jpa.repository.Query("SELECT dp FROM DepartmentPhase dp " +
                        "WHERE dp.phaseStatus = 'ACTIVE' " +
                        "AND dp.phaseEndDate < :currentDate")
        List<DepartmentPhase> findActiveDepartmentPhasesReadyToComplete(
                        @org.springframework.data.repository.query.Param("currentDate") java.time.LocalDate currentDate);

        /**
         * Tìm các department phase có ngày bắt đầu = hôm nay và trạng thái ACTIVE
         * (để gửi thông báo cho giảng viên khi phase bắt đầu)
         * Fetch join department và innovationRound để tránh LazyInitializationException
         */
        @org.springframework.data.jpa.repository.Query("SELECT dp FROM DepartmentPhase dp " +
                        "LEFT JOIN FETCH dp.department " +
                        "LEFT JOIN FETCH dp.innovationRound " +
                        "WHERE dp.phaseStartDate = :currentDate " +
                        "AND dp.phaseStatus = 'ACTIVE'")
        List<DepartmentPhase> findDepartmentPhasesStartingToday(
                        @org.springframework.data.repository.query.Param("currentDate") java.time.LocalDate currentDate);

        /**
         * Tìm các department phase SUBMISSION có trạng thái ACTIVE và sắp hết hạn
         * (để thông báo cho TRUONG_KHOA cần thành lập hội đồng chấm điểm)
         * Fetch join department và innovationRound để tránh LazyInitializationException
         */
        @org.springframework.data.jpa.repository.Query("SELECT dp FROM DepartmentPhase dp " +
                        "LEFT JOIN FETCH dp.department " +
                        "LEFT JOIN FETCH dp.innovationRound " +
                        "WHERE dp.phaseType = 'SUBMISSION' " +
                        "AND dp.phaseStatus = 'ACTIVE' " +
                        "AND dp.phaseEndDate >= :startDate " +
                        "AND dp.phaseEndDate <= :endDate")
        List<DepartmentPhase> findActiveSubmissionPhasesExpiringSoon(
                        @org.springframework.data.repository.query.Param("startDate") java.time.LocalDate startDate,
                        @org.springframework.data.repository.query.Param("endDate") java.time.LocalDate endDate);

        /**
         * Tìm các department phase SCORING có phaseStartDate = targetDate
         * (để thông báo cho TV_HOI_DONG_KHOA chuẩn bị chấm điểm trước 1 ngày)
         * Fetch join department và innovationRound để tránh LazyInitializationException
         * 
         * @param targetDate Ngày bắt đầu phase SCORING (thường là ngày mai, để thông
         *                   báo trước 1 ngày)
         */
        @org.springframework.data.jpa.repository.Query("SELECT dp FROM DepartmentPhase dp " +
                        "LEFT JOIN FETCH dp.department " +
                        "LEFT JOIN FETCH dp.innovationRound " +
                        "WHERE dp.phaseType = 'SCORING' " +
                        "AND dp.phaseStatus IN ('SCHEDULED', 'ACTIVE') " +
                        "AND dp.phaseStartDate = :targetDate")
        List<DepartmentPhase> findScoringPhasesStartingTomorrow(
                        @org.springframework.data.repository.query.Param("targetDate") java.time.LocalDate targetDate);
}
