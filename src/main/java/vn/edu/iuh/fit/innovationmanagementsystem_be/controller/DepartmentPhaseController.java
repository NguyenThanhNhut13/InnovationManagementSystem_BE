package vn.edu.iuh.fit.innovationmanagementsystem_be.controller;

import com.turkraft.springfilter.boot.Filter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.DepartmentPhase;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.DepartmentPhaseRequest;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.SimpleUpdateDepartmentPhaseRequest;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.DepartmentPhaseResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.service.DepartmentPhaseService;
import vn.edu.iuh.fit.innovationmanagementsystem_be.utils.ResultPaginationDTO;
import vn.edu.iuh.fit.innovationmanagementsystem_be.utils.annotation.ApiMessage;

import java.util.List;

@RestController
@RequestMapping("/api/v1/department-phases")
@Tag(name = "Department Phase", description = "Department phase management APIs")
@SecurityRequirement(name = "Bearer Authentication")
public class DepartmentPhaseController {

        private final DepartmentPhaseService departmentPhaseService;

        public DepartmentPhaseController(DepartmentPhaseService departmentPhaseService) {
                this.departmentPhaseService = departmentPhaseService;
        }

        // 1. Tạo nhiều phase cho khoa cùng lúc
        @PostMapping("/multiple")
        @PreAuthorize("hasAnyRole('TRUONG_KHOA', 'QUAN_TRI_VIEN_HE_THONG')")
        @ApiMessage("Tạo nhiều giai đoạn cho khoa thành công")
        @Operation(summary = "Create Multiple Department Phases", description = "Create multiple department phases at once")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Department phases created successfully"),
                        @ApiResponse(responseCode = "400", description = "Invalid request data"),
                        @ApiResponse(responseCode = "401", description = "Unauthorized")
        })
        public ResponseEntity<List<DepartmentPhaseResponse>> createMultipleDepartmentPhases(
                        @Parameter(description = "List of department phase details", required = true) @Valid @RequestBody List<DepartmentPhaseRequest> requests) {
                List<DepartmentPhaseResponse> departmentPhases = departmentPhaseService
                                .createMultipleDepartmentPhases(requests);
                return ResponseEntity.ok(departmentPhases);
        }

        // 2. Cập nhật nhiều phase theo phaseType cùng lúc (không cần ID)
        @PutMapping("/multiple/by-type")
        @PreAuthorize("hasAnyRole('TRUONG_KHOA', 'QUAN_TRI_VIEN_HE_THONG')")
        @ApiMessage("Cập nhật nhiều giai đoạn khoa theo loại thành công")
        @Operation(summary = "Update Multiple Department Phases by Type", description = "Update multiple department phases by phase type without requiring ID")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Department phases updated successfully"),
                        @ApiResponse(responseCode = "400", description = "Invalid request data"),
                        @ApiResponse(responseCode = "404", description = "Department phase not found"),
                        @ApiResponse(responseCode = "401", description = "Unauthorized")
        })
        public ResponseEntity<List<DepartmentPhaseResponse>> updateMultipleDepartmentPhasesByType(
                        @Parameter(description = "List of department phase update requests without ID", required = true) @Valid @RequestBody List<SimpleUpdateDepartmentPhaseRequest> requests) {
                List<DepartmentPhaseResponse> departmentPhases = departmentPhaseService
                                .updateMultipleDepartmentPhasesByType(requests);
                return ResponseEntity.ok(departmentPhases);
        }

        // 3. Lấy danh sách tất cả giai đoạn khoa với pagination và filtering
        @GetMapping
        @PreAuthorize("hasAnyRole('TRUONG_KHOA', 'QUAN_TRI_VIEN_HE_THONG', 'GIANG_VIEN')")
        @ApiMessage("Lấy danh sách giai đoạn khoa thành công")
        @Operation(summary = "Get All Department Phases", description = "Get paginated list of all department phases with filtering")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Department phases retrieved successfully", content = @Content(schema = @Schema(implementation = ResultPaginationDTO.class))),
                        @ApiResponse(responseCode = "401", description = "Unauthorized")
        })
        public ResponseEntity<ResultPaginationDTO> getAllDepartmentPhases(
                        @Parameter(description = "Filter specification for department phases") @Filter Specification<DepartmentPhase> specification,
                        @Parameter(description = "Pagination parameters") Pageable pageable) {
                return ResponseEntity.ok(departmentPhaseService
                                .getAllDepartmentPhasesWithPaginationAndFilter(specification, pageable));
        }

        // 4. Xóa phase (chỉ được xóa khi status là DRAFT)
        @DeleteMapping("/{id}")
        @PreAuthorize("hasAnyRole('TRUONG_KHOA', 'QUAN_TRI_VIEN_HE_THONG')")
        @ApiMessage("Xóa giai đoạn khoa thành công")
        @Operation(summary = "Delete Department Phase", description = "Delete department phase. Only allowed when status is DRAFT")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Department phase deleted successfully"),
                        @ApiResponse(responseCode = "400", description = "Cannot delete phase with status other than DRAFT"),
                        @ApiResponse(responseCode = "404", description = "Department phase not found"),
                        @ApiResponse(responseCode = "401", description = "Unauthorized")
        })
        public ResponseEntity<Void> deleteDepartmentPhase(
                        @Parameter(description = "Department phase ID", required = true) @PathVariable String id) {
                departmentPhaseService.deleteDepartmentPhase(id);
                return ResponseEntity.ok().build();
        }
}
