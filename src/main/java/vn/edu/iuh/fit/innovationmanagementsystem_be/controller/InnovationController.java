package vn.edu.iuh.fit.innovationmanagementsystem_be.controller;

import com.turkraft.springfilter.boot.Filter;
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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.Innovation;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.CreateInnovationWithTemplatesRequest;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.InnovationFormDataResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.InnovationStatisticsDTO;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.InnovationAcademicYearStatisticsDTO;
import vn.edu.iuh.fit.innovationmanagementsystem_be.service.InnovationService;
import vn.edu.iuh.fit.innovationmanagementsystem_be.utils.ResultPaginationDTO;
import vn.edu.iuh.fit.innovationmanagementsystem_be.utils.annotation.ApiMessage;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Innovation Management", description = "Innovation managementAPIs")
@SecurityRequirement(name = "Bearer Authentication")
public class InnovationController {

        private final InnovationService innovationService;

        public InnovationController(InnovationService innovationService) {
                this.innovationService = innovationService;
        }

        // 1. Lấy tất cả sáng kiến của user hiện tại với filter - OK
        @GetMapping("/innovations/my-innovations")
        @ApiMessage("Lấy danh sách sáng kiến của tôi thành công")
        @Operation(summary = "Get My Innovations with Filter", description = "Get paginated list of all innovations for current user with filtering support")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "User innovations retrieved successfully", content = @Content(schema = @Schema(implementation = ResultPaginationDTO.class))),
                        @ApiResponse(responseCode = "401", description = "Unauthorized")
        })

        public ResponseEntity<ResultPaginationDTO> getMyInnovations(
                        @Parameter(description = "Filter specification for innovations") @Filter Specification<Innovation> specification,
                        @Parameter(description = "Pagination parameters") Pageable pageable) {
                return ResponseEntity.ok(
                                innovationService.getAllInnovationsByCurrentUserWithFilter(specification,
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
                        @ApiResponse(responseCode = "200", description = "Innovation with form data and form fields retrieved successfully", content = @Content(schema = @Schema(implementation = InnovationFormDataResponse.class))),
                        @ApiResponse(responseCode = "404", description = "Innovation not found"),
                        @ApiResponse(responseCode = "403", description = "Forbidden - You can only view your own innovations"),
                        @ApiResponse(responseCode = "401", description = "Unauthorized")
        })
        public ResponseEntity<InnovationFormDataResponse> getMyInnovationById(
                        @Parameter(description = "Innovation ID", required = true) @PathVariable String id) {
                return ResponseEntity.ok(innovationService.getMyInnovationWithFormDataById(id));
        }

        // 6. Lấy sáng kiến by Id cho QUAN_TRI_VIEN_KHOA và TRUONG_KHOA (chỉ cho phép
        // xem sáng kiến của phòng ban mình)
        @GetMapping("/department-innovations/{id}")
        @PreAuthorize("hasAnyRole('QUAN_TRI_VIEN_KHOA', 'TRUONG_KHOA')")
        @ApiMessage("Lấy thông tin sáng kiến của phòng ban bằng id thành công")
        @Operation(summary = "Get Department Innovation by ID", description = "Get innovation details with all form data and form fields by innovation ID (only for QUAN_TRI_VIEN_KHOA and TRUONG_KHOA to view innovations of their department)")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Innovation with form data and form fields retrieved successfully", content = @Content(schema = @Schema(implementation = InnovationFormDataResponse.class))),
                        @ApiResponse(responseCode = "404", description = "Innovation not found"),
                        @ApiResponse(responseCode = "403", description = "Forbidden - You can only view innovations of your department"),
                        @ApiResponse(responseCode = "401", description = "Unauthorized")
        })
        public ResponseEntity<InnovationFormDataResponse> getDepartmentInnovationById(
                        @Parameter(description = "Innovation ID", required = true) @PathVariable String id) {
                return ResponseEntity.ok(innovationService.getDepartmentInnovationWithFormDataById(id));
        }

        // 1. Lấy danh sách sáng kiến
        // @GetMapping("/innovations")
        // @ApiMessage("Lấy danh sách sáng kiến thành công")
        // @Operation(summary = "Get All Innovations", description = "Get paginated list
        // of all innovations with filtering")
        // @ApiResponses(value = {
        // @ApiResponse(responseCode = "200", description = "Innovations retrieved
        // successfully", content = @Content(schema = @Schema(implementation =
        // ResultPaginationDTO.class))),
        // @ApiResponse(responseCode = "401", description = "Unauthorized")
        // })

        // public ResponseEntity<ResultPaginationDTO> getAllInnovations(
        // @Parameter(description = "Filter specification for innovations") @Filter
        // Specification<Innovation> specification,
        // @Parameter(description = "Pagination parameters") Pageable pageable) {
        // return ResponseEntity.ok(innovationService.getAllInnovations(specification,
        // pageable));
        // }

        // 4. Cập nhật FormData sáng kiến
        // @PutMapping("/innovations/{innovationId}/form-data")
        // @ApiMessage("Cập nhật thông tin form thành công")
        // public ResponseEntity<InnovationFormDataResponse> updateInnovationFormData(
        // @PathVariable String innovationId,
        // @Valid @RequestBody InnovationFormDataRequest request) {
        // InnovationFormDataResponse response =
        // innovationService.updateInnovationFormData(innovationId, request);
        // return ResponseEntity.ok(response);
        // }

        // 5. Lấy FormData sáng kiến
        // @GetMapping("/innovations/{innovationId}/form-data")
        // @ApiMessage("Lấy FormData của sáng kiến thành công")
        // public ResponseEntity<InnovationFormDataResponse> getInnovationFormData(
        // @PathVariable String innovationId,
        // @RequestParam(required = false) String templateId) {
        // InnovationFormDataResponse response =
        // innovationService.getInnovationFormData(innovationId, templateId);
        // return ResponseEntity.ok(response);
        // }

        // 16. Duyệt sáng kiến
        // @PutMapping("/innovations/{innovationId}/approve")
        // @ApiMessage("Duyệt sáng kiến thành công")
        // @Operation(summary = "Approve Innovation", description = "Approve an
        // innovation")
        // @ApiResponses(value = {
        // @ApiResponse(responseCode = "200", description = "Innovation approved
        // successfully", content = @Content(schema = @Schema(implementation =
        // InnovationResponse.class))),
        // @ApiResponse(responseCode = "401", description = "Unauthorized"),
        // @ApiResponse(responseCode = "404", description = "Innovation not found")

        // })

        // public ResponseEntity<InnovationResponse> approveInnovation(
        // @Parameter(description = "Innovation ID") @PathVariable String innovationId,
        // @Parameter(description = "Approval reason") @RequestParam(required = false)
        // String reason) {
        // InnovationResponse response =
        // innovationService.approveInnovation(innovationId, reason);
        // return ResponseEntity.ok(response);
        // }

        // 17. Từ chối sáng kiến
        // @PutMapping("/innovations/{innovationId}/reject")
        // @ApiMessage("Từ chối sáng kiến thành công")
        // @Operation(summary = "Reject Innovation", description = "Reject an
        // innovation")
        // @ApiResponses(value = {
        // @ApiResponse(responseCode = "200", description = "Innovation rejected
        // successfully", content = @Content(schema = @Schema(implementation =
        // InnovationResponse.class))),
        // @ApiResponse(responseCode = "401", description = "Unauthorized"),
        // @ApiResponse(responseCode = "404", description = "Innovation not found")

        // })

        // public ResponseEntity<InnovationResponse> rejectInnovation(
        // @Parameter(description = "Innovation ID") @PathVariable String innovationId,
        // @Parameter(description = "Rejection reason") @RequestParam(required = false)
        // String reason) {
        // InnovationResponse response =
        // innovationService.rejectInnovation(innovationId, reason);
        // return ResponseEntity.ok(response);
        // }

}
