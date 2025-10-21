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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.FormFieldRequest;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.UpdateFormFieldRequest;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.FormFieldResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.service.FormFieldService;
import vn.edu.iuh.fit.innovationmanagementsystem_be.utils.annotation.ApiMessage;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Form Field", description = "Form field management APIs")
@SecurityRequirement(name = "Bearer Authentication")
public class FormFieldController {

        private final FormFieldService formFieldService;

        public FormFieldController(FormFieldService formFieldService) {
                this.formFieldService = formFieldService;
        }

        // 1. Tạo Form Field
        @PostMapping("/form-fields")
        @ApiMessage("Tạo Form Field thành công")
        @Operation(summary = "Create Form Field", description = "Create a new form field")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Form field created successfully", content = @Content(schema = @Schema(implementation = FormFieldResponse.class))),
                        @ApiResponse(responseCode = "400", description = "Invalid request data")
        })
        public ResponseEntity<FormFieldResponse> createFormField(
                        @Parameter(description = "Form field creation request", required = true) @Valid @RequestBody FormFieldRequest request,
                        @Parameter(description = "Template ID", required = true) @RequestParam String templateId) {
                FormFieldResponse response = formFieldService.createFormField(request, templateId);
                return ResponseEntity.ok(response);
        }

        // 2. Tạo nhiều Form Field
        @PostMapping("/form-fields/bulk")
        @ApiMessage("Tạo nhiều Form Field thành công")
        @Operation(summary = "Create Multiple Form Fields", description = "Create multiple form fields")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Multiple form fields created successfully", content = @Content(schema = @Schema(implementation = List.class))),
                        @ApiResponse(responseCode = "400", description = "Invalid request data")
        })
        public ResponseEntity<List<FormFieldResponse>> createMultipleFormFields(
                        @Parameter(description = "List of form field creation requests", required = true) @Valid @RequestBody List<FormFieldRequest> requests,
                        @Parameter(description = "Template ID", required = true) @RequestParam String templateId) {
                List<FormFieldResponse> responses = formFieldService.createMultipleFormFields(requests, templateId);
                return ResponseEntity.ok(responses);
        }

        // 3. Cập nhật Form Field
        @PutMapping("/form-fields/{id}")
        @ApiMessage("Cập nhật Form Field thành công")
        @Operation(summary = "Update Form Field", description = "Update form field by ID")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Form field updated successfully", content = @Content(schema = @Schema(implementation = FormFieldResponse.class))),
                        @ApiResponse(responseCode = "400", description = "Invalid request data"),
                        @ApiResponse(responseCode = "404", description = "Form field not found")
        })
        public ResponseEntity<FormFieldResponse> updateFormField(
                        @Parameter(description = "Form field update request", required = true) @Valid @RequestBody UpdateFormFieldRequest request) {
                FormFieldResponse response = formFieldService.updateFormField(request);
                return ResponseEntity.ok(response);
        }

        // 4. Xóa Form Field
        @DeleteMapping("/form-fields/{id}")
        @ApiMessage("Xóa Form Field thành công")
        @Operation(summary = "Delete Form Field", description = "Delete form field by ID")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "204", description = "Form field deleted successfully"),
                        @ApiResponse(responseCode = "404", description = "Form field not found")
        })
        public ResponseEntity<Void> deleteFormField(
                        @Parameter(description = "Form field ID", required = true) @PathVariable String id) {
                formFieldService.deleteFormField(id);
                return ResponseEntity.noContent().build();
        }

        // 5. Lấy Form Field By Id
        @GetMapping("/form-fields/{id}")
        @ApiMessage("Lấy Form Field thành công")
        @Operation(summary = "Get Form Field by ID", description = "Get form field details by ID")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Form field retrieved successfully", content = @Content(schema = @Schema(implementation = FormFieldResponse.class))),
                        @ApiResponse(responseCode = "404", description = "Form field not found")
        })
        public ResponseEntity<FormFieldResponse> getFormFieldById(
                        @Parameter(description = "Form field ID", required = true) @PathVariable String id) {
                FormFieldResponse response = formFieldService.getFormFieldById(id);
                return ResponseEntity.ok(response);
        }

        // 6. Lấy danh sách Form Field By Template Id
        @GetMapping("/form-fields/template/{templateId}")
        @ApiMessage("Lấy danh sách Form Field thành công")
        @Operation(summary = "Get Form Fields by Template ID", description = "Get all form fields by template ID")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Form fields retrieved successfully", content = @Content(schema = @Schema(implementation = List.class))),
                        @ApiResponse(responseCode = "404", description = "Template not found")
        })
        public ResponseEntity<List<FormFieldResponse>> getFormFieldsByTemplateId(
                        @Parameter(description = "Template ID", required = true) @PathVariable String templateId) {
                List<FormFieldResponse> responses = formFieldService.getFormFieldsByTemplateId(templateId);
                return ResponseEntity.ok(responses);
        }

}
