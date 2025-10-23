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
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.InnovationDecision;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.InnovationDecisionResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.service.InnovationDecisionService;
import vn.edu.iuh.fit.innovationmanagementsystem_be.utils.ResultPaginationDTO;
import vn.edu.iuh.fit.innovationmanagementsystem_be.utils.annotation.ApiMessage;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Innovation Decision", description = "Innovation decision management APIs")
@SecurityRequirement(name = "Bearer Authentication")
public class InnovationDecisionController {

        private final InnovationDecisionService innovationDecisionService;

        public InnovationDecisionController(InnovationDecisionService innovationDecisionService) {
                this.innovationDecisionService = innovationDecisionService;
        }

        // 1. Lấy danh sách quyết định với pagination và filtering - OK
        @GetMapping("/innovation-decisions")
        @ApiMessage("Lấy danh sách quyết định thành công")
        @Operation(summary = "Get All Innovation Decisions", description = "Get all innovation decisions with pagination and filtering")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Innovation decisions retrieved successfully", content = @Content(schema = @Schema(implementation = ResultPaginationDTO.class)))
        })
        public ResponseEntity<ResultPaginationDTO> getAllInnovationDecisions(
                        @Parameter(description = "Filter specification for innovation decisions") @Filter Specification<InnovationDecision> specification,
                        @Parameter(description = "Pagination parameters") Pageable pageable) {
                return ResponseEntity.ok(innovationDecisionService.getAllInnovationDecisions(specification,
                                pageable));
        }

        // 2. Lấy InnovationDecision by Id - OK
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

}
