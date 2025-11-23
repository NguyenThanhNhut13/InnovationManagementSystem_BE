package vn.edu.iuh.fit.innovationmanagementsystem_be.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.CreateCouncilRequest;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.CouncilResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.service.CouncilService;
import vn.edu.iuh.fit.innovationmanagementsystem_be.utils.annotation.ApiMessage;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Council", description = "Council management APIs")
@SecurityRequirement(name = "Bearer Authentication")
public class CouncilController {

    private final CouncilService councilService;

    public CouncilController(CouncilService councilService) {
        this.councilService = councilService;
    }

    // 1. Tạo Hội đồng mới
    @PostMapping("/councils")
    @PreAuthorize("hasAnyRole('TRUONG_KHOA','QUAN_TRI_VIEN_KHOA','QUAN_TRI_VIEN_HE_THONG', 'QUAN_TRI_VIEN_QLKH_HTQT')")
    @ApiMessage("Tạo Hội đồng thành công")
    @Operation(summary = "Create Council", description = "Create a new council with members and assigned innovations")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Council created successfully", content = @Content(schema = @Schema(implementation = CouncilResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<CouncilResponse> createCouncil(
            @Parameter(description = "Council creation request", required = true) @Valid @RequestBody CreateCouncilRequest request) {
        CouncilResponse response = councilService.createCouncil(request);
        return ResponseEntity.ok(response);
    }

}
