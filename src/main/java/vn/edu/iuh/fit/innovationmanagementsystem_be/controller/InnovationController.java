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
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.InnovationStatusEnum;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.CreateInnovationWithTemplatesRequest;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.FilterMyInnovationRequest;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.FilterAdminInnovationRequest;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.InnovationFormDataResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.MyInnovationFormDataResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.InnovationScoreResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.DepartmentInnovationDetailResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.InnovationStatisticsDTO;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.InnovationAcademicYearStatisticsDTO;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.InnovationDetailForGiangVienResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.service.InnovationService;
import vn.edu.iuh.fit.innovationmanagementsystem_be.service.InnovationDetailService;
import vn.edu.iuh.fit.innovationmanagementsystem_be.service.ReviewScoreService;
import vn.edu.iuh.fit.innovationmanagementsystem_be.utils.ResultPaginationDTO;
import vn.edu.iuh.fit.innovationmanagementsystem_be.utils.annotation.ApiMessage;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Innovation Management", description = "Innovation managementAPIs")
@SecurityRequirement(name = "Bearer Authentication")
public class InnovationController {

        private final InnovationService innovationService;
        private final InnovationDetailService innovationDetailService;
        private final ReviewScoreService reviewScoreService;

        public InnovationController(InnovationService innovationService,
                        InnovationDetailService innovationDetailService,
                        ReviewScoreService reviewScoreService) {
                this.innovationService = innovationService;
                this.innovationDetailService = innovationDetailService;
                this.reviewScoreService = reviewScoreService;
        }

