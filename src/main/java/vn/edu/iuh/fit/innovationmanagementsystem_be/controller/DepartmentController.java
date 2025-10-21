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
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.turkraft.springfilter.boot.Filter;
import jakarta.validation.Valid;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.Department;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.DepartmentRequest;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.DepartmentResponse;
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

        // 1. Tạo phòng ban
        @PostMapping("/departments")
        @ApiMessage("Tạo phòng ban thành công")
        @Operation(summary = "Create Department", description = "Create a new department")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Department created successfully", content = @Content(schema = @Schema(implementation = DepartmentResponse.class))),
                        @ApiResponse(responseCode = "400", description = "Invalid request data")
        })
        public ResponseEntity<DepartmentResponse> createDepartment(
                        @Parameter(description = "Department creation request", required = true) @Valid @RequestBody DepartmentRequest departmentRequest) {
                DepartmentResponse departmentResponse = departmentService.createDepartment(departmentRequest);
                return ResponseEntity.ok(departmentResponse);
        }

        // 2. Lấy danh sách phòng ban
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

        // 3. Lấy thông tin phòng ban by Id
        @GetMapping("/departments/{id}")
        @ApiMessage("Lấy phòng ban thành công")
        @Operation(summary = "Get Department by Id", description = "Get department details by department ID")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Department retrieved successfully", content = @Content(schema = @Schema(implementation = DepartmentResponse.class))),
                        @ApiResponse(responseCode = "400", description = "Invalid request data")
        })
        public ResponseEntity<DepartmentResponse> getDepartmentById(
                        @Parameter(description = "Department ID", required = true) @PathVariable String id) {
                return ResponseEntity.ok(departmentService.getDepartmentById(id));
        }

        // 4. Cập nhật phòng ban
        @PutMapping("/departments/{id}")
        @ApiMessage("Cập nhật phòng ban thành công")
        @Operation(summary = "Update Department", description = "Update department details by department ID")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Department updated successfully", content = @Content(schema = @Schema(implementation = DepartmentResponse.class))),
                        @ApiResponse(responseCode = "400", description = "Invalid request data")
        })
        public ResponseEntity<DepartmentResponse> updateDepartment(
                        @Parameter(description = "Department ID", required = true) @PathVariable String id,
                        @Parameter(description = "Department update request", required = true) @Valid @RequestBody DepartmentRequest departmentRequest) {
                return ResponseEntity.ok(departmentService.updateDepartment(id, departmentRequest));
        }

        // 5. Lấy thống kê users theo phòng ban (Tất cả phòng ban)
        @GetMapping("/departments/users/statistics")
        @ApiMessage("Lấy thống kê users theo department thành công")
        @Operation(summary = "Get Department User Statistics", description = "Get department user statistics")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Department user statistics retrieved successfully", content = @Content(schema = @Schema(implementation = List.class))),
                        @ApiResponse(responseCode = "400", description = "Invalid request data")
        })
        public ResponseEntity<List<DepartmentResponse>> getDepartmentUserStatistics() {
                List<DepartmentResponse> statistics = departmentService.getDepartmentUserStatistics();
                return ResponseEntity.ok(statistics);
        }

        // 6. Lấy thống kê users theo phòng ban by ID
        @GetMapping("/departments/{id}/users/statistics")
        @ApiMessage("Lấy thống kê users của phòng ban thành công")
        @Operation(summary = "Get Department User Statistics by Department ID", description = "Get department user statistics by department ID")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Department user statistics retrieved successfully", content = @Content(schema = @Schema(implementation = DepartmentResponse.class))),
                        @ApiResponse(responseCode = "400", description = "Invalid request data")
        })
        public ResponseEntity<DepartmentResponse> getDepartmentUserStatisticsById(
                        @Parameter(description = "Department ID", required = true) @PathVariable String id) {
                DepartmentResponse statistics = departmentService.getDepartmentUserStatisticsById(id);
                return ResponseEntity.ok(statistics);
        }

        // 7. Tìm kiếm phòng ban by từ khóa với phân trang
        @GetMapping("/departments/search")
        @ApiMessage("Tìm kiếm phòng ban thành công")
        @Operation(summary = "Search Departments By Keyword With Pagination", description = "Search departments by keyword with pagination")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Departments retrieved successfully", content = @Content(schema = @Schema(implementation = ResultPaginationDTO.class))),
                        @ApiResponse(responseCode = "400", description = "Invalid request data")
        })
        public ResponseEntity<ResultPaginationDTO> searchDepartmentsByKeywordWithPagination(
                        @Parameter(description = "Search keyword", required = true) @RequestParam String keyword,
                        @Parameter(description = "Pagination parameters") Pageable pageable) {
                return ResponseEntity.ok(departmentService.searchDepartmentsByKeywordWithPagination(keyword, pageable));
        }

        // 8. Lấy danh sách người dùng trong phòng ban
        @GetMapping("/departments/{id}/users")
        @ApiMessage("Lấy danh sách người dùng trong phòng ban thành công")
        @Operation(summary = "Get all User in Department", description = "Get all users in department")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "All users retrieved successfully", content = @Content(schema = @Schema(implementation = ResultPaginationDTO.class))),
                        @ApiResponse(responseCode = "400", description = "Invalid request data")
        })
        public ResponseEntity<ResultPaginationDTO> getAllUserInDepartment(
                        @Parameter(description = "Department ID", required = true) @PathVariable String id,
                        @Parameter(description = "Pagination parameters") Pageable pageable) {
                return ResponseEntity.ok(departmentService.getAllUserInDepartment(id, pageable));
        }

        // 9. Lấy danh sách người dùng đang hoạt động trong phòng ban
        @GetMapping("/departments/{id}/users/active")
        @ApiMessage("Lấy danh sách người dùng đang hoạt động trong phòng ban thành công")
        @Operation(summary = "Get Active User in Department", description = "Get active users in department")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Active users retrieved successfully", content = @Content(schema = @Schema(implementation = ResultPaginationDTO.class))),
                        @ApiResponse(responseCode = "400", description = "Invalid request data")
        })
        public ResponseEntity<ResultPaginationDTO> getActiveUserInDepartment(
                        @Parameter(description = "Department ID", required = true) @PathVariable String id,
                        @Parameter(description = "Pagination parameters") Pageable pageable) {
                return ResponseEntity.ok(departmentService.getActiveUserInDepartment(id, pageable));
        }

        // 10. Lấy danh sách người dùng không hoạt động trong phòng ban
        @GetMapping("/departments/{id}/users/inactive")
        @ApiMessage("Lấy danh sách người dùng không hoạt động trong phòng ban thành công")
        @Operation(summary = "Get Inactive User in Department", description = "Get inactive users in department")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Inactive users retrieved successfully", content = @Content(schema = @Schema(implementation = ResultPaginationDTO.class))),
                        @ApiResponse(responseCode = "400", description = "Invalid request data")
        })
        public ResponseEntity<ResultPaginationDTO> getInactiveUserInDepartment(
                        @Parameter(description = "Department ID", required = true) @PathVariable String id,
                        @Parameter(description = "Pagination parameters") Pageable pageable) {
                return ResponseEntity.ok(departmentService.getInactiveUserInDepartment(id, pageable));
        }

        // 11. Xóa người dùng khỏi phòng ban
        @DeleteMapping("/departments/{departmentId}/users/{userId}")
        @ApiMessage("Xóa người dùng khỏi phòng ban thành công")
        @Operation(summary = "Remove User from Department", description = "Remove user from department")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "User removed from department successfully"),
                        @ApiResponse(responseCode = "400", description = "Invalid request data")
        })
        public ResponseEntity<Void> removeUserFromDepartment(
                        @Parameter(description = "Department ID", required = true) @PathVariable String departmentId,
                        @Parameter(description = "User ID", required = true) @PathVariable String userId) {
                departmentService.removeUserFromDepartment(departmentId, userId);
                return ResponseEntity.noContent().build();
        }

        // 12. Lấy thống kê số lượng sáng kiến của tất cả các khoa
        @GetMapping("/departments/innovations/statistics")
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

}
