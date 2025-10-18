package vn.edu.iuh.fit.innovationmanagementsystem_be.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.Innovation;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.InnovationPhase;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.User;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.InnovationStatusEnum;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.FormDataRequest;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.UpdateFormDataRequest;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.InnovationFormDataRequest;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.FormDataResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.InnovationFormDataResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.InnovationResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.InnovationStatisticsDTO;
import vn.edu.iuh.fit.innovationmanagementsystem_be.exception.IdInvalidException;
import vn.edu.iuh.fit.innovationmanagementsystem_be.mapper.InnovationMapper;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.InnovationRepository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.InnovationPhaseRepository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.utils.ResultPaginationDTO;
import vn.edu.iuh.fit.innovationmanagementsystem_be.utils.Utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.Arrays;

@Service
@Transactional
public class InnovationService {

    private final InnovationRepository innovationRepository;
    private final InnovationPhaseRepository innovationPhaseRepository;
    private final FormDataService formDataService;
    private final InnovationMapper innovationMapper;
    private final UserService userService;
    private final DigitalSignatureService digitalSignatureService;

    public InnovationService(InnovationRepository innovationRepository,
            InnovationPhaseRepository innovationPhaseRepository,
            FormDataService formDataService,
            InnovationMapper innovationMapper,
            UserService userService,
            DigitalSignatureService digitalSignatureService) {
        this.innovationRepository = innovationRepository;
        this.innovationPhaseRepository = innovationPhaseRepository;
        this.formDataService = formDataService;
        this.innovationMapper = innovationMapper;
        this.userService = userService;
        this.digitalSignatureService = digitalSignatureService;
    }

    // 1. Lấy tất cả sáng kiến
    public ResultPaginationDTO getAllInnovations(Specification<Innovation> specification, Pageable pageable) {

        if (pageable.getSort().isUnsorted()) {
            pageable = org.springframework.data.domain.PageRequest.of(
                    pageable.getPageNumber(),
                    pageable.getPageSize(),
                    org.springframework.data.domain.Sort.by("createdAt").descending());
        }

        Page<Innovation> innovations = innovationRepository.findAll(specification, pageable);
        Page<InnovationResponse> responses = innovations.map(innovationMapper::toInnovationResponse);
        return Utils.toResultPaginationDTO(responses, pageable);
    }

    // 2. Lấy sáng kiến bởi ID
    public InnovationResponse getInnovationById(String id) {
        Innovation innovation = innovationRepository.findById(id)
                .orElseThrow(() -> new IdInvalidException("Không tìm thấy sáng kiến với ID: " + id));
        return innovationMapper.toInnovationResponse(innovation);
    }

    // 3. Tạo sáng kiến & Submit Form Data (Tạo sáng kiến tự động khi điền form)
    public InnovationFormDataResponse createInnovationAndSubmitFormData(InnovationFormDataRequest request) {

        String actionType = request.getActionType() != null ? request.getActionType().toUpperCase() : "DRAFT";
        if (!InnovationStatusEnum.DRAFT.name().equals(actionType)
                && !InnovationStatusEnum.SUBMITTED.name().equals(actionType)) {
            throw new IdInvalidException(
                    "Action type chỉ được là DRAFT hoặc SUBMITTED. Các trạng thái khác sẽ được xử lý bởi hội đồng chấm điểm.");
        }

        InnovationPhase innovationPhase = innovationPhaseRepository.findById(request.getInnovationPhaseId())
                .orElseThrow(() -> new IdInvalidException(
                        "Không tìm thấy giai đoạn sáng kiến với ID: " + request.getInnovationPhaseId()));

        User currentUser = userService.getCurrentUser();

        Innovation innovation = new Innovation();
        innovation.setInnovationName(request.getInnovationName());
        innovation.setUser(currentUser);
        innovation.setDepartment(currentUser.getDepartment());
        innovation.setInnovationPhase(innovationPhase);
        innovation.setIsScore(request.getIsScore() != null ? request.getIsScore() : false);

        if (InnovationStatusEnum.SUBMITTED.name().equals(actionType)) {
            innovation.setStatus(InnovationStatusEnum.DRAFT);
        } else {
            innovation.setStatus(InnovationStatusEnum.DRAFT);
        }

        Innovation savedInnovation = innovationRepository.save(innovation);

        List<FormDataResponse> formDataResponses = request.getFormDataItems().stream()
                .map(item -> {
                    FormDataRequest createRequest = new FormDataRequest();
                    createRequest.setFieldValue(item.getFieldValue());
                    createRequest.setFormFieldId(item.getFormFieldId());
                    createRequest.setInnovationId(savedInnovation.getId());
                    return formDataService.createFormData(createRequest);
                })
                .collect(Collectors.toList());

        // Tạo documentHash từ dữ liệu form
        String documentHash = generateDocumentHash(request.getFormDataItems(), request.getTemplateId());

        InnovationFormDataResponse response = new InnovationFormDataResponse();
        response.setInnovation(innovationMapper.toInnovationResponse(savedInnovation));
        response.setFormDataList(formDataResponses);
        response.setDocumentHash(documentHash);

        return response;
    }