        // 1. Lấy tất cả sáng kiến của user hiện tại với filter chi tiết
        @GetMapping("/innovations/my-innovations/filter")
        @ApiMessage("Lấy danh sách sáng kiến của tôi với filter chi tiết thành công")
        @Operation(summary = "Get My Innovations with Detailed Filter", description = "Get paginated list of all innovations for current user. If no filter parameters are provided, returns all innovations of the current user. Supports detailed filtering: search by name/author, status, round, and score")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "User innovations retrieved successfully", content = @Content(schema = @Schema(implementation = ResultPaginationDTO.class))),
                        @ApiResponse(responseCode = "401", description = "Unauthorized")
        })
        public ResponseEntity<ResultPaginationDTO> getMyInnovationsWithDetailedFilter(
                        @Parameter(description = "Search text for innovation name or author name") @RequestParam(required = false) String searchText,
                        @Parameter(description = "Innovation status") @RequestParam(required = false) InnovationStatusEnum status,
                        @Parameter(description = "Innovation round ID") @RequestParam(required = false) String innovationRoundId,
                        @Parameter(description = "Is scored or not") @RequestParam(required = false) Boolean isScore,
                        @Parameter(description = "Pagination parameters") Pageable pageable) {
                FilterMyInnovationRequest filterRequest = new FilterMyInnovationRequest(
                                searchText, status, innovationRoundId, isScore);

                return ResponseEntity.ok(
                                innovationService.getAllInnovationsByCurrentUserWithDetailedFilter(filterRequest,
                                                pageable));
        }

        // 2. Lấy thống kê sáng kiến của Current User - OK
        @GetMapping("/innovations/statistics")
        @ApiMessage("Lấy thống kê sáng kiến thành công")
        @Operation(summary = "Get Innovation Statistics", description = "Get innovation statistics for current user (GIANG_VIEN role)")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Statistics retrieved successfully", content = @Content(schema = @Schema(implementation = InnovationStatisticsDTO.class))),
                        @ApiResponse(responseCode = "401", description = "Unauthorized"),
                        @ApiResponse(responseCode = "403", description = "Forbidden - GIANG_VIEN role required")
        })

        public ResponseEntity<InnovationStatisticsDTO> getInnovationStatistics() {
                InnovationStatisticsDTO statistics = innovationService.getInnovationStatisticsForCurrentUser();
                return ResponseEntity.ok(statistics);
        }

        // 3. Lấy thống kê sáng kiến theo năm học - OK
        @GetMapping("/innovations/statistics/academic-year")
        @ApiMessage("Lấy thống kê sáng kiến theo năm học thành công")
        @Operation(summary = "Get Innovation Statistics by Academic Year", description = "Get innovation statistics grouped by academic year for currentuser")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Academic year statistics retrieved successfully", content = @Content(schema = @Schema(implementation = InnovationAcademicYearStatisticsDTO.class))),
                        @ApiResponse(responseCode = "401", description = "Unauthorized"),
                        @ApiResponse(responseCode = "403", description = "Forbidden - GIANG_VIEN role required")

        })
        public ResponseEntity<InnovationAcademicYearStatisticsDTO> getInnovationStatisticsByAcademicYear() {
                InnovationAcademicYearStatisticsDTO statistics = innovationService
                                .getInnovationStatisticsByAcademicYearForCurrentUser();
                return ResponseEntity.ok(statistics);
        }

        // 4. Tạo Innovation & Submit FormData nhiều Template
        @PostMapping("/innovations/templates")
        @ApiMessage("Tạo sáng kiến với nhiều template thành công")
        @Operation(summary = "Create Innovation with Multiple Templates", description = "Create a new innovation and submit form data for multiple templates")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Innovation created and formdata for all templates submitted successfully", content = @Content(schema = @Schema(implementation = InnovationFormDataResponse.class))),
                        @ApiResponse(responseCode = "400", description = "Invalid request data"),
                        @ApiResponse(responseCode = "401", description = "Unauthorized")
        })
        public ResponseEntity<InnovationFormDataResponse> createInnovationWithMultipleTemplates(
                        @Parameter(description = "Innovation with multiple templates request", required = true) @Valid @RequestBody CreateInnovationWithTemplatesRequest request) {
                InnovationFormDataResponse response = innovationService.createInnovationWithMultipleTemplates(request);
                return ResponseEntity.ok(response);
        }

        // 5. Lấy sáng kiến by Id của user hiện tại (chỉ cho phép xem sáng kiến của
        // chính mình)
        @GetMapping("/my-innovations/{id}")
        @PreAuthorize("hasAnyRole('GIANG_VIEN')")
        @ApiMessage("Lấy thông tin sáng kiến của tôi bằng id thành công")
        @Operation(summary = "Get My Innovation by ID", description = "Get innovation details with all form data and form fields by innovation ID (only for current user's innovations)")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Innovation with form data and form fields retrieved successfully", content = @Content(schema = @Schema(implementation = MyInnovationFormDataResponse.class))),
                        @ApiResponse(responseCode = "404", description = "Innovation not found"),
                        @ApiResponse(responseCode = "403", description = "Forbidden - You can only view your own innovations"),
                        @ApiResponse(responseCode = "401", description = "Unauthorized")
        })
        public ResponseEntity<MyInnovationFormDataResponse> getMyInnovationById(
                        @Parameter(description = "Innovation ID", required = true) @PathVariable String id) {
                return ResponseEntity.ok(innovationService.getMyInnovationWithFormDataById(id));
        }

        // 6. Lấy sáng kiến by Id cho QUAN_TRI_VIEN_KHOA và TRUONG_KHOA (chỉ cho phép
        // xem sáng kiến của phòng ban mình)
        // @GetMapping("/department-innovations/{id}")
        // @PreAuthorize("hasAnyRole('QUAN_TRI_VIEN_KHOA', 'TRUONG_KHOA')")
        // @ApiMessage("Lấy thông tin sáng kiến của phòng ban bằng id thành công")
        // @Operation(summary = "Get Department Innovation by ID", description = "Get
        // innovation details with all form data and form fields by innovation ID (only
        // for QUAN_TRI_VIEN_KHOA and TRUONG_KHOA to view innovations of their
        // department)")
        // @ApiResponses(value = {
        // @ApiResponse(responseCode = "200", description = "Innovation with form data
        // and form fields retrieved successfully", content = @Content(schema =
        // @Schema(implementation = InnovationFormDataResponse.class))),
        // @ApiResponse(responseCode = "404", description = "Innovation not found"),
        // @ApiResponse(responseCode = "403", description = "Forbidden - You can only
        // view innovations of your department"),
        // @ApiResponse(responseCode = "401", description = "Unauthorized")
        // })
        // public ResponseEntity<InnovationFormDataResponse>
        // getDepartmentInnovationById(
        // @Parameter(description = "Innovation ID", required = true) @PathVariable
        // String id) {
        // return
        // ResponseEntity.ok(innovationService.getDepartmentInnovationWithFormDataById(id));
        // }

        // 7. Lấy chi tiết sáng kiến của phòng ban cho QUAN_TRI_VIEN_KHOA và TRUONG_KHOA
        @GetMapping("/innovations/department/{id}/detail")
        @PreAuthorize("hasAnyRole('TRUONG_KHOA', 'QUAN_TRI_VIEN_KHOA')")
        @ApiMessage("Lấy chi tiết sáng kiến của phòng ban bằng id thành công")
        @Operation(summary = "Get Department Innovation Detail by ID", description = "Get innovation detail information including overview, co-authors, and attachment count by innovation ID (only for department innovations)")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Innovation detail retrieved successfully", content = @Content(schema = @Schema(implementation = DepartmentInnovationDetailResponse.class))),
                        @ApiResponse(responseCode = "404", description = "Innovation not found"),
                        @ApiResponse(responseCode = "403", description = "Forbidden - You can only view innovations of your department"),
                        @ApiResponse(responseCode = "401", description = "Unauthorized")
        })
        public ResponseEntity<DepartmentInnovationDetailResponse> getDepartmentInnovationDetail(
                        @Parameter(description = "Innovation ID", required = true) @PathVariable String id) {
                return ResponseEntity.ok(innovationService.getDepartmentInnovationDetailById(id));
        }

        // 8. Lấy tất cả sáng kiến của khoa với filter cho QUAN_TRI_VIEN_KHOA và
        // TRUONG_KHOA
        @GetMapping("/department-innovations")
        @PreAuthorize("hasAnyRole('QUAN_TRI_VIEN_KHOA', 'TRUONG_KHOA')")
        @ApiMessage("Lấy danh sách sáng kiến của khoa thành công")
        @Operation(summary = "Get All Department Innovations with Filter", description = "Get paginated list of all innovations for department with filtering support (only for QUAN_TRI_VIEN_KHOA and TRUONG_KHOA)")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Department innovations retrieved successfully", content = @Content(schema = @Schema(implementation = ResultPaginationDTO.class))),
                        @ApiResponse(responseCode = "403", description = "Forbidden - Only QUAN_TRI_VIEN_KHOA and TRUONG_KHOA can access"),
                        @ApiResponse(responseCode = "401", description = "Unauthorized")
        })
        public ResponseEntity<ResultPaginationDTO> getAllDepartmentInnovations(
                        @Parameter(description = "Search text for innovation name or author name") @RequestParam(required = false) String searchText,
                        @Parameter(description = "Innovation status") @RequestParam(required = false) InnovationStatusEnum status,
                        @Parameter(description = "Innovation round ID") @RequestParam(required = false) String innovationRoundId,
                        @Parameter(description = "Is scored or not") @RequestParam(required = false) Boolean isScore,
                        @Parameter(description = "Pagination parameters") Pageable pageable) {
                FilterMyInnovationRequest filterRequest = new FilterMyInnovationRequest(
                                searchText, status, innovationRoundId, isScore);
                return ResponseEntity.ok(
                                innovationService.getAllDepartmentInnovationsWithDetailedFilter(filterRequest,
                                                pageable));
        }

        // 9. Xóa sáng kiến trạng thái DRAFT của user hiện tại
        @DeleteMapping("/my-innovations/{id}")
        @PreAuthorize("hasAnyRole('GIANG_VIEN')")
        @ApiMessage("Xóa sáng kiến của tôi thành công")
        @Operation(summary = "Delete My Draft Innovation", description = "Xóa sáng kiến đang ở trạng thái DRAFT của user hiện tại và xóa tệp MinIO liên quan")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Innovation deleted successfully"),
                        @ApiResponse(responseCode = "401", description = "Unauthorized"),
                        @ApiResponse(responseCode = "403", description = "Forbidden - You can only delete your own innovations"),
                        @ApiResponse(responseCode = "404", description = "Innovation not found")
        })
        public ResponseEntity<Void> deleteMyDraftInnovation(
                        @Parameter(description = "Innovation ID", required = true) @PathVariable String id) {
                innovationService.deleteMyDraftInnovation(id);
                return ResponseEntity.ok().build();
        }

        // 10. Lấy tất cả sáng kiến với filter cho QUAN_TRI_VIEN_QLKH_HTQT,
        // TV_HOI_DONG_TRUONG, CHU_TICH_HD_TRUONG, QUAN_TRI_VIEN_HE_THONG
        @GetMapping("/innovations/school-innovations")
        @PreAuthorize("hasAnyRole('QUAN_TRI_VIEN_QLKH_HTQT', 'TV_HOI_DONG_TRUONG', 'CHU_TICH_HD_TRUONG', 'QUAN_TRI_VIEN_HE_THONG')")
        @ApiMessage("Lấy danh sách tất cả sáng kiến cho trường thành công")
        @Operation(summary = "Get All Innovations for School with Filter", description = "Get paginated list of all innovations with filtering support (only for QUAN_TRI_VIEN_QLKH_HTQT, TV_HOI_DONG_TRUONG, CHU_TICH_HD_TRUONG, QUAN_TRI_VIEN_HE_THONG). Supports detailed filtering: search by name/author, status, round, and department")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "All innovations retrieved successfully", content = @Content(schema = @Schema(implementation = ResultPaginationDTO.class))),
                        @ApiResponse(responseCode = "403", description = "Forbidden - Only QUAN_TRI_VIEN_QLKH_HTQT, TV_HOI_DONG_TRUONG, CHU_TICH_HD_TRUONG, QUAN_TRI_VIEN_HE_THONG can access"),
                        @ApiResponse(responseCode = "401", description = "Unauthorized")
        })
        public ResponseEntity<ResultPaginationDTO> getAllInnovationsForAdminRoles(
                        @Parameter(description = "Search text for innovation name or author name") @RequestParam(required = false) String searchText,
                        @Parameter(description = "Innovation status") @RequestParam(required = false) InnovationStatusEnum status,
                        @Parameter(description = "Innovation round ID") @RequestParam(required = false) String innovationRoundId,
                        @Parameter(description = "Department ID") @RequestParam(required = false) String departmentId,
                        @Parameter(description = "Pagination parameters") Pageable pageable) {
                FilterAdminInnovationRequest filterRequest = new FilterAdminInnovationRequest(
                                searchText, status, innovationRoundId, departmentId);
                return ResponseEntity.ok(
                                innovationService.getAllInnovationsForAdminRolesWithFilter(filterRequest, pageable));
        }

        // 11. Lấy chi tiết sáng kiến của user hiện tại bằng ID
        // @GetMapping("/my-innovations/{id}/detail")
        // @PreAuthorize("hasAnyRole('GIANG_VIEN')")
        // @ApiMessage("Lấy chi tiết sáng kiến của tôi bằng id thành công")
        // @Operation(summary = "Get My Innovation Detail by ID", description = "Get
        // innovation detail information including overview, co-authors, and attachment
        // count by innovation ID (only for current user's innovations)")
        // @ApiResponses(value = {
        // @ApiResponse(responseCode = "200", description = "Innovation detail retrieved
        // successfully", content = @Content(schema = @Schema(implementation =
        // InnovationDetailResponse.class))),
        // @ApiResponse(responseCode = "404", description = "Innovation not found"),
        // @ApiResponse(responseCode = "403", description = "Forbidden - You can only
        // view your own innovations"),
        // @ApiResponse(responseCode = "401", description = "Unauthorized")
        // })
        // public ResponseEntity<InnovationDetailResponse> getMyInnovationDetailById(
        // @Parameter(description = "Innovation ID", required = true) @PathVariable
        // String id) {
        // return ResponseEntity.ok(innovationService.getInnovationDetailById(id));
        // }

        // 12. Lấy chi tiết sáng kiến kèm bảng điểm để chấm điểm
        @GetMapping("/innovations/{id}/scoring-detail")
        @PreAuthorize("hasAnyRole('TV_HOI_DONG_KHOA', 'TV_HOI_DONG_TRUONG')")
        @ApiMessage("Lấy chi tiết sáng kiến kèm bảng điểm thành công")
        @Operation(summary = "Get Innovation Scoring Detail", description = "Get innovation detail with scoring criteria for scoring/evaluation")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Innovation scoring detail retrieved successfully", content = @Content(schema = @Schema(implementation = vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.InnovationScoringDetailResponse.class))),
                        @ApiResponse(responseCode = "404", description = "Innovation not found"),
                        @ApiResponse(responseCode = "403", description = "Forbidden"),
                        @ApiResponse(responseCode = "401", description = "Unauthorized")
        })
        public ResponseEntity<vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.InnovationScoringDetailResponse> getInnovationScoringDetail(
                        @Parameter(description = "Innovation ID", required = true) @PathVariable String id) {
                return ResponseEntity.ok(innovationService.getInnovationScoringDetailById(id));
        }

        // 13. Lấy đánh giá đã chấm của user hiện tại
        @GetMapping("/innovations/{id}/my-evaluation")
        @PreAuthorize("hasAnyRole('TV_HOI_DONG_KHOA', 'TV_HOI_DONG_TRUONG')")
        @ApiMessage("Lấy đánh giá đã chấm thành công")
        @Operation(summary = "Get My Evaluation", description = "Get the evaluation/score submitted by current user for an innovation")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Evaluation retrieved successfully", content = @Content(schema = @Schema(implementation = vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.InnovationScoreResponse.class))),
                        @ApiResponse(responseCode = "400", description = "User has not scored this innovation yet"),
                        @ApiResponse(responseCode = "403", description = "Forbidden"),
                        @ApiResponse(responseCode = "404", description = "Innovation not found")
        })
        public ResponseEntity<InnovationScoreResponse> getMyEvaluation(
                        @Parameter(description = "Innovation ID", required = true) @PathVariable String id) {
                return ResponseEntity.ok(reviewScoreService.getMyEvaluation(id));
        }

        // 14. Chấm điểm sáng kiến (có thể cập nhật lại trong thời gian chấm điểm)
        @PostMapping("/innovations/{id}/score")
        @PreAuthorize("hasAnyRole('TV_HOI_DONG_KHOA', 'TV_HOI_DONG_TRUONG', 'QUAN_TRI_VIEN_HE_THONG')")
        @ApiMessage("Chấm điểm sáng kiến thành công")
        @Operation(summary = "Submit or Update Innovation Score", description = "Submit or update score/evaluation for an innovation by council member. Only allowed during the scoring period. Auto-approves if score >= 70 for scored innovations. Can be used to update existing evaluation within the scoring period.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Score submitted/updated successfully", content = @Content(schema = @Schema(implementation = vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.InnovationScoreResponse.class))),
                        @ApiResponse(responseCode = "400", description = "Invalid request, validation error, or scoring period has ended"),
                        @ApiResponse(responseCode = "403", description = "Forbidden"),
                        @ApiResponse(responseCode = "404", description = "Innovation not found")
        })
        public ResponseEntity<vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.InnovationScoreResponse> submitInnovationScore(
                        @Parameter(description = "Innovation ID", required = true) @PathVariable String id,
                        @Valid @RequestBody vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.SubmitInnovationScoreRequest request) {
                return ResponseEntity.ok(reviewScoreService.submitInnovationScore(id, request));
        }

        // 14. Lấy chi tiết đầy đủ sáng kiến cho GIANG_VIEN (6 tabs)
        @GetMapping("/innovations/my-innovations/{id}/full-detail")
        @PreAuthorize("hasAnyRole('GIANG_VIEN')")
        @ApiMessage("Lấy chi tiết đầy đủ sáng kiến của tôi thành công")
        @Operation(summary = "Get My Innovation Full Detail", description = "Get full innovation detail with all 6 tabs for GIANG_VIEN: Overview, Form Content, Attachments, Reviews, Workflow, History")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Success", content = @Content(schema = @Schema(implementation = InnovationDetailForGiangVienResponse.class))),
                        @ApiResponse(responseCode = "404", description = "Innovation not found"),
                        @ApiResponse(responseCode = "403", description = "Forbidden - Not the owner of this innovation")
        })
        public ResponseEntity<InnovationDetailForGiangVienResponse> getMyInnovationFullDetail(
                        @Parameter(description = "Innovation ID", required = true) @PathVariable String id) {
                return ResponseEntity.ok(innovationDetailService.getMyInnovationDetailForGiangVien(id));
        }

}
