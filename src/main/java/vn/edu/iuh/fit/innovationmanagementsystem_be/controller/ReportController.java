package vn.edu.iuh.fit.innovationmanagementsystem_be.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.DepartmentReportsStatusResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.service.ReportService;
import vn.edu.iuh.fit.innovationmanagementsystem_be.utils.annotation.ApiMessage;

@RestController
@RequestMapping("/api/v1/reports")
@Tag(name = "Report", description = "Report management APIs")
@SecurityRequirement(name = "Bearer Authentication")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    /**
     * Lấy trạng thái của tất cả reports (Mẫu 3, 4, 5) cho department hiện tại
     */
    @GetMapping("/status")
    @PreAuthorize("hasAnyRole('TRUONG_KHOA', 'TV_HOI_DONG_TRUONG', 'TV_HOI_DONG_KHOA')")
    @ApiMessage("Lấy trạng thái reports thành công")
    @Operation(summary = "Get department reports status", description = "Lấy trạng thái của tất cả reports (Mẫu 3, 4, 5) cho department hiện tại")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Trả về trạng thái các reports", content = @Content(schema = @Schema(implementation = DepartmentReportsStatusResponse.class))),
            @ApiResponse(responseCode = "401", description = "Không được phép truy cập"),
            @ApiResponse(responseCode = "400", description = "Người dùng chưa được gán vào khoa nào")
    })
    public ResponseEntity<DepartmentReportsStatusResponse> getDepartmentReportsStatus() {
        DepartmentReportsStatusResponse response = reportService.getDepartmentReportsStatus();
        return ResponseEntity.ok(response);
    }
}

