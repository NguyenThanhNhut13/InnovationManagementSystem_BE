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
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.Council;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.CreateCouncilRequest;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.UpdateCouncilMembersRequest;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.ResolveViolationRequest;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.CouncilResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.CouncilResultsResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.CouncilListResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.InnovationViolationResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.ScoringStatusEnum;
import vn.edu.iuh.fit.innovationmanagementsystem_be.service.CouncilService;
import vn.edu.iuh.fit.innovationmanagementsystem_be.utils.ResultPaginationDTO;
import vn.edu.iuh.fit.innovationmanagementsystem_be.utils.annotation.ApiMessage;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Council", description = "Council management APIs")
@SecurityRequirement(name = "Bearer Authentication")
public class CouncilController {

    private final CouncilService councilService;

    public CouncilController(CouncilService councilService) {
        this.councilService = councilService;
    }

    // 1. Tạo Hội đồng mới
    @PostMapping("/councils")
    @PreAuthorize("hasAnyRole('TRUONG_KHOA','QUAN_TRI_VIEN_KHOA','QUAN_TRI_VIEN_HE_THONG', 'QUAN_TRI_VIEN_QLKH_HTQT')")
    @ApiMessage("Tạo Hội đồng thành công")
    @Operation(summary = "Create Council", description = "Create a new council with members and assigned innovations")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Council created successfully", content = @Content(schema = @Schema(implementation = CouncilResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<CouncilResponse> createCouncil(
            @Parameter(description = "Council creation request", required = true) @Valid @RequestBody CreateCouncilRequest request) {
        CouncilResponse response = councilService.createCouncil(request);
        return ResponseEntity.ok(response);
    }

    // 2. Lấy thông tin hội đồng hiện tại
    @GetMapping("/councils/current")
    @PreAuthorize("hasAnyRole('TRUONG_KHOA','QUAN_TRI_VIEN_KHOA','QUAN_TRI_VIEN_HE_THONG', 'QUAN_TRI_VIEN_QLKH_HTQT', 'TV_HOI_DONG_KHOA', 'TV_HOI_DONG_TRUONG', 'CHU_TICH_HD_TRUONG')")
    @Operation(summary = "Get Current Council", description = "Get the current council for the active round and user's department (for faculty level) or school level")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Council found successfully", content = @Content(schema = @Schema(implementation = CouncilResponse.class))),
            @ApiResponse(responseCode = "404", description = "No council found for current round"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<CouncilResponse> getCurrentCouncil() {
        CouncilResponse response = councilService.getCurrentCouncil();
        return ResponseEntity.ok(response);
    }

    // 3. Lấy thông tin chi tiết hội đồng theo ID
    @GetMapping("/councils/{id}")
    @PreAuthorize("hasAnyRole('TRUONG_KHOA','QUAN_TRI_VIEN_KHOA','QUAN_TRI_VIEN_HE_THONG', 'QUAN_TRI_VIEN_QLKH_HTQT', 'TV_HOI_DONG_KHOA', 'TV_HOI_DONG_TRUONG', 'CHU_TICH_HD_TRUONG')")
    @Operation(summary = "Get Council by ID", description = "Get detailed information of a council by ID. Access is restricted based on user's role and department.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Council found successfully", content = @Content(schema = @Schema(implementation = CouncilResponse.class))),
            @ApiResponse(responseCode = "404", description = "Council not found"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<CouncilResponse> getCouncilById(
            @Parameter(description = "Council ID", required = true) @PathVariable String id) {
        CouncilResponse response = councilService.getCouncilById(id);
        return ResponseEntity.ok(response);
    }

    // 4. Lấy danh sách sáng kiến của hội đồng với pagination và scoring progress
    @GetMapping("/councils/{id}/innovations")
    @PreAuthorize("hasAnyRole('TRUONG_KHOA','QUAN_TRI_VIEN_KHOA','QUAN_TRI_VIEN_HE_THONG', 'QUAN_TRI_VIEN_QLKH_HTQT', 'TV_HOI_DONG_KHOA', 'TV_HOI_DONG_TRUONG', 'CHU_TICH_HD_TRUONG')")
    @ApiMessage("Lấy danh sách sáng kiến của hội đồng thành công")
    @Operation(summary = "Get Council Innovations", description = "Get paginated list of innovations in a council with scoring progress. Access is restricted based on user's role and department.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Innovations retrieved successfully", content = @Content(schema = @Schema(implementation = ResultPaginationDTO.class))),
            @ApiResponse(responseCode = "404", description = "Council not found"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<ResultPaginationDTO> getCouncilInnovations(
            @Parameter(description = "Council ID", required = true) @PathVariable String id,
            @Parameter(description = "Pagination parameters") Pageable pageable) {
        return ResponseEntity.ok(councilService.getCouncilInnovations(id, pageable));
    }

    // 5. Lấy danh sách hội đồng với pagination và filtering
    @GetMapping("/councils")
    @PreAuthorize("hasAnyRole('TRUONG_KHOA','QUAN_TRI_VIEN_KHOA','QUAN_TRI_VIEN_HE_THONG', 'QUAN_TRI_VIEN_QLKH_HTQT', 'TV_HOI_DONG_KHOA', 'TV_HOI_DONG_TRUONG', 'CHU_TICH_HD_TRUONG', 'CHU_TICH', 'THU_KY', 'THANH_VIEN')")
    @ApiMessage("Lấy danh sách hội đồng thành công")
    @Operation(summary = "Get All Councils", description = "Get paginated list of all councils with filtering. Results are filtered based on user's role (faculty vs school level)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Councils retrieved successfully", content = @Content(schema = @Schema(implementation = ResultPaginationDTO.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<ResultPaginationDTO> getAllCouncils(
            @Parameter(description = "Filter specification for councils") @Filter Specification<Council> specification,
            @Parameter(description = "Pagination parameters") Pageable pageable) {
        return ResponseEntity.ok(councilService.getAllCouncilsWithPaginationAndFilter(specification, pageable));
    }

    // 6. Cập nhật hội đồng (thành viên và tự động gán sáng kiến mới)
    @PutMapping("/councils/{id}")
    @PreAuthorize("hasAnyRole('TRUONG_KHOA','QUAN_TRI_VIEN_KHOA','QUAN_TRI_VIEN_HE_THONG', 'QUAN_TRI_VIEN_QLKH_HTQT')")
    @ApiMessage("Cập nhật hội đồng thành công")
    @Operation(summary = "Update Council", description = "Update council members and automatically assign newly submitted innovations. Only allowed before scoring has started.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Council updated successfully", content = @Content(schema = @Schema(implementation = CouncilResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request data or scoring has already started"),
            @ApiResponse(responseCode = "404", description = "Council not found"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<CouncilResponse> updateCouncil(
            @Parameter(description = "Council ID", required = true) @PathVariable String id,
            @Parameter(description = "Updated members list", required = true) @Valid @RequestBody UpdateCouncilMembersRequest request) {
        CouncilResponse response = councilService.updateCouncil(id, request);
        return ResponseEntity.ok(response);
    }

    // 7. Lấy danh sách sáng kiến được phân công cho thành viên hội đồng hiện tại
    @GetMapping("/councils/my-assigned-innovations")
    @PreAuthorize("hasAnyRole('TV_HOI_DONG_KHOA', 'TV_HOI_DONG_TRUONG')")
    @ApiMessage("Lấy danh sách sáng kiến được phân công thành công")
    @Operation(summary = "Get My Assigned Innovations", description = "Get paginated list of innovations assigned to current council member for scoring. Returns innovations from current council only.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Innovations retrieved successfully", content = @Content(schema = @Schema(implementation = ResultPaginationDTO.class))),
            @ApiResponse(responseCode = "404", description = "No council found for current round"),
            @ApiResponse(responseCode = "403", description = "User is not a member of current council"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<ResultPaginationDTO> getMyAssignedInnovations(
            @Parameter(description = "Filter by scoring status: ALL, PENDING, SCORED") @RequestParam(required = false, defaultValue = "ALL") ScoringStatusEnum scoringStatus,
            @Parameter(description = "Pagination parameters") Pageable pageable) {
        return ResponseEntity.ok(councilService.getMyAssignedInnovations(scoringStatus, pageable));
    }

    // 8. Lấy kết quả chấm điểm của hội đồng
    @GetMapping("/councils/{id}/results")
    @PreAuthorize("hasAnyRole('TRUONG_KHOA','QUAN_TRI_VIEN_KHOA','QUAN_TRI_VIEN_HE_THONG', 'QUAN_TRI_VIEN_QLKH_HTQT', 'TV_HOI_DONG_KHOA', 'TV_HOI_DONG_TRUONG', 'CHU_TICH_HD_TRUONG')")
    @ApiMessage("Lấy kết quả chấm điểm thành công")
    @Operation(summary = "Get Council Results", description = "Get detailed scoring results for all innovations in a council. Only available after scoring period has ended.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Results retrieved successfully", content = @Content(schema = @Schema(implementation = CouncilResultsResponse.class))),
            @ApiResponse(responseCode = "400", description = "Scoring period has not ended yet"),
            @ApiResponse(responseCode = "404", description = "Council not found"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<CouncilResultsResponse> getCouncilResults(
            @Parameter(description = "Council ID", required = true) @PathVariable String id) {
        CouncilResultsResponse response = councilService.getCouncilResults(id);
        return ResponseEntity.ok(response);
    }

    // 9. Lấy danh sách hội đồng mà user hiện tại là thư ký
    @GetMapping("/councils/secretary/my-councils")
    @PreAuthorize("hasAnyRole('TV_HOI_DONG_KHOA', 'TV_HOI_DONG_TRUONG')")
    @ApiMessage("Lấy danh sách hội đồng mà user là thư ký thành công")
    @Operation(summary = "Get Councils Where User Is Secretary", description = "Get list of councils where the current user is secretary (THU_KY). Returns both faculty and school level councils.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Councils retrieved successfully", content = @Content(schema = @Schema(implementation = List.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<List<CouncilListResponse>> getCouncilsWhereUserIsSecretary() {
        List<CouncilListResponse> response = councilService.getCouncilsWhereUserIsSecretary();
        return ResponseEntity.ok(response);
    }

    // 10. Lấy danh sách innovations có vi phạm cần Chủ tịch xem xét
    @GetMapping("/councils/{id}/violations")
    @PreAuthorize("hasAnyRole('CHU_TICH_HD_TRUONG', 'CHU_TICH')")
    @ApiMessage("Lấy danh sách vi phạm cần xem xét thành công")
    @Operation(summary = "Get Innovations With Violations", description = "Get list of innovations in a council that have violations reported by members. Only accessible by the council chairman.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Violations retrieved successfully", content = @Content(schema = @Schema(implementation = List.class))),
            @ApiResponse(responseCode = "403", description = "User is not the chairman of this council"),
            @ApiResponse(responseCode = "404", description = "Council not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<List<InnovationViolationResponse>> getInnovationsWithViolations(
            @Parameter(description = "Council ID", required = true) @PathVariable String id) {
        List<InnovationViolationResponse> response = councilService.getInnovationsWithViolations(id);
        return ResponseEntity.ok(response);
    }

    // 11. Chủ tịch quyết định vi phạm (bỏ qua cảnh báo hoặc từ chối sáng kiến)
    @PostMapping("/councils/{id}/innovations/{innovationId}/resolve-violation")
    @PreAuthorize("hasAnyRole('CHU_TICH_HD_TRUONG', 'CHU_TICH')")
    @ApiMessage("Quyết định vi phạm thành công")
    @Operation(summary = "Resolve Violation", description = "Chairman resolves a violation: dismiss the warning (recalculate results) or reject the innovation. Only accessible by the council chairman.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Violation resolved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "403", description = "User is not the chairman of this council"),
            @ApiResponse(responseCode = "404", description = "Council or Innovation not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<Void> resolveViolation(
            @Parameter(description = "Council ID", required = true) @PathVariable String id,
            @Parameter(description = "Innovation ID", required = true) @PathVariable String innovationId,
            @Parameter(description = "Resolve violation request", required = true) @Valid @RequestBody ResolveViolationRequest request) {
        councilService.resolveViolation(id, innovationId, request.getDismissViolation());
        return ResponseEntity.ok().build();
    }

}
