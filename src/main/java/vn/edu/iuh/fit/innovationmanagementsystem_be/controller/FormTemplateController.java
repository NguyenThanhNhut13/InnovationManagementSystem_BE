package vn.edu.iuh.fit.innovationmanagementsystem_be.controller;

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
@RequestMapping("/api/form-templates")
public class FormTemplateController {

    private final FormTemplateService formTemplateService;

    public FormTemplateController(FormTemplateService formTemplateService) {
        this.formTemplateService = formTemplateService;
    }

    // 1. Get form template by id
    @GetMapping("/{id}")
    @ApiMessage("Lấy form template theo id")
    public ResponseEntity<FormTemplateResponse> getFormTemplateById(@PathVariable String id) {
        return ResponseEntity.ok(formTemplateService.getFormTemplateById(id));
    }

    // 2. Get form templates by innovation round
    @GetMapping("/innovation-rounds/{roundId}")
    @ApiMessage("Lấy form templates theo innovation round")
    public ResponseEntity<List<FormTemplateResponse>> getFormTemplatesByInnovationRound(
            @PathVariable String roundId) {
        return ResponseEntity.ok(formTemplateService.getFormTemplatesByInnovationRound(roundId));
    }

    // 3. Create form template
    @PostMapping
    @ApiMessage("Tạo form template")
    public ResponseEntity<FormTemplateResponse> createFormTemplate(
            @Valid @RequestBody CreateFormTemplateRequest request) {
        return ResponseEntity.ok(formTemplateService.createFormTemplate(request));
    }

    // 3.1. Create Multiple Form Templates
    @PostMapping("/bulk")
    @ApiMessage("Tạo nhiều form templates thành công")
    public ResponseEntity<CreateMultipleFormTemplatesResponse> createMultipleFormTemplates(
            @Valid @RequestBody CreateMultipleFormTemplatesRequest request) {
        return ResponseEntity.ok(formTemplateService.createMultipleFormTemplates(request));
    }

    // 4. Update form template
    @PutMapping("/{id}")
    @ApiMessage("Cập nhật form template thành công")
    public ResponseEntity<FormTemplateResponse> updateFormTemplate(
            @PathVariable String id,
            @Valid @RequestBody UpdateFormTemplateRequest request) {
        return ResponseEntity.ok(formTemplateService.updateFormTemplate(id, request));
    }

    // 6. Get all form templates with pagination and search
    @GetMapping
    @ApiMessage("Lấy danh sách form templates với phân trang và tìm kiếm")
    public ResponseEntity<ResultPaginationDTO> getAllFormTemplatesWithPaginationAndSearch(
            @Filter Specification<FormTemplate> specification,
            Pageable pageable) {
        return ResponseEntity
                .ok(formTemplateService.getAllFormTemplatesWithPaginationAndSearch(specification, pageable));
    }
}
