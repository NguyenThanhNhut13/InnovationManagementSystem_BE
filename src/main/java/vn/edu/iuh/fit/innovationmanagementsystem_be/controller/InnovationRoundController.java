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
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.CreateInnovationRoundRequest;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.UpdateInnovationRoundRequest;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.InnovationRoundResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.service.InnovationRoundService;
import vn.edu.iuh.fit.innovationmanagementsystem_be.utils.ResultPaginationDTO;
import vn.edu.iuh.fit.innovationmanagementsystem_be.utils.annotation.ApiMessage;
import com.turkraft.springfilter.boot.Filter;

import jakarta.validation.Valid;
// import java.util.List;

@RestController
@RequestMapping("/api/v1/innovation-rounds")
@Tag(name = "Innovation Round", description = "Innovation round management APIs")
@SecurityRequirement(name = "Bearer Authentication")
public class InnovationRoundController {

        private final InnovationRoundService innovationRoundService;

        public InnovationRoundController(InnovationRoundService innovationRoundService) {
                this.innovationRoundService = innovationRoundService;
        }

        // 1. Get All Innovation Rounds với Pagination và Filtering
        @GetMapping
        @ApiMessage("Lấy danh sách tất cả đợt sáng kiến với phân trang và lọc thành công")
        @Operation(summary = "Get All Innovation Rounds", description = "Get paginated list of all innovation rounds with filtering")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Innovation rounds retrieved successfully", content = @Content(schema = @Schema(implementation = ResultPaginationDTO.class))),
                        @ApiResponse(responseCode = "401", description = "Unauthorized")
        })
        public ResponseEntity<ResultPaginationDTO> getAllInnovationRounds(
                        @Parameter(description = "Filter specification for innovation rounds") @Filter Specification<InnovationRound> specification,
                        @Parameter(description = "Pagination parameters") Pageable pageable) {
                return ResponseEntity.ok(innovationRoundService
                                .getAllInnovationRoundsWithPaginationAndFilter(specification, pageable));
        }

        // 2. Get Innovation Rounds List - No InnovationDecision and InnovationPhase
        @GetMapping("/list")
        @ApiMessage("Lấy danh sách đợt sáng kiến cho hiển thị bảng thành công")
        @Operation(summary = "Get Innovation Rounds List for Table", description = "Get paginated list of innovation rounds with specific fields for table display")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Innovation rounds list retrieved successfully", content = @Content(schema = @Schema(implementation = ResultPaginationDTO.class))),
                        @ApiResponse(responseCode = "401", description = "Unauthorized")
        })
        public ResponseEntity<ResultPaginationDTO> getInnovationRoundsListForTable(
                        @Parameter(description = "Filter specification for innovation rounds") @Filter Specification<InnovationRound> specification,
                        @Parameter(description = "Pagination parameters") Pageable pageable) {
                return ResponseEntity
                                .ok(innovationRoundService.getInnovationRoundsListForTable(specification, pageable));
        }

        // 3. Tạo innovationRound
        @PostMapping
        @ApiMessage("Tạo đợt sáng kiến thành công")
        @Operation(summary = "Create Innovation Round", description = "Create a new innovation round")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Innovation round created successfully", content = @Content(schema = @Schema(implementation = InnovationRoundResponse.class))),
                        @ApiResponse(responseCode = "400", description = "Invalid request data")
        })
        public ResponseEntity<InnovationRoundResponse> createInnovationRound(
                        @Parameter(description = "Innovation round details", required = true) @Valid @RequestBody CreateInnovationRoundRequest request) {

                InnovationRoundResponse createdRound = innovationRoundService.createInnovationRound(request);
                return ResponseEntity.ok(createdRound);
        }

        // 4. Cập nhật round
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

        // 5. Lấy current round
        @GetMapping("/current")
        @ApiMessage("Lấy thông tin đợt sáng kiến hiện tại thành công")
        @Operation(summary = "Get Current Round", description = "Get current active innovation round (restricted to TRUONG_KHOA, QUAN_TRI_VIEN_QLKH_HTQT, QUAN_TRI_VIEN_HE_THONG)")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Current round retrieved successfully", content = @Content(schema = @Schema(implementation = InnovationRoundResponse.class))),
                        @ApiResponse(responseCode = "404", description = "No current round found"),
                        @ApiResponse(responseCode = "403", description = "Access denied - insufficient privileges")
        })
        public ResponseEntity<InnovationRoundResponse> getCurrentRound() {
                InnovationRoundResponse currentRound = innovationRoundService.getCurrentRound();
                if (currentRound == null) {
                        return ResponseEntity.notFound().build();
                }
                return ResponseEntity.ok(currentRound);
        }

}
