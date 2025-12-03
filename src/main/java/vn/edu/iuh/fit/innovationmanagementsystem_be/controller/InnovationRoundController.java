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
import org.springframework.security.access.prepost.PreAuthorize;
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

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Innovation Round", description = "Innovation round management APIs")
@SecurityRequirement(name = "Bearer Authentication")
public class InnovationRoundController {

        private final InnovationRoundService innovationRoundService;

        public InnovationRoundController(InnovationRoundService innovationRoundService) {
                this.innovationRoundService = innovationRoundService;
        }

        // 1. Get All Innovation Rounds với Pagination và Filtering
        @GetMapping("/innovation-rounds")
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
        @GetMapping("/innovation-rounds/list")
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
        @PostMapping("/innovation-rounds")
        @PreAuthorize("hasAnyRole('QUAN_TRI_VIEN_QLKH_HTQT', 'QUAN_TRI_VIEN_HE_THONG')")
        @ApiMessage("Tạo đợt sáng kiến thành công")
        @Operation(summary = "Create Innovation Round", description = "Create a new innovation round with default status DRAFT")
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
        @PutMapping("/innovation-rounds/{roundId}")
        @PreAuthorize("hasAnyRole('QUAN_TRI_VIEN_QLKH_HTQT', 'QUAN_TRI_VIEN_HE_THONG')")
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
        @GetMapping("/innovation-rounds/current")
        @ApiMessage("Lấy thông tin đợt sáng kiến hiện tại thành công")
        @Operation(summary = "Get Current Round", description = "Get current active innovation round (restricted to TRUONG_KHOA, QUAN_TRI_VIEN_QLKH_HTQT, QUAN_TRI_VIEN_HE_THONG)")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Current round retrieved successfully", content = @Content(schema = @Schema(implementation = InnovationRoundResponse.class))),
                        @ApiResponse(responseCode = "400", description = "No current round found or round is in DRAFT status"),
                        @ApiResponse(responseCode = "403", description = "Access denied - insufficient privileges")
        })
        public ResponseEntity<InnovationRoundResponse> getCurrentRound() {
                InnovationRoundResponse currentRound = innovationRoundService.getCurrentRound();
                return ResponseEntity.ok(currentRound);
        }

        // 6. Công bố Round
        @PutMapping("/innovation-rounds/{roundId}/publish")
        @PreAuthorize("hasAnyRole('QUAN_TRI_VIEN_QLKH_HTQT', 'QUAN_TRI_VIEN_HE_THONG')")
        @ApiMessage("Công bố đợt sáng kiến thành công")
        @Operation(summary = "Publish Innovation Round", description = "Publish innovation round by changing status from DRAFT to OPEN")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Round published successfully", content = @Content(schema = @Schema(implementation = InnovationRoundResponse.class))),
                        @ApiResponse(responseCode = "400", description = "Invalid request - round cannot be published"),
                        @ApiResponse(responseCode = "404", description = "Round not found")
        })
        public ResponseEntity<InnovationRoundResponse> publishRound(
                        @Parameter(description = "Round ID", required = true) @PathVariable String roundId) {
                InnovationRoundResponse publishedRound = innovationRoundService.publishRound(roundId);
                return ResponseEntity.ok(publishedRound);
        }

        // 7. Đóng Round
        @PutMapping("/innovation-rounds/{roundId}/close")
        @PreAuthorize("hasAnyRole('QUAN_TRI_VIEN_QLKH_HTQT', 'QUAN_TRI_VIEN_HE_THONG')")
        @ApiMessage("Đóng đợt sáng kiến thành công")
        @Operation(summary = "Close Innovation Round", description = "Close innovation round by changing status to CLOSED")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Round closed successfully", content = @Content(schema = @Schema(implementation = InnovationRoundResponse.class))),
                        @ApiResponse(responseCode = "400", description = "Invalid request - round cannot be closed"),
                        @ApiResponse(responseCode = "404", description = "Round not found")
        })
        public ResponseEntity<InnovationRoundResponse> closeRound(
                        @Parameter(description = "Round ID", required = true) @PathVariable String roundId) {
                InnovationRoundResponse closedRound = innovationRoundService.closeRound(roundId);
                return ResponseEntity.ok(closedRound);
        }

        // 8. Get Round by ID
        @GetMapping("/innovation-rounds/{roundId}")
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

        // 9. Delete Round (Only DRAFT status)
        @DeleteMapping("/innovation-rounds/{roundId}")
        @PreAuthorize("hasAnyRole('QUAN_TRI_VIEN_QLKH_HTQT', 'QUAN_TRI_VIEN_HE_THONG')")
        @ApiMessage("Xóa đợt sáng kiến thành công")
        @Operation(summary = "Delete Innovation Round", description = "Delete innovation round (only allowed for rounds with DRAFT status)")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Round deleted successfully"),
                        @ApiResponse(responseCode = "400", description = "Invalid request - round cannot be deleted (not in DRAFT status)"),
                        @ApiResponse(responseCode = "404", description = "Round not found")
        })
        public ResponseEntity<Void> deleteRound(
                        @Parameter(description = "Round ID", required = true) @PathVariable String roundId) {
                innovationRoundService.deleteRound(roundId);
                return ResponseEntity.ok().build();
        }

}
