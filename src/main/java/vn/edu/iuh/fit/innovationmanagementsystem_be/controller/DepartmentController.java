package vn.edu.iuh.fit.innovationmanagementsystem_be.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import com.turkraft.springfilter.boot.Filter;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.Department;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.DepartmentImportResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.DepartmentInnovationStatisticsResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.service.DepartmentService;
import vn.edu.iuh.fit.innovationmanagementsystem_be.utils.ResultPaginationDTO;
import vn.edu.iuh.fit.innovationmanagementsystem_be.utils.annotation.ApiMessage;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Department", description = "Department management APIs")
@SecurityRequirement(name = "Bearer Authentication")
public class DepartmentController {

        private final DepartmentService departmentService;

        public DepartmentController(DepartmentService departmentService) {
                this.departmentService = departmentService;
        }

        // 1. Lấy all Departments với Pagination and Filtering
        @GetMapping("/departments")
        @ApiMessage("Lấy danh sách phòng ban thành công")
        @Operation(summary = "Get All Departments", description = "Get all departments")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "All departments retrieved successfully", content = @Content(schema = @Schema(implementation = ResultPaginationDTO.class))),
                        @ApiResponse(responseCode = "400", description = "Invalid request data")
        })
        public ResponseEntity<ResultPaginationDTO> getAllDepartments(
                        @Parameter(description = "Filter specification for departments") @Filter Specification<Department> specification,
                        @Parameter(description = "Pagination parameters") Pageable pageable) {
                return ResponseEntity.ok(departmentService.getAllDepartments(specification, pageable));
        }

        // 2. Lấy thống kê số lượng Innovation của tất cả các Department
        @GetMapping("/departments/innovations/statistics")
        @PreAuthorize("hasAnyRole('QUAN_TRI_VIEN_HE_THONG', 'QUAN_TRI_VIEN_QLKH_HTQT')")
        @ApiMessage("Lấy thống kê số lượng sáng kiến của tất cả các khoa thành công")
        @Operation(summary = "Get Department Innovation Statistics", description = "Get innovation statistics for all departments")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Department innovation statistics retrieved successfully", content = @Content(schema = @Schema(implementation = DepartmentInnovationStatisticsResponse.class))),
                        @ApiResponse(responseCode = "400", description = "Invalid request data")
        })
        public ResponseEntity<List<DepartmentInnovationStatisticsResponse>> getDepartmentInnovationStatistics() {
                List<DepartmentInnovationStatisticsResponse> statistics = departmentService
                                .getDepartmentInnovationStatistics();
                return ResponseEntity.ok(statistics);
        }

        // 3. Import departments từ file Excel
        @PostMapping(value = "/departments/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
        @PreAuthorize("hasAnyRole('QUAN_TRI_VIEN_HE_THONG', 'QUAN_TRI_VIEN_QLKH_HTQT')")
        @ApiMessage("Import phòng ban từ file Excel thành công")
        @Operation(summary = "Import Departments from Excel", description = "Import departments from Excel file (.xlsx). File should have 2 columns: department_code, department_name. First row is header.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Departments imported successfully", content = @Content(schema = @Schema(implementation = DepartmentImportResponse.class))),
                        @ApiResponse(responseCode = "400", description = "Invalid file format or data")
        })
        public ResponseEntity<DepartmentImportResponse> importDepartments(
                        @Parameter(description = "Excel file (.xlsx) with department_code and department_name columns") @RequestParam("file") MultipartFile file) {
                DepartmentImportResponse response = departmentService.importDepartmentsFromExcel(file);
                return ResponseEntity.ok(response);
        }

}
