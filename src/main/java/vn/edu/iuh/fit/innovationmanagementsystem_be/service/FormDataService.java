package vn.edu.iuh.fit.innovationmanagementsystem_be.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.FormData;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.FormField;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.Innovation;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.FormDataRequest;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.UpdateFormDataRequest;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.FormDataResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.exception.IdInvalidException;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.FormDataRepository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.FormFieldRepository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.InnovationRepository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.mapper.FormDataMapper;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class FormDataService {

        private final FormDataRepository formDataRepository;
        private final FormFieldRepository formFieldRepository;
        private final InnovationRepository innovationRepository;
        private final FormDataMapper formDataMapper;

        public FormDataService(FormDataRepository formDataRepository,
                        FormFieldRepository formFieldRepository,
                        InnovationRepository innovationRepository,
                        FormDataMapper formDataMapper) {
                this.formDataRepository = formDataRepository;
                this.formFieldRepository = formFieldRepository;
                this.innovationRepository = innovationRepository;
                this.formDataMapper = formDataMapper;
        }

        // 1. Tạo FormData
        public FormDataResponse createFormData(FormDataRequest request) {

                if (request == null || request.getInnovationId() == null || request.getFormFieldId() == null) {
                        throw new IllegalArgumentException("Innovation ID và Form Field ID không được để trống");
                }

                Innovation innovation = innovationRepository.findById(request.getInnovationId())
                                .orElseThrow(
                                                () -> new IdInvalidException("Không tìm thấy Innovation với ID: "
                                                                + request.getInnovationId()));
                FormField formField = formFieldRepository.findByIdWithTemplate(request.getFormFieldId())
                                .orElseThrow(
                                                () -> new IdInvalidException("Không tìm thấy Form Field với ID: "
                                                                + request.getFormFieldId()));

                FormData formData = formDataMapper.toFormData(request);
                formData.setInnovation(innovation);
                formData.setFormField(formField);

                FormData savedFormData = formDataRepository.save(formData);

                FormData formDataWithRelations = formDataRepository.findByIdWithRelations(savedFormData.getId())
                                .orElse(savedFormData);

                return formDataMapper.toFormDataResponse(formDataWithRelations);
        }

        // 2. Cập nhật FormData
        public FormDataResponse updateFormData(String formDataId, UpdateFormDataRequest request) {
                FormData formData = formDataRepository.findById(formDataId)
                                .orElseThrow(() -> new IdInvalidException(
                                                "Không tìm thấy Form Data với ID: " + formDataId));

                if (request.getFieldValue() == null && request.getFormFieldId() == null &&
                                request.getInnovationId() == null) {
                        throw new IdInvalidException("Ít nhất một trường phải được cung cấp để cập nhật");
                }

                if (request.getFieldValue() != null) {
                        formData.setFieldValue(request.getFieldValue());
                }
                if (request.getFormFieldId() != null) {
                        FormField formField = formFieldRepository.findById(request.getFormFieldId())
                                        .orElseThrow(() -> new IdInvalidException("Không tìm thấy Form Field với ID: "
                                                        + request.getFormFieldId()));
                        formData.setFormField(formField);
                }
                if (request.getInnovationId() != null) {
                        Innovation innovation = innovationRepository.findById(request.getInnovationId())
                                        .orElseThrow(() -> new IdInvalidException("Không tìm thấy Innovation với ID: "
                                                        + request.getInnovationId()));
                        formData.setInnovation(innovation);
                }

                FormData savedFormData = formDataRepository.save(formData);

                FormData formDataWithRelations = formDataRepository.findByIdWithRelations(savedFormData.getId())
                                .orElse(savedFormData);

                return formDataMapper.toFormDataResponse(formDataWithRelations);
        }

        // 3. Lấy FormData by InnovationId
        public List<FormDataResponse> getFormDataByInnovationId(String innovationId) {
                List<FormData> formDataList = formDataRepository.findByInnovationIdWithRelations(innovationId);
                return formDataList.stream()
                                .map(formDataMapper::toFormDataResponse)
                                .collect(Collectors.toList());
        }

        // 4. Lấy FormData với FormField
        public List<FormDataResponse> getFormDataWithFormFields(String innovationId, String templateId) {
                List<FormData> formDataList = formDataRepository.findByInnovationIdAndTemplateIdWithRelations(
                                innovationId,
                                templateId);
                return formDataList.stream()
                                .map(formDataMapper::toFormDataResponse)
                                .collect(Collectors.toList());
        }

}