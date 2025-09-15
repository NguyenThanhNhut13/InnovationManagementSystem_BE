package vn.edu.iuh.fit.innovationmanagementsystem_be.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.InnovationPhase;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.InnovationPhaseEnum;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.CreateRoundPhasesRequest;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.InnovationPhaseRequest;
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

    // 1. Create phases for a round with custom dates
    @PostMapping("/round/{roundId}/create-phases")
    @ApiMessage("Tạo giai đoạn cho round thành công")
    @Operation(summary = "Create Phases for Round", description = "Create all phases for an innovation round with custom dates")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Phases created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data")
    })
    public ResponseEntity<List<InnovationPhase>> createPhasesForRound(
            @Parameter(description = "Round ID", required = true) @PathVariable String roundId,
            @Parameter(description = "List of phases with custom dates", required = true) @Valid @RequestBody List<InnovationPhaseRequest> phases) {

        CreateRoundPhasesRequest request = new CreateRoundPhasesRequest();
        request.setRoundId(roundId);
        request.setPhases(phases);

        List<InnovationPhase> createdPhases = innovationPhaseService.createPhasesForRound(request);
        return ResponseEntity.ok(createdPhases);
    }

    // 2. Create single phase
    @PostMapping("/round/{roundId}/create-phase")
    @ApiMessage("Tạo giai đoạn đơn lẻ thành công")
    @Operation(summary = "Create Single Phase", description = "Create a single phase for an innovation round")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Phase created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data")
    })
    public ResponseEntity<InnovationPhase> createSinglePhase(
            @Parameter(description = "Round ID", required = true) @PathVariable String roundId,
            @Parameter(description = "Phase details", required = true) @Valid @RequestBody InnovationPhaseRequest phaseRequest) {

        InnovationPhase createdPhase = innovationPhaseService.createSinglePhase(roundId, phaseRequest);
        return ResponseEntity.ok(createdPhase);
    }

    // 3. Get phases by round id
    @GetMapping("/round/{roundId}")
    @ApiMessage("Lấy danh sách giai đoạn thành công")
    @Operation(summary = "Get Phases by Round", description = "Get all phases of an innovation round")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Phases retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data")
    })
    public ResponseEntity<List<InnovationPhase>> getPhasesByRound(
            @Parameter(description = "Round ID", required = true) @PathVariable String roundId) {

        List<InnovationPhase> phases = innovationPhaseService.getPhasesByRoundId(roundId);
        return ResponseEntity.ok(phases);
    }

    // 4. Get current phase
    @GetMapping("/round/{roundId}/current")
    @ApiMessage("Lấy giai đoạn hiện tại thành công")
    @Operation(summary = "Get Current Phase", description = "Get current active phase of an innovation round")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Current phase retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "No active phase found")
    })
    public ResponseEntity<InnovationPhase> getCurrentPhase(
            @Parameter(description = "Round ID", required = true) @PathVariable String roundId) {

        InnovationPhase currentPhase = innovationPhaseService.getCurrentPhase(roundId);
        if (currentPhase == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(currentPhase);
    }

    // 5. Get phase by type
    @GetMapping("/round/{roundId}/type/{phaseType}")
    @ApiMessage("Lấy giai đoạn theo loại thành công")
    @Operation(summary = "Get Phase by Type", description = "Get phase by type of an innovation round")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Phase retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Phase not found")
    })
    public ResponseEntity<InnovationPhase> getPhaseByType(
            @Parameter(description = "Round ID", required = true) @PathVariable String roundId,
            @Parameter(description = "Phase Type", required = true) @PathVariable InnovationPhaseEnum phaseType) {

        InnovationPhase phase = innovationPhaseService.getPhaseByType(roundId, phaseType);
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
            @ApiResponse(responseCode = "200", description = "Phase dates updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data")
    })
    public ResponseEntity<InnovationPhase> updatePhaseDates(
            @Parameter(description = "Phase ID", required = true) @PathVariable String phaseId,
            @Parameter(description = "Start date", required = true) @RequestParam LocalDate startDate,
            @Parameter(description = "End date", required = true) @RequestParam LocalDate endDate) {

        InnovationPhase updatedPhase = innovationPhaseService.updatePhaseDates(phaseId, startDate, endDate);
        return ResponseEntity.ok(updatedPhase);
    }

    // 7. Update phase status
    @PutMapping("/{phaseId}/toggle-status")
    @ApiMessage("Cập nhật trạng thái giai đoạn thành công")
    @Operation(summary = "Toggle Phase Status", description = "Enable or disable a phase")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Phase status updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data")
    })
    public ResponseEntity<InnovationPhase> togglePhaseStatus(
            @Parameter(description = "Phase ID", required = true) @PathVariable String phaseId,
            @Parameter(description = "Is Active", required = true) @RequestParam boolean isActive) {

        InnovationPhase updatedPhase = innovationPhaseService.togglePhaseStatus(phaseId, isActive);
        return ResponseEntity.ok(updatedPhase);
    }

    // 8. Check if can submit innovation
    @GetMapping("/round/{roundId}/can-submit")
    @ApiMessage("Kiểm tra khả năng nộp hồ sơ thành công")
    @Operation(summary = "Check Can Submit", description = "Check if can submit innovation in current phase")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Check result retrieved successfully")
    })
    public ResponseEntity<Boolean> canSubmitInnovation(
            @Parameter(description = "Round ID", required = true) @PathVariable String roundId) {

        boolean canSubmit = innovationPhaseService.canSubmitInnovation(roundId);
        return ResponseEntity.ok(canSubmit);
    }

    // 9. Check if can review at department level
    @GetMapping("/round/{roundId}/can-review-department")
    @ApiMessage("Kiểm tra khả năng chấm cấp khoa thành công")
    @Operation(summary = "Check Can Review Department", description = "Check if can review at department level")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Check result retrieved successfully")
    })
    public ResponseEntity<Boolean> canReviewAtDepartmentLevel(
            @Parameter(description = "Round ID", required = true) @PathVariable String roundId) {

        boolean canReview = innovationPhaseService.canReviewAtDepartmentLevel(roundId);
        return ResponseEntity.ok(canReview);
    }

    // 10. Check if can review at university level
    @GetMapping("/round/{roundId}/can-review-university")
    @ApiMessage("Kiểm tra khả năng chấm cấp trường thành công")
    @Operation(summary = "Check Can Review University", description = "Check if can review at university level")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Check result retrieved successfully")
    })
    public ResponseEntity<Boolean> canReviewAtUniversityLevel(
            @Parameter(description = "Round ID", required = true) @PathVariable String roundId) {

        boolean canReview = innovationPhaseService.canReviewAtUniversityLevel(roundId);
        return ResponseEntity.ok(canReview);
    }

    // 11. Check if can announce results
    @GetMapping("/round/{roundId}/can-announce")
    @ApiMessage("Kiểm tra khả năng công bố kết quả thành công")
    @Operation(summary = "Check Can Announce", description = "Check if can announce results")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Check result retrieved successfully")
    })
    public ResponseEntity<Boolean> canAnnounceResults(
            @Parameter(description = "Round ID", required = true) @PathVariable String roundId) {

        boolean canAnnounce = innovationPhaseService.canAnnounceResults(roundId);
        return ResponseEntity.ok(canAnnounce);
    }
}
