package vn.edu.iuh.fit.innovationmanagementsystem_be.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
// import org.springframework.data.domain.Pageable;
// import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
// import com.turkraft.springfilter.boot.Filter;
// import jakarta.validation.Valid;
// import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.InnovationDecision;
// import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.InnovationDecisionRequest;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.InnovationDecisionResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.service.InnovationDecisionService;
// import vn.edu.iuh.fit.innovationmanagementsystem_be.utils.ResultPaginationDTO;
import vn.edu.iuh.fit.innovationmanagementsystem_be.utils.annotation.ApiMessage;

// import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Innovation Decision", description = "Innovation decision management APIs")
@SecurityRequirement(name = "Bearer Authentication")
public class InnovationDecisionController {

        private final InnovationDecisionService innovationDecisionService;

        public InnovationDecisionController(InnovationDecisionService innovationDecisionService) {
                this.innovationDecisionService = innovationDecisionService;
        }

        // // 1. Tạo quyết định
        // @PostMapping("/innovation-decisions")
        // @ApiMessage("Tạo quyết định thành công")
        // @Operation(summary = "Create Innovation Decision", description = "Create a
        // new innovation decision")
        // @ApiResponses(value = {
        // @ApiResponse(responseCode = "200", description = "Innovation decision created
        // successfully", content = @Content(schema = @Schema(implementation =
        // InnovationDecisionResponse.class))),
        // @ApiResponse(responseCode = "400", description = "Invalid request data")
        // })
        // public ResponseEntity<InnovationDecisionResponse> createInnovationDecision(
        // @Parameter(description = "Innovation decision creation request", required =
        // true) @Valid @RequestBody InnovationDecisionRequest request) {
        // InnovationDecisionResponse response =
        // innovationDecisionService.createInnovationDecision(request);
        // return ResponseEntity.ok(response);
        // }

        // // 2. Lấy danh sách quyết định
        // @GetMapping("/innovation-decisions")
        // @ApiMessage("Lấy danh sách quyết định thành công")
        // @Operation(summary = "Get All Innovation Decisions", description = "Get all
        // innovation decisions with pagination and filtering")
        // @ApiResponses(value = {
        // @ApiResponse(responseCode = "200", description = "Innovation decisions
        // retrieved successfully", content = @Content(schema = @Schema(implementation =
        // ResultPaginationDTO.class)))
        // })
        // public ResponseEntity<ResultPaginationDTO> getAllInnovationDecisions(
        // @Parameter(description = "Filter specification for innovation decisions")
        // @Filter Specification<InnovationDecision> specification,
        // @Parameter(description = "Pagination parameters") Pageable pageable) {
        // return
        // ResponseEntity.ok(innovationDecisionService.getAllInnovationDecisions(specification,
        // pageable));
        // }

        // 3. Lấy InnovationDecision by Id
        @GetMapping("/innovation-decisions/{id}")
        @ApiMessage("Lấy quyết định thành công")
        @Operation(summary = "Get Innovation Decision by ID", description = "Get innovation decision details by ID")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Innovation decision retrieved successfully", content = @Content(schema = @Schema(implementation = InnovationDecisionResponse.class))),
                        @ApiResponse(responseCode = "404", description = "Innovation decision not found")
        })
        public ResponseEntity<InnovationDecisionResponse> getInnovationDecisionById(
                        @Parameter(description = "Innovation decision ID", required = true) @PathVariable String id) {
                return ResponseEntity.ok(innovationDecisionService.getInnovationDecisionById(id));
        }

        // // 4. Cập nhật quyết định
        // @PutMapping("/innovation-decisions/{id}")
        // @ApiMessage("Cập nhật quyết định thành công")
        // @Operation(summary = "Update Innovation Decision", description = "Update
        // innovation decision by ID")
        // @ApiResponses(value = {
        // @ApiResponse(responseCode = "200", description = "Innovation decision updated
        // successfully", content = @Content(schema = @Schema(implementation =
        // InnovationDecisionResponse.class))),
        // @ApiResponse(responseCode = "400", description = "Invalid request data"),
        // @ApiResponse(responseCode = "404", description = "Innovation decision not
        // found")
        // })
        // public ResponseEntity<InnovationDecisionResponse> updateInnovationDecision(
        // @Parameter(description = "Innovation decision ID", required = true)
        // @PathVariable String id,
        // @Parameter(description = "Innovation decision update request", required =
        // true) @Valid @RequestBody InnovationDecisionRequest request) {
        // return
        // ResponseEntity.ok(innovationDecisionService.updateInnovationDecision(id,
        // request));
        // }

        // // 5. Lấy quyết định theo khoảng thời gian
        // @GetMapping("/innovation-decisions/date-range")
        // @ApiMessage("Lấy quyết định theo khoảng thời gian thành công")
        // @Operation(summary = "Get Innovation Decisions by Date Range", description =
        // "Get innovation decisions filtered by date range")
        // @ApiResponses(value = {
        // @ApiResponse(responseCode = "200", description = "Innovation decisions
        // retrieved successfully", content = @Content(schema = @Schema(implementation =
        // ResultPaginationDTO.class)))
        // })
        // public ResponseEntity<ResultPaginationDTO> getInnovationDecisionsByDateRange(
        // @Parameter(description = "Start date", required = true) @RequestParam
        // LocalDate startDate,
        // @Parameter(description = "End date", required = true) @RequestParam LocalDate
        // endDate,
        // @Parameter(description = "Pagination parameters") Pageable pageable) {
        // return ResponseEntity
        // .ok(innovationDecisionService.getInnovationDecisionsByDateRange(startDate,
        // endDate,
        // pageable));
        // }
}
