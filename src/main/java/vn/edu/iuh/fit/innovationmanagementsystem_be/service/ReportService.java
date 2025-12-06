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
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.DigitalSignatureRepository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.ReportRepository;

import java.util.Optional;

@Service
@Transactional
public class ReportService {

    private final ReportRepository reportRepository;
    private final DigitalSignatureRepository digitalSignatureRepository;
    private final UserService userService;

    public ReportService(
            ReportRepository reportRepository,
            DigitalSignatureRepository digitalSignatureRepository,
            UserService userService) {
        this.reportRepository = reportRepository;
        this.digitalSignatureRepository = digitalSignatureRepository;
        this.userService = userService;
    }

    /**
     * Lấy trạng thái của tất cả reports (Mẫu 3, 4, 5) cho department hiện tại
     */
    public DepartmentReportsStatusResponse getDepartmentReportsStatus() {
        // 1. Lấy current user và departmentId
        User currentUser = userService.getCurrentUser();
        if (currentUser.getDepartment() == null) {
            throw new IdInvalidException("Người dùng hiện tại chưa được gán vào khoa nào.");
        }
        String departmentId = currentUser.getDepartment().getId();

        // 2. Check từng report type
        ReportStatusResponse meetingMinutes = getReportStatus(departmentId, DocumentTypeEnum.REPORT_MAU_3, UserRoleEnum.TV_HOI_DONG_KHOA);
        ReportStatusResponse proposalSummary = getReportStatus(departmentId, DocumentTypeEnum.REPORT_MAU_4, UserRoleEnum.TRUONG_KHOA);
        ReportStatusResponse scoringSummary = getReportStatus(departmentId, DocumentTypeEnum.REPORT_MAU_5, UserRoleEnum.TRUONG_KHOA);

        // 3. Tạo response
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
        } else {
            // Không có report → status = null, isSigned = false
            status.setStatus(null);
            status.setSigned(false);
        }

        return status;
    }
}

