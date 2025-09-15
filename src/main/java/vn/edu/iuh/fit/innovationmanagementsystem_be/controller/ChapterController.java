package vn.edu.iuh.fit.innovationmanagementsystem_be.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Chapter", description = "Chapter management APIs")
@SecurityRequirement(name = "Bearer Authentication")
public class ChapterController {

        private final ChapterService chapterService;

        public ChapterController(ChapterService chapterService) {
                this.chapterService = chapterService;
        }

        // 1. Create Chapter
        @PostMapping("/chapters")
        @ApiMessage("Tạo chương thành công")
        @Operation(summary = "Create Chapter", description = "Create a new chapter")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Chapter created successfully", content = @Content(schema = @Schema(implementation = ChapterResponse.class))),
                        @ApiResponse(responseCode = "400", description = "Invalid request data")
        })
        public ResponseEntity<ChapterResponse> createChapter(
                        @Parameter(description = "Chapter creation request", required = true) @Valid @RequestBody ChapterRequest request) {
                ChapterResponse response = chapterService.createChapter(request);
                return ResponseEntity.ok(response);
        }

        // 2. Create Multiple Chapters
        @PostMapping("/chapters/bulk")
        @ApiMessage("Tạo nhiều chương thành công")
        @Operation(summary = "Create Multiple Chapters", description = "Create multiple chapters")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Multiple chapters created successfully", content = @Content(schema = @Schema(implementation = CreateMultipleChaptersResponse.class))),
                        @ApiResponse(responseCode = "400", description = "Invalid request data")
        })
        public ResponseEntity<CreateMultipleChaptersResponse> createMultipleChapters(
                        @Parameter(description = "Multiple chapters creation request", required = true) @Valid @RequestBody CreateMultipleChaptersRequest request) {
                CreateMultipleChaptersResponse response = chapterService.createMultipleChapters(request);
                return ResponseEntity.ok(response);
        }

        // 3. Get All Chapters
        @GetMapping("/chapters")
        @ApiMessage("Lấy danh sách chương thành công")
        @Operation(summary = "Get All Chapters", description = "Get all chapters")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "All chapters retrieved successfully", content = @Content(schema = @Schema(implementation = ResultPaginationDTO.class))),
                        @ApiResponse(responseCode = "400", description = "Invalid request data")
        })
        public ResponseEntity<ResultPaginationDTO> getAllChapters(
                        @Parameter(description = "Filter specification for chapters") @Filter Specification<Chapter> specification,
                        @Parameter(description = "Pagination parameters") Pageable pageable) {
                return ResponseEntity.ok(chapterService.getAllChapters(specification, pageable));
        }

        // 4. Get Chapter by Id
        @GetMapping("/chapters/{id}")
        @ApiMessage("Lấy chương thành công")
        @Operation(summary = "Get Chapter by Id", description = "Get chapter details by chapter ID")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Chapter retrieved successfully", content = @Content(schema = @Schema(implementation = ChapterResponse.class))),
                        @ApiResponse(responseCode = "400", description = "Invalid request data")
        })
        public ResponseEntity<ChapterResponse> getChapterById(
                        @Parameter(description = "Chapter ID", required = true) @PathVariable String id) {
                return ResponseEntity.ok(chapterService.getChapterById(id));
        }

        // 5. Update Chapter
        @PutMapping("/chapters/{id}")
        @ApiMessage("Cập nhật chương thành công")
        @Operation(summary = "Update Chapter", description = "Update chapter details by chapter ID")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Chapter updated successfully", content = @Content(schema = @Schema(implementation = ChapterResponse.class))),
                        @ApiResponse(responseCode = "400", description = "Invalid request data")
        })
        public ResponseEntity<ChapterResponse> updateChapter(
                        @Parameter(description = "Chapter ID", required = true) @PathVariable String id,
                        @Parameter(description = "Chapter update request", required = true) @Valid @RequestBody ChapterRequest request) {
                return ResponseEntity.ok(chapterService.updateChapter(id, request));
        }

        // 6. Get Chapters by InnovationDecision
        @GetMapping("/innovation-decisions/{innovationDecisionId}/chapters")
        @ApiMessage("Lấy danh sách chương theo quyết định thành công")
        @Operation(summary = "Get Chapters by InnovationDecision", description = "Get chapters by innovation decision ID")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Chapters retrieved successfully", content = @Content(schema = @Schema(implementation = ResultPaginationDTO.class))),
                        @ApiResponse(responseCode = "400", description = "Invalid request data")
        })
        public ResponseEntity<ResultPaginationDTO> getChaptersByInnovationDecision(
                        @Parameter(description = "Innovation decision ID", required = true) @PathVariable String innovationDecisionId,
                        @Parameter(description = "Pagination parameters") Pageable pageable) {
                return ResponseEntity
                                .ok(chapterService.getChaptersByInnovationDecision(innovationDecisionId, pageable));
        }
}
