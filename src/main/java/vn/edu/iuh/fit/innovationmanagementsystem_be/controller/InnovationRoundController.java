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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.turkraft.springfilter.boot.Filter;

import jakarta.validation.Valid;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.InnovationRound;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.InnovationRoundStatusEnum;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.InnovationRoundRequest;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.UpdateInnovationRoundRequest;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.InnovationRoundResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.service.InnovationRoundService;
import vn.edu.iuh.fit.innovationmanagementsystem_be.utils.ResultPaginationDTO;
import vn.edu.iuh.fit.innovationmanagementsystem_be.utils.annotation.ApiMessage;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Innovation Round", description = "Innovation round management APIs")
@SecurityRequirement(name = "Bearer Authentication")
public class InnovationRoundController {

    private final InnovationRoundService innovationRoundService;

    public InnovationRoundController(InnovationRoundService innovationRoundService) {
        this.innovationRoundService = innovationRoundService;
    }

    // 1. Create Innovation Round
    @PostMapping("/innovation-rounds")
    @ApiMessage("Tạo đợt sáng kiến thành công")
    @Operation(summary = "Create Innovation Round", description = "Create a new innovation round")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Innovation round created successfully", content = @Content(schema = @Schema(implementation = InnovationRoundResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request data")
    })
    public ResponseEntity<InnovationRoundResponse> createInnovationRound(
            @Parameter(description = "Innovation round creation request", required = true) @Valid @RequestBody InnovationRoundRequest request) {
        InnovationRoundResponse response = innovationRoundService.createInnovationRound(request);
        return ResponseEntity.ok(response);
    }

    // 2. Get All InnovationRounds with pagination and filter
    @GetMapping("/innovation-rounds")
    @ApiMessage("Lấy danh sách đợt sáng kiến thành công")
    @Operation(summary = "Get All Innovation Rounds", description = "Get all innovation rounds with pagination and filtering")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Innovation rounds retrieved successfully", content = @Content(schema = @Schema(implementation = ResultPaginationDTO.class)))
    })
    public ResponseEntity<ResultPaginationDTO> getAllInnovationRounds(
            @Parameter(description = "Filter specification for innovation rounds") @Filter Specification<InnovationRound> specification,
            @Parameter(description = "Pagination parameters") Pageable pageable) {
        ResultPaginationDTO response = innovationRoundService.getAllInnovationRounds(specification, pageable);
        return ResponseEntity.ok(response);
    }

    // 3. Get InnovationRound by Id
    @GetMapping("/innovation-rounds/{id}")
    @ApiMessage("Lấy đợt sáng kiến theo ID thành công")
    @Operation(summary = "Get Innovation Round by ID", description = "Get innovation round details by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Innovation round retrieved successfully", content = @Content(schema = @Schema(implementation = InnovationRoundResponse.class))),
            @ApiResponse(responseCode = "404", description = "Innovation round not found")
    })
    public ResponseEntity<InnovationRoundResponse> getInnovationRoundById(
            @Parameter(description = "Innovation round ID", required = true) @PathVariable String id) {
        InnovationRoundResponse response = innovationRoundService.getInnovationRoundById(id);
        return ResponseEntity.ok(response);
    }

    // 4. Update InnovationRound
    @PutMapping("/innovation-rounds/{id}")
    @ApiMessage("Cập nhật đợt sáng kiến thành công")
    @Operation(summary = "Update Innovation Round", description = "Update innovation round by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Innovation round updated successfully", content = @Content(schema = @Schema(implementation = InnovationRoundResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "404", description = "Innovation round not found")
    })
    public ResponseEntity<InnovationRoundResponse> updateInnovationRound(
            @Parameter(description = "Innovation round ID", required = true) @PathVariable String id,
            @Parameter(description = "Innovation round update request", required = true) @Valid @RequestBody UpdateInnovationRoundRequest request) {
        InnovationRoundResponse response = innovationRoundService.updateInnovationRound(id, request);
        return ResponseEntity.ok(response);
    }

    // 5. Change Status InnovationRound
    @PostMapping("/innovation-rounds/{id}/status")
    @ApiMessage("Thay đổi trạng thái đợt sáng kiến thành công")
    @Operation(summary = "Change Innovation Round Status", description = "Change the status of an innovation round")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Innovation round status changed successfully", content = @Content(schema = @Schema(implementation = InnovationRoundResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "404", description = "Innovation round not found")
    })
    public ResponseEntity<InnovationRoundResponse> changeStatusInnovationRound(
            @Parameter(description = "Innovation round ID", required = true) @PathVariable String id,
            @Parameter(description = "New status for innovation round", required = true) @Valid @RequestBody InnovationRoundStatusEnum newStatus) {
        InnovationRoundResponse response = innovationRoundService.changeStatusInnovationRound(id, newStatus);
        return ResponseEntity.ok(response);
    }

    // 6. Get InnovationRound by Status
    @GetMapping("/innovation-rounds/status/{status}")
    @ApiMessage("Lấy danh sách vòng đổi mới theo trạng thái thành công")
    @Operation(summary = "Get Innovation Rounds by Status", description = "Get innovation rounds filtered by status")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Innovation rounds retrieved successfully", content = @Content(schema = @Schema(implementation = ResultPaginationDTO.class)))
    })
    public ResponseEntity<ResultPaginationDTO> getInnovationRoundsByStatus(
            @Parameter(description = "Innovation round status", required = true) @PathVariable InnovationRoundStatusEnum status,
            @Parameter(description = "Pagination parameters") Pageable pageable) {
        return ResponseEntity.ok(innovationRoundService.getInnovationRoundByStatus(status, pageable));
    }
}
