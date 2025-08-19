package vn.edu.iuh.fit.innovationmanagementsystem_be.controller;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.turkraft.springfilter.boot.Filter;
import jakarta.validation.Valid;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.dto.requestDTO.RegulationRequest;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.dto.responseDTO.RegulationResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.Regulation;
import vn.edu.iuh.fit.innovationmanagementsystem_be.service.RegulationService;
import vn.edu.iuh.fit.innovationmanagementsystem_be.utils.ResultPaginationDTO;
import vn.edu.iuh.fit.innovationmanagementsystem_be.utils.annotation.ApiMessage;

@RestController
@RequestMapping("/api/v1")
public class RegulationController {

    private final RegulationService regulationService;

    public RegulationController(RegulationService regulationService) {
        this.regulationService = regulationService;
    }

    // 1. Create Regulation
    @PostMapping("/regulations")
    @ApiMessage("Tạo điều thành công")
    public ResponseEntity<RegulationResponse> createRegulation(
            @Valid @RequestBody RegulationRequest request) {
        RegulationResponse response = regulationService.createRegulation(request);
        return ResponseEntity.ok(response);
    }

    // 2. Get All Regulations
    @GetMapping("/regulations")
    @ApiMessage("Lấy danh sách điều thành công")
    public ResponseEntity<ResultPaginationDTO> getAllRegulations(
            @Filter Specification<Regulation> specification, Pageable pageable) {
        return ResponseEntity.ok(regulationService.getAllRegulations(specification, pageable));
    }

    // 3. Get Regulation by Id
    @GetMapping("/regulations/{id}")
    @ApiMessage("Lấy điều thành công")
    public ResponseEntity<RegulationResponse> getRegulationById(@PathVariable String id) {
        return ResponseEntity.ok(regulationService.getRegulationById(id));
    }

    // 4. Update Regulation
    @PutMapping("/regulations/{id}")
    @ApiMessage("Cập nhật điều thành công")
    public ResponseEntity<RegulationResponse> updateRegulation(
            @PathVariable String id, @Valid @RequestBody RegulationRequest request) {
        return ResponseEntity.ok(regulationService.updateRegulation(id, request));
    }

    // 5. Delete Regulation
    @DeleteMapping("/regulations/{id}")
    @ApiMessage("Xóa điều thành công")
    public ResponseEntity<Void> deleteRegulation(@PathVariable String id) {
        regulationService.deleteRegulation(id);
        return ResponseEntity.ok().build();
    }

    // 6. Get Regulations by InnovationDecision
    @GetMapping("/innovation-decisions/{innovationDecisionId}/regulations")
    @ApiMessage("Lấy danh sách điều theo quyết định thành công")
    public ResponseEntity<ResultPaginationDTO> getRegulationsByInnovationDecision(
            @PathVariable String innovationDecisionId, Pageable pageable) {
        return ResponseEntity.ok(regulationService.getRegulationsByInnovationDecision(innovationDecisionId, pageable));
    }

    // 7. Get Regulations by Chapter
    @GetMapping("/chapters/{chapterId}/regulations")
    @ApiMessage("Lấy danh sách điều theo chương thành công")
    public ResponseEntity<ResultPaginationDTO> getRegulationsByChapter(
            @PathVariable String chapterId, Pageable pageable) {
        return ResponseEntity.ok(regulationService.getRegulationsByChapter(chapterId, pageable));
    }

    // 8. Get Regulations not in any Chapter
    @GetMapping("/regulations/not-in-chapter")
    @ApiMessage("Lấy danh sách điều không thuộc chương nào thành công")
    public ResponseEntity<ResultPaginationDTO> getRegulationsNotInChapter(Pageable pageable) {
        return ResponseEntity.ok(regulationService.getRegulationsNotInChapter(pageable));
    }

    // 9. Search Regulations by keyword
    @GetMapping("/regulations/search")
    @ApiMessage("Tìm kiếm điều thành công")
    public ResponseEntity<ResultPaginationDTO> searchRegulations(
            @RequestParam String keyword, Pageable pageable) {
        return ResponseEntity.ok(regulationService.searchRegulations(keyword, pageable));
    }

    // 10. Search Regulations by InnovationDecision and keyword
    @GetMapping("/innovation-decisions/{innovationDecisionId}/regulations/search")
    @ApiMessage("Tìm kiếm điều theo quyết định thành công")
    public ResponseEntity<ResultPaginationDTO> searchRegulationsByInnovationDecision(
            @PathVariable String innovationDecisionId, @RequestParam String keyword, Pageable pageable) {
        return ResponseEntity
                .ok(regulationService.searchRegulationsByInnovationDecision(innovationDecisionId, keyword, pageable));
    }

    // 11. Search Regulations by Chapter and keyword
    @GetMapping("/chapters/{chapterId}/regulations/search")
    @ApiMessage("Tìm kiếm điều theo chương thành công")
    public ResponseEntity<ResultPaginationDTO> searchRegulationsByChapter(
            @PathVariable String chapterId, @RequestParam String keyword, Pageable pageable) {
        return ResponseEntity.ok(regulationService.searchRegulationsByChapter(chapterId, keyword, pageable));
    }
}
