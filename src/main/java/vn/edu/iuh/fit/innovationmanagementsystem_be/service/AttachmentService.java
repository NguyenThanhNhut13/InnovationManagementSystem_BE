package vn.edu.iuh.fit.innovationmanagementsystem_be.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.Attachment;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.Innovation;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.AttachmentTypeEnum;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.AttachmentRequest;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.UpdateAttachmentRequest;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.AttachmentResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.exception.IdInvalidException;
import vn.edu.iuh.fit.innovationmanagementsystem_be.mapper.AttachmentMapper;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.AttachmentRepository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.InnovationRepository;

@Service
@Transactional
public class AttachmentService {

    private final AttachmentRepository attachmentRepository;
    private final InnovationRepository innovationRepository;
    private final AttachmentMapper attachmentMapper;

    public AttachmentService(AttachmentRepository attachmentRepository,
            InnovationRepository innovationRepository,
            AttachmentMapper attachmentMapper) {
        this.attachmentRepository = attachmentRepository;
        this.innovationRepository = innovationRepository;
        this.attachmentMapper = attachmentMapper;
    }

    // 1. Tạo mới tệp đính kèm cho sáng kiến
    public AttachmentResponse createAttachment(AttachmentRequest request) {
        Innovation innovation = innovationRepository.findById(request.getInnovationId())
                .orElseThrow(() -> new IdInvalidException(
                        "Không tìm thấy sáng kiến với ID: " + request.getInnovationId()));

        Attachment attachment = attachmentMapper.toAttachment(request);
        attachment.setInnovation(innovation);

        attachment = attachmentRepository.save(attachment);
        return attachmentMapper.toAttachmentResponse(attachment);
    }

    // 2. Lấy chi tiết tệp đính kèm theo ID
    public AttachmentResponse getAttachment(String id) {
        Attachment attachment = findAttachmentById(id);
        return attachmentMapper.toAttachmentResponse(attachment);
    }

    // 3. Lấy danh sách tệp theo sáng kiến và tùy chọn loại tệp
    public List<AttachmentResponse> getAttachmentsByInnovation(String innovationId, AttachmentTypeEnum type) {
        if (innovationId == null || innovationId.isBlank()) {
            throw new IdInvalidException("Innovation ID không được để trống");
        }

        innovationRepository.findById(innovationId)
                .orElseThrow(() -> new IdInvalidException(
                        "Không tìm thấy sáng kiến với ID: " + innovationId));

        List<Attachment> attachments = type != null
                ? attachmentRepository.findByInnovationIdAndType(innovationId, type)
                : attachmentRepository.findByInnovationId(innovationId);

        return attachments.stream()
                .map(attachmentMapper::toAttachmentResponse)
                .toList();
    }

    // 4. Cập nhật thông tin tệp đính kèm
    public AttachmentResponse updateAttachment(String id, UpdateAttachmentRequest request) {
        Attachment attachment = findAttachmentById(id);
        attachmentMapper.updateAttachmentFromRequest(request, attachment);
        attachment = attachmentRepository.save(attachment);
        return attachmentMapper.toAttachmentResponse(attachment);
    }

    // 5. Xóa tệp đính kèm
    public void deleteAttachment(String id) {
        Attachment attachment = findAttachmentById(id);
        attachmentRepository.delete(attachment);
    }

    private Attachment findAttachmentById(String id) {
        return attachmentRepository.findById(id)
                .orElseThrow(() -> new IdInvalidException(
                        "Không tìm thấy tệp đính kèm với ID: " + id));
    }
}
