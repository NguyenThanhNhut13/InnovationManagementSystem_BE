package vn.edu.iuh.fit.innovationmanagementsystem_be.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.InnovationPhaseTypeEnum;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.DepartmentPhaseRequest;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.UpdateDepartmentPhaseRequest;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.DepartmentPhaseResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.service.DepartmentPhaseService;
import vn.edu.iuh.fit.innovationmanagementsystem_be.utils.annotation.ApiMessage;

import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/department-phases")
@Tag(name = "Department Phase", description = "Department phase management APIs. Only TRUONG_KHOA and THU_KY_QLKH_HTQT can access these APIs.")
@SecurityRequirement(name = "Bearer Authentication")
public class DepartmentPhaseController {

        private final DepartmentPhaseService departmentPhaseService;

        public DepartmentPhaseController(DepartmentPhaseService departmentPhaseService) {
                this.departmentPhaseService = departmentPhaseService;
        }

        // 1. Create department phase (for department head)
        @PostMapping("/department/{departmentId}/create-phase")
        @ApiMessage("Tạo giai đoạn cho khoa thành công")
        @Operation(summary = "Create Department Phase", description = "Create a phase for specific department with custom dates. Only TRUONG_KHOA of that department can create phases for their department.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Department phase created successfully", content = @Content(schema = @Schema(implementation = DepartmentPhaseResponse.class))),
                        @ApiResponse(responseCode = "400", description = "Invalid request data or time constraints violated"),
                        @ApiResponse(responseCode = "403", description = "Access denied - only TRUONG_KHOA and THU_KY_QLKH_HTQT can create phases")
        })
        public ResponseEntity<DepartmentPhaseResponse> createDepartmentPhase(
                        @Parameter(description = "Department ID", required = true) @PathVariable String departmentId,
                        @Parameter(description = "Department phase details", required = true) @Valid @RequestBody DepartmentPhaseRequest phaseRequest) {

                // Validate user is department head of this department
                departmentPhaseService.validateDepartmentHeadAccess(departmentId);

                DepartmentPhaseResponse createdPhase = departmentPhaseService.createDepartmentPhase(departmentId,
                                phaseRequest);
                return ResponseEntity.ok(createdPhase);
        }

        // 2. Create department phase based on InnovationPhase (copy timeframe)
        @PostMapping("/department/{departmentId}/copy-from-innovation-phase/{innovationPhaseId}")
        @ApiMessage("Tạo giai đoạn khoa từ InnovationPhase thành công")
        @Operation(summary = "Create Department Phase from Innovation Phase", description = "Create department phase with same timeframe as InnovationPhase. Only TRUONG_KHOA of that department can create phases for their department.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Department phase created successfully", content = @Content(schema = @Schema(implementation = List.class))),
                        @ApiResponse(responseCode = "400", description = "Invalid request data"),
                        @ApiResponse(responseCode = "403", description = "Access denied - only TRUONG_KHOA and THU_KY_QLKH_HTQT can create phases")
        })
        public ResponseEntity<List<DepartmentPhaseResponse>> createPhaseFromInnovationPhase(
                        @Parameter(description = "Department ID", required = true) @PathVariable String departmentId,
                        @Parameter(description = "Innovation Phase ID", required = true) @PathVariable String innovationPhaseId) {

                // Validate user is department head of this department
                departmentPhaseService.validateDepartmentHeadAccess(departmentId);

                List<DepartmentPhaseResponse> createdPhases = departmentPhaseService
                                .createPhasesForDepartmentFromInnovationPhase(departmentId, innovationPhaseId);
                return ResponseEntity.ok(createdPhases);
        }

        // 3. Get phases by department and InnovationPhase
        @GetMapping("/department/{departmentId}/phase/{phaseId}")
        @ApiMessage("Lấy danh sách giai đoạn khoa thành công")
        @Operation(summary = "Get Department Phases", description = "Get all phases of a department in a specific InnovationPhase")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Department phases retrieved successfully", content = @Content(schema = @Schema(implementation = List.class))),
                        @ApiResponse(responseCode = "403", description = "Access denied - only TRUONG_KHOA and THU_KY_QLKH_HTQT can access")
        })
        public ResponseEntity<List<DepartmentPhaseResponse>> getPhasesByDepartmentAndPhase(
                        @Parameter(description = "Department ID", required = true) @PathVariable String departmentId,
                        @Parameter(description = "Innovation Phase ID", required = true) @PathVariable String phaseId) {

                // Validate user is department head of this department
                departmentPhaseService.validateDepartmentHeadAccess(departmentId);

                List<DepartmentPhaseResponse> phases = departmentPhaseService.getPhasesByDepartmentAndPhase(
                                departmentId,
                                phaseId);
                return ResponseEntity.ok(phases);
        }

        // 4. Get current phase of department
        @GetMapping("/department/{departmentId}/phase/{phaseId}/current")
        @ApiMessage("Lấy giai đoạn hiện tại của khoa thành công")
        @Operation(summary = "Get Current Department Phase", description = "Get current active phase of a department")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Current department phase retrieved successfully", content = @Content(schema = @Schema(implementation = DepartmentPhaseResponse.class))),
                        @ApiResponse(responseCode = "404", description = "No active phase found"),
                        @ApiResponse(responseCode = "403", description = "Access denied - only TRUONG_KHOA and THU_KY_QLKH_HTQT can access")
        })
        public ResponseEntity<DepartmentPhaseResponse> getCurrentPhase(
                        @Parameter(description = "Department ID", required = true) @PathVariable String departmentId,
                        @Parameter(description = "Innovation Phase ID", required = true) @PathVariable String phaseId) {

                // Validate user is department head of this department
                departmentPhaseService.validateDepartmentHeadAccess(departmentId);

                DepartmentPhaseResponse currentPhase = departmentPhaseService.getCurrentPhase(departmentId, phaseId);
                if (currentPhase == null) {
                        return ResponseEntity.notFound().build();
                }
                return ResponseEntity.ok(currentPhase);
        }

        // 5. Update phase dates
        @PutMapping("/{phaseId}/dates")
        @ApiMessage("Cập nhật thời gian giai đoạn khoa thành công")
        @Operation(summary = "Update Department Phase Dates", description = "Update start and end dates of a department phase")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Department phase dates updated successfully", content = @Content(schema = @Schema(implementation = DepartmentPhaseResponse.class))),
                        @ApiResponse(responseCode = "400", description = "Invalid request data or time constraints violated"),
                        @ApiResponse(responseCode = "403", description = "Access denied - only TRUONG_KHOA and THU_KY_QLKH_HTQT can access")
        })
        public ResponseEntity<DepartmentPhaseResponse> updatePhaseDates(
                        @Parameter(description = "Phase ID", required = true) @PathVariable String phaseId,
                        @Parameter(description = "Start date", required = true) @RequestParam LocalDate startDate,
                        @Parameter(description = "End date", required = true) @RequestParam LocalDate endDate) {

                // Validate user is department head of this phase's department
                departmentPhaseService.validateDepartmentHeadAccessForPhase(phaseId);

                DepartmentPhaseResponse updatedPhase = departmentPhaseService.updatePhaseDates(phaseId, startDate,
                                endDate);
                return ResponseEntity.ok(updatedPhase);
        }

        // 6. Update phase
        @PutMapping("/{phaseId}")
        @ApiMessage("Cập nhật giai đoạn khoa thành công")
        @Operation(summary = "Update Department Phase", description = "Update department phase details")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Department phase updated successfully", content = @Content(schema = @Schema(implementation = DepartmentPhaseResponse.class))),
                        @ApiResponse(responseCode = "400", description = "Invalid request data"),
                        @ApiResponse(responseCode = "404", description = "Phase not found"),
                        @ApiResponse(responseCode = "403", description = "Access denied - only TRUONG_KHOA and THU_KY_QLKH_HTQT can access")
        })
        public ResponseEntity<DepartmentPhaseResponse> updatePhase(
                        @Parameter(description = "Phase ID", required = true) @PathVariable String phaseId,
                        @Parameter(description = "Phase update request", required = true) @Valid @RequestBody UpdateDepartmentPhaseRequest request) {

                // Validate user is department head of this phase's department
                departmentPhaseService.validateDepartmentHeadAccessForPhase(phaseId);

                DepartmentPhaseResponse updatedPhase = departmentPhaseService.updatePhase(phaseId, request);
                return ResponseEntity.ok(updatedPhase);
        }

        // 7. Toggle phase status
        @PutMapping("/{phaseId}/toggle-status")
        @ApiMessage("Cập nhật trạng thái giai đoạn khoa thành công")
        @Operation(summary = "Toggle Department Phase Status", description = "Enable or disable a department phase")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Department phase status updated successfully", content = @Content(schema = @Schema(implementation = DepartmentPhaseResponse.class))),
                        @ApiResponse(responseCode = "400", description = "Invalid request data"),
                        @ApiResponse(responseCode = "403", description = "Access denied - only TRUONG_KHOA and THU_KY_QLKH_HTQT can access")
        })
        public ResponseEntity<DepartmentPhaseResponse> togglePhaseStatus(
                        @Parameter(description = "Phase ID", required = true) @PathVariable String phaseId,
                        @Parameter(description = "Is Active", required = true) @RequestParam boolean isActive) {

                // Validate user is department head of this phase's department
                departmentPhaseService.validateDepartmentHeadAccessForPhase(phaseId);

                DepartmentPhaseResponse updatedPhase = departmentPhaseService.togglePhaseStatus(phaseId, isActive);
                return ResponseEntity.ok(updatedPhase);
        }

        // 8. Create all 3 required phases for department (SUBMISSION,
        // DEPARTMENT_EVALUATION, DOCUMENT_SUBMISSION)
        @PostMapping("/department/{departmentId}/round/{roundId}/create-all-phases")
        @ApiMessage("Tạo tất cả 3 giai đoạn cần thiết cho khoa thành công")
        @Operation(summary = "Create All Required Phases for Department", description = "Create all 3 required phases (SUBMISSION, DEPARTMENT_EVALUATION, DOCUMENT_SUBMISSION) for a department in a specific round. Only TRUONG_KHOA and THU_KY_QLKH_HTQT can create phases.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "All department phases created successfully", content = @Content(schema = @Schema(implementation = List.class))),
                        @ApiResponse(responseCode = "400", description = "Invalid request data or phases already exist"),
                        @ApiResponse(responseCode = "403", description = "Access denied - only TRUONG_KHOA and THU_KY_QLKH_HTQT can create phases")
        })
        public ResponseEntity<List<DepartmentPhaseResponse>> createAllRequiredPhasesForDepartment(
                        @Parameter(description = "Department ID", required = true) @PathVariable String departmentId,
                        @Parameter(description = "Round ID", required = true) @PathVariable String roundId) {

                // Validate user is department head of this department
                departmentPhaseService.validateDepartmentHeadAccess(departmentId);

                List<DepartmentPhaseResponse> createdPhases = departmentPhaseService
                                .createAllRequiredPhasesForDepartment(departmentId, roundId);
                return ResponseEntity.ok(createdPhases);
        }

        // 9. Get phases by department and round
        @GetMapping("/department/{departmentId}/round/{roundId}")
        @ApiMessage("Lấy danh sách giai đoạn khoa theo round thành công")
        @Operation(summary = "Get Department Phases by Round", description = "Get all phases of a department in a specific round")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Department phases retrieved successfully", content = @Content(schema = @Schema(implementation = List.class))),
                        @ApiResponse(responseCode = "403", description = "Access denied - only TRUONG_KHOA and THU_KY_QLKH_HTQT can access")
        })
        public ResponseEntity<List<DepartmentPhaseResponse>> getPhasesByDepartmentAndRound(
                        @Parameter(description = "Department ID", required = true) @PathVariable String departmentId,
                        @Parameter(description = "Round ID", required = true) @PathVariable String roundId) {

                // Validate user is department head of this department
                departmentPhaseService.validateDepartmentHeadAccess(departmentId);

                List<DepartmentPhaseResponse> phases = departmentPhaseService
                                .getPhasesByDepartmentAndRound(departmentId, roundId);
                return ResponseEntity.ok(phases);
        }

        // 10. Get current active phase of department in round
        @GetMapping("/department/{departmentId}/round/{roundId}/current")
        @ApiMessage("Lấy giai đoạn hiện tại đang hoạt động của khoa thành công")
        @Operation(summary = "Get Current Active Department Phase", description = "Get current active phase of a department in a specific round")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Current active department phase retrieved successfully", content = @Content(schema = @Schema(implementation = DepartmentPhaseResponse.class))),
                        @ApiResponse(responseCode = "404", description = "No active phase found"),
                        @ApiResponse(responseCode = "403", description = "Access denied - only TRUONG_KHOA and THU_KY_QLKH_HTQT can access")
        })
        public ResponseEntity<DepartmentPhaseResponse> getCurrentActivePhase(
                        @Parameter(description = "Department ID", required = true) @PathVariable String departmentId,
                        @Parameter(description = "Round ID", required = true) @PathVariable String roundId) {

                // Validate user is department head of this department
                departmentPhaseService.validateDepartmentHeadAccess(departmentId);

                DepartmentPhaseResponse currentPhase = departmentPhaseService.getCurrentActivePhase(departmentId,
                                roundId);
                if (currentPhase == null) {
                        return ResponseEntity.notFound().build();
                }
                return ResponseEntity.ok(currentPhase);
        }

        // 11. Create single required phase for department
        @PostMapping("/department/{departmentId}/round/{roundId}/create-phase/{phaseType}")
        @ApiMessage("Tạo giai đoạn cần thiết cho khoa thành công")
        @Operation(summary = "Create Single Required Phase for Department", description = "Create a single required phase (SUBMISSION, DEPARTMENT_EVALUATION, or DOCUMENT_SUBMISSION) for a department. Only TRUONG_KHOA can create phases for their department.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Department phase created successfully", content = @Content(schema = @Schema(implementation = DepartmentPhaseResponse.class))),
                        @ApiResponse(responseCode = "400", description = "Invalid request data or phase already exists"),
                        @ApiResponse(responseCode = "403", description = "Access denied - only TRUONG_KHOA and THU_KY_QLKH_HTQT can create phases")
        })
        public ResponseEntity<DepartmentPhaseResponse> createRequiredPhaseForDepartment(
                        @Parameter(description = "Department ID", required = true) @PathVariable String departmentId,
                        @Parameter(description = "Round ID", required = true) @PathVariable String roundId,
                        @Parameter(description = "Phase Type", required = true) @PathVariable InnovationPhaseTypeEnum phaseType,
                        @Parameter(description = "Start date", required = true) @RequestParam LocalDate startDate,
                        @Parameter(description = "End date", required = true) @RequestParam LocalDate endDate,
                        @Parameter(description = "Description", required = false) @RequestParam(required = false) String description) {

                // Validate user is department head of this department
                departmentPhaseService.validateDepartmentHeadAccess(departmentId);

                DepartmentPhaseResponse createdPhase = departmentPhaseService.createRequiredPhaseForDepartment(
                                departmentId, roundId, phaseType, startDate, endDate, description);
                return ResponseEntity.ok(createdPhase);
        }

}