    // 4. Cập nhật FormData sáng kiến (Cập nhật FormData cho sáng kiến đã tồn tại)
    public InnovationFormDataResponse updateInnovationFormData(String innovationId, InnovationFormDataRequest request) {

        String actionType = request.getActionType() != null ? request.getActionType().toUpperCase() : "DRAFT";
        if (!"DRAFT".equals(actionType) && !"SUBMITTED".equals(actionType)) {
            throw new IdInvalidException(
                    "Action type chỉ được là DRAFT hoặc SUBMITTED. Các trạng thái khác sẽ được xử lý bởi hội đồng chấm điểm.");
        }

        Innovation innovation = innovationRepository.findById(innovationId)
                .orElseThrow(() -> new IdInvalidException("Không tìm thấy sáng kiến với ID: " + innovationId));

        if (!userService.isOwnerOfInnovation(innovation.getUser().getId())) {
            throw new IdInvalidException("Bạn không có quyền chỉnh sửa sáng kiến này");
        }

        // Chỉ cho phép chỉnh sửa khi ở trạng thái DRAFT)
        if (innovation.getStatus() != InnovationStatusEnum.DRAFT) {
            throw new IdInvalidException(
                    "Chỉ có thể chỉnh sửa sáng kiến ở trạng thái DRAFT. Sáng kiến hiện tại đang ở trạng thái: "
                            + innovation.getStatus());
        }

        // Xử lý các mục form data (cập nhật tồn tại hoặc tạo mới)
        List<FormDataResponse> formDataResponses = request.getFormDataItems().stream()
                .<FormDataResponse>map(item -> {
                    if (item.getDataId() != null && !item.getDataId().trim().isEmpty()) {
                        // Update existing form data
                        UpdateFormDataRequest updateRequest = new UpdateFormDataRequest();
                        updateRequest.setFieldValue(item.getFieldValue());
                        updateRequest.setFormFieldId(item.getFormFieldId());
                        updateRequest.setInnovationId(innovationId);
                        return formDataService.updateFormData(item.getDataId(), updateRequest);
                    } else {
                        // Create new form data
                        FormDataRequest createRequest = new FormDataRequest();
                        createRequest.setFieldValue(item.getFieldValue());
                        createRequest.setFormFieldId(item.getFormFieldId());
                        createRequest.setInnovationId(innovationId);
                        return formDataService.createFormData(createRequest);
                    }
                })
                .collect(Collectors.toList());

        // Cập nhật trạng thái sáng kiến nếu SUBMITTED
        if (InnovationStatusEnum.SUBMITTED.name().equals(actionType)) {
            // Kiểm tra xem đã điền đủ cả 2 mẫu chưa
            if (!hasCompletedBothTemplates(innovationId)) {
                throw new IdInvalidException(
                        "Chỉ có thể SUBMITTED khi đã điền xong cả 2 mẫu form. Vui lòng hoàn thành mẫu còn lại trước khi nộp.");
            }

            // Kiểm tra xem cả 2 mẫu đã được ký đủ chưa
            if (!digitalSignatureService.isBothFormsFullySigned(innovationId)) {
                throw new IdInvalidException(
                        "Chỉ có thể SUBMITTED khi cả 2 mẫu đã được ký đủ. Vui lòng hoàn thành chữ ký số cho các mẫu còn lại.");
            }

            innovation.setStatus(InnovationStatusEnum.SUBMITTED);
            innovationRepository.save(innovation);
        }

        // Tạo documentHash từ dữ liệu form
        String documentHash = generateDocumentHash(request.getFormDataItems(), request.getTemplateId());

        InnovationFormDataResponse response = new InnovationFormDataResponse();
        response.setInnovation(innovationMapper.toInnovationResponse(innovation));
        response.setFormDataList(formDataResponses);
        response.setDocumentHash(documentHash);

        return response;
    }

