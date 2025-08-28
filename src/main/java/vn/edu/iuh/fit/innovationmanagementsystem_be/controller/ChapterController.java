package vn.edu.iuh.fit.innovationmanagementsystem_be.controller;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.turkraft.springfilter.boot.Filter;
import jakarta.validation.Valid;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.Chapter;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.ChapterRequest;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.CreateMultipleChaptersRequest;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.ChapterResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.CreateMultipleChaptersResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.service.ChapterService;
import vn.edu.iuh.fit.innovationmanagementsystem_be.utils.ResultPaginationDTO;
import vn.edu.iuh.fit.innovationmanagementsystem_be.utils.annotation.ApiMessage;

@RestController
@RequestMapping("/api/v1")
public class ChapterController {

    private final ChapterService chapterService;

    public ChapterController(ChapterService chapterService) {
        this.chapterService = chapterService;
    }

    // 1. Create Chapter
    @PostMapping("/chapters")
    @ApiMessage("Tạo chương thành công")
    public ResponseEntity<ChapterResponse> createChapter(
            @Valid @RequestBody ChapterRequest request) {
        ChapterResponse response = chapterService.createChapter(request);
        return ResponseEntity.ok(response);
    }

    // 2. Create Multiple Chapters
    @PostMapping("/chapters/bulk")
    @ApiMessage("Tạo nhiều chương thành công")
    public ResponseEntity<CreateMultipleChaptersResponse> createMultipleChapters(
            @Valid @RequestBody CreateMultipleChaptersRequest request) {
        CreateMultipleChaptersResponse response = chapterService.createMultipleChapters(request);
        return ResponseEntity.ok(response);
    }

    // 3. Get All Chapters
    @GetMapping("/chapters")
    @ApiMessage("Lấy danh sách chương thành công")
    public ResponseEntity<ResultPaginationDTO> getAllChapters(
            @Filter Specification<Chapter> specification, Pageable pageable) {
        return ResponseEntity.ok(chapterService.getAllChapters(specification, pageable));
    }

    // 4. Get Chapter by Id
    @GetMapping("/chapters/{id}")
    @ApiMessage("Lấy chương thành công")
    public ResponseEntity<ChapterResponse> getChapterById(@PathVariable String id) {
        return ResponseEntity.ok(chapterService.getChapterById(id));
    }

    // 5. Update Chapter
    @PutMapping("/chapters/{id}")
    @ApiMessage("Cập nhật chương thành công")
    public ResponseEntity<ChapterResponse> updateChapter(
            @PathVariable String id, @Valid @RequestBody ChapterRequest request) {
        return ResponseEntity.ok(chapterService.updateChapter(id, request));
    }

    // 6. Get Chapters by InnovationDecision
    @GetMapping("/innovation-decisions/{innovationDecisionId}/chapters")
    @ApiMessage("Lấy danh sách chương theo quyết định thành công")
    public ResponseEntity<ResultPaginationDTO> getChaptersByInnovationDecision(
            @PathVariable String innovationDecisionId, Pageable pageable) {
        return ResponseEntity.ok(chapterService.getChaptersByInnovationDecision(innovationDecisionId, pageable));
    }
}
