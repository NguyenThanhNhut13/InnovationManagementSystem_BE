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

import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.InnovationRound;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.InnovationRoundRequest;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.UpdateInnovationRoundRequest;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.InnovationRoundResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.service.InnovationRoundService;
import vn.edu.iuh.fit.innovationmanagementsystem_be.utils.ResultPaginationDTO;
import vn.edu.iuh.fit.innovationmanagementsystem_be.utils.annotation.ApiMessage;
import com.turkraft.springfilter.boot.Filter;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/v1/innovation-rounds")
@Tag(name = "Innovation Round", description = "Innovation round management APIs")
@SecurityRequirement(name = "Bearer Authentication")
public class InnovationRoundController {

        private final InnovationRoundService innovationRoundService;

        public InnovationRoundController(InnovationRoundService innovationRoundService) {
                this.innovationRoundService = innovationRoundService;
        }

        // 1. Create innovation round
        @PostMapping
        @ApiMessage("Tạo đợt sáng kiến thành công")
        @Operation(summary = "Create Innovation Round", description = "Create a new innovation round")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Innovation round created successfully", content = @Content(schema = @Schema(implementation = InnovationRoundResponse.class))),
                        @ApiResponse(responseCode = "400", description = "Invalid request data")
        })
        public ResponseEntity<InnovationRoundResponse> createInnovationRound(
                        @Parameter(description = "Innovation round details", required = true) @Valid @RequestBody InnovationRoundRequest request) {

                InnovationRoundResponse createdRound = innovationRoundService.createInnovationRound(request);
                return ResponseEntity.ok(createdRound);
        }

        // 2. Get all rounds by decision with pagination and filtering
        @GetMapping("/decision/{decisionId}")
        @ApiMessage("Lấy danh sách đợt sáng kiến thành công")
        @Operation(summary = "Get Rounds by Decision", description = "Get all innovation rounds for a specific decision with pagination and filtering")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Innovation rounds retrieved successfully", content = @Content(schema = @Schema(implementation = ResultPaginationDTO.class)))
        })
        public ResponseEntity<ResultPaginationDTO> getRoundsByDecision(
                        @Parameter(description = "Decision ID", required = true) @PathVariable String decisionId,
                        @Parameter(description = "Filter specification for innovation rounds") @Filter Specification<InnovationRound> specification,
                        @Parameter(description = "Pagination parameters") Pageable pageable) {

                ResultPaginationDTO rounds = innovationRoundService.getRoundsByDecision(decisionId, specification,
                                pageable);
                return ResponseEntity.ok(rounds);
        }

        // 3. Get round by ID
        @GetMapping("/{roundId}")
        @ApiMessage("Lấy thông tin đợt sáng kiến thành công")
        @Operation(summary = "Get Round by ID", description = "Get innovation round by ID")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Innovation round retrieved successfully", content = @Content(schema = @Schema(implementation = InnovationRoundResponse.class))),
                        @ApiResponse(responseCode = "404", description = "Round not found")
        })
        public ResponseEntity<InnovationRoundResponse> getRoundById(
                        @Parameter(description = "Round ID", required = true) @PathVariable String roundId) {

                InnovationRoundResponse round = innovationRoundService.getRoundById(roundId);
                return ResponseEntity.ok(round);
        }

        // 4. Get current active round
        @GetMapping("/decision/{decisionId}/current")
        @ApiMessage("Lấy đợt sáng kiến hiện tại thành công")
        @Operation(summary = "Get Current Active Round", description = "Get current active innovation round for a decision")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Current active round retrieved successfully", content = @Content(schema = @Schema(implementation = InnovationRoundResponse.class))),
                        @ApiResponse(responseCode = "404", description = "No active round found")
        })
        public ResponseEntity<InnovationRoundResponse> getCurrentActiveRound(
                        @Parameter(description = "Decision ID", required = true) @PathVariable String decisionId) {

                InnovationRoundResponse currentRound = innovationRoundService.getCurrentActiveRound(decisionId);
                if (currentRound == null) {
                        return ResponseEntity.notFound().build();
                }
                return ResponseEntity.ok(currentRound);
        }

        // 5. Update round
        @PutMapping("/{roundId}")
        @ApiMessage("Cập nhật đợt sáng kiến thành công")
        @Operation(summary = "Update Innovation Round", description = "Update innovation round details")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Innovation round updated successfully", content = @Content(schema = @Schema(implementation = InnovationRoundResponse.class))),
                        @ApiResponse(responseCode = "400", description = "Invalid request data"),
                        @ApiResponse(responseCode = "404", description = "Round not found")
        })
        public ResponseEntity<InnovationRoundResponse> updateRound(
                        @Parameter(description = "Round ID", required = true) @PathVariable String roundId,
                        @Parameter(description = "Round update request", required = true) @Valid @RequestBody UpdateInnovationRoundRequest request) {

                InnovationRoundResponse updatedRound = innovationRoundService.updateRound(roundId, request);
                return ResponseEntity.ok(updatedRound);
        }

        // 7. Toggle round status
        @PutMapping("/{roundId}/toggle-status")
        @ApiMessage("Cập nhật trạng thái đợt sáng kiến thành công")
        @Operation(summary = "Toggle Round Status", description = "Enable or disable innovation round")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Round status updated successfully", content = @Content(schema = @Schema(implementation = InnovationRoundResponse.class))),
                        @ApiResponse(responseCode = "400", description = "Invalid request data"),
                        @ApiResponse(responseCode = "404", description = "Round not found")
        })
        public ResponseEntity<InnovationRoundResponse> toggleRoundStatus(
                        @Parameter(description = "Round ID", required = true) @PathVariable String roundId,
                        @Parameter(description = "Is Active", required = true) @RequestParam boolean isActive) {

                InnovationRoundResponse updatedRound = innovationRoundService.toggleRoundStatus(roundId, isActive);
                return ResponseEntity.ok(updatedRound);
        }

        // 8. Get rounds by status
        @GetMapping("/status/{status}")
        @ApiMessage("Lấy danh sách đợt sáng kiến theo trạng thái thành công")
        @Operation(summary = "Get Rounds by Status", description = "Get innovation rounds by status")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Innovation rounds retrieved successfully", content = @Content(schema = @Schema(implementation = List.class)))
        })
        public ResponseEntity<List<InnovationRoundResponse>> getRoundsByStatus(
                        @Parameter(description = "Status", required = true) @PathVariable String status) {

                List<InnovationRoundResponse> rounds = innovationRoundService.getRoundsByStatus(status);
                return ResponseEntity.ok(rounds);
        }

}
