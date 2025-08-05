package vn.edu.iuh.fit.innovationmanagementsystem_be.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.Attachment;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.AttachmentRequestDTO;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.PageRequestDTO;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.AttachmentResponseDTO;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.PageResponseDTO;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.AttachmentRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AttachmentService {

    private final AttachmentRepository attachmentRepository;

    // Create attachment
    public AttachmentResponseDTO createAttachment(AttachmentRequestDTO requestDTO, String pathUrl, String userId) {
        Attachment attachment = new Attachment();
        attachment.setInitiativeId(requestDTO.getInitiativeId());
        attachment.setPathUrl(pathUrl);
        attachment.setType(requestDTO.getType());
        attachment.setFileName(requestDTO.getFileName());
        attachment.setFileSize(requestDTO.getFileSize());
        attachment.setMimeType(requestDTO.getMimeType());
        attachment.setCreatedBy(userId);
        attachment.setUpdatedBy(userId);

        Attachment savedAttachment = attachmentRepository.save(attachment);
        return new AttachmentResponseDTO(savedAttachment);
    }

    // Get attachment by ID
    public Optional<AttachmentResponseDTO> getAttachmentById(UUID id) {
        return attachmentRepository.findById(id)
                .map(AttachmentResponseDTO::new);
    }

    // Get all attachments with pagination
    public PageResponseDTO<AttachmentResponseDTO> getAllAttachments(PageRequestDTO pageRequest) {
        Pageable pageable = createPageable(pageRequest);
        Page<Attachment> attachmentPage = attachmentRepository.findAll(pageable);

        List<AttachmentResponseDTO> content = attachmentPage.getContent().stream()
                .map(AttachmentResponseDTO::new)
                .toList();

        return new PageResponseDTO<>(content, pageRequest.getPage(), pageRequest.getSize(),
                attachmentPage.getTotalElements(), pageRequest.getSortBy(), pageRequest.getSortDirection());
    }

    // Get attachments by innovation ID with pagination
    public PageResponseDTO<AttachmentResponseDTO> getAttachmentsByInnovationId(UUID innovationId,
            PageRequestDTO pageRequest) {
        Pageable pageable = createPageable(pageRequest);
        Page<Attachment> attachmentPage = attachmentRepository.findByInitiativeId(innovationId, pageable);

        List<AttachmentResponseDTO> content = attachmentPage.getContent().stream()
                .map(AttachmentResponseDTO::new)
                .toList();

        return new PageResponseDTO<>(content, pageRequest.getPage(), pageRequest.getSize(),
                attachmentPage.getTotalElements(), pageRequest.getSortBy(), pageRequest.getSortDirection());
    }

    // Get attachments by type with pagination
    public PageResponseDTO<AttachmentResponseDTO> getAttachmentsByType(Attachment.AttachmentType type,
            PageRequestDTO pageRequest) {
        Pageable pageable = createPageable(pageRequest);
        Page<Attachment> attachmentPage = attachmentRepository.findByType(type, pageable);

        List<AttachmentResponseDTO> content = attachmentPage.getContent().stream()
                .map(AttachmentResponseDTO::new)
                .toList();

        return new PageResponseDTO<>(content, pageRequest.getPage(), pageRequest.getSize(),
                attachmentPage.getTotalElements(), pageRequest.getSortBy(), pageRequest.getSortDirection());
    }

    // Get attachments by innovation ID and type with pagination
    public PageResponseDTO<AttachmentResponseDTO> getAttachmentsByInnovationIdAndType(UUID innovationId,
            Attachment.AttachmentType type, PageRequestDTO pageRequest) {
        Pageable pageable = createPageable(pageRequest);
        Page<Attachment> attachmentPage = attachmentRepository.findByInitiativeIdAndType(innovationId, type, pageable);

        List<AttachmentResponseDTO> content = attachmentPage.getContent().stream()
                .map(AttachmentResponseDTO::new)
                .toList();

        return new PageResponseDTO<>(content, pageRequest.getPage(), pageRequest.getSize(),
                attachmentPage.getTotalElements(), pageRequest.getSortBy(), pageRequest.getSortDirection());
    }

    // Search attachments by file name with pagination
    public PageResponseDTO<AttachmentResponseDTO> findAttachmentsByFileName(String fileName,
            PageRequestDTO pageRequest) {
        Pageable pageable = createPageable(pageRequest);
        Page<Attachment> attachmentPage = attachmentRepository.findByFileNameContainingIgnoreCase(fileName, pageable);

        List<AttachmentResponseDTO> content = attachmentPage.getContent().stream()
                .map(AttachmentResponseDTO::new)
                .toList();

        return new PageResponseDTO<>(content, pageRequest.getPage(), pageRequest.getSize(),
                attachmentPage.getTotalElements(), pageRequest.getSortBy(), pageRequest.getSortDirection());
    }

    // Search attachments by MIME type with pagination
    public PageResponseDTO<AttachmentResponseDTO> findAttachmentsByMimeType(String mimeType,
            PageRequestDTO pageRequest) {
        Pageable pageable = createPageable(pageRequest);
        Page<Attachment> attachmentPage = attachmentRepository.findByMimeTypeContainingIgnoreCase(mimeType, pageable);

        List<AttachmentResponseDTO> content = attachmentPage.getContent().stream()
                .map(AttachmentResponseDTO::new)
                .toList();

        return new PageResponseDTO<>(content, pageRequest.getPage(), pageRequest.getSize(),
                attachmentPage.getTotalElements(), pageRequest.getSortBy(), pageRequest.getSortDirection());
    }

    // Update attachment
    public Optional<AttachmentResponseDTO> updateAttachment(UUID id, AttachmentRequestDTO requestDTO, String userId) {
        return attachmentRepository.findById(id)
                .map(attachment -> {
                    attachment.setInitiativeId(requestDTO.getInitiativeId());
                    attachment.setType(requestDTO.getType());
                    attachment.setFileName(requestDTO.getFileName());
                    attachment.setFileSize(requestDTO.getFileSize());
                    attachment.setMimeType(requestDTO.getMimeType());
                    attachment.setUpdatedBy(userId);
                    attachment.setUpdatedAt(LocalDateTime.now());

                    Attachment savedAttachment = attachmentRepository.save(attachment);
                    return new AttachmentResponseDTO(savedAttachment);
                });
    }

    // Delete attachment
    public boolean deleteAttachment(UUID id) {
        if (attachmentRepository.existsById(id)) {
            attachmentRepository.deleteById(id);
            return true;
        }
        return false;
    }

    // Count operations
    public long countAttachmentsByInnovationId(UUID innovationId) {
        return attachmentRepository.countByInitiativeId(innovationId);
    }

    public long countAttachmentsByType(Attachment.AttachmentType type) {
        return attachmentRepository.countByType(type);
    }

    // Helper method to create Pageable
    private Pageable createPageable(PageRequestDTO pageRequest) {
        Sort sort = Sort.by(
                pageRequest.getSortDirection().equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC,
                pageRequest.getSortBy());
        return PageRequest.of(pageRequest.getPage(), pageRequest.getSize(), sort);
    }
}