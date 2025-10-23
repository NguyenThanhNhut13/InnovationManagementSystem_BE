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

    // 1. Tạo Form Field
    public FormFieldResponse createFormField(FormFieldRequest request, String templateId) {

        FormField formField = toFormField(request, templateId);

        this.formFieldRepository.save(formField);

        return toResponse(formField);
    }

    // 2. Tạo Multiple Form Fields
    public List<FormFieldResponse> createMultipleFormFields(List<FormFieldRequest> requests, String templateId) {
        List<FormField> formFields = requests.stream()
                .map(request -> toFormField(request, templateId))
                .collect(Collectors.toList());
        this.formFieldRepository.saveAll(formFields);

        return formFields.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // 3. Cập nhật Form Field
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

        if (request.getRepeatable() != null) {
            formField.setRepeatable(request.getRepeatable());
        }

        if (request.getUserDataConfig() != null) {
            formField.setUserDataConfig(request.getUserDataConfig());
        }

        if (request.getInnovationDataConfig() != null) {
            formField.setInnovationDataConfig(request.getInnovationDataConfig());
        }

        if (request.getSigningRole() != null) {
            formField.setSigningRole(request.getSigningRole());
        }

        if (request.getChildren() != null) {
            formField.setChildren(request.getChildren());
        }

        this.formFieldRepository.save(formField);

        return toResponse(formField);
    }

    // 4. Xóa Form Field
    public void deleteFormField(String id) {
        FormField formField = formFieldRepository.findById(id)
                .orElseThrow(() -> new IdInvalidException("Không tìm thấy form field với id: " + id));
        this.formFieldRepository.delete(formField);
    }

    // 5. Lấy Form Field by Id
    public FormFieldResponse getFormFieldById(String id) {
        FormField formField = formFieldRepository.findById(id)
                .orElseThrow(() -> new IdInvalidException("Không tìm thấy form field với id: " + id));
        return toResponse(formField);
    }

    // 6. Lấy Form Fields by Template Id
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
        formField.setRepeatable(request.getRepeatable());
        formField.setFormTemplate(formTemplateRepository.findById(templateId)
                .orElseThrow(() -> new IdInvalidException("Không tìm thấy template với id: " + templateId)));

        // Handle table config for TABLE field type
        if (request.getTableConfig() != null && request.getFieldType().name().equals("TABLE")) {
            formField.setTableConfig(request.getTableConfig());
        }

        // Handle user data config
        if (request.getUserDataConfig() != null) {
            formField.setUserDataConfig(request.getUserDataConfig());
        }

        // Handle innovation data config
        if (request.getInnovationDataConfig() != null) {
            formField.setInnovationDataConfig(request.getInnovationDataConfig());
        }

        // Handle signing role
        if (request.getSigningRole() != null) {
            formField.setSigningRole(request.getSigningRole());
        }

        // Handle children
        if (request.getChildren() != null) {
            formField.setChildren(request.getChildren());
        }

        return formField;
    }

    private FormFieldResponse toResponse(FormField formField) {
        FormFieldResponse response = new FormFieldResponse();
        response.setId(formField.getId());
        response.setFieldKey(formField.getFieldKey());
        response.setLabel(formField.getLabel());
        response.setFieldType(formField.getFieldType());
        response.setRequired(formField.getRequired());
        response.setRepeatable(formField.getRepeatable());
        response.setFormTemplateId(formField.getFormTemplate().getId());

        // Map table config if exists
        if (formField.getTableConfig() != null) {
            response.setTableConfig(formField.getTableConfig());
        }

        // Map user data config if exists
        if (formField.getUserDataConfig() != null) {
            response.setUserDataConfig(formField.getUserDataConfig());
        }

        // Map innovation data config if exists
        if (formField.getInnovationDataConfig() != null) {
            response.setInnovationDataConfig(formField.getInnovationDataConfig());
        }

        // Map signing role if exists
        if (formField.getSigningRole() != null) {
            response.setSigningRole(formField.getSigningRole());
        }

        // Map children if exists
        if (formField.getChildren() != null) {
            response.setChildren(formField.getChildren());
        }

        return response;
    }
}