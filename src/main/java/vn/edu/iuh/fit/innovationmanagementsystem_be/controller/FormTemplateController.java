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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.turkraft.springfilter.boot.Filter;

import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.FormTemplate;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.CreateTemplateRequest;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.CreateTemplateWithFieldsRequest;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.UpdateFormTemplateRequest;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.CreateTemplateResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.CreateTemplateWithFieldsResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.FormTemplateResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.SecretarySummaryTemplateResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.TemplateTypeEnum;
import vn.edu.iuh.fit.innovationmanagementsystem_be.service.FormTemplateService;
import vn.edu.iuh.fit.innovationmanagementsystem_be.utils.ResultPaginationDTO;
import vn.edu.iuh.fit.innovationmanagementsystem_be.utils.annotation.ApiMessage;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Form Template", description = "Form template management APIs")
@SecurityRequirement(name = "Bearer Authentication")
public class FormTemplateController {

        private final FormTemplateService formTemplateService;

        public FormTemplateController(FormTemplateService formTemplateService) {
                this.formTemplateService = formTemplateService;
        }

        // 1. Lấy FormTemplate by id - OK
        @GetMapping("/form-templates/{id}")
        @ApiMessage("Lấy form template theo id thành công")
        @Operation(summary = "Get Form Template by ID", description = "Get form template details by ID")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Form template retrieved successfully", content = @Content(schema = @Schema(implementation = CreateTemplateWithFieldsResponse.class))),
                        @ApiResponse(responseCode = "404", description = "Form template not found")

        })
        public ResponseEntity<CreateTemplateWithFieldsResponse> getFormTemplateById(
                        @Parameter(description = "Form template ID", required = true) @PathVariable String id) {
                return ResponseEntity.ok(formTemplateService.getFormTemplateById(id));
        }

        // 2. Lấy FormTemplate by current InnovationRound - OK
        @GetMapping("/form-templates/innovation-round/current")
        @ApiMessage("Lấy form templates theo innovation round hiện tại thành công")
        @Operation(summary = "Get Form Templates by Current Round", description = "Get form templates for the current active innovation round")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Form templates retrieved successfully", content = @Content(schema = @Schema(implementation = List.class))),
                        @ApiResponse(responseCode = "404", description = "No current round found")
        })
        public ResponseEntity<List<FormTemplateResponse>> getFormTemplatesByCurrentRound() {
                return ResponseEntity.ok(formTemplateService.getFormTemplatesByCurrentRound());
        }

        // 3. Cập nhật FormTemplate by id - OK
        @PutMapping("/form-templates/{id}")
        @PreAuthorize("hasAnyRole( 'QUAN_TRI_VIEN_QLKH_HTQT', 'QUAN_TRI_VIEN_HE_THONG')")
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

        // 4. Tạo FormTemplate với FormFields - OK
        @PostMapping("/form-templates/with-fields")
        @PreAuthorize("hasAnyRole( 'QUAN_TRI_VIEN_QLKH_HTQT', 'QUAN_TRI_VIEN_HE_THONG')")
        @ApiMessage("Tạo form template với fields thành công")
        @Operation(summary = "Create Form Template with Fields", description = "Create a new form template with fields and table configuration")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Form template with fields created successfully", content = @Content(schema = @Schema(implementation = CreateTemplateWithFieldsResponse.class))),
                        @ApiResponse(responseCode = "400", description = "Invalid request data")
        })
        public ResponseEntity<CreateTemplateWithFieldsResponse> createTemplateWithFields(
                        @Parameter(description = "Form template with fields creation request", required = true) @Valid @RequestBody CreateTemplateWithFieldsRequest request) {
                return ResponseEntity.ok(formTemplateService.createTemplateWithFields(request));
        }

        // 5. Lấy danh sách FormTemplate với pagination và filtering - OK
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

        // 6. Xóa FormTemplate by id (InnovationRound ở trạng thái DRAFT) - OK
        @DeleteMapping("/form-templates/{id}")
        @PreAuthorize("hasAnyRole( 'QUAN_TRI_VIEN_QLKH_HTQT', 'QUAN_TRI_VIEN_HE_THONG')")
        @ApiMessage("Xóa form template thành công")
        @Operation(summary = "Delete Form Template", description = "Delete a form template by ID (only when round is DRAFT)")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Form template deleted successfully"),
                        @ApiResponse(responseCode = "400", description = "Invalid request"),
                        @ApiResponse(responseCode = "404", description = "Form template not found")
        })
        public ResponseEntity<Void> deleteFormTemplate(
                        @Parameter(description = "Form template ID", required = true) @PathVariable String id) {
                formTemplateService.deleteFormTemplate(id);
                return ResponseEntity.ok().build();
        }

        // 7. Tạo FormTemplate (không gắn InnovationRoundId ) - OK
        @PostMapping("/form-templates")
        @PreAuthorize("hasAnyRole( 'QUAN_TRI_VIEN_QLKH_HTQT', 'QUAN_TRI_VIEN_HE_THONG')")
        @ApiMessage("Tạo form template không gắn với round cụ thể thành công")
        @Operation(summary = "Create Form Template no roundId", description = "Create a new form template with optional round association")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Form template created successfully", content = @Content(schema = @Schema(implementation = CreateTemplateResponse.class))),
                        @ApiResponse(responseCode = "400", description = "Invalid request data")
        })
        public ResponseEntity<CreateTemplateResponse> createTemplate(
                        @Parameter(description = "Form template creation request", required = true) @Valid @RequestBody CreateTemplateRequest request) {
                return ResponseEntity.ok(formTemplateService.createTemplate(request));
        }

        // 8. Lấy FormTemplate (InnovationRoundId = null) với pagination, filtering - OK
        @GetMapping("/form-templates/library")
        @ApiMessage("Lấy thư viện form templates với phân trang và tìm kiếm thành công")
        @Operation(summary = "Get Template Library", description = "Get all form templates that are not associated with any specific round (template library) with pagination and filtering")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Template library retrieved successfully", content = @Content(schema = @Schema(implementation = ResultPaginationDTO.class)))
        })
        public ResponseEntity<ResultPaginationDTO> getTemplateLibraryWithPaginationAndSearch(
                        @Parameter(description = "Filter specification for template library") @Filter Specification<FormTemplate> specification,
                        @Parameter(description = "Pagination parameters") Pageable pageable) {
                return ResponseEntity.ok(
                                formTemplateService.getTemplateLibraryWithPaginationAndSearch(specification, pageable));
        }

        // 9. Lấy form templates theo innovation round ID - OK
        @GetMapping("/form-templates/innovation-round/{roundId}")
        @ApiMessage("Lấy form templates theo innovation round thành công")
        @Operation(summary = "Get Form Templates by Innovation Round", description = "Get form templates filtered by innovation round ID")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Form templates retrieved successfully", content = @Content(schema = @Schema(implementation = List.class))),
                        @ApiResponse(responseCode = "404", description = "Innovation round not found")
        })
        public ResponseEntity<List<FormTemplateResponse>> getFormTemplatesByInnovationRound(
                        @Parameter(description = "Innovation round ID", required = true) @PathVariable String roundId) {
                return ResponseEntity.ok(formTemplateService.getFormTemplatesByInnovationRound(roundId));
        }

        // 10. Lấy form templates theo roles của user hiện tại - OK
        @GetMapping("/form-templates/current-user")
        @ApiMessage("Lấy form templates theo roles của user hiện tại thành công")
        @Operation(summary = "Get Form Templates by Current User Roles", description = "Get form templates for the current active innovation round based on current user's roles")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Form templates retrieved successfully", content = @Content(schema = @Schema(implementation = List.class))),
                        @ApiResponse(responseCode = "404", description = "No current round found"),
                        @ApiResponse(responseCode = "401", description = "Unauthorized")
        })
        public ResponseEntity<List<FormTemplateResponse>> getFormTemplatesByCurrentUserRoles() {
                return ResponseEntity.ok(formTemplateService.getFormTemplatesByCurrentUserRoles());
        }

        // 11. Lấy template tổng hợp cho thư ký (Mẫu 3, 4, 5)
        @GetMapping("/form-templates/council/{councilId}/secretary/{templateType}")
        @PreAuthorize("hasAnyRole('TV_HOI_DONG_KHOA', 'TV_HOI_DONG_TRUONG', 'TRUONG_KHOA')")
        @ApiMessage("Lấy mẫu tổng hợp cho thư ký thành công")
        @Operation(summary = "Get Secretary Summary Template", 
                   description = "Get template 3, 4, or 5 with data for secretary or department head. templateType: BIEN_BAN_HOP, TONG_HOP_DE_NGHI, TONG_HOP_CHAM_DIEM")
        @ApiResponses(value = {
                @ApiResponse(responseCode = "200", description = "Template retrieved successfully"),
                @ApiResponse(responseCode = "404", description = "Template or council not found"),
                @ApiResponse(responseCode = "403", description = "User is not secretary of this council"),
                @ApiResponse(responseCode = "400", description = "Invalid template type or scoring period has not ended")
        })
        public ResponseEntity<SecretarySummaryTemplateResponse> getSecretarySummaryTemplate(
                @Parameter(description = "Council ID", required = true) @PathVariable String councilId,
                @Parameter(description = "Template Type", required = true) 
                @PathVariable TemplateTypeEnum templateType) {
                
                return ResponseEntity.ok(formTemplateService.getSecretarySummaryTemplate(councilId, templateType));
        }

}
