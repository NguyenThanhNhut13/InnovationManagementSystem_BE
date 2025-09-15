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
import com.turkraft.springfilter.boot.Filter;
import jakarta.validation.Valid;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.Regulation;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.ImportMultipleRegulationsRequest;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.ImportRegulationsToMultipleChaptersRequest;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.RegulationRequest;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.ImportMultipleRegulationsResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.ImportRegulationsToMultipleChaptersResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.RegulationResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.service.RegulationService;
import vn.edu.iuh.fit.innovationmanagementsystem_be.utils.ResultPaginationDTO;
import vn.edu.iuh.fit.innovationmanagementsystem_be.utils.annotation.ApiMessage;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Regulation", description = "Regulation management APIs")
@SecurityRequirement(name = "Bearer Authentication")
public class RegulationController {

    private final RegulationService regulationService;

    public RegulationController(RegulationService regulationService) {
        this.regulationService = regulationService;
    }

    // 1. Create Regulation
    @PostMapping("/regulations")
    @ApiMessage("Tạo điều thành công")
    @Operation(summary = "Create Regulation", description = "Create a new regulation")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Regulation created successfully", content = @Content(schema = @Schema(implementation = RegulationResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request data")
    })
    public ResponseEntity<RegulationResponse> createRegulation(
            @Parameter(description = "Regulation creation request", required = true) @Valid @RequestBody RegulationRequest request) {
        RegulationResponse response = regulationService.createRegulation(request);
        return ResponseEntity.ok(response);
    }

    // 2. Get All Regulations
    @GetMapping("/regulations")
    @ApiMessage("Lấy danh sách điều thành công")
    @Operation(summary = "Get All Regulations", description = "Get all regulations with pagination and filtering")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Regulations retrieved successfully", content = @Content(schema = @Schema(implementation = ResultPaginationDTO.class)))
    })
    public ResponseEntity<ResultPaginationDTO> getAllRegulations(
            @Parameter(description = "Filter specification for regulations") @Filter Specification<Regulation> specification,
            @Parameter(description = "Pagination parameters") Pageable pageable) {
        return ResponseEntity.ok(regulationService.getAllRegulations(specification, pageable));
    }

    // 3. Get Regulation by Id
    @GetMapping("/regulations/{id}")
    @ApiMessage("Lấy điều thành công")
    @Operation(summary = "Get Regulation by ID", description = "Get regulation details by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Regulation retrieved successfully", content = @Content(schema = @Schema(implementation = RegulationResponse.class))),
            @ApiResponse(responseCode = "404", description = "Regulation not found")
    })
    public ResponseEntity<RegulationResponse> getRegulationById(
            @Parameter(description = "Regulation ID", required = true) @PathVariable String id) {
        return ResponseEntity.ok(regulationService.getRegulationById(id));
    }

    // 4. Update Regulation
    @PutMapping("/regulations/{id}")
    @ApiMessage("Cập nhật điều thành công")
    @Operation(summary = "Update Regulation", description = "Update regulation by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Regulation updated successfully", content = @Content(schema = @Schema(implementation = RegulationResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "404", description = "Regulation not found")
    })
    public ResponseEntity<RegulationResponse> updateRegulation(
            @Parameter(description = "Regulation ID", required = true) @PathVariable String id,
            @Parameter(description = "Regulation update request", required = true) @Valid @RequestBody RegulationRequest request) {
        return ResponseEntity.ok(regulationService.updateRegulation(id, request));
    }

    // 5. Get Regulations by InnovationDecision
    @GetMapping("/innovation-decisions/{innovationDecisionId}/regulations")
    @ApiMessage("Lấy danh sách điều theo quyết định thành công")
    @Operation(summary = "Get Regulations by Innovation Decision", description = "Get regulations filtered by innovation decision ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Regulations retrieved successfully", content = @Content(schema = @Schema(implementation = ResultPaginationDTO.class)))
    })
    public ResponseEntity<ResultPaginationDTO> getRegulationsByInnovationDecision(
            @Parameter(description = "Innovation decision ID", required = true) @PathVariable String innovationDecisionId,
            @Parameter(description = "Pagination parameters") Pageable pageable) {
        return ResponseEntity.ok(regulationService.getRegulationsByInnovationDecision(innovationDecisionId, pageable));
    }

    // 6. Get Regulations by Chapter
    @GetMapping("/chapters/{chapterId}/regulations")
    @ApiMessage("Lấy danh sách điều theo chương thành công")
    @Operation(summary = "Get Regulations by Chapter", description = "Get regulations filtered by chapter ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Regulations retrieved successfully", content = @Content(schema = @Schema(implementation = ResultPaginationDTO.class)))
    })
    public ResponseEntity<ResultPaginationDTO> getRegulationsByChapter(
            @Parameter(description = "Chapter ID", required = true) @PathVariable String chapterId,
            @Parameter(description = "Pagination parameters") Pageable pageable) {
        return ResponseEntity.ok(regulationService.getRegulationsByChapter(chapterId, pageable));
    }

    // 8. Import Multiple Regulations to Chapter
    @PostMapping("/chapters/{chapterId}/regulations/import")
    @ApiMessage("Import danh sách điều khoản vào chương thành công")
    @Operation(summary = "Import Multiple Regulations to Chapter", description = "Import multiple regulations to a specific chapter")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Regulations imported successfully", content = @Content(schema = @Schema(implementation = ImportMultipleRegulationsResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request data")
    })
    public ResponseEntity<ImportMultipleRegulationsResponse> importMultipleRegulationsToChapter(
            @Parameter(description = "Chapter ID", required = true) @PathVariable String chapterId,
            @Parameter(description = "Import multiple regulations request", required = true) @Valid @RequestBody ImportMultipleRegulationsRequest request) {
        request.setChapterId(chapterId);
        ImportMultipleRegulationsResponse response = regulationService.importMultipleRegulationsToChapter(request);
        return ResponseEntity.ok(response);
    }

    // 9. Import Regulations to Multiple Chapters
    @PostMapping("/innovation-decisions/chapters/regulations/import")
    @ApiMessage("Import danh sách điều khoản vào nhiều chương thành công")
    @Operation(summary = "Import Regulations to Multiple Chapters", description = "Import regulations to multiple chapters")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Regulations imported successfully", content = @Content(schema = @Schema(implementation = ImportRegulationsToMultipleChaptersResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request data")
    })
    public ResponseEntity<ImportRegulationsToMultipleChaptersResponse> importRegulationsToMultipleChapters(
            @Parameter(description = "Import regulations to multiple chapters request", required = true) @Valid @RequestBody ImportRegulationsToMultipleChaptersRequest request) {
        ImportRegulationsToMultipleChaptersResponse response = regulationService
                .importRegulationsToMultipleChapters(request);
        return ResponseEntity.ok(response);
    }

}
