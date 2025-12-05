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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.AiAnalysisResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.service.AiService;
import vn.edu.iuh.fit.innovationmanagementsystem_be.utils.annotation.ApiMessage;

@RestController
@RequestMapping("/api/v1/ai")
@Tag(name = "AI Services", description = "AI-powered innovation analysis APIs")
@SecurityRequirement(name = "Bearer Authentication")
@PreAuthorize("hasAnyRole('QUAN_TRI_VIEN_HE_THONG')")
public class AiController {

    private final AiService aiService;

    public AiController(AiService aiService) {
        this.aiService = aiService;
    }

    @PostMapping("/analyze/{innovationId}")
    @ApiMessage("Phân tích sáng kiến thành công")
    @Operation(summary = "Analyze Innovation with AI", description = "Use AI to summarize and analyze an innovation with scores for creativity, feasibility, impact, strengths, weaknesses, and suggestions")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Analysis completed successfully", content = @Content(schema = @Schema(implementation = AiAnalysisResponse.class))),
            @ApiResponse(responseCode = "404", description = "Innovation not found"),
            @ApiResponse(responseCode = "400", description = "Innovation has no content to analyze"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "429", description = "Rate limit exceeded - please try again later")
    })
    public ResponseEntity<AiAnalysisResponse> analyzeInnovation(
            @Parameter(description = "Innovation ID", required = true) @PathVariable String innovationId) {
        return ResponseEntity.ok(aiService.analyzeInnovation(innovationId));
    }
}
