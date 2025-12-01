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
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.TemplateFieldResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.TemplateFormDataResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.TemplateSignatureResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.exception.IdInvalidException;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.AttachmentRepository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.FormFieldRepository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.FormTemplateRepository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.utils.Utils;

import java.nio.charset.StandardCharsets;
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

            generateAndStoreTemplatePdf(innovation, formTemplate, htmlContent);

            if (templateType != TemplateTypeEnum.DON_DE_NGHI
                    && templateType != TemplateTypeEnum.BAO_CAO_MO_TA) {
                continue;
            }

            if (documentType == null) {
                continue;
            }

            String documentHash = digitalSignatureService
                    .generateDocumentHash(htmlContent.getBytes(StandardCharsets.UTF_8));
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

    private void generateAndStoreTemplatePdf(
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
                .map(entry -> new TemplateFormDataResponse(entry.getKey(), entry.getValue()))
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
     * Build template form data responses with tableConfig for viewing innovation detail.
     * This method is specifically for DepartmentInnovationDetailResponse to include tableConfig
     * for rendering table headers in the frontend.
     */
    public List<TemplateFormDataResponse> buildTemplateFormDataResponsesWithTableConfig(
            List<FormData> formDataList) {
        if (formDataList == null || formDataList.isEmpty()) {
            return new ArrayList<>();
        }

        Map<String, List<TemplateFieldResponse>> groupedByTemplate = new LinkedHashMap<>();

        for (FormData formData : formDataList) {
            FormField formField = formData.getFormField();
            if (formField == null) {
                continue;
            }

            String templateId = formField.getFormTemplate() != null 
                ? formField.getFormTemplate().getId() 
                : null;
            if (templateId == null || templateId.isBlank()) {
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

            groupedByTemplate
                    .computeIfAbsent(templateId, id -> new ArrayList<>())
                    .add(fieldResponse);
        }

        return groupedByTemplate.entrySet()
                .stream()
                .map(entry -> new TemplateFormDataResponse(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
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
