package vn.edu.iuh.fit.innovationmanagementsystem_be.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.dto.requestDTO.FormFieldRequest;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.dto.requestDTO.UpdateFormFieldRequest;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.dto.responseDTO.FormFieldResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.service.FormFieldService;

@RestController
@RequestMapping("/api/v1")
public class FormFieldController {

    private final FormFieldService formFieldService;

    public FormFieldController(FormFieldService formFieldService) {
        this.formFieldService = formFieldService;
    }

    // 1. Create Form Field
    @PostMapping("/form-fields")
    public ResponseEntity<FormFieldResponse> createFormField(
            @Valid @RequestBody FormFieldRequest request,
            @PathVariable String templateId) {
        FormFieldResponse response = formFieldService.createFormField(request, templateId);
        return ResponseEntity.ok(response);
    }

    // 2. Create Multiple Form Fields
    @PostMapping("/form-fields/bulk")
    public ResponseEntity<List<FormFieldResponse>> createMultipleFormFields(
            @Valid @RequestBody List<FormFieldRequest> requests,
            @PathVariable String templateId) {
        List<FormFieldResponse> responses = formFieldService.createMultipleFormFields(requests, templateId);
        return ResponseEntity.ok(responses);
    }

    // 3. Update Form Field
    @PutMapping("/form-fields/{id}")
    public ResponseEntity<FormFieldResponse> updateFormField(
            @Valid @RequestBody UpdateFormFieldRequest request) {
        FormFieldResponse response = formFieldService.updateFormField(request);
        return ResponseEntity.ok(response);
    }

    // 4. Delete Form Field
    @DeleteMapping("/form-fields/{id}")
    public ResponseEntity<Void> deleteFormField(@PathVariable String id) {
        formFieldService.deleteFormField(id);
        return ResponseEntity.noContent().build();
    }

    // 5. Get Form Field By Id
    @GetMapping("/form-fields/{id}")
    public ResponseEntity<FormFieldResponse> getFormFieldById(@PathVariable String id) {
        FormFieldResponse response = formFieldService.getFormFieldById(id);
        return ResponseEntity.ok(response);
    }

    // 6. Get Form Fields By Template Id
    @GetMapping("/form-fields/template/{templateId}")
    public ResponseEntity<List<FormFieldResponse>> getFormFieldsByTemplateId(@PathVariable String templateId) {
        List<FormFieldResponse> responses = formFieldService.getFormFieldsByTemplateId(templateId);
        return ResponseEntity.ok(responses);
    }

    // 7. Reorder Form Field
    @PutMapping("/form-fields/{id}/reorder")
    public ResponseEntity<FormFieldResponse> reorderFormField(
            @Valid @RequestBody FormFieldRequest request,
            @PathVariable String id) {
        FormFieldResponse response = formFieldService.reorderFormField(id, request);
        return ResponseEntity.ok(response);
    }
}
