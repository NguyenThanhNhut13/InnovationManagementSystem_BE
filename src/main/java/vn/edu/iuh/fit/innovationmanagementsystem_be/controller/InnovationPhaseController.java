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
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.InnovationPhaseEnum;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.CreateRoundPhasesRequest;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.InnovationPhaseRequest;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.UpdateInnovationPhaseRequest;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.InnovationPhaseResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.service.InnovationPhaseService;
import vn.edu.iuh.fit.innovationmanagementsystem_be.utils.annotation.ApiMessage;

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

        // 1. Create phases with custom dates
        @PostMapping("/create-phases")
        @ApiMessage("Tạo giai đoạn cho round thành công")
        @Operation(summary = "Create Phases for Round", description = "Create all phases for an innovation round with custom dates")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Phases created successfully", content = @Content(schema = @Schema(implementation = List.class))),
                        @ApiResponse(responseCode = "400", description = "Invalid request data")
        })
        public ResponseEntity<List<InnovationPhaseResponse>> createPhasesForDecision(
                        @Parameter(description = "Create phases request with decision info and phases", required = true) @Valid @RequestBody CreateRoundPhasesRequest request) {

                List<InnovationPhaseResponse> createdPhases = innovationPhaseService.createPhasesForDecision(request);
                return ResponseEntity.ok(createdPhases);
        }

        // 2. Create single phase
        @PostMapping("/decision/{decisionId}/create-phase")
        @ApiMessage("Tạo giai đoạn đơn lẻ thành công")
        @Operation(summary = "Create Single Phase", description = "Create a single phase for an innovation round")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Phase created successfully", content = @Content(schema = @Schema(implementation = InnovationPhaseResponse.class))),
                        @ApiResponse(responseCode = "400", description = "Invalid request data")
        })
        public ResponseEntity<InnovationPhaseResponse> createSinglePhase(
                        @Parameter(description = "Decision ID", required = true) @PathVariable String decisionId,
                        @Parameter(description = "Phase details", required = true) @Valid @RequestBody InnovationPhaseRequest phaseRequest) {

                InnovationPhaseResponse createdPhase = innovationPhaseService.createSinglePhase(decisionId,
                                phaseRequest);
                return ResponseEntity.ok(createdPhase);
        }

        // 3. Get phases by decision id
        @GetMapping("/decision/{decisionId}")
        @ApiMessage("Lấy danh sách giai đoạn thành công")
        @Operation(summary = "Get Phases by Decision", description = "Get all phases of an innovation decision")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Phases retrieved successfully", content = @Content(schema = @Schema(implementation = List.class))),
                        @ApiResponse(responseCode = "400", description = "Invalid request data")
        })
        public ResponseEntity<List<InnovationPhaseResponse>> getPhasesByDecision(
                        @Parameter(description = "Decision ID", required = true) @PathVariable String decisionId) {

                List<InnovationPhaseResponse> phases = innovationPhaseService.getPhasesByDecisionId(decisionId);
                return ResponseEntity.ok(phases);
        }

        // 4. Get current phase
        @GetMapping("/decision/{decisionId}/current")
        @ApiMessage("Lấy giai đoạn hiện tại thành công")
        @Operation(summary = "Get Current Phase", description = "Get current active phase of an innovation decision")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Current phase retrieved successfully", content = @Content(schema = @Schema(implementation = InnovationPhaseResponse.class))),
                        @ApiResponse(responseCode = "404", description = "No active phase found")
        })
        public ResponseEntity<InnovationPhaseResponse> getCurrentPhase(
                        @Parameter(description = "Decision ID", required = true) @PathVariable String decisionId) {

                InnovationPhaseResponse currentPhase = innovationPhaseService.getCurrentPhase(decisionId);
                if (currentPhase == null) {
                        return ResponseEntity.notFound().build();
                }
                return ResponseEntity.ok(currentPhase);
        }

        // 5. Get phase by type
        @GetMapping("/decision/{decisionId}/type/{phaseType}")
        @ApiMessage("Lấy giai đoạn theo loại thành công")
        @Operation(summary = "Get Phase by Type", description = "Get phase by type of an innovation decision")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Phase retrieved successfully", content = @Content(schema = @Schema(implementation = InnovationPhaseResponse.class))),
                        @ApiResponse(responseCode = "404", description = "Phase not found")
        })
        public ResponseEntity<InnovationPhaseResponse> getPhaseByType(
                        @Parameter(description = "Decision ID", required = true) @PathVariable String decisionId,
                        @Parameter(description = "Phase Type", required = true) @PathVariable InnovationPhaseEnum phaseType) {

                InnovationPhaseResponse phase = innovationPhaseService.getPhaseByType(decisionId, phaseType);
                if (phase == null) {
                        return ResponseEntity.notFound().build();
                }
                return ResponseEntity.ok(phase);
        }

        // 6. Update phase dates
        @PutMapping("/{phaseId}/dates")
        @ApiMessage("Cập nhật thời gian giai đoạn thành công")
        @Operation(summary = "Update Phase Dates", description = "Update start and end dates of a phase")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Phase dates updated successfully", content = @Content(schema = @Schema(implementation = InnovationPhaseResponse.class))),
                        @ApiResponse(responseCode = "400", description = "Invalid request data")
        })
        public ResponseEntity<InnovationPhaseResponse> updatePhaseDates(
                        @Parameter(description = "Phase ID", required = true) @PathVariable String phaseId,
                        @Parameter(description = "Start date", required = true) @RequestParam LocalDate startDate,
                        @Parameter(description = "End date", required = true) @RequestParam LocalDate endDate) {

                InnovationPhaseResponse updatedPhase = innovationPhaseService.updatePhaseDates(phaseId, startDate,
                                endDate);
                return ResponseEntity.ok(updatedPhase);
        }

        // 7. Update phase status
        @PutMapping("/{phaseId}/toggle-status")
        @ApiMessage("Cập nhật trạng thái giai đoạn thành công")
        @Operation(summary = "Toggle Phase Status", description = "Enable or disable a phase")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Phase status updated successfully", content = @Content(schema = @Schema(implementation = InnovationPhaseResponse.class))),
                        @ApiResponse(responseCode = "400", description = "Invalid request data")
        })
        public ResponseEntity<InnovationPhaseResponse> togglePhaseStatus(
                        @Parameter(description = "Phase ID", required = true) @PathVariable String phaseId,
                        @Parameter(description = "Is Active", required = true) @RequestParam boolean isActive) {

                InnovationPhaseResponse updatedPhase = innovationPhaseService.togglePhaseStatus(phaseId, isActive);
                return ResponseEntity.ok(updatedPhase);
        }

        // 8. Update phase
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

}
