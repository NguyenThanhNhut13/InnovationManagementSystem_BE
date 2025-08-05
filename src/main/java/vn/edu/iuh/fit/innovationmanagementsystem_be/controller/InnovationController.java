package vn.edu.iuh.fit.innovationmanagementsystem_be.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.Innovation;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.InnovationRequestDTO;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.PageRequestDTO;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.InnovationResponseDTO;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.PageResponseDTO;

import vn.edu.iuh.fit.innovationmanagementsystem_be.service.InnovationService;
import vn.edu.iuh.fit.innovationmanagementsystem_be.utils.InnovationStatusUtils;

import java.util.UUID;

@RestController
@RequestMapping("/api/innovations")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class InnovationController {

        private final InnovationService innovationService;

        @PostMapping
        public ResponseEntity<InnovationResponseDTO> createInnovation(
                        @Valid @RequestBody InnovationRequestDTO requestDTO,
                        @RequestParam UUID userId) {
                InnovationResponseDTO createdInnovation = innovationService.createInnovation(requestDTO, userId);
                return ResponseEntity.status(201).body(createdInnovation);
        }

        @GetMapping("/{id}")
        public ResponseEntity<InnovationResponseDTO> getInnovationById(
                        @PathVariable UUID id) {
                return innovationService.getInnovationById(id)
                                .map(innovation -> ResponseEntity.ok(innovation))
                                .orElse(ResponseEntity.notFound().build());
        }

        @GetMapping
        public ResponseEntity<PageResponseDTO<InnovationResponseDTO>> getAllInnovations(
                        PageRequestDTO pageRequest) {
                PageResponseDTO<InnovationResponseDTO> result = innovationService.getAllInnovations(pageRequest);
                return ResponseEntity.ok(result);
        }

        @GetMapping("/user/{userId}")
        public ResponseEntity<PageResponseDTO<InnovationResponseDTO>> getInnovationsByUserId(
                        @PathVariable UUID userId,
                        PageRequestDTO pageRequest) {
                PageResponseDTO<InnovationResponseDTO> result = innovationService.getInnovationsByUserId(userId,
                                pageRequest);
                return ResponseEntity.ok(result);
        }

        @GetMapping("/department/{departmentId}")
        public ResponseEntity<PageResponseDTO<InnovationResponseDTO>> getInnovationsByDepartmentId(
                        @PathVariable UUID departmentId,
                        PageRequestDTO pageRequest) {
                PageResponseDTO<InnovationResponseDTO> result = innovationService.getInnovationsByDepartmentId(
                                departmentId,
                                pageRequest);
                return ResponseEntity.ok(result);
        }

        @GetMapping("/status/{status}")
        public ResponseEntity<PageResponseDTO<InnovationResponseDTO>> getInnovationsByStatus(
                        @PathVariable Innovation.InnovationStatus status,
                        PageRequestDTO pageRequest) {
                PageResponseDTO<InnovationResponseDTO> result = innovationService.getInnovationsByStatus(status,
                                pageRequest);
                return ResponseEntity.ok(result);
        }

        @GetMapping("/status-group/{statusGroup}")
        public ResponseEntity<PageResponseDTO<InnovationResponseDTO>> getInnovationsByStatusGroup(
                        @PathVariable String statusGroup,
                        PageRequestDTO pageRequest) {
                PageResponseDTO<InnovationResponseDTO> result = innovationService.getInnovationsByStatusGroup(
                                statusGroup,
                                pageRequest);
                return ResponseEntity.ok(result);
        }

        @GetMapping("/search")
        public ResponseEntity<PageResponseDTO<InnovationResponseDTO>> searchInnovationsByName(
                        @RequestParam String keyword,
                        PageRequestDTO pageRequest) {
                PageResponseDTO<InnovationResponseDTO> result = innovationService.searchInnovationsByName(keyword,
                                pageRequest);
                return ResponseEntity.ok(result);
        }

        @PutMapping("/{id}")
        public ResponseEntity<InnovationResponseDTO> updateInnovation(
                        @PathVariable UUID id,
                        @Valid @RequestBody InnovationRequestDTO requestDTO,
                        @RequestParam UUID userId) {
                return innovationService.updateInnovation(id, requestDTO, userId)
                                .map(innovation -> ResponseEntity.ok(innovation))
                                .orElse(ResponseEntity.notFound().build());
        }

        @PutMapping("/{id}/status")
        public ResponseEntity<InnovationResponseDTO> changeInnovationStatus(
                        @PathVariable UUID id,
                        @RequestParam Innovation.InnovationStatus newStatus,
                        @RequestParam String userId) {
                try {
                        return innovationService.changeInnovationStatus(id, newStatus, userId)
                                        .map(innovation -> ResponseEntity.ok(innovation))
                                        .orElse(ResponseEntity.notFound().build());
                } catch (IllegalArgumentException e) {
                        return ResponseEntity.badRequest().build();
                }
        }

        @GetMapping("/status-color/{status}")
        public ResponseEntity<String> getStatusColor(
                        @PathVariable Innovation.InnovationStatus status) {
                String color = InnovationStatusUtils.getStatusColor(status);
                return ResponseEntity.ok(color);
        }

        @GetMapping("/count/status/{status}")
        public ResponseEntity<Long> countInnovationsByStatus(
                        @PathVariable Innovation.InnovationStatus status) {
                long count = innovationService.countInnovationsByStatus(status);
                return ResponseEntity.ok(count);
        }

        @GetMapping("/count/user/{userId}")
        public ResponseEntity<Long> countInnovationsByUserId(
                        @PathVariable UUID userId) {
                long count = innovationService.countInnovationsByUserId(userId);
                return ResponseEntity.ok(count);
        }

        @GetMapping("/count/department/{departmentId}")
        public ResponseEntity<Long> countInnovationsByDepartmentId(
                        @PathVariable UUID departmentId) {
                long count = innovationService.countInnovationsByDepartmentId(departmentId);
                return ResponseEntity.ok(count);
        }
}