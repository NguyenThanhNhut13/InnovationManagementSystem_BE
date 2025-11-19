package vn.edu.iuh.fit.innovationmanagementsystem_be.controller;

import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.AttachmentTypeEnum;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.AttachmentRequest;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.UpdateAttachmentRequest;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.AttachmentResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.service.AttachmentService;
import vn.edu.iuh.fit.innovationmanagementsystem_be.utils.annotation.ApiMessage;

@RestController
@RequestMapping("/api/v1/attachments")
@Tag(name = "Attachment", description = "Attachment management APIs")
@SecurityRequirement(name = "Bearer Authentication")
public class AttachmentController {

    private final AttachmentService attachmentService;

    public AttachmentController(AttachmentService attachmentService) {
        this.attachmentService = attachmentService;
    }

    // 1. Tạo mới tệp đính kèm
    @PostMapping
    @PreAuthorize("hasAnyRole('GIANG_VIEN', 'TRUONG_KHOA', 'QUAN_TRI_VIEN_HE_THONG', 'QUAN_TRI_VIEN_KHOA')")
    @ApiMessage("Tạo tệp đính kèm thành công")
    @Operation(summary = "Create Attachment", description = "Tạo tệp đính kèm mới cho sáng kiến")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Attachment created successfully", content = @Content(schema = @Schema(implementation = AttachmentResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<AttachmentResponse> createAttachment(
            @Parameter(description = "Attachment payload", required = true) @Valid @RequestBody AttachmentRequest request) {
        return ResponseEntity.ok(attachmentService.createAttachment(request));
    }

    // 2. Lấy chi tiết tệp đính kèm
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('GIANG_VIEN', 'TRUONG_KHOA', 'QUAN_TRI_VIEN_HE_THONG', 'QUAN_TRI_VIEN_KHOA')")
    @ApiMessage("Lấy thông tin tệp đính kèm thành công")
    @Operation(summary = "Get Attachment Detail", description = "Lấy thông tin chi tiết của tệp đính kèm theo ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Attachment detail retrieved successfully", content = @Content(schema = @Schema(implementation = AttachmentResponse.class))),
            @ApiResponse(responseCode = "404", description = "Attachment not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<AttachmentResponse> getAttachment(
            @Parameter(description = "Attachment ID", required = true) @PathVariable String id) {
        return ResponseEntity.ok(attachmentService.getAttachment(id));
    }

    // 3. Lấy danh sách tệp theo sáng kiến
    @GetMapping("/innovation/{innovationId}")
    @PreAuthorize("hasAnyRole('GIANG_VIEN', 'TRUONG_KHOA', 'QUAN_TRI_VIEN_HE_THONG', 'QUAN_TRI_VIEN_KHOA')")
    @ApiMessage("Lấy danh sách tệp đính kèm thành công")
    @Operation(summary = "Get Attachments by Innovation", description = "Lấy danh sách tệp đính kèm của sáng kiến theo ID và lọc theo loại nếu cần")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Attachments retrieved successfully", content = @Content(schema = @Schema(implementation = AttachmentResponse[].class))),
            @ApiResponse(responseCode = "404", description = "Innovation not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<List<AttachmentResponse>> getAttachmentsByInnovation(
            @Parameter(description = "Innovation ID", required = true) @PathVariable String innovationId,
            @Parameter(description = "Attachment type filter") @RequestParam(name = "type", required = false) AttachmentTypeEnum type) {
        return ResponseEntity.ok(attachmentService.getAttachmentsByInnovation(innovationId, type));
    }

    // 4. Cập nhật tệp đính kèm
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('GIANG_VIEN', 'TRUONG_KHOA', 'QUAN_TRI_VIEN_HE_THONG', 'QUAN_TRI_VIEN_KHOA')")
    @ApiMessage("Cập nhật tệp đính kèm thành công")
    @Operation(summary = "Update Attachment", description = "Cập nhật thông tin của tệp đính kèm")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Attachment updated successfully", content = @Content(schema = @Schema(implementation = AttachmentResponse.class))),
            @ApiResponse(responseCode = "404", description = "Attachment not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<AttachmentResponse> updateAttachment(
            @Parameter(description = "Attachment ID", required = true) @PathVariable String id,
            @Parameter(description = "Attachment update payload", required = true) @Valid @RequestBody UpdateAttachmentRequest request) {
        return ResponseEntity.ok(attachmentService.updateAttachment(id, request));
    }

    // 5. Xóa tệp đính kèm
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('GIANG_VIEN', 'TRUONG_KHOA', 'QUAN_TRI_VIEN_HE_THONG', 'QUAN_TRI_VIEN_KHOA')")
    @ApiMessage("Xóa tệp đính kèm thành công")
    @Operation(summary = "Delete Attachment", description = "Xóa tệp đính kèm theo ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Attachment deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Attachment not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<Void> deleteAttachment(
            @Parameter(description = "Attachment ID", required = true) @PathVariable String id) {
        attachmentService.deleteAttachment(id);
        return ResponseEntity.ok().build();
    }
}