    // 5. Lấy FormData sáng kiến
    public InnovationFormDataResponse getInnovationFormData(String innovationId, String templateId) {

        Innovation innovation = innovationRepository.findById(innovationId)
                .orElseThrow(() -> new IdInvalidException("Không tìm thấy sáng kiến với ID: " + innovationId));

        if (!userService.isOwnerOfInnovation(innovation.getUser().getId())) {
            throw new IdInvalidException("Bạn không có quyền xem thông tin sáng kiến này");
        }

        List<FormDataResponse> formDataList;
        if (templateId != null) {
            formDataList = formDataService.getFormDataWithFormFields(innovationId, templateId);
        } else {
            formDataList = formDataService.getFormDataByInnovationId(innovationId);
        }

        // Tạo documentHash từ dữ liệu form hiện tại
        String documentHash = generateDocumentHashFromFormData(formDataList, templateId);

        InnovationFormDataResponse response = new InnovationFormDataResponse();
        response.setInnovation(innovationMapper.toInnovationResponse(innovation));
        response.setFormDataList(formDataList);
        response.setDocumentHash(documentHash);

        return response;
    }

    // 6. Lấy sáng kiến bởi User và Status
    public ResultPaginationDTO getInnovationsByUserAndStatus(String status, Pageable pageable) {
        if (status == null || status.trim().isEmpty()) {
            throw new IdInvalidException("Status không được để trống");
        }

        if (pageable.getSort().isUnsorted()) {
            pageable = org.springframework.data.domain.PageRequest.of(
                    pageable.getPageNumber(),
                    pageable.getPageSize(),
                    org.springframework.data.domain.Sort.by("createdAt").descending());
        }

        try {
            InnovationStatusEnum statusEnum = InnovationStatusEnum.valueOf(status.toUpperCase());
            String currentUserId = userService.getCurrentUserId();
            Page<Innovation> innovations = innovationRepository.findByUserIdAndStatus(currentUserId, statusEnum,
                    pageable);
            Page<InnovationResponse> responses = innovations.map(innovationMapper::toInnovationResponse);
            return Utils.toResultPaginationDTO(responses, pageable);
        } catch (IllegalArgumentException e) {
            throw new IdInvalidException("Status không hợp lệ: " + status + ". Các status hợp lệ: " +
                    java.util.Arrays.toString(InnovationStatusEnum.values()));
        }
    }

    // 7. Thống kê innovation cho giảng viên
    public InnovationStatisticsDTO getInnovationStatisticsForCurrentUser() {
        User currentUser = userService.getCurrentUser();
        String userId = currentUser.getId();

        // Thống kê cơ bản
        long totalInnovations = innovationRepository.countByUserId(userId);
        long submittedInnovations = innovationRepository.countByUserIdAndStatus(userId, InnovationStatusEnum.SUBMITTED);
        long approvedInnovations = innovationRepository.countByUserIdAndStatus(userId,
                InnovationStatusEnum.TRUONG_APPROVED);
        List<InnovationStatusEnum> rejectedStatuses = Arrays.asList(
                InnovationStatusEnum.KHOA_REJECTED,
                InnovationStatusEnum.TRUONG_REJECTED);
        long rejectedInnovations = innovationRepository.countByUserIdAndStatusIn(userId, rejectedStatuses);

        // Thống kê phần trăm kết quả sáng kiến đã nộp
        long pendingCount = innovationRepository.countPendingInnovationsByUserId(userId);

        // Tính phần trăm dựa trên tổng số sáng kiến
        double achievedPercentage = totalInnovations > 0 ? (double) approvedInnovations / totalInnovations * 100 : 0.0;
        double notAchievedPercentage = totalInnovations > 0 ? (double) rejectedInnovations / totalInnovations * 100
                : 0.0;
        double pendingPercentage = totalInnovations > 0 ? (double) pendingCount / totalInnovations * 100 : 0.0;

        return InnovationStatisticsDTO.builder()
                .totalInnovations(totalInnovations)
                .submittedInnovations(submittedInnovations)
                .approvedInnovations(approvedInnovations)
                .rejectedInnovations(rejectedInnovations)
                .pendingCount(pendingCount)
                .achievedPercentage(Math.round(achievedPercentage * 100.0) / 100.0)
                .notAchievedPercentage(Math.round(notAchievedPercentage * 100.0) / 100.0)
                .pendingPercentage(Math.round(pendingPercentage * 100.0) / 100.0)
                .build();
    }

    /*
     * Helper method: Kiểm tra xem innovation đã có form data cho cả 2 template chưa
     */
    private boolean hasCompletedBothTemplates(String innovationId) {
        // Lấy tất cả form data của innovation
        List<FormDataResponse> allFormData = formDataService.getFormDataByInnovationId(innovationId);

        if (allFormData.isEmpty()) {
            return false;
        }

        // Lấy danh sách các template ID đã có form data
        Set<String> completedTemplateIds = allFormData.stream()
                .map(FormDataResponse::getTemplateId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        // Kiểm tra xem có ít nhất 2 template khác nhau không
        return completedTemplateIds.size() >= 2;
    }

    /*
     * Helper method: Tạo documentHash từ dữ liệu form
     */
    private String generateDocumentHash(List<InnovationFormDataRequest.FormDataItemRequest> formDataItems,
            String templateId) {
        try {
            // Tạo chuỗi dữ liệu để hash
            StringBuilder dataBuilder = new StringBuilder();
            dataBuilder.append("templateId:").append(templateId).append("|");

            // Sắp xếp formDataItems theo formFieldId để đảm bảo tính nhất quán
            formDataItems.stream()
                    .sorted((a, b) -> a.getFormFieldId().compareTo(b.getFormFieldId()))
                    .forEach(item -> {
                        dataBuilder.append("fieldId:").append(item.getFormFieldId())
                                .append("|value:").append(item.getFieldValue()).append("|");
                    });

            String dataString = dataBuilder.toString();

            // Tạo SHA-256 hash
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(dataString.getBytes());
            String hashString = Base64.getEncoder().encodeToString(hash);

            return "sha256:" + hashString;
        } catch (NoSuchAlgorithmException e) {
            throw new IdInvalidException("Không thể tạo document hash: " + e.getMessage(), e);
        }
    }

    /*
     * Helper method: Tạo documentHash từ FormDataResponse
     */
    private String generateDocumentHashFromFormData(List<FormDataResponse> formDataList, String templateId) {
        try {
            // Tạo chuỗi dữ liệu để hash
            StringBuilder dataBuilder = new StringBuilder();
            dataBuilder.append("templateId:").append(templateId).append("|");

            // Sắp xếp formDataList theo formFieldId để đảm bảo tính nhất quán
            formDataList.stream()
                    .sorted((a, b) -> a.getFormFieldId().compareTo(b.getFormFieldId()))
                    .forEach(item -> {
                        dataBuilder.append("fieldId:").append(item.getFormFieldId())
                                .append("|value:").append(item.getFieldValue()).append("|");
                    });

            String dataString = dataBuilder.toString();

            // Tạo SHA-256 hash
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(dataString.getBytes());
            String hashString = Base64.getEncoder().encodeToString(hash);

            return "sha256:" + hashString;
        } catch (NoSuchAlgorithmException e) {
            throw new IdInvalidException("Không thể tạo document hash: " + e.getMessage(), e);
        }
    }

}
