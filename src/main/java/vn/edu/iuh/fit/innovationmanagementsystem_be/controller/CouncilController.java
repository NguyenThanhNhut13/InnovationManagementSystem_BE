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
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.Council;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.ReviewLevelEnum;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.AddCouncilMemberRequest;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.CouncilRequest;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.CouncilResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.CouncilMemberResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.service.CouncilService;
import vn.edu.iuh.fit.innovationmanagementsystem_be.utils.ResultPaginationDTO;
import vn.edu.iuh.fit.innovationmanagementsystem_be.utils.annotation.ApiMessage;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Council Management", description = "Council management APIs")
@SecurityRequirement(name = "Bearer Authentication")
public class CouncilController {

    private final CouncilService councilService;

    public CouncilController(CouncilService councilService) {
        this.councilService = councilService;
    }

    // 1. Create Council
    @PostMapping("/councils")
    @ApiMessage("Tạo hội đồng thành công")
    @Operation(summary = "Create Council", description = "Create a new council")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Council created successfully", content = @Content(schema = @Schema(implementation = CouncilResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "409", description = "Council name already exists")
    })
    public ResponseEntity<CouncilResponse> createCouncil(
            @Parameter(description = "Council creation request", required = true) @Valid @RequestBody CouncilRequest councilRequest) {
        CouncilResponse councilResponse = councilService.createCouncil(councilRequest);
        return ResponseEntity.ok(councilResponse);
    }

    // 2. Get All Councils
    @GetMapping("/councils")
    @ApiMessage("Lấy danh sách hội đồng thành công")
    @Operation(summary = "Get All Councils", description = "Get paginated list of all councils with filtering")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Councils retrieved successfully", content = @Content(schema = @Schema(implementation = ResultPaginationDTO.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<ResultPaginationDTO> getAllCouncils(
            @Parameter(description = "Filter specification for councils") @Filter Specification<Council> specification,
            @Parameter(description = "Pagination parameters") Pageable pageable) {
        return ResponseEntity.ok(councilService.getAllCouncils(specification, pageable));
    }

    // 3. Get Council by Id
    @GetMapping("/councils/{id}")
    @ApiMessage("Lấy thông tin hội đồng thành công")
    @Operation(summary = "Get Council by ID", description = "Get council details by council ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Council retrieved successfully", content = @Content(schema = @Schema(implementation = CouncilResponse.class))),
            @ApiResponse(responseCode = "404", description = "Council not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<CouncilResponse> getCouncilById(
            @Parameter(description = "Council ID", required = true) @PathVariable String id) {
        return ResponseEntity.ok(councilService.getCouncilById(id));
    }

    // 4. Update Council
    @PutMapping("/councils/{id}")
    @ApiMessage("Cập nhật thông tin hội đồng thành công")
    @Operation(summary = "Update Council", description = "Update council information by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Council updated successfully", content = @Content(schema = @Schema(implementation = CouncilResponse.class))),
            @ApiResponse(responseCode = "404", description = "Council not found"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<CouncilResponse> updateCouncil(
            @Parameter(description = "Council ID", required = true) @PathVariable String id,
            @Parameter(description = "Updated council information", required = true) @Valid @RequestBody CouncilRequest councilRequest) {
        return ResponseEntity.ok(councilService.updateCouncil(id, councilRequest));
    }

    // 5. Search Councils by Name
    @GetMapping("/councils/search")
    @ApiMessage("Tìm kiếm hội đồng thành công")
    @Operation(summary = "Search Councils by Name", description = "Search councils by name with pagination")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Councils retrieved successfully", content = @Content(schema = @Schema(implementation = ResultPaginationDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request data")
    })
    public ResponseEntity<ResultPaginationDTO> searchCouncilsByName(
            @Parameter(description = "Search keyword", required = true) @RequestParam String keyword,
            @Parameter(description = "Pagination parameters") Pageable pageable) {
        return ResponseEntity.ok(councilService.searchCouncilsByName(keyword, pageable));
    }

    // 6. Get Councils by Review Level
    @GetMapping("/councils/review-level")
    @ApiMessage("Lấy danh sách hội đồng theo cấp độ đánh giá thành công")
    @Operation(summary = "Get Councils by Review Level", description = "Get councils by review level with pagination")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Councils retrieved successfully", content = @Content(schema = @Schema(implementation = ResultPaginationDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request data")
    })
    public ResponseEntity<ResultPaginationDTO> getCouncilsByReviewLevel(
            @Parameter(description = "Review level", required = true) @RequestParam ReviewLevelEnum level,
            @Parameter(description = "Pagination parameters") Pageable pageable) {
        return ResponseEntity.ok(councilService.getCouncilsByReviewLevel(level, pageable));
    }

    // 7. Get Active Councils
    @GetMapping("/councils/active")
    @ApiMessage("Lấy danh sách hội đồng đang hoạt động thành công")
    @Operation(summary = "Get Active Councils", description = "Get active councils with pagination")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Active councils retrieved successfully", content = @Content(schema = @Schema(implementation = ResultPaginationDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request data")
    })
    public ResponseEntity<ResultPaginationDTO> getActiveCouncils(
            @Parameter(description = "Pagination parameters") Pageable pageable) {
        return ResponseEntity.ok(councilService.getActiveCouncils(pageable));
    }

    // 8. Get All Active Councils (List)
    @GetMapping("/councils/active/list")
    @ApiMessage("Lấy danh sách tất cả hội đồng đang hoạt động thành công")
    @Operation(summary = "Get All Active Councils", description = "Get all active councils as list")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Active councils retrieved successfully", content = @Content(schema = @Schema(implementation = List.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request data")
    })
    public ResponseEntity<List<CouncilResponse>> getAllActiveCouncils() {
        return ResponseEntity.ok(councilService.getAllActiveCouncils());
    }

    // 9. Get Councils by Review Level (List)
    @GetMapping("/councils/review-level/list")
    @ApiMessage("Lấy danh sách hội đồng theo cấp độ đánh giá thành công")
    @Operation(summary = "Get Councils by Review Level", description = "Get councils by review level as list")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Councils retrieved successfully", content = @Content(schema = @Schema(implementation = List.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request data")
    })
    public ResponseEntity<List<CouncilResponse>> getActiveCouncilsByReviewLevel(
            @Parameter(description = "Review level", required = true) @RequestParam ReviewLevelEnum level) {
        return ResponseEntity.ok(councilService.getActiveCouncilsByReviewLevel(level));
    }

    // COUNCIL MEMBER MANAGEMENT

    // 10. Add Member to Council
    @PostMapping("/councils/{councilId}/members")
    @ApiMessage("Thêm thành viên vào hội đồng thành công")
    @Operation(summary = "Add Member to Council", description = "Add a member to council")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Member added successfully", content = @Content(schema = @Schema(implementation = CouncilMemberResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "404", description = "Council or user not found"),
            @ApiResponse(responseCode = "409", description = "User already a member")
    })
    public ResponseEntity<CouncilMemberResponse> addMemberToCouncil(
            @Parameter(description = "Council ID", required = true) @PathVariable String councilId,
            @Parameter(description = "Add member request", required = true) @Valid @RequestBody AddCouncilMemberRequest request) {
        return ResponseEntity.ok(councilService.addMemberToCouncil(councilId, request));
    }

    // 11. Remove Member from Council
    @DeleteMapping("/councils/{councilId}/members/{userId}")
    @ApiMessage("Xóa thành viên khỏi hội đồng thành công")
    @Operation(summary = "Remove Member from Council", description = "Remove a member from council")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Member removed successfully"),
            @ApiResponse(responseCode = "404", description = "Council or member not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<Void> removeMemberFromCouncil(
            @Parameter(description = "Council ID", required = true) @PathVariable String councilId,
            @Parameter(description = "User ID", required = true) @PathVariable String userId) {
        councilService.removeMemberFromCouncil(councilId, userId);
        return ResponseEntity.ok().build();
    }

    // 12. Get Council Members
    @GetMapping("/councils/{councilId}/members")
    @ApiMessage("Lấy danh sách thành viên hội đồng thành công")
    @Operation(summary = "Get Council Members", description = "Get council members with pagination")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Council members retrieved successfully", content = @Content(schema = @Schema(implementation = ResultPaginationDTO.class))),
            @ApiResponse(responseCode = "404", description = "Council not found"),
            @ApiResponse(responseCode = "400", description = "Invalid request data")
    })
    public ResponseEntity<ResultPaginationDTO> getCouncilMembers(
            @Parameter(description = "Council ID", required = true) @PathVariable String councilId,
            @Parameter(description = "Pagination parameters") Pageable pageable) {
        return ResponseEntity.ok(councilService.getCouncilMembers(councilId, pageable));
    }

    // 13. Get All Council Members (List)
    @GetMapping("/councils/{councilId}/members/list")
    @ApiMessage("Lấy danh sách tất cả thành viên hội đồng thành công")
    @Operation(summary = "Get All Council Members", description = "Get all council members as list")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Council members retrieved successfully", content = @Content(schema = @Schema(implementation = List.class))),
            @ApiResponse(responseCode = "404", description = "Council not found"),
            @ApiResponse(responseCode = "400", description = "Invalid request data")
    })
    public ResponseEntity<List<CouncilMemberResponse>> getAllCouncilMembers(
            @Parameter(description = "Council ID", required = true) @PathVariable String councilId) {
        return ResponseEntity.ok(councilService.getAllCouncilMembers(councilId));
    }

    // 14. Get User's Councils
    @GetMapping("/users/{userId}/councils")
    @ApiMessage("Lấy danh sách hội đồng của người dùng thành công")
    @Operation(summary = "Get User's Councils", description = "Get all councils where user is a member")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User councils retrieved successfully", content = @Content(schema = @Schema(implementation = List.class))),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "400", description = "Invalid request data")
    })
    public ResponseEntity<List<CouncilResponse>> getUserCouncils(
            @Parameter(description = "User ID", required = true) @PathVariable String userId) {
        return ResponseEntity.ok(councilService.getUserCouncils(userId));
    }
}
