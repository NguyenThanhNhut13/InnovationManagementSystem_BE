package vn.edu.iuh.fit.innovationmanagementsystem_be.controller;

import com.turkraft.springfilter.boot.Filter;
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

import jakarta.validation.Valid;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.Innovation;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.InnovationFormDataRequest;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.InnovationFormDataResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.InnovationResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.InnovationStatisticsDTO;
import vn.edu.iuh.fit.innovationmanagementsystem_be.service.InnovationService;
import vn.edu.iuh.fit.innovationmanagementsystem_be.utils.ResultPaginationDTO;
import vn.edu.iuh.fit.innovationmanagementsystem_be.utils.annotation.ApiMessage;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Innovation Management", description = "Innovation management APIs")
@SecurityRequirement(name = "Bearer Authentication")
public class InnovationController {

    private final InnovationService innovationService;

    public InnovationController(InnovationService innovationService) {
        this.innovationService = innovationService;
    }

    // 1. Lấy danh sách sáng kiến
    @GetMapping("/innovations")
    @ApiMessage("Lấy danh sách sáng kiến thành công")
    @Operation(summary = "Get All Innovations", description = "Get paginated list of all innovations with filtering")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Innovations retrieved successfully", content = @Content(schema = @Schema(implementation = ResultPaginationDTO.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<ResultPaginationDTO> getAllInnovations(
            @Parameter(description = "Filter specification for innovations") @Filter Specification<Innovation> specification,
            @Parameter(description = "Pagination parameters") Pageable pageable) {
        return ResponseEntity.ok(innovationService.getAllInnovations(specification, pageable));
    }

    // 2. Lấy sáng kiến by Id
    @GetMapping("/innovations/{id}")
    @ApiMessage("Lấy thông tin sáng kiến bằng id thành công")
    @Operation(summary = "Get Innovation by ID", description = "Get innovation details by innovation ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Innovation retrieved successfully", content = @Content(schema = @Schema(implementation = InnovationResponse.class))),
            @ApiResponse(responseCode = "404", description = "Innovation not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<InnovationResponse> getInnovationById(
            @Parameter(description = "Innovation ID", required = true) @PathVariable String id) {
        return ResponseEntity.ok(innovationService.getInnovationById(id));
    }

    // 3. Tạo sáng kiến & Submit Form Data (Tạo sáng kiến tự động khi điền form)
    @PostMapping("/innovations/form-data")
    @ApiMessage("Tạo sáng kiến và điền thông tin thành công")
    @Operation(summary = "Create Innovation with Form Data", description = "Create a new innovation and submit form data")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Innovation created and form data submitted successfully", content = @Content(schema = @Schema(implementation = InnovationFormDataResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<InnovationFormDataResponse> createInnovationAndSubmitFormData(
            @Parameter(description = "Innovation form data request", required = true) @Valid @RequestBody InnovationFormDataRequest request) {
        InnovationFormDataResponse response = innovationService.createInnovationAndSubmitFormData(request);
        return ResponseEntity.ok(response);
    }

    // 4. Cập nhật FormData sáng kiến
    @PutMapping("/innovations/{innovationId}/form-data")
    @ApiMessage("Cập nhật thông tin form thành công")
    public ResponseEntity<InnovationFormDataResponse> updateInnovationFormData(
            @PathVariable String innovationId,
            @Valid @RequestBody InnovationFormDataRequest request) {
        InnovationFormDataResponse response = innovationService.updateInnovationFormData(innovationId, request);
        return ResponseEntity.ok(response);
    }

    // 5. Lấy FormData sáng kiến
    @GetMapping("/innovations/{innovationId}/form-data")
    @ApiMessage("Lấy FormData của sáng kiến thành công")
    public ResponseEntity<InnovationFormDataResponse> getInnovationFormData(
            @PathVariable String innovationId,
            @RequestParam(required = false) String templateId) {
        InnovationFormDataResponse response = innovationService.getInnovationFormData(innovationId, templateId);
        return ResponseEntity.ok(response);
    }

    // 6. Lấy danh sách sáng kiến của tôi theo trạng thái
    @GetMapping("/innovations/my-innovations")
    @ApiMessage("Lấy danh sách sáng kiến của tôi theo trạng thái thành công")
    public ResponseEntity<ResultPaginationDTO> getMyInnovationsByStatus(
            @RequestParam String status,
            Pageable pageable) {
        return ResponseEntity.ok(innovationService.getInnovationsByUserAndStatus(status, pageable));
    }

    // 7. Lấy thống kê sáng kiến của giảng viên
    @GetMapping("/innovations/statistics")
    @ApiMessage("Lấy thống kê sáng kiến thành công")
    @Operation(summary = "Get Innovation Statistics", description = "Get innovation statistics for current user (GIANG_VIEN role)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Statistics retrieved successfully", content = @Content(schema = @Schema(implementation = InnovationStatisticsDTO.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - GIANG_VIEN role required")
    })
    public ResponseEntity<InnovationStatisticsDTO> getInnovationStatistics() {
        InnovationStatisticsDTO statistics = innovationService.getInnovationStatisticsForCurrentUser();
        return ResponseEntity.ok(statistics);
    }
}
