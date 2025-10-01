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
import org.springframework.web.bind.annotation.*;

import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.InnovationPhase;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.InnovationPhaseTypeEnum;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.PhaseStatusEnum;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.InnovationPhaseRequest;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.UpdateInnovationPhaseRequest;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.InnovationPhaseResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.service.InnovationPhaseService;
import vn.edu.iuh.fit.innovationmanagementsystem_be.utils.ResultPaginationDTO;
import vn.edu.iuh.fit.innovationmanagementsystem_be.utils.annotation.ApiMessage;
import com.turkraft.springfilter.boot.Filter;

import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/innovation-phases")
@Tag(name = "Innovation Phase", description = "Innovation phase management APIs")
@SecurityRequirement(name = "Bearer Authentication")
public class InnovationPhaseController {

        private final InnovationPhaseService innovationPhaseService;

        public InnovationPhaseController(InnovationPhaseService innovationPhaseService) {
                this.innovationPhaseService = innovationPhaseService;
        }

        // 0. Get All Innovation Phases with Pagination and Filtering
        @GetMapping
        @ApiMessage("Lấy danh sách tất cả giai đoạn sáng kiến với phân trang và lọc thành công")
        @Operation(summary = "Get All Innovation Phases", description = "Get paginated list of all innovation phases with filtering")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Innovation phases retrieved successfully", content = @Content(schema = @Schema(implementation = ResultPaginationDTO.class))),
                        @ApiResponse(responseCode = "401", description = "Unauthorized")
        })
        public ResponseEntity<ResultPaginationDTO> getAllInnovationPhases(
                        @Parameter(description = "Filter specification for innovation phases") @Filter Specification<InnovationPhase> specification,
                        @Parameter(description = "Pagination parameters") Pageable pageable) {
                return ResponseEntity.ok(innovationPhaseService
                                .getAllInnovationPhasesWithPaginationAndFilter(specification, pageable));
        }

        // 1. Create phases for round
        @PostMapping("/round/{roundId}/create-phases")
        @ApiMessage("Tạo giai đoạn cho round thành công")
        @Operation(summary = "Create Phases for Round", description = "Create all phases for an innovation round")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Phases created successfully", content = @Content(schema = @Schema(implementation = List.class))),
                        @ApiResponse(responseCode = "400", description = "Invalid request data")
        })
        public ResponseEntity<List<InnovationPhaseResponse>> createPhasesForRound(
                        @Parameter(description = "Round ID", required = true) @PathVariable String roundId,
                        @Parameter(description = "List of phases to create", required = true) @Valid @RequestBody List<InnovationPhaseRequest> phaseRequests) {

                List<InnovationPhaseResponse> createdPhases = innovationPhaseService.createPhasesForRound(roundId,
                                phaseRequests);
                return ResponseEntity.ok(createdPhases);
        }

        // 2. Create single phase
        @PostMapping("/round/{roundId}/create-phase")
        @ApiMessage("Tạo giai đoạn đơn lẻ thành công")
        @Operation(summary = "Create Single Phase", description = "Create a single phase for an innovation round")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Phase created successfully", content = @Content(schema = @Schema(implementation = InnovationPhaseResponse.class))),
                        @ApiResponse(responseCode = "400", description = "Invalid request data")
        })
        public ResponseEntity<InnovationPhaseResponse> createSinglePhase(
                        @Parameter(description = "Round ID", required = true) @PathVariable String roundId,
                        @Parameter(description = "Phase details", required = true) @Valid @RequestBody InnovationPhaseRequest phaseRequest) {

                InnovationPhaseResponse createdPhase = innovationPhaseService.createSinglePhase(roundId, phaseRequest);
                return ResponseEntity.ok(createdPhase);
        }

        // 3. Get phases by round
        @GetMapping("/round/{roundId}")
        @ApiMessage("Lấy danh sách giai đoạn thành công")
        @Operation(summary = "Get Phases by Round", description = "Get all phases for an innovation round")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Phases retrieved successfully", content = @Content(schema = @Schema(implementation = List.class)))
        })
        public ResponseEntity<List<InnovationPhaseResponse>> getPhasesByRound(
                        @Parameter(description = "Round ID", required = true) @PathVariable String roundId) {

                List<InnovationPhaseResponse> phases = innovationPhaseService.getPhasesByRound(roundId);
                return ResponseEntity.ok(phases);
        }

        // 4. Get current active phase
        @GetMapping("/round/{roundId}/current")
        @ApiMessage("Lấy giai đoạn hiện tại thành công")
        @Operation(summary = "Get Current Active Phase", description = "Get current active phase for an innovation round")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Current phase retrieved successfully", content = @Content(schema = @Schema(implementation = InnovationPhaseResponse.class))),
                        @ApiResponse(responseCode = "404", description = "No active phase found")
        })
        public ResponseEntity<InnovationPhaseResponse> getCurrentActivePhase(
                        @Parameter(description = "Round ID", required = true) @PathVariable String roundId) {

                InnovationPhaseResponse currentPhase = innovationPhaseService.getCurrentActivePhase(roundId);
                if (currentPhase == null) {
                        return ResponseEntity.notFound().build();
                }
                return ResponseEntity.ok(currentPhase);
        }

        // 5. Get phase by type
        @GetMapping("/round/{roundId}/type/{phaseType}")
        @ApiMessage("Lấy giai đoạn theo loại thành công")
        @Operation(summary = "Get Phase by Type", description = "Get phase by type for an innovation round")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Phase retrieved successfully", content = @Content(schema = @Schema(implementation = InnovationPhaseResponse.class))),
                        @ApiResponse(responseCode = "404", description = "Phase not found")
        })
        public ResponseEntity<InnovationPhaseResponse> getPhaseByType(
                        @Parameter(description = "Round ID", required = true) @PathVariable String roundId,
                        @Parameter(description = "Phase Type", required = true) @PathVariable InnovationPhaseTypeEnum phaseType) {

                InnovationPhaseResponse phase = innovationPhaseService.getPhaseByType(roundId, phaseType);
                if (phase == null) {
                        return ResponseEntity.notFound().build();
                }
                return ResponseEntity.ok(phase);
        }

        // 6. Update phase
        @PutMapping("/{phaseId}")
        @ApiMessage("Cập nhật giai đoạn thành công")
        @Operation(summary = "Update Phase", description = "Update innovation phase details")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Phase updated successfully", content = @Content(schema = @Schema(implementation = InnovationPhaseResponse.class))),
                        @ApiResponse(responseCode = "400", description = "Invalid request data"),
                        @ApiResponse(responseCode = "404", description = "Phase not found")
        })
        public ResponseEntity<InnovationPhaseResponse> updatePhase(
                        @Parameter(description = "Phase ID", required = true) @PathVariable String phaseId,
                        @Parameter(description = "Phase update request", required = true) @Valid @RequestBody UpdateInnovationPhaseRequest request) {

                InnovationPhaseResponse updatedPhase = innovationPhaseService.updatePhase(phaseId, request);
                return ResponseEntity.ok(updatedPhase);
        }

        // 7. Update phase dates
        @PutMapping("/{phaseId}/dates")
        @ApiMessage("Cập nhật thời gian giai đoạn thành công")
        @Operation(summary = "Update Phase Dates", description = "Update start and end dates of an innovation phase")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Phase dates updated successfully", content = @Content(schema = @Schema(implementation = InnovationPhaseResponse.class))),
                        @ApiResponse(responseCode = "400", description = "Invalid request data or time constraints violated"),
                        @ApiResponse(responseCode = "404", description = "Phase not found")
        })
        public ResponseEntity<InnovationPhaseResponse> updatePhaseDates(
                        @Parameter(description = "Phase ID", required = true) @PathVariable String phaseId,
                        @Parameter(description = "Start date", required = true) @RequestParam LocalDate startDate,
                        @Parameter(description = "End date", required = true) @RequestParam LocalDate endDate) {

                InnovationPhaseResponse updatedPhase = innovationPhaseService.updatePhaseDates(phaseId, startDate,
                                endDate);
                return ResponseEntity.ok(updatedPhase);
        }

        // 8. Toggle phase status
        @PutMapping("/{phaseId}/toggle-status")
        @ApiMessage("Cập nhật trạng thái giai đoạn thành công")
        @Operation(summary = "Toggle Phase Status", description = "Enable or disable an innovation phase")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Phase status updated successfully", content = @Content(schema = @Schema(implementation = InnovationPhaseResponse.class))),
                        @ApiResponse(responseCode = "400", description = "Invalid request data"),
                        @ApiResponse(responseCode = "404", description = "Phase not found")
        })
        public ResponseEntity<InnovationPhaseResponse> togglePhaseStatus(
                        @Parameter(description = "Phase ID", required = true) @PathVariable String phaseId,
                        @Parameter(description = "Is Active", required = true) @RequestParam boolean isActive) {

                InnovationPhaseResponse updatedPhase = innovationPhaseService.togglePhaseStatus(phaseId, isActive);
                return ResponseEntity.ok(updatedPhase);
        }

        // 9. Transition phase status - ADMIN ONLY
        @PutMapping("/{phaseId}/transition")
        @ApiMessage("Chuyển đổi trạng thái giai đoạn thành công")
        @Operation(summary = "Transition Phase Status", description = "Manually transition phase to a new status")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Phase transitioned successfully", content = @Content(schema = @Schema(implementation = InnovationPhaseResponse.class))),
                        @ApiResponse(responseCode = "400", description = "Invalid transition or business rule violation"),
                        @ApiResponse(responseCode = "404", description = "Phase not found")
        })
        public ResponseEntity<InnovationPhaseResponse> transitionPhase(
                        @Parameter(description = "Phase ID", required = true) @PathVariable String phaseId,
                        @Parameter(description = "Target Status", required = true) @RequestParam PhaseStatusEnum targetStatus,
                        @Parameter(description = "Transition Reason", required = false) @RequestParam(required = false) String reason) {

                InnovationPhaseResponse updatedPhase = innovationPhaseService.transitionPhase(phaseId, targetStatus,
                                reason);
                return ResponseEntity.ok(updatedPhase);
        }

        // 10. Complete phase - ADMIN ONLY
        @PutMapping("/{phaseId}/complete")
        @ApiMessage("Hoàn thành giai đoạn thành công")
        @Operation(summary = "Complete Phase", description = "Mark phase as completed")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Phase completed successfully", content = @Content(schema = @Schema(implementation = InnovationPhaseResponse.class))),
                        @ApiResponse(responseCode = "400", description = "Invalid transition"),
                        @ApiResponse(responseCode = "404", description = "Phase not found")
        })
        public ResponseEntity<InnovationPhaseResponse> completePhase(
                        @Parameter(description = "Phase ID", required = true) @PathVariable String phaseId,
                        @Parameter(description = "Completion Reason", required = false) @RequestParam(required = false) String reason) {

                InnovationPhaseResponse updatedPhase = innovationPhaseService.completePhase(phaseId, reason);
                return ResponseEntity.ok(updatedPhase);
        }

        // 11. Suspend phase - ADMIN ONLY
        @PutMapping("/{phaseId}/suspend")
        @ApiMessage("Tạm dừng giai đoạn thành công")
        @Operation(summary = "Suspend Phase", description = "Suspend an active phase")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Phase suspended successfully", content = @Content(schema = @Schema(implementation = InnovationPhaseResponse.class))),
                        @ApiResponse(responseCode = "400", description = "Invalid transition"),
                        @ApiResponse(responseCode = "404", description = "Phase not found")
        })
        public ResponseEntity<InnovationPhaseResponse> suspendPhase(
                        @Parameter(description = "Phase ID", required = true) @PathVariable String phaseId,
                        @Parameter(description = "Suspension Reason", required = false) @RequestParam(required = false) String reason) {

                InnovationPhaseResponse updatedPhase = innovationPhaseService.suspendPhase(phaseId, reason);
                return ResponseEntity.ok(updatedPhase);
        }

        // 12. Cancel phase - ADMIN ONLY
        @PutMapping("/{phaseId}/cancel")
        @ApiMessage("Hủy giai đoạn thành công")
        @Operation(summary = "Cancel Phase", description = "Cancel a phase")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Phase cancelled successfully", content = @Content(schema = @Schema(implementation = InnovationPhaseResponse.class))),
                        @ApiResponse(responseCode = "400", description = "Invalid transition"),
                        @ApiResponse(responseCode = "404", description = "Phase not found")
        })
        public ResponseEntity<InnovationPhaseResponse> cancelPhase(
                        @Parameter(description = "Phase ID", required = true) @PathVariable String phaseId,
                        @Parameter(description = "Cancellation Reason", required = false) @RequestParam(required = false) String reason) {

                InnovationPhaseResponse updatedPhase = innovationPhaseService.cancelPhase(phaseId, reason);
                return ResponseEntity.ok(updatedPhase);
        }

        // 13. Get phases by status
        @GetMapping("/round/{roundId}/status/{status}")
        @ApiMessage("Lấy danh sách giai đoạn theo trạng thái thành công")
        @Operation(summary = "Get Phases by Status", description = "Get phases by status for a round")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Phases retrieved successfully", content = @Content(schema = @Schema(implementation = List.class)))
        })
        public ResponseEntity<List<InnovationPhaseResponse>> getPhasesByStatus(
                        @Parameter(description = "Round ID", required = true) @PathVariable String roundId,
                        @Parameter(description = "Phase Status", required = true) @PathVariable PhaseStatusEnum status) {

                List<InnovationPhaseResponse> phases = innovationPhaseService.getPhasesByStatus(roundId, status);
                return ResponseEntity.ok(phases);
        }

        // 14. Get phase status summary
        @GetMapping("/round/{roundId}/status-summary")
        @ApiMessage("Lấy tóm tắt trạng thái giai đoạn thành công")
        @Operation(summary = "Get Phase Status Summary", description = "Get summary of all phase statuses for a round")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Status summary retrieved successfully")
        })
        public ResponseEntity<Object> getPhaseStatusSummary(
                        @Parameter(description = "Round ID", required = true) @PathVariable String roundId) {

                Object summary = innovationPhaseService.getPhaseStatusSummary(roundId);
                return ResponseEntity.ok(summary);
        }
}