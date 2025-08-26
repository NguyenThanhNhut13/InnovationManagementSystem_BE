package vn.edu.iuh.fit.innovationmanagementsystem_be.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.dto.requestDTO.FormFieldRequest;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.dto.requestDTO.UpdateFormFieldRequest;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.dto.responseDTO.FormFieldResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.FormField;
import vn.edu.iuh.fit.innovationmanagementsystem_be.exception.IdInvalidException;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.FormFieldRepository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.FormTemplateRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class FormFieldService {

    private final FormFieldRepository formFieldRepository;
    private final FormTemplateRepository formTemplateRepository;

    public FormFieldService(FormFieldRepository formFieldRepository,
            FormTemplateRepository formTemplateRepository) {
        this.formFieldRepository = formFieldRepository;
        this.formTemplateRepository = formTemplateRepository;
    }

    // 1. Create Form Field
    public FormFieldResponse createFormField(FormFieldRequest request, String templateId) {

        FormField formField = mapToFormField(request, templateId);

        this.formFieldRepository.save(formField);

        return mapToResponse(formField);
    }

    // 2. Create Multiple Form Fields
    public List<FormFieldResponse> createMultipleFormFields(List<FormFieldRequest> requests, String templateId) {
        List<FormField> formFields = requests.stream()
                .map(request -> mapToFormField(request, templateId))
                .collect(Collectors.toList());
        this.formFieldRepository.saveAll(formFields);

        return formFields.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // 3. Update Form Field
    public FormFieldResponse updateFormField(UpdateFormFieldRequest request) {
        FormField formField = formFieldRepository.findById(request.getId())
                .orElseThrow(() -> new IdInvalidException("Không tìm thấy form field với id: " + request.getId()));

        if (request.getLabel() != null) {
            formField.setLabel(request.getLabel());
        }

        if (request.getFieldKey() != null) {
            formField.setFieldKey(request.getFieldKey());
        }

        if (request.getFieldType() != null) {
            formField.setFieldType(request.getFieldType());
        }

        if (request.getIsRequired() != null) {
            formField.setIsRequired(request.getIsRequired());
        }

        if (request.getOrderInTemplate() != null) {
            formField.setOrderInTemplate(request.getOrderInTemplate());
        }

        this.formFieldRepository.save(formField);

        return mapToResponse(formField);
    }

    // 4. Delete Form Field
    public void deleteFormField(String id) {
        FormField formField = formFieldRepository.findById(id)
                .orElseThrow(() -> new IdInvalidException("Không tìm thấy form field với id: " + id));
        this.formFieldRepository.delete(formField);
    }

    // 5. Get Form Field by Id
    public FormFieldResponse getFormFieldById(String id) {
        FormField formField = formFieldRepository.findById(id)
                .orElseThrow(() -> new IdInvalidException("Không tìm thấy form field với id: " + id));
        return mapToResponse(formField);
    }

    // 6. Get Form Fields by Template Id
    public List<FormFieldResponse> getFormFieldsByTemplateId(String templateId) {
        List<FormField> formFields = formFieldRepository.findByFormTemplateId(templateId);
        return formFields.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // 7. Reorder Form Field
    public FormFieldResponse reorderFormField(String id, FormFieldRequest request) {
        FormField formField = formFieldRepository.findById(id)
                .orElseThrow(() -> new IdInvalidException("Không tìm thấy form field với id: " + id));
        formField.setOrderInTemplate(request.getOrderInTemplate());
        this.formFieldRepository.save(formField);
        return mapToResponse(formField);
    }

    // Mapper
    private FormField mapToFormField(FormFieldRequest request, String templateId) {
        FormField formField = new FormField();
        formField.setLabel(request.getLabel());
        formField.setFieldKey(request.getFieldKey());
        formField.setFieldType(request.getFieldType());
        formField.setIsRequired(request.getIsRequired());
        formField.setOrderInTemplate(request.getOrderInTemplate());
        formField.setFormTemplate(formTemplateRepository.findById(templateId)
                .orElseThrow(() -> new IdInvalidException("Không tìm thấy template với id: " + templateId)));
        return formField;
    }

    private FormFieldResponse mapToResponse(FormField formField) {
        FormFieldResponse response = new FormFieldResponse();
        response.setId(formField.getId());
        response.setLabel(formField.getLabel());
        response.setFieldKey(formField.getFieldKey());
        response.setFieldType(formField.getFieldType());
        response.setIsRequired(formField.getIsRequired());
        response.setOrderInTemplate(formField.getOrderInTemplate());
        response.setFormTemplateId(formField.getFormTemplate().getId());
        response.setFormTemplateName(formField.getFormTemplate().getName());
        return response;
    }
}