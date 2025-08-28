package vn.edu.iuh.fit.innovationmanagementsystem_be.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.dto.requestDTO.FormDataRequest;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.dto.responseDTO.FormDataResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.FormData;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.FormField;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.Innovation;
import vn.edu.iuh.fit.innovationmanagementsystem_be.exception.IdInvalidException;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.FormDataRepository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.FormFieldRepository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.InnovationRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class FormDataService {

    private final FormDataRepository formDataRepository;
    private final FormFieldRepository formFieldRepository;
    private final InnovationRepository innovationRepository;

    public FormDataService(FormDataRepository formDataRepository,
            FormFieldRepository formFieldRepository,
            InnovationRepository innovationRepository) {
        this.formDataRepository = formDataRepository;
        this.formFieldRepository = formFieldRepository;
        this.innovationRepository = innovationRepository;
    }

    // 1. Create FormData
    public FormDataResponse createFormData(FormDataRequest request) {
        // 1.1. Validate request
        if (request == null || request.getInnovationId() == null || request.getFormFieldId() == null) {
            throw new IllegalArgumentException("Innovation ID và Form Field ID không được để trống");
        }

        // 1.2. Get innovation and form field
        Innovation innovation = innovationRepository.findById(request.getInnovationId())
                .orElseThrow(
                        () -> new IdInvalidException("Không tìm thấy Innovation với ID: " + request.getInnovationId()));
        FormField formField = formFieldRepository.findById(request.getFormFieldId())
                .orElseThrow(
                        () -> new IdInvalidException("Không tìm thấy Form Field với ID: " + request.getFormFieldId()));

        // 1.3. Create FormData
        FormData formData = new FormData();
        formData.setInnovation(innovation);
        formData.setFormField(formField);
        formData.setFieldValue(request.getFieldValue());

        // 1.4. Save FormData
        FormData savedFormData = formDataRepository.save(formData);

        // 1.5. Convert to response
        return toFormDataResponse(savedFormData);
    }

    // 2. Create Multiple FormData
    public List<FormDataResponse> createMultipleFormData(List<FormDataRequest> requests) {
        List<FormData> formDataList = requests.stream()
                .map(this::toFormData)
                .collect(Collectors.toList());
        List<FormData> savedFormDataList = formDataRepository.saveAll(formDataList);
        return savedFormDataList.stream()
                .map(this::toFormDataResponse)
                .collect(Collectors.toList());
    }

    // 3. Update FormData
    public FormDataResponse updateFormData(String formDataId, FormDataRequest request) {
        FormData formData = formDataRepository.findById(formDataId)
                .orElseThrow(() -> new IdInvalidException("Không tìm thấy Form Data với ID: " + formDataId));
        formData.setFieldValue(request.getFieldValue());
        formDataRepository.save(formData);

        return toFormDataResponse(formData);
    }

    // 4. Update Multiple FormData
    public List<FormDataResponse> updateMultipleFormData(List<FormDataRequest> requests) {
        List<FormData> formDataList = requests.stream()
                .map(this::toFormData)
                .collect(Collectors.toList());
        List<FormData> savedFormDataList = formDataRepository.saveAll(formDataList);
        return savedFormDataList.stream()
                .map(this::toFormDataResponse)
                .collect(Collectors.toList());
    }

    // 5. Get FormData by Id
    public FormDataResponse getFormDataById(String formDataId) {
        FormData formData = formDataRepository.findById(formDataId)
                .orElseThrow(() -> new IdInvalidException("Không tìm thấy Form Data với ID: " + formDataId));
        return toFormDataResponse(formData);
    }

    // 6. Get FormData by InnovationId
    public List<FormDataResponse> getFormDataByInnovationId(String innovationId) {
        List<FormData> formDataList = formDataRepository.findByInnovationId(innovationId);
        return formDataList.stream()
                .map(this::toFormDataResponse)
                .collect(Collectors.toList());
    }

    // 7. Get FormData by TemplateId
    public List<FormDataResponse> getFormDataByTemplateId(String templateId) {
        List<FormData> formDataList = formDataRepository.findByFormFieldFormTemplateId(templateId);
        return formDataList.stream()
                .map(this::toFormDataResponse)
                .collect(Collectors.toList());
    }

    // 8. Get FormData with FormField
    public List<FormDataResponse> getFormDataWithFormFields(String innovationId, String templateId) {
        List<FormData> formDataList = formDataRepository.findByInnovationIdAndFormFieldFormTemplateId(innovationId,
                templateId);
        return formDataList.stream()
                .map(this::toFormDataResponse)
                .collect(Collectors.toList());
    }

    // Mapper
    private FormDataResponse toFormDataResponse(FormData formData) {
        FormDataResponse response = new FormDataResponse();
        response.setId(formData.getId());
        response.setFieldValue(formData.getFieldValue());
        response.setFormFieldId(formData.getFormField().getId());
        response.setFormFieldLabel(formData.getFormField().getLabel());
        return response;
    }

    private FormData toFormData(FormDataRequest request) {
        FormData formData = new FormData();
        formData.setInnovation(innovationRepository.findById(request.getInnovationId())
                .orElseThrow(() -> new IdInvalidException(
                        "Không tìm thấy Innovation với ID: " + request.getInnovationId())));
        formData.setFormField(formFieldRepository.findById(request.getFormFieldId())
                .orElseThrow(
                        () -> new IdInvalidException("Không tìm thấy Form Field với ID: " + request.getFormFieldId())));
        formData.setFieldValue(request.getFieldValue());
        return formData;
    }

}