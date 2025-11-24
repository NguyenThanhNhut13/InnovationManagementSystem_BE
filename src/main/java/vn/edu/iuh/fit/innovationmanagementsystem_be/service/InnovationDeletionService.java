package vn.edu.iuh.fit.innovationmanagementsystem_be.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.Attachment;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.Innovation;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.InnovationStatusEnum;
import vn.edu.iuh.fit.innovationmanagementsystem_be.exception.IdInvalidException;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.AttachmentRepository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.CoInnovationRepository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.DigitalSignatureRepository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.FormDataRepository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.InnovationRepository;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@Transactional
public class InnovationDeletionService {

    private static final Logger logger = LoggerFactory.getLogger(InnovationDeletionService.class);

    private final InnovationRepository innovationRepository;
    private final AttachmentRepository attachmentRepository;
    private final FormDataRepository formDataRepository;
    private final CoInnovationRepository coInnovationRepository;
    private final DigitalSignatureRepository digitalSignatureRepository;
    private final FileService fileService;
    private final UserService userService;

    public InnovationDeletionService(
            InnovationRepository innovationRepository,
            AttachmentRepository attachmentRepository,
            FormDataRepository formDataRepository,
            CoInnovationRepository coInnovationRepository,
            DigitalSignatureRepository digitalSignatureRepository,
            FileService fileService,
            UserService userService) {
        this.innovationRepository = innovationRepository;
        this.attachmentRepository = attachmentRepository;
        this.formDataRepository = formDataRepository;
        this.coInnovationRepository = coInnovationRepository;
        this.digitalSignatureRepository = digitalSignatureRepository;
        this.fileService = fileService;
        this.userService = userService;
    }

    public void deleteMyDraftInnovation(String innovationId) {
        String currentUserId = userService.getCurrentUserId();

        Innovation innovation = innovationRepository.findById(innovationId)
                .orElseThrow(() -> new IdInvalidException(
                        "Không tìm thấy sáng kiến với ID: " + innovationId));

        if (innovation.getUser() == null || !innovation.getUser().getId().equals(currentUserId)) {
            logger.error("User {} không có quyền xóa sáng kiến {}", currentUserId, innovationId);
            throw new IdInvalidException("Bạn chỉ có thể xóa sáng kiến của chính mình");
        }

        if (innovation.getStatus() != InnovationStatusEnum.DRAFT) {
            logger.error("Innovation {} không ở trạng thái DRAFT. Trạng thái hiện tại: {}", innovationId,
                    innovation.getStatus());
            throw new IdInvalidException("Chỉ có thể xóa sáng kiến ở trạng thái DRAFT");
        }

        List<Attachment> attachments = attachmentRepository.findByInnovationId(innovationId);
        Set<String> deletedFileNames = new HashSet<>();

        for (Attachment attachment : attachments) {
            String pathUrl = attachment.getPathUrl();
            if (pathUrl == null || pathUrl.isBlank()) {
                continue;
            }
            String displayName = attachment.getOriginalFileName();
            if (displayName == null || displayName.isBlank()) {
                displayName = attachment.getFileName();
            }
            deleteFileSafely(pathUrl, innovationId, displayName, deletedFileNames);
        }

        formDataRepository.deleteByInnovationId(innovationId);
        coInnovationRepository.deleteByInnovationId(innovationId);
        digitalSignatureRepository.deleteByInnovationId(innovationId);
        innovationRepository.delete(innovation);
    }

    private void deleteFileSafely(String fileName, String innovationId, String displayName,
            Set<String> deletedFileNames) {
        if (fileName == null || fileName.isBlank()) {
            return;
        }

        if (deletedFileNames.contains(fileName)) {
            return;
        }

        try {
            fileService.deleteFile(fileName);
            deletedFileNames.add(fileName);
        } catch (Exception e) {
            logger.error("Không thể xóa file {} của innovation {}: {}", fileName, innovationId,
                    e.getMessage());
            String finalDisplayName = (displayName != null && !displayName.isBlank()) ? displayName
                    : fileName;
            throw new IdInvalidException("Không thể xóa tệp đính kèm: " + finalDisplayName);
        }
    }
}
