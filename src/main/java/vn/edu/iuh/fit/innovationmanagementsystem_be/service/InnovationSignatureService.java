package vn.edu.iuh.fit.innovationmanagementsystem_be.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.Attachment;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.FormData;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.FormField;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.FormTemplate;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.Innovation;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.FieldTypeEnum;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.DocumentTypeEnum;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.TemplateTypeEnum;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.UserRoleEnum;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.CreateInnovationWithTemplatesRequest;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.DigitalSignatureRequest;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.TemplateDataRequest;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.FormDataResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.FormSignatureResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.MyTemplateFormDataResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.TemplateFieldResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.TemplateFormDataResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.TemplateSignatureResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.exception.IdInvalidException;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.AttachmentRepository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.FormFieldRepository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.FormTemplateRepository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.utils.Utils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class InnovationSignatureService {

    private final FormTemplateRepository formTemplateRepository;
    private final DigitalSignatureService digitalSignatureService;
    private final PdfGeneratorService pdfGeneratorService;
    private final FileService fileService;
    private final AttachmentRepository attachmentRepository;
    private final ObjectMapper objectMapper;
    private final FormFieldRepository formFieldRepository;
    private final InnovationFormService innovationFormService;

    public InnovationSignatureService(FormTemplateRepository formTemplateRepository,
            DigitalSignatureService digitalSignatureService,
            PdfGeneratorService pdfGeneratorService,
            FileService fileService,
            AttachmentRepository attachmentRepository,
            ObjectMapper objectMapper,
            FormFieldRepository formFieldRepository,
            InnovationFormService innovationFormService) {
        this.formTemplateRepository = formTemplateRepository;
        this.digitalSignatureService = digitalSignatureService;
        this.pdfGeneratorService = pdfGeneratorService;
        this.fileService = fileService;
        this.attachmentRepository = attachmentRepository;
        this.objectMapper = objectMapper;
        this.formFieldRepository = formFieldRepository;
        this.innovationFormService = innovationFormService;
    }

    public List<SignatureProcessingResult> signInnovationDocuments(
            Innovation innovation,
            CreateInnovationWithTemplatesRequest request) {

        if (request.getTemplates() == null || request.getTemplates().isEmpty()) {
            throw new IdInvalidException("Danh sách template không được để trống khi nộp sáng kiến.");
        }

        List<SignatureProcessingResult> signatureResults = new ArrayList<>();

        for (TemplateDataRequest templateRequest : request.getTemplates()) {
            FormTemplate formTemplate = formTemplateRepository.findById(templateRequest.getTemplateId())
                    .orElseThrow(() -> new IdInvalidException(
                            "Không tìm thấy template với ID: "
                                    + templateRequest.getTemplateId()));

            TemplateTypeEnum templateType = formTemplate.getTemplateType();
            DocumentTypeEnum documentType = mapTemplateTypeToDocumentType(templateType);

            String encodedHtml = templateRequest.getHtmlContentBase64();

            boolean isRequiredTemplate = templateType == TemplateTypeEnum.DON_DE_NGHI
                    || templateType == TemplateTypeEnum.BAO_CAO_MO_TA;

            if (isRequiredTemplate && (encodedHtml == null || encodedHtml.isBlank())) {
                throw new IdInvalidException(
                        "htmlContentBase64 của template "
                                + formTemplate.getTemplateType().getValue()
                                + " không được để trống khi nộp sáng kiến.");
            }

            if (!isRequiredTemplate && (encodedHtml == null || encodedHtml.isBlank())) {
                continue;
            }

            String htmlContent = Utils.decode(encodedHtml);
            if (htmlContent == null || htmlContent.isBlank()) {
                throw new IdInvalidException(
                        "Nội dung HTML sau khi giải mã của template "
                                + formTemplate.getTemplateType().getValue()
                                + " đang trống.");
            }

            // Tạo PDF và lấy pdfBytes để hash
            byte[] pdfBytes = generateAndStoreTemplatePdf(innovation, formTemplate, htmlContent);

            if (templateType != TemplateTypeEnum.DON_DE_NGHI
                    && templateType != TemplateTypeEnum.BAO_CAO_MO_TA) {
                continue;
            }

            if (documentType == null) {
                continue;
            }

            // Hash PDF bytes thay vì HTML content để đảm bảo tính toàn vẹn của file PDF
            String documentHash = digitalSignatureService.generateDocumentHash(pdfBytes);
            String signatureHash = digitalSignatureService.generateSignatureForDocument(documentHash);

            DigitalSignatureRequest signatureRequest = new DigitalSignatureRequest();
            signatureRequest.setInnovationId(innovation.getId());
            signatureRequest.setDocumentType(documentType);
            signatureRequest.setSignedAsRole(UserRoleEnum.GIANG_VIEN);
            signatureRequest.setDocumentHash(documentHash);
            signatureRequest.setSignatureHash(signatureHash);

            digitalSignatureService.createDigitalSignature(signatureRequest);

            signatureResults.add(new SignatureProcessingResult(
                    templateRequest.getTemplateId(),
                    formTemplate.getTemplateType(),
                    documentType,
                    documentHash,
                    signatureHash));
        }

        if (signatureResults.isEmpty()) {
            throw new IdInvalidException("Không tìm thấy template hợp lệ để ký số.");
        }

        return signatureResults;
    }

    private byte[] generateAndStoreTemplatePdf(
            Innovation innovation,
            FormTemplate formTemplate,
            String htmlContent) {

        try {
            byte[] pdfBytes = pdfGeneratorService.convertHtmlToPdf(htmlContent);
            String fileName = buildTemplatePdfFileName(innovation.getId(), formTemplate.getId());
            String objectName = fileService.uploadBytes(pdfBytes, fileName, "application/pdf");

            attachmentRepository.deleteByInnovationIdAndTemplateId(
                    innovation.getId(),
                    formTemplate.getId());

            Attachment attachment = new Attachment();
            attachment.setInnovation(innovation);
            attachment.setTemplateId(formTemplate.getId());
            attachment.setFileName(fileName);
            attachment.setOriginalFileName(resolveTemplateOriginalFileName(formTemplate));
            attachment.setFileSize((long) pdfBytes.length);
            attachment.setPathUrl(objectName);

            attachmentRepository.save(attachment);

            // Return pdfBytes để có thể hash PDF thay vì HTML
            return pdfBytes;
        } catch (IdInvalidException e) {
            throw e;
        } catch (Exception e) {
            String errorMessage = e.getMessage();
            if (errorMessage == null || errorMessage.isBlank()) {
                errorMessage = e.getClass().getSimpleName();
                if (e.getCause() != null && e.getCause().getMessage() != null) {
                    errorMessage += ": " + e.getCause().getMessage();
                }
            }
            throw new IdInvalidException(
                    "Không thể lưu PDF cho template "
                            + formTemplate.getTemplateType().getValue()
                            + ": "
                            + errorMessage);
        }
    }

    private String buildTemplatePdfFileName(String innovationId, String templateId) {
        return innovationId + "_" + templateId + ".pdf";
    }

    private String resolveTemplateOriginalFileName(FormTemplate formTemplate) {
        if (formTemplate != null && formTemplate.getTemplateType() != null) {
            return formTemplate.getTemplateType().getValue() + ".pdf";
        }
        return "template.pdf";
    }

    private DocumentTypeEnum mapTemplateTypeToDocumentType(TemplateTypeEnum templateType) {
        if (templateType == null) {
            return null;
        }

        switch (templateType) {
            case DON_DE_NGHI:
                return DocumentTypeEnum.FORM_1;
            case BAO_CAO_MO_TA:
                return DocumentTypeEnum.FORM_2;
            case BIEN_BAN_HOP:
                return DocumentTypeEnum.REPORT_MAU_3;
            case TONG_HOP_DE_NGHI:
                return DocumentTypeEnum.REPORT_MAU_4;
            case TONG_HOP_CHAM_DIEM:
                return DocumentTypeEnum.REPORT_MAU_5;
            case PHIEU_DANH_GIA:
                return DocumentTypeEnum.REPORT_MAU_7;
            default:
                return null;
        }
    }

    public List<TemplateSignatureResponse> buildTemplateSignatureResponses(
            List<SignatureProcessingResult> signatureResults) {
        if (signatureResults == null || signatureResults.isEmpty()) {
            return new ArrayList<>();
        }

        return signatureResults.stream()
                .map(result -> new TemplateSignatureResponse(
                        result.templateId(),
                        result.templateType(),
                        result.documentType(),
                        result.documentHash(),
                        result.signatureHash()))
                .collect(Collectors.toList());
    }

    public List<TemplateFormDataResponse> buildTemplateFormDataResponses(
            List<FormDataResponse> formDataResponses) {
        if (formDataResponses == null || formDataResponses.isEmpty()) {
            return new ArrayList<>();
        }

        Map<String, List<TemplateFieldResponse>> groupedByTemplate = new LinkedHashMap<>();

        for (FormDataResponse formDataResponse : formDataResponses) {
            String templateId = formDataResponse.getTemplateId();
            if (templateId == null || templateId.isBlank()) {
                continue;
            }

            String label = formDataResponse.getFormFieldLabel();
            if (label == null || label.isBlank()) {
                continue;
            }

            String fieldType = formDataResponse.getFieldType() != null
                    ? formDataResponse.getFieldType().name()
                    : "TEXT";

            com.fasterxml.jackson.databind.JsonNode valueNode = extractEffectiveFieldValue(formDataResponse);

            TemplateFieldResponse fieldResponse = new TemplateFieldResponse(label, fieldType, valueNode);

            groupedByTemplate
                    .computeIfAbsent(templateId, id -> new ArrayList<>())
                    .add(fieldResponse);
        }

        return groupedByTemplate.entrySet()
                .stream()
                .map(entry -> new TemplateFormDataResponse(entry.getKey(), null, entry.getValue()))
                .collect(Collectors.toList());
    }

    private com.fasterxml.jackson.databind.JsonNode extractEffectiveFieldValue(FormDataResponse formDataResponse) {
        com.fasterxml.jackson.databind.JsonNode fieldValue = formDataResponse.getFieldValue();
        if (fieldValue == null) {
            return objectMapper.nullNode();
        }
        if (fieldValue.has("value")) {
            com.fasterxml.jackson.databind.JsonNode valueNode = fieldValue.get("value");
            return valueNode != null ? valueNode : objectMapper.nullNode();
        }
        return fieldValue;
    }

    /**
     * Build template form data responses with formData Map for draft loading.
     * This method converts FormDataResponse list to MyTemplateFormDataResponse list
     * with formData Map (fieldKey -> value) for FE draft loading.
     */
    public List<MyTemplateFormDataResponse> buildMyTemplateFormDataResponses(
            List<FormDataResponse> formDataResponses) {
        if (formDataResponses == null || formDataResponses.isEmpty()) {
            return new ArrayList<>();
        }

        Map<String, Map<String, Object>> groupedByTemplate = new LinkedHashMap<>();

        for (FormDataResponse formDataResponse : formDataResponses) {
            String templateId = formDataResponse.getTemplateId();
            if (templateId == null || templateId.isBlank()) {
                continue;
            }

            com.fasterxml.jackson.databind.JsonNode fieldValueNode = formDataResponse.getFieldValue();
            String fieldKey;
            com.fasterxml.jackson.databind.JsonNode valueNode;

            // Check if fieldValue has structure {"fieldKey": "...", "value": ...}
            // This is the case for child fields in sections/tables
            if (fieldValueNode != null && fieldValueNode.isObject() && fieldValueNode.has("fieldKey")) {
                // This is a child field in section/table
                fieldKey = fieldValueNode.get("fieldKey").asText();
                valueNode = fieldValueNode.has("value") ? fieldValueNode.get("value") : null;
            } else {
                // This is a regular field (root level)
                fieldKey = formDataResponse.getFormFieldKey();
                valueNode = extractEffectiveFieldValue(formDataResponse);
            }

            if (fieldKey == null || fieldKey.isBlank()) {
                continue;
            }

            Object value = convertJsonNodeToObject(valueNode);

            groupedByTemplate
                    .computeIfAbsent(templateId, id -> new LinkedHashMap<>())
                    .put(fieldKey, value);
        }

        return groupedByTemplate.entrySet()
                .stream()
                .map(entry -> new MyTemplateFormDataResponse(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }

    /**
     * Build form signature responses from FormDataResponse list.
     * Extracts SIGNATURE fields and returns them in the format expected by FE.
     */
    public List<FormSignatureResponse> buildFormSignatureResponses(
            List<FormDataResponse> formDataResponses,
            Map<String, String> signerNames) { // Map: fieldKey -> signerName (có thể null)
        if (formDataResponses == null || formDataResponses.isEmpty()) {
            return new ArrayList<>();
        }

        List<FormSignatureResponse> signatures = new ArrayList<>();

        for (FormDataResponse formDataResponse : formDataResponses) {
            // Check if this is a SIGNATURE field
            if (formDataResponse.getFieldType() != FieldTypeEnum.SIGNATURE) {
                continue;
            }

            String templateId = formDataResponse.getTemplateId();
            if (templateId == null || templateId.isBlank()) {
                continue;
            }

            com.fasterxml.jackson.databind.JsonNode fieldValueNode = formDataResponse.getFieldValue();
            String fieldKey;
            String signatureUrl;

            // Check if fieldValue has structure {"fieldKey": "...", "value": ...}
            // This is the case for child fields in sections/tables
            if (fieldValueNode != null && fieldValueNode.isObject() && fieldValueNode.has("fieldKey")) {
                // This is a child signature field in section/table
                fieldKey = fieldValueNode.get("fieldKey").asText();
                com.fasterxml.jackson.databind.JsonNode valueNode = fieldValueNode.has("value")
                        ? fieldValueNode.get("value")
                        : null;
                signatureUrl = (valueNode != null && valueNode.isTextual())
                        ? valueNode.asText()
                        : null;
            } else {
                // This is a regular signature field (root level)
                fieldKey = formDataResponse.getFormFieldKey();
                com.fasterxml.jackson.databind.JsonNode valueNode = extractEffectiveFieldValue(formDataResponse);
                signatureUrl = (valueNode != null && valueNode.isTextual())
                        ? valueNode.asText()
                        : null;
            }

            if (fieldKey == null || fieldKey.isBlank() || signatureUrl == null || signatureUrl.isBlank()) {
                continue;
            }

            // Lấy signerName từ map nếu có
            String signerName = signerNames != null ? signerNames.get(fieldKey) : null;

            signatures.add(new FormSignatureResponse(templateId, fieldKey, signatureUrl, signerName));
        }

        return signatures;
    }

    /**
     * Convert JsonNode to Object (String, Number, Boolean, List, Map)
     */
    private Object convertJsonNodeToObject(com.fasterxml.jackson.databind.JsonNode valueNode) {
        if (valueNode == null || valueNode.isNull()) {
            return null;
        }
        if (valueNode.isTextual()) {
            return valueNode.asText();
        } else if (valueNode.isNumber()) {
            if (valueNode.isInt()) {
                return valueNode.asInt();
            } else if (valueNode.isLong()) {
                return valueNode.asLong();
            } else {
                return valueNode.asDouble();
            }
        } else if (valueNode.isBoolean()) {
            return valueNode.asBoolean();
        } else if (valueNode.isArray()) {
            // Array: convert to List
            List<Object> list = new ArrayList<>();
            for (com.fasterxml.jackson.databind.JsonNode item : valueNode) {
                list.add(convertJsonNodeToObject(item));
            }
            return list;
        } else if (valueNode.isObject()) {
            // Object: convert to Map
            Map<String, Object> valueMap = new LinkedHashMap<>();
            valueNode.fieldNames().forEachRemaining(fieldName -> {
                valueMap.put(fieldName, convertJsonNodeToObject(valueNode.get(fieldName)));
            });
            return valueMap;
        }
        return null;
    }

    /**
     * Build template form data responses with tableConfig for viewing innovation
     * detail.
     * This method is specifically for DepartmentInnovationDetailResponse to include
     * tableConfig
     * for rendering table headers in the frontend.
     * Only returns templates with type DON_DE_NGHI and BAO_CAO_MO_TA.
     */
    public List<TemplateFormDataResponse> buildTemplateFormDataResponsesWithTableConfig(
            List<FormData> formDataList) {
        if (formDataList == null || formDataList.isEmpty()) {
            return new ArrayList<>();
        }

        // Map to store templateId -> (templateType, fields)
        Map<String, TemplateInfo> templateMap = new LinkedHashMap<>();

        for (FormData formData : formDataList) {
            FormField formField = formData.getFormField();
            if (formField == null) {
                continue;
            }

            FormTemplate formTemplate = formField.getFormTemplate();
            if (formTemplate == null) {
                continue;
            }

            String templateId = formTemplate.getId();
            if (templateId == null || templateId.isBlank()) {
                continue;
            }

            // Filter: chỉ lấy DON_DE_NGHI và BAO_CAO_MO_TA
            TemplateTypeEnum templateType = formTemplate.getTemplateType();
            if (templateType != TemplateTypeEnum.DON_DE_NGHI
                    && templateType != TemplateTypeEnum.BAO_CAO_MO_TA) {
                continue;
            }

            String label = formField.getLabel();
            if (label == null || label.isBlank()) {
                continue;
            }

            String fieldType = formField.getFieldType() != null
                    ? formField.getFieldType().name()
                    : "TEXT";

            com.fasterxml.jackson.databind.JsonNode valueNode = extractEffectiveFieldValueFromFormData(formData);

            // Get tableConfig if fieldType is TABLE
            com.fasterxml.jackson.databind.JsonNode tableConfig = null;
            if (formField.getFieldType() == FieldTypeEnum.TABLE) {
                tableConfig = formField.getTableConfig();
            }

            TemplateFieldResponse fieldResponse = new TemplateFieldResponse(label, fieldType, valueNode, tableConfig);

            // Store template info (templateType and fields)
            templateMap.computeIfAbsent(templateId, id -> new TemplateInfo(templateType, new ArrayList<>()))
                    .getFields().add(fieldResponse);
        }

        return templateMap.entrySet()
                .stream()
                .map(entry -> new TemplateFormDataResponse(entry.getKey(), entry.getValue().getTemplateType(),
                        entry.getValue().getFields()))
                .collect(Collectors.toList());
    }

    /**
     * Helper class to store template info (templateType and fields)
     */
    private static class TemplateInfo {
        private final TemplateTypeEnum templateType;
        private final List<TemplateFieldResponse> fields;

        public TemplateInfo(TemplateTypeEnum templateType, List<TemplateFieldResponse> fields) {
            this.templateType = templateType;
            this.fields = fields;
        }

        public TemplateTypeEnum getTemplateType() {
            return templateType;
        }

        public List<TemplateFieldResponse> getFields() {
            return fields;
        }
    }

    /**
     * Helper method to extract effective field value from FormData entity.
     */
    private com.fasterxml.jackson.databind.JsonNode extractEffectiveFieldValueFromFormData(FormData formData) {
        com.fasterxml.jackson.databind.JsonNode fieldValue = formData.getFieldValue();
        if (fieldValue == null) {
            return objectMapper.nullNode();
        }
        if (fieldValue.has("value")) {
            com.fasterxml.jackson.databind.JsonNode valueNode = fieldValue.get("value");
            return valueNode != null ? valueNode : objectMapper.nullNode();
        }
        return fieldValue;
    }

    public void validateSignaturesBeforeSubmit(CreateInnovationWithTemplatesRequest request) {
        List<String> missingSignatures = new ArrayList<>();

        for (TemplateDataRequest templateRequest : request.getTemplates()) {
            FormTemplate formTemplate = formTemplateRepository
                    .findById(templateRequest.getTemplateId())
                    .orElse(null);

            if (formTemplate == null
                    || (formTemplate.getTemplateType() != TemplateTypeEnum.DON_DE_NGHI
                            && formTemplate.getTemplateType() != TemplateTypeEnum.BAO_CAO_MO_TA)) {
                continue;
            }

            List<FormField> formFields = formFieldRepository
                    .findByTemplateId(templateRequest.getTemplateId());

            List<FormField> authorSignatureFields = innovationFormService.getAuthorSignatureFields(formFields,
                    templateRequest.getTemplateId());

            Map<String, Object> formData = templateRequest.getFormData();
            if (formData == null) {
                formData = Map.of();
            }

            for (FormField signatureField : authorSignatureFields) {
                String fieldKey = signatureField.getFieldKey();
                String fieldLabel = signatureField.getLabel();

                if (!formData.containsKey(fieldKey)) {
                    missingSignatures.add(fieldLabel + " (Template: "
                            + formTemplate.getTemplateType().getValue() + ")");
                    continue;
                }

                Object fieldValue = formData.get(fieldKey);
                if (fieldValue == null) {
                    missingSignatures.add(fieldLabel + " (Template: "
                            + formTemplate.getTemplateType().getValue() + ")");
                    continue;
                }

                if (fieldValue instanceof String && ((String) fieldValue).trim().isEmpty()) {
                    missingSignatures.add(fieldLabel + " (Template: "
                            + formTemplate.getTemplateType().getValue() + ")");
                    continue;
                }

                if (fieldValue instanceof JsonNode) {
                    JsonNode jsonNode = (JsonNode) fieldValue;
                    if (jsonNode.isNull() || jsonNode.isMissingNode()
                            || (jsonNode.isTextual()
                                    && jsonNode.asText().trim().isEmpty())) {
                        missingSignatures.add(fieldLabel + " (Template: "
                                + formTemplate.getTemplateType().getValue() + ")");
                    }
                }
            }
        }

        if (!missingSignatures.isEmpty()) {
            String missingList = String.join(", ", missingSignatures);
            throw new IdInvalidException(
                    "Không thể nộp sáng kiến vì thiếu chữ ký. Các chữ ký còn thiếu: " + missingList
                            + ". Vui lòng kiểm tra lại và ký đầy đủ trước khi nộp.");
        }
    }

    public record SignatureProcessingResult(
            String templateId,
            TemplateTypeEnum templateType,
            DocumentTypeEnum documentType,
            String documentHash,
            String signatureHash) {
    }
}
