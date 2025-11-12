package vn.edu.iuh.fit.innovationmanagementsystem_be.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.UserSignatureProfileResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.service.UserSignatureProfileService;
import vn.edu.iuh.fit.innovationmanagementsystem_be.utils.annotation.ApiMessage;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "User Signature Profile", description = "User signature profile management APIs")
@SecurityRequirement(name = "Bearer Authentication")
public class UserSignatureProfileController {

    private final UserSignatureProfileService userSignatureProfileService;

    public UserSignatureProfileController(UserSignatureProfileService userSignatureProfileService) {
        this.userSignatureProfileService = userSignatureProfileService;
    }

    // 1. Upload ảnh chữ ký và cập nhật pathUrl của UserSignatureProfile
    @PostMapping(value = "/signature-profile/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ApiMessage("Upload ảnh chữ ký và cập nhật path URL thành công")
    @Operation(summary = "Upload Signature Image", description = "Upload signature image to MinIO and update path URL of signature profile for current authenticated user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Signature image uploaded and path URL updated successfully", content = @Content(schema = @Schema(implementation = UserSignatureProfileResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid file or file is not an image"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Signature profile not found")
    })
    public ResponseEntity<UserSignatureProfileResponse> uploadSignatureImage(
            @Parameter(description = "Signature image file to upload", required = true) @RequestParam("file") MultipartFile file) {
        UserSignatureProfileResponse updatedProfile = userSignatureProfileService
                .uploadSignatureImageForCurrentUser(file);
        return ResponseEntity.ok(updatedProfile);
    }

    // 2. Lấy thông tin chữ ký của user hiện tại
    @GetMapping("/signature-profile/current")
    @ApiMessage("Lấy thông tin chữ ký thành công")
    @Operation(summary = "Get Current User Signature Profile", description = "Get signature profile information of current authenticated user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Signature profile retrieved successfully", content = @Content(schema = @Schema(implementation = UserSignatureProfileResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Signature profile not found")
    })
    public ResponseEntity<UserSignatureProfileResponse> getCurrentUserSignatureProfile() {
        UserSignatureProfileResponse profile = userSignatureProfileService.getCurrentUserSignatureProfile();
        return ResponseEntity.ok(profile);
    }

    // 3. Xóa một chữ ký cụ thể theo index
    @DeleteMapping("/signature-profile/{index}")
    @ApiMessage("Xóa ảnh chữ ký thành công")
    @Operation(summary = "Delete Signature Image by Index", description = "Delete a specific signature image by index from current authenticated user's signature profile")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Signature image deleted successfully", content = @Content(schema = @Schema(implementation = UserSignatureProfileResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid index"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Signature profile not found")
    })
    public ResponseEntity<UserSignatureProfileResponse> deleteSignatureImageByIndex(
            @Parameter(description = "Index of signature image to delete (0-based)", required = true) @PathVariable int index) {
        UserSignatureProfileResponse updatedProfile = userSignatureProfileService.deleteSignatureImageByIndex(index);
        return ResponseEntity.ok(updatedProfile);
    }

}
