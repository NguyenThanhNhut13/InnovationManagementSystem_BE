package vn.edu.iuh.fit.innovationmanagementsystem_be.controller;

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
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.FormDataResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.service.FormDataService;
import vn.edu.iuh.fit.innovationmanagementsystem_be.utils.annotation.ApiMessage;

@RestController
@RequestMapping("/api/v1")
public class FormDataController {

    private final FormDataService formDataService;

    public FormDataController(FormDataService formDataService) {
        this.formDataService = formDataService;
    }

    // 1. Create Form Data
    @PostMapping("/form-data")
    @ApiMessage("Tạo Form Data thành công")
    public ResponseEntity<FormDataResponse> createFormData(
            @Valid @RequestBody FormDataRequest request) {
        FormDataResponse response = formDataService.createFormData(request);
        return ResponseEntity.ok(response);
    }

    // 2. Create Multiple Form Data
    @PostMapping("/form-data/bulk")
    @ApiMessage("Tạo nhiều Form Data thành công")
    public ResponseEntity<List<FormDataResponse>> createMultipleFormData(
            @Valid @RequestBody List<FormDataRequest> requests) {
        List<FormDataResponse> responses = formDataService.createMultipleFormData(requests);
        return ResponseEntity.ok(responses);
    }

    // 3. Update Form Data
    @PutMapping("/form-data/{id}")
    @ApiMessage("Cập nhật Form Data thành công")
    public ResponseEntity<FormDataResponse> updateFormData(
            @PathVariable String id,
            @Valid @RequestBody FormDataRequest request) {
        FormDataResponse response = formDataService.updateFormData(id, request);
        return ResponseEntity.ok(response);
    }

    // 4. Update Multiple Form Data
    @PutMapping("/form-data/bulk")
    @ApiMessage("Cập nhật nhiều Form Data thành công")
    public ResponseEntity<List<FormDataResponse>> updateMultipleFormData(
            @Valid @RequestBody List<FormDataRequest> requests) {
        List<FormDataResponse> responses = formDataService.updateMultipleFormData(requests);
        return ResponseEntity.ok(responses);
    }

    // 5. Get Form Data By Id
    @GetMapping("/form-data/{id}")
    @ApiMessage("Lấy Form Data thành công")
    public ResponseEntity<FormDataResponse> getFormDataById(@PathVariable String id) {
        FormDataResponse response = formDataService.getFormDataById(id);
        return ResponseEntity.ok(response);
    }

    // 6. Get Form Data By Innovation Id
    @GetMapping("/form-data/innovation/{innovationId}")
    @ApiMessage("Lấy danh sách Form Data theo Innovation thành công")
    public ResponseEntity<List<FormDataResponse>> getFormDataByInnovationId(
            @PathVariable String innovationId) {
        List<FormDataResponse> responses = formDataService.getFormDataByInnovationId(innovationId);
        return ResponseEntity.ok(responses);
    }

    // 7. Get Form Data By Template Id
    @GetMapping("/form-data/template/{templateId}")
    @ApiMessage("Lấy danh sách Form Data theo Template thành công")
    public ResponseEntity<List<FormDataResponse>> getFormDataByTemplateId(
            @PathVariable String templateId) {
        List<FormDataResponse> responses = formDataService.getFormDataByTemplateId(templateId);
        return ResponseEntity.ok(responses);
    }

    // 8. Get Form Data With Form Fields (Innovation + Template)
    @GetMapping("/form-data/innovation/{innovationId}/template/{templateId}")
    @ApiMessage("Lấy Form Data với Form Fields thành công")
    public ResponseEntity<List<FormDataResponse>> getFormDataWithFormFields(
            @PathVariable String innovationId,
            @PathVariable String templateId) {
        List<FormDataResponse> responses = formDataService.getFormDataWithFormFields(innovationId, templateId);
        return ResponseEntity.ok(responses);
    }
}