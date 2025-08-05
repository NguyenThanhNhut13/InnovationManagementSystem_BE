package vn.edu.iuh.fit.innovationmanagementsystem_be.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.Attachment;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.AttachmentRequestDTO;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.PageRequestDTO;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.AttachmentResponseDTO;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.PageResponseDTO;
import vn.edu.iuh.fit.innovationmanagementsystem_be.service.AttachmentService;

import java.util.UUID;

@RestController
@RequestMapping("/api/attachments")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AttachmentController {

        private final AttachmentService attachmentService;

        @PostMapping
        public ResponseEntity<AttachmentResponseDTO> createAttachment(
                        @Valid @RequestBody AttachmentRequestDTO requestDTO,
                        @RequestParam String pathUrl,
                        @RequestParam String userId) {
                AttachmentResponseDTO createdAttachment = attachmentService.createAttachment(requestDTO, pathUrl,
                                userId);
                return ResponseEntity.status(201).body(createdAttachment);
        }

        @GetMapping("/{id}")
        public ResponseEntity<AttachmentResponseDTO> getAttachmentById(
                        @PathVariable UUID id) {
                return attachmentService.getAttachmentById(id)
                                .map(attachment -> ResponseEntity.ok(attachment))
                                .orElse(ResponseEntity.notFound().build());
        }

        @GetMapping
        public ResponseEntity<PageResponseDTO<AttachmentResponseDTO>> getAllAttachments(
                        PageRequestDTO pageRequest) {
                PageResponseDTO<AttachmentResponseDTO> result = attachmentService.getAllAttachments(pageRequest);
                return ResponseEntity.ok(result);
        }

        @GetMapping("/innovation/{innovationId}")
        public ResponseEntity<PageResponseDTO<AttachmentResponseDTO>> getAttachmentsByInnovationId(
                        @PathVariable UUID innovationId,
                        PageRequestDTO pageRequest) {
                PageResponseDTO<AttachmentResponseDTO> result = attachmentService.getAttachmentsByInnovationId(
                                innovationId,
                                pageRequest);
                return ResponseEntity.ok(result);
        }

        @GetMapping("/type/{type}")
        public ResponseEntity<PageResponseDTO<AttachmentResponseDTO>> getAttachmentsByType(
                        @PathVariable Attachment.AttachmentType type,
                        PageRequestDTO pageRequest) {
                PageResponseDTO<AttachmentResponseDTO> result = attachmentService.getAttachmentsByType(type,
                                pageRequest);
                return ResponseEntity.ok(result);
        }

        @GetMapping("/innovation/{innovationId}/type/{type}")
        public ResponseEntity<PageResponseDTO<AttachmentResponseDTO>> getAttachmentsByInnovationIdAndType(
                        @PathVariable UUID innovationId,
                        @PathVariable Attachment.AttachmentType type,
                        PageRequestDTO pageRequest) {
                PageResponseDTO<AttachmentResponseDTO> result = attachmentService.getAttachmentsByInnovationIdAndType(
                                innovationId, type, pageRequest);
                return ResponseEntity.ok(result);
        }

        @GetMapping("/search/filename")
        public ResponseEntity<PageResponseDTO<AttachmentResponseDTO>> findAttachmentsByFileName(
                        @RequestParam String fileName,
                        PageRequestDTO pageRequest) {
                PageResponseDTO<AttachmentResponseDTO> result = attachmentService.findAttachmentsByFileName(fileName,
                                pageRequest);
                return ResponseEntity.ok(result);
        }

        @GetMapping("/search/mimetype")
        public ResponseEntity<PageResponseDTO<AttachmentResponseDTO>> findAttachmentsByMimeType(
                        @RequestParam String mimeType,
                        PageRequestDTO pageRequest) {
                PageResponseDTO<AttachmentResponseDTO> result = attachmentService.findAttachmentsByMimeType(mimeType,
                                pageRequest);
                return ResponseEntity.ok(result);
        }

        @PutMapping("/{id}")
        public ResponseEntity<AttachmentResponseDTO> updateAttachment(
                        @PathVariable UUID id,
                        @Valid @RequestBody AttachmentRequestDTO requestDTO,
                        @RequestParam String userId) {
                return attachmentService.updateAttachment(id, requestDTO, userId)
                                .map(attachment -> ResponseEntity.ok(attachment))
                                .orElse(ResponseEntity.notFound().build());
        }

        @DeleteMapping("/{id}")
        public ResponseEntity<Void> deleteAttachment(
                        @PathVariable UUID id) {
                boolean deleted = attachmentService.deleteAttachment(id);
                if (deleted) {
                        return ResponseEntity.ok().build();
                } else {
                        return ResponseEntity.notFound().build();
                }
        }

        @GetMapping("/count/innovation/{innovationId}")
        public ResponseEntity<Long> countAttachmentsByInnovationId(
                        @PathVariable UUID innovationId) {
                long count = attachmentService.countAttachmentsByInnovationId(innovationId);
                return ResponseEntity.ok(count);
        }

        @GetMapping("/count/type/{type}")
        public ResponseEntity<Long> countAttachmentsByType(
                        @PathVariable Attachment.AttachmentType type) {
                long count = attachmentService.countAttachmentsByType(type);
                return ResponseEntity.ok(count);
        }
}