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
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.turkraft.springfilter.boot.Filter;

import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.FormTemplate;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.CreateFormTemplateRequest;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.CreateMultipleFormTemplatesRequest;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.UpdateFormTemplateRequest;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.CreateMultipleFormTemplatesResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.FormTemplateResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.service.FormTemplateService;
import vn.edu.iuh.fit.innovationmanagementsystem_be.utils.ResultPaginationDTO;
import vn.edu.iuh.fit.innovationmanagementsystem_be.utils.annotation.ApiMessage;

import java.util.List;

@RestController
@RequestMapping("/api/v1/form-templates")
@Tag(name = "Form Template", description = "Form template management APIs")
@SecurityRequirement(name = "Bearer Authentication")
public class FormTemplateController {

        private final FormTemplateService formTemplateService;

        public FormTemplateController(FormTemplateService formTemplateService) {
                this.formTemplateService = formTemplateService;
        }

        // 1. Get form template by id
        @GetMapping("/{id}")
        @ApiMessage("Lấy form template theo id thành công")
        @Operation(summary = "Get Form Template by ID", description = "Get form template details by ID")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Form template retrieved successfully", content = @Content(schema = @Schema(implementation = FormTemplateResponse.class))),
                        @ApiResponse(responseCode = "404", description = "Form template not found")
        })
        public ResponseEntity<FormTemplateResponse> getFormTemplateById(
                        @Parameter(description = "Form template ID", required = true) @PathVariable String id) {
                return ResponseEntity.ok(formTemplateService.getFormTemplateById(id));
        }

        // 2. Get form templates by innovation phase
        @GetMapping("/innovation-phase/{phaseId}")
        @ApiMessage("Lấy form templates theo innovation phase thành công")
        @Operation(summary = "Get Form Templates by Innovation Phase", description = "Get form templates filtered by innovation phase ID")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Form templates retrieved successfully", content = @Content(schema = @Schema(implementation = List.class)))
        })
        public ResponseEntity<List<FormTemplateResponse>> getFormTemplatesByInnovationPhase(
                        @Parameter(description = "Innovation phase ID", required = true) @PathVariable String phaseId) {
                return ResponseEntity.ok(formTemplateService.getFormTemplatesByInnovationPhase(phaseId));
        }

        // 3. Create form template
        @PostMapping
        @ApiMessage("Tạo form template thành công")
        @Operation(summary = "Create Form Template", description = "Create a new form template")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Form template created successfully", content = @Content(schema = @Schema(implementation = FormTemplateResponse.class))),
                        @ApiResponse(responseCode = "400", description = "Invalid request data")
        })
        public ResponseEntity<FormTemplateResponse> createFormTemplate(
                        @Parameter(description = "Form template creation request", required = true) @Valid @RequestBody CreateFormTemplateRequest request) {
                return ResponseEntity.ok(formTemplateService.createFormTemplate(request));
        }

        // 4. Create Multiple Form Templates
        @PostMapping("/bulk")
        @ApiMessage("Tạo nhiều form templates thành công")
        @Operation(summary = "Create Multiple Form Templates", description = "Create multiple form templates")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Multiple form templates created successfully", content = @Content(schema = @Schema(implementation = CreateMultipleFormTemplatesResponse.class))),
                        @ApiResponse(responseCode = "400", description = "Invalid request data")
        })
        public ResponseEntity<CreateMultipleFormTemplatesResponse> createMultipleFormTemplates(
                        @Parameter(description = "Multiple form templates creation request", required = true) @Valid @RequestBody CreateMultipleFormTemplatesRequest request) {
                return ResponseEntity.ok(formTemplateService.createMultipleFormTemplates(request));
        }

        // 5. Update form template
        @PutMapping("/{id}")
        @ApiMessage("Cập nhật form template thành công")
        @Operation(summary = "Update Form Template", description = "Update form template by ID")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Form template updated successfully", content = @Content(schema = @Schema(implementation = FormTemplateResponse.class))),
                        @ApiResponse(responseCode = "400", description = "Invalid request data"),
                        @ApiResponse(responseCode = "404", description = "Form template not found")
        })
        public ResponseEntity<FormTemplateResponse> updateFormTemplate(
                        @Parameter(description = "Form template ID", required = true) @PathVariable String id,
                        @Parameter(description = "Form template update request", required = true) @Valid @RequestBody UpdateFormTemplateRequest request) {
                return ResponseEntity.ok(formTemplateService.updateFormTemplate(id, request));
        }

        // 6. Get all form templates with pagination and search
        @GetMapping
        @ApiMessage("Lấy danh sách form templates với phân trang và tìm kiếm")
        @Operation(summary = "Get All Form Templates", description = "Get all form templates with pagination and filtering")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Form templates retrieved successfully", content = @Content(schema = @Schema(implementation = ResultPaginationDTO.class)))
        })
        public ResponseEntity<ResultPaginationDTO> getAllFormTemplatesWithPaginationAndSearch(
                        @Parameter(description = "Filter specification for form templates") @Filter Specification<FormTemplate> specification,
                        @Parameter(description = "Pagination parameters") Pageable pageable) {
                return ResponseEntity
                                .ok(formTemplateService.getAllFormTemplatesWithPaginationAndSearch(specification,
                                                pageable));
        }
}
