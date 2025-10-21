package vn.edu.iuh.fit.innovationmanagementsystem_be.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.FormDataRequest;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.UpdateFormDataRequest;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.FormDataResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.service.FormDataService;
import vn.edu.iuh.fit.innovationmanagementsystem_be.utils.annotation.ApiMessage;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Form Data", description = "Form data management APIs")
@SecurityRequirement(name = "Bearer Authentication")
public class FormDataController {

        private final FormDataService formDataService;

        public FormDataController(FormDataService formDataService) {
                this.formDataService = formDataService;
        }

        // 1. Tạo Form Data
        @PostMapping("/form-data")
        @ApiMessage("Tạo Form Data thành công")
        @Operation(summary = "Create Form Data", description = "Create a new form data")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Form Data created successfully", content = @Content(schema = @Schema(implementation = FormDataResponse.class))),
                        @ApiResponse(responseCode = "400", description = "Invalid request data")
        })
        public ResponseEntity<FormDataResponse> createFormData(
                        @Parameter(description = "Form data creation request", required = true) @Valid @RequestBody FormDataRequest request) {
                FormDataResponse response = formDataService.createFormData(request);
                return ResponseEntity.ok(response);
        }

        // 2. Tạo nhiều Form Data
        @PostMapping("/form-data/bulk")
        @ApiMessage("Tạo nhiều Form Data thành công")
        @Operation(summary = "Create Multiple Form Data", description = "Create multiple form data")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Multiple Form Data created successfully", content = @Content(schema = @Schema(implementation = List.class))),
                        @ApiResponse(responseCode = "400", description = "Invalid request data")
        })
        public ResponseEntity<List<FormDataResponse>> createMultipleFormData(
                        @Parameter(description = "List of form data creation requests", required = true) @Valid @RequestBody List<FormDataRequest> requests) {
                List<FormDataResponse> responses = formDataService.createMultipleFormData(requests);
                return ResponseEntity.ok(responses);
        }

        // 3. Cập nhật Form Data
        @PutMapping("/form-data/{id}")
        @ApiMessage("Cập nhật Form Data thành công")
        @Operation(summary = "Update Form Data", description = "Update form data")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Form Data updated successfully", content = @Content(schema = @Schema(implementation = FormDataResponse.class))),
                        @ApiResponse(responseCode = "400", description = "Invalid request data")
        })
        public ResponseEntity<FormDataResponse> updateFormData(
                        @Parameter(description = "Form data ID", required = true) @PathVariable String id,
                        @Parameter(description = "Form data update request", required = true) @Valid @RequestBody UpdateFormDataRequest request) {
                FormDataResponse response = formDataService.updateFormData(id, request);
                return ResponseEntity.ok(response);
        }

        // 4. Cập nhật nhiều Form Data
        @PutMapping("/form-data/bulk")
        @ApiMessage("Cập nhật nhiều Form Data thành công")
        @Operation(summary = "Update Multiple Form Data", description = "Update multiple form data")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Multiple Form Data updated successfully", content = @Content(schema = @Schema(implementation = List.class))),
                        @ApiResponse(responseCode = "400", description = "Invalid request data")
        })
        public ResponseEntity<List<FormDataResponse>> updateMultipleFormData(
                        @Parameter(description = "List of form data update requests", required = true) @Valid @RequestBody List<FormDataRequest> requests) {
                List<FormDataResponse> responses = formDataService.updateMultipleFormData(requests);
                return ResponseEntity.ok(responses);
        }

        // 5. Lấy Form Data By Id
        @GetMapping("/form-data/{id}")
        @ApiMessage("Lấy Form Data thành công")
        @Operation(summary = "Get Form Data by Id", description = "Get form data by id")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Form Data retrieved successfully", content = @Content(schema = @Schema(implementation = FormDataResponse.class))),
                        @ApiResponse(responseCode = "400", description = "Invalid request data")
        })
        public ResponseEntity<FormDataResponse> getFormDataById(
                        @Parameter(description = "Form data ID", required = true) @PathVariable String id) {
                FormDataResponse response = formDataService.getFormDataById(id);
                return ResponseEntity.ok(response);
        }

        // 6. Lấy Form Data By Innovation Id
        @GetMapping("/form-data/innovation/{innovationId}")
        @ApiMessage("Lấy danh sách Form Data theo Innovation thành công")
        @Operation(summary = "Get Form Data by Innovation Id", description = "Get form data by innovation id")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Form Data retrieved successfully", content = @Content(schema = @Schema(implementation = List.class))),
                        @ApiResponse(responseCode = "400", description = "Invalid request data")
        })
        public ResponseEntity<List<FormDataResponse>> getFormDataByInnovationId(
                        @Parameter(description = "Innovation ID", required = true) @PathVariable String innovationId) {
                List<FormDataResponse> responses = formDataService.getFormDataByInnovationId(innovationId);
                return ResponseEntity.ok(responses);
        }

        // 7. Lấy Form Data By Template Id
        @GetMapping("/form-data/template/{templateId}")
        @ApiMessage("Lấy danh sách Form Data theo Template thành công")
        @Operation(summary = "Get Form Data by Template Id", description = "Get form data by template id")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Form Data retrieved successfully", content = @Content(schema = @Schema(implementation = List.class))),
                        @ApiResponse(responseCode = "400", description = "Invalid request data")
        })
        public ResponseEntity<List<FormDataResponse>> getFormDataByTemplateId(
                        @Parameter(description = "Template ID", required = true) @PathVariable String templateId) {
                List<FormDataResponse> responses = formDataService.getFormDataByTemplateId(templateId);
                return ResponseEntity.ok(responses);
        }

        // 8. Lấy Form Data With Form Fields (Innovation + Template)
        @GetMapping("/form-data/innovation/{innovationId}/template/{templateId}")
        @ApiMessage("Lấy Form Data với Form Fields thành công")
        @Operation(summary = "Get Form Data With Form Fields", description = "Get form data with form fields")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Form Data retrieved successfully", content = @Content(schema = @Schema(implementation = List.class))),
                        @ApiResponse(responseCode = "400", description = "Invalid request data")
        })
        public ResponseEntity<List<FormDataResponse>> getFormDataWithFormFields(
                        @Parameter(description = "Innovation ID", required = true) @PathVariable String innovationId,
                        @Parameter(description = "Template ID", required = true) @PathVariable String templateId) {
                List<FormDataResponse> responses = formDataService.getFormDataWithFormFields(innovationId, templateId);
                return ResponseEntity.ok(responses);
        }
}