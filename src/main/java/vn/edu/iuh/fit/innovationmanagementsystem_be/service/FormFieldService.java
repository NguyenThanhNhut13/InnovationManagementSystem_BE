package vn.edu.iuh.fit.innovationmanagementsystem_be.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.FormField;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.FormFieldRequest;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.UpdateFormFieldRequest;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.FormFieldResponse;
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

        FormField formField = toFormField(request, templateId);

        this.formFieldRepository.save(formField);

        return toResponse(formField);
    }

    // 2. Create Multiple Form Fields
    public List<FormFieldResponse> createMultipleFormFields(List<FormFieldRequest> requests, String templateId) {
        List<FormField> formFields = requests.stream()
                .map(request -> toFormField(request, templateId))
                .collect(Collectors.toList());
        this.formFieldRepository.saveAll(formFields);

        return formFields.stream()
                .map(this::toResponse)
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

        if (request.getRequired() != null) {
            formField.setRequired(request.getRequired());
        }

        if (request.getPlaceholder() != null) {
            formField.setPlaceholder(request.getPlaceholder());
        }

        this.formFieldRepository.save(formField);

        return toResponse(formField);
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
        return toResponse(formField);
    }

    // 6. Get Form Fields by Template Id
    public List<FormFieldResponse> getFormFieldsByTemplateId(String templateId) {
        List<FormField> formFields = formFieldRepository.findByFormTemplateId(templateId);
        return formFields.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // Mapper
    private FormField toFormField(FormFieldRequest request, String templateId) {
        FormField formField = new FormField();
        formField.setLabel(request.getLabel());
        formField.setFieldKey(request.getFieldKey());
        formField.setFieldType(request.getFieldType());
        formField.setRequired(request.getRequired());
        formField.setPlaceholder(request.getPlaceholder());
        formField.setFormTemplate(formTemplateRepository.findById(templateId)
                .orElseThrow(() -> new IdInvalidException("Không tìm thấy template với id: " + templateId)));
        return formField;
    }

    private FormFieldResponse toResponse(FormField formField) {
        FormFieldResponse response = new FormFieldResponse();
        response.setId(formField.getId());
        response.setFieldKey(formField.getFieldKey());
        response.setLabel(formField.getLabel());
        response.setFieldType(formField.getFieldType());
        response.setRequired(formField.getRequired());
        response.setPlaceholder(formField.getPlaceholder());
        response.setFormTemplateId(formField.getFormTemplate().getId());
        response.setFormTemplateName(formField.getFormTemplate().getName());
        return response;
    }
}