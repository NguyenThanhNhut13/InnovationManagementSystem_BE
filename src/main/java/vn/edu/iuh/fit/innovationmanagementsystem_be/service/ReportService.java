package vn.edu.iuh.fit.innovationmanagementsystem_be.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.Report;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.User;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.DocumentTypeEnum;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.ReportStatusEnum;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.SignatureStatusEnum;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.UserRoleEnum;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.DepartmentReportsStatusResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.ReportStatusResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.exception.IdInvalidException;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.CouncilRepository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.DigitalSignatureRepository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.FormTemplateRepository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.InnovationRoundRepository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.ReportRepository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.Council;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.InnovationRound;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.InnovationRoundStatusEnum;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.ReviewLevelEnum;

import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class ReportService {

    private final ReportRepository reportRepository;
    private final DigitalSignatureRepository digitalSignatureRepository;
    private final FormTemplateRepository formTemplateRepository;
    private final UserService userService;
    private final CouncilRepository councilRepository;
    private final InnovationRoundRepository innovationRoundRepository;

    public ReportService(
            ReportRepository reportRepository,
            DigitalSignatureRepository digitalSignatureRepository,
            FormTemplateRepository formTemplateRepository,
            UserService userService,
            CouncilRepository councilRepository,
            InnovationRoundRepository innovationRoundRepository) {
        this.reportRepository = reportRepository;
        this.digitalSignatureRepository = digitalSignatureRepository;
        this.formTemplateRepository = formTemplateRepository;
        this.userService = userService;
        this.councilRepository = councilRepository;
        this.innovationRoundRepository = innovationRoundRepository;
    }

    /**
     * Lấy trạng thái của tất cả reports (Mẫu 3, 4, 5) cho department hiện tại
     * CHU_TICH_HD_TRUONG: chỉ lấy mẫu BIEN_BAN_HOP (Mẫu 3) từ council cấp trường
     */
    public DepartmentReportsStatusResponse getDepartmentReportsStatus() {
        // 1. Lấy current user
        User currentUser = userService.getCurrentUser();
        
        // 2. Kiểm tra nếu là CHU_TICH_HD_TRUONG
        Set<UserRoleEnum> userRoles = currentUser.getUserRoles().stream()
                .map(userRole -> userRole.getRole().getRoleName())
                .collect(Collectors.toSet());
        
        boolean isChuTichHDTruong = userRoles.contains(UserRoleEnum.CHU_TICH_HD_TRUONG);
        
        if (isChuTichHDTruong) {
            // CHU_TICH_HD_TRUONG: chỉ lấy mẫu BIEN_BAN_HOP từ council cấp trường
            ReportStatusResponse meetingMinutes = getReportStatusForSchoolLevel(DocumentTypeEnum.REPORT_MAU_3, UserRoleEnum.CHU_TICH_HD_TRUONG);
            
            // Tạo response - chỉ có meetingMinutes
            DepartmentReportsStatusResponse response = new DepartmentReportsStatusResponse();
            response.setMeetingMinutes(meetingMinutes);
            response.setProposalSummary(null); // Không có
            response.setScoringSummary(null); // Không có
            return response;
        }
        
        // 3. Logic cũ cho các role khác (TRUONG_KHOA, TV_HOI_DONG_KHOA, TV_HOI_DONG_TRUONG)
        if (currentUser.getDepartment() == null) {
            throw new IdInvalidException("Người dùng hiện tại chưa được gán vào khoa nào.");
        }
        String departmentId = currentUser.getDepartment().getId();

        // 4. Check từng report type
        ReportStatusResponse meetingMinutes = getReportStatus(departmentId, DocumentTypeEnum.REPORT_MAU_3, UserRoleEnum.TV_HOI_DONG_KHOA);
        ReportStatusResponse proposalSummary = getReportStatus(departmentId, DocumentTypeEnum.REPORT_MAU_4, UserRoleEnum.TRUONG_KHOA);
        ReportStatusResponse scoringSummary = getReportStatus(departmentId, DocumentTypeEnum.REPORT_MAU_5, UserRoleEnum.TRUONG_KHOA);

        // 5. Tạo response
        DepartmentReportsStatusResponse response = new DepartmentReportsStatusResponse();
        response.setMeetingMinutes(meetingMinutes);
        response.setProposalSummary(proposalSummary);
        response.setScoringSummary(scoringSummary);
        return response;
    }

    /**
     * Helper: Lấy trạng thái của một report
     */
    private ReportStatusResponse getReportStatus(String departmentId, DocumentTypeEnum documentType, UserRoleEnum checkRole) {
        ReportStatusResponse status = new ReportStatusResponse();

        // Tìm Report
        Optional<Report> reportOpt = reportRepository.findByDepartmentIdAndReportType(departmentId, documentType);

        if (reportOpt.isPresent()) {
            Report report = reportOpt.get();

            // Check status
            ReportStatusEnum reportStatus = report.getStatus();
            if (reportStatus == null) {
                reportStatus = ReportStatusEnum.DRAFT; // Default nếu null
            }

            status.setStatus(reportStatus);

            // Check có signed
            boolean isSigned = digitalSignatureRepository.existsByReportIdAndSignedAsRoleAndStatus(
                    report.getId(), checkRole, SignatureStatusEnum.SIGNED);

            status.setSigned(isSigned);

            // Lấy roundId từ template
            if (report.getTemplateId() != null) {
                formTemplateRepository.findById(report.getTemplateId())
                        .ifPresent(template -> {
                            if (template.getInnovationRound() != null) {
                                status.setRoundId(template.getInnovationRound().getId());
                            }
                        });
            }
        } else {
            // Không có report → status = null, isSigned = false, roundId = null
            status.setStatus(null);
            status.setSigned(false);
            status.setRoundId(null);
        }

        return status;
    }

    /**
     * Helper: Lấy trạng thái của một report cấp trường (cho CHU_TICH_HD_TRUONG)
     * Tìm report theo council cấp trường và reportType
     */
    private ReportStatusResponse getReportStatusForSchoolLevel(DocumentTypeEnum documentType, UserRoleEnum checkRole) {
        ReportStatusResponse status = new ReportStatusResponse();

        // 1. Lấy round hiện tại đang mở
        InnovationRound currentRound = innovationRoundRepository.findCurrentActiveRound(
                LocalDate.now(), InnovationRoundStatusEnum.OPEN)
                .orElse(null); // Nếu không có round, trả về null status

        if (currentRound == null) {
            status.setStatus(null);
            status.setSigned(false);
            status.setRoundId(null);
            return status;
        }

        // 2. Tìm council cấp trường (không có department)
        Optional<Council> councilOpt = councilRepository.findByRoundIdAndLevelAndNoDepartment(
                currentRound.getId(), ReviewLevelEnum.TRUONG);

        if (councilOpt.isEmpty()) {
            // Không có council cấp trường
            status.setStatus(null);
            status.setSigned(false);
            status.setRoundId(currentRound.getId());
            return status;
        }

        Council council = councilOpt.get();
        String councilId = council.getId();

        // 3. Tìm report theo councilId và reportType
        Optional<Report> reportOpt = reportRepository.findByCouncilIdAndReportType(councilId, documentType);

        if (reportOpt.isPresent()) {
            Report report = reportOpt.get();

            // Check status
            ReportStatusEnum reportStatus = report.getStatus();
            if (reportStatus == null) {
                reportStatus = ReportStatusEnum.DRAFT; // Default nếu null
            }

            status.setStatus(reportStatus);

            // Check có signed
            boolean isSigned = digitalSignatureRepository.existsByReportIdAndSignedAsRoleAndStatus(
                    report.getId(), checkRole, SignatureStatusEnum.SIGNED);

            status.setSigned(isSigned);

            // Lấy roundId từ template
            if (report.getTemplateId() != null) {
                formTemplateRepository.findById(report.getTemplateId())
                        .ifPresent(template -> {
                            if (template.getInnovationRound() != null) {
                                status.setRoundId(template.getInnovationRound().getId());
                            }
                        });
            } else {
                status.setRoundId(currentRound.getId());
            }
        } else {
            // Không có report → status = null, isSigned = false
            status.setStatus(null);
            status.setSigned(false);
            status.setRoundId(currentRound.getId());
        }

        return status;
    }
}

