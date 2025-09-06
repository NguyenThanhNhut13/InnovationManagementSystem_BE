package vn.edu.iuh.fit.innovationmanagementsystem_be.controller;

import com.turkraft.springfilter.boot.Filter;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.Innovation;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.InnovationFormDataRequest;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.InnovationFormDataResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.InnovationResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.service.InnovationService;
import vn.edu.iuh.fit.innovationmanagementsystem_be.utils.ResultPaginationDTO;
import vn.edu.iuh.fit.innovationmanagementsystem_be.utils.annotation.ApiMessage;

@RestController
@RequestMapping("/api/v1")
public class InnovationController {

    private final InnovationService innovationService;

    public InnovationController(InnovationService innovationService) {
        this.innovationService = innovationService;
    }

    // 1. Get All Innovations
    @GetMapping("/innovations")
    @ApiMessage("Lấy danh sách sáng kiến thành công")
    public ResponseEntity<ResultPaginationDTO> getAllInnovations(
            @Filter Specification<Innovation> specification, Pageable pageable) {
        return ResponseEntity.ok(innovationService.getAllInnovations(specification, pageable));
    }

    // 2. Get Innovation by Id
    @GetMapping("/innovations/{id}")
    @ApiMessage("Lấy thông tin sáng kiến bằng id thành công")
    public ResponseEntity<InnovationResponse> getInnovationById(@PathVariable String id) {
        return ResponseEntity.ok(innovationService.getInnovationById(id));
    }

    // 3. Create Innovation & Submit Form Data (Tạo sáng kiến tự động khi điền form)
    @PostMapping("/innovations/form-data")
    @ApiMessage("Tạo sáng kiến và điền thông tin thành công")
    public ResponseEntity<InnovationFormDataResponse> createInnovationAndSubmitFormData(
            @Valid @RequestBody InnovationFormDataRequest request) {
        InnovationFormDataResponse response = innovationService.createInnovationAndSubmitFormData(request);
        return ResponseEntity.ok(response);
    }

    // 4. Get Innovation Form Data
    @GetMapping("/innovations/{innovationId}/form-data")
    @ApiMessage("Lấy thông tin form sáng kiến thành công")
    public ResponseEntity<InnovationFormDataResponse> getInnovationFormData(
            @PathVariable String innovationId,
            @RequestParam(required = false) String templateId) {
        InnovationFormDataResponse response = innovationService.getInnovationFormData(innovationId, templateId);
        return ResponseEntity.ok(response);
    }

    // 5. Get Draft Innovations by Current User
    @GetMapping("/innovations/my-innovations/draft")
    @ApiMessage("Lấy danh sách sáng kiến DRAFT của tôi thành công")
    public ResponseEntity<ResultPaginationDTO> getMyDraftInnovations(Pageable pageable) {
        return ResponseEntity.ok(innovationService.getInnovationsByUserAndStatus("DRAFT", pageable));
    }

    // 6. Get Submitted Innovations by Current User
    @GetMapping("/innovations/my-innovations/submitted")
    @ApiMessage("Lấy danh sách sáng kiến SUBMITTED của tôi thành công")
    public ResponseEntity<ResultPaginationDTO> getMySubmittedInnovations(Pageable pageable) {
        return ResponseEntity.ok(innovationService.getInnovationsByUserAndStatus("SUBMITTED", pageable));
    }
}
