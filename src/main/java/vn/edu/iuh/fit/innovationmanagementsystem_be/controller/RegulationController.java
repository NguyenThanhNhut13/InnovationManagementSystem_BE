package vn.edu.iuh.fit.innovationmanagementsystem_be.controller;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.turkraft.springfilter.boot.Filter;
import jakarta.validation.Valid;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.Regulation;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.ImportMultipleRegulationsRequest;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.ImportRegulationsToMultipleChaptersRequest;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.RegulationRequest;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.ImportMultipleRegulationsResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.ImportRegulationsToMultipleChaptersResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.RegulationResponse;
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

    // 5. Get Regulations by InnovationDecision
    @GetMapping("/innovation-decisions/{innovationDecisionId}/regulations")
    @ApiMessage("Lấy danh sách điều theo quyết định thành công")
    public ResponseEntity<ResultPaginationDTO> getRegulationsByInnovationDecision(
            @PathVariable String innovationDecisionId, Pageable pageable) {
        return ResponseEntity.ok(regulationService.getRegulationsByInnovationDecision(innovationDecisionId, pageable));
    }

    // 6. Get Regulations by Chapter
    @GetMapping("/chapters/{chapterId}/regulations")
    @ApiMessage("Lấy danh sách điều theo chương thành công")
    public ResponseEntity<ResultPaginationDTO> getRegulationsByChapter(
            @PathVariable String chapterId, Pageable pageable) {
        return ResponseEntity.ok(regulationService.getRegulationsByChapter(chapterId, pageable));
    }

    // 8. Import Multiple Regulations to Chapter
    @PostMapping("/chapters/{chapterId}/regulations/import")
    @ApiMessage("Import danh sách điều khoản vào chương thành công")
    public ResponseEntity<ImportMultipleRegulationsResponse> importMultipleRegulationsToChapter(
            @PathVariable String chapterId,
            @Valid @RequestBody ImportMultipleRegulationsRequest request) {
        request.setChapterId(chapterId);
        ImportMultipleRegulationsResponse response = regulationService.importMultipleRegulationsToChapter(request);
        return ResponseEntity.ok(response);
    }

    // 9. Import Regulations to Multiple Chapters
    @PostMapping("/innovation-decisions/chapters/regulations/import")
    @ApiMessage("Import danh sách điều khoản vào nhiều chương thành công")
    public ResponseEntity<ImportRegulationsToMultipleChaptersResponse> importRegulationsToMultipleChapters(
            @Valid @RequestBody ImportRegulationsToMultipleChaptersRequest request) {
        ImportRegulationsToMultipleChaptersResponse response = regulationService
                .importRegulationsToMultipleChapters(request);
        return ResponseEntity.ok(response);
    }

}
