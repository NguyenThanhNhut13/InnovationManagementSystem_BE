package vn.edu.iuh.fit.innovationmanagementsystem_be.controller;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.turkraft.springfilter.boot.Filter;
import jakarta.validation.Valid;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.dto.requestDTO.InnovationDecisionRequest;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.dto.responseDTO.InnovationDecisionResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.InnovationDecision;
import vn.edu.iuh.fit.innovationmanagementsystem_be.service.InnovationDecisionService;
import vn.edu.iuh.fit.innovationmanagementsystem_be.utils.ResultPaginationDTO;
import vn.edu.iuh.fit.innovationmanagementsystem_be.utils.annotation.ApiMessage;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1")
public class InnovationDecisionController {

    private final InnovationDecisionService innovationDecisionService;

    public InnovationDecisionController(InnovationDecisionService innovationDecisionService) {
        this.innovationDecisionService = innovationDecisionService;
    }

    // 1. Create InnovationDecision
    @PostMapping("/innovation-decisions")
    @ApiMessage("Tạo quyết định thành công")
    public ResponseEntity<InnovationDecisionResponse> createInnovationDecision(
            @Valid @RequestBody InnovationDecisionRequest request) {
        InnovationDecisionResponse response = innovationDecisionService.createInnovationDecision(request);
        return ResponseEntity.ok(response);
    }

    // 2. Get All InnovationDecisions
    @GetMapping("/innovation-decisions")
    @ApiMessage("Lấy danh sách quyết định thành công")
    public ResponseEntity<ResultPaginationDTO> getAllInnovationDecisions(
            @Filter Specification<InnovationDecision> specification, Pageable pageable) {
        return ResponseEntity.ok(innovationDecisionService.getAllInnovationDecisions(specification, pageable));
    }

    // 3. Get InnovationDecision by Id
    @GetMapping("/innovation-decisions/{id}")
    @ApiMessage("Lấy quyết định thành công")
    public ResponseEntity<InnovationDecisionResponse> getInnovationDecisionById(@PathVariable String id) {
        return ResponseEntity.ok(innovationDecisionService.getInnovationDecisionById(id));
    }

    // 4. Update InnovationDecision
    @PutMapping("/innovation-decisions/{id}")
    @ApiMessage("Cập nhật quyết định thành công")
    public ResponseEntity<InnovationDecisionResponse> updateInnovationDecision(
            @PathVariable String id, @Valid @RequestBody InnovationDecisionRequest request) {
        return ResponseEntity.ok(innovationDecisionService.updateInnovationDecision(id, request));
    }

    // 5. Get InnovationDecisions by signed by
    @GetMapping("/innovation-decisions/signed-by")
    @ApiMessage("Lấy quyết định theo người ký thành công")
    public ResponseEntity<ResultPaginationDTO> getInnovationDecisionsBySignedBy(
            @RequestParam String signedBy, Pageable pageable) {
        return ResponseEntity.ok(innovationDecisionService.getInnovationDecisionsBySignedBy(signedBy, pageable));
    }

    // 6. Get InnovationDecisions by date range
    @GetMapping("/innovation-decisions/date-range")
    @ApiMessage("Lấy quyết định theo khoảng thời gian thành công")
    public ResponseEntity<ResultPaginationDTO> getInnovationDecisionsByDateRange(
            @RequestParam LocalDate startDate, @RequestParam LocalDate endDate, Pageable pageable) {
        return ResponseEntity
                .ok(innovationDecisionService.getInnovationDecisionsByDateRange(startDate, endDate, pageable));
    }
}
