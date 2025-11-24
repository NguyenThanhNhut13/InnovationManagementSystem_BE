package vn.edu.iuh.fit.innovationmanagementsystem_be.service;

import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.FormField;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.FieldTypeEnum;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.UserRoleEnum;
import vn.edu.iuh.fit.innovationmanagementsystem_be.exception.IdInvalidException;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.FormTemplateRepository;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class InnovationFormService {

    private static final Logger logger = LoggerFactory.getLogger(InnovationFormService.class);

    private final FormTemplateRepository formTemplateRepository;

    public InnovationFormService(FormTemplateRepository formTemplateRepository) {
        this.formTemplateRepository = formTemplateRepository;
    }

    public FormFieldSearchResult findFormFieldByKeyWithParent(List<FormField> formFields, String fieldKey,
            String templateId) {
        for (FormField field : formFields) {
            if (fieldKey.equals(field.getFieldKey())) {
                return new FormFieldSearchResult(field, null);
            }

            if (field.getChildren() != null && field.getChildren().isArray()) {
                FormField foundField = findFormFieldInChildren(field.getChildren(), fieldKey,
                        templateId);
                if (foundField != null) {
                    return new FormFieldSearchResult(foundField, field);
                }
            }

            if (field.getFieldType() != null && field.getFieldType() == FieldTypeEnum.TABLE
                    && field.getTableConfig() != null) {
                FormField foundField = findFormFieldInTableColumns(field.getTableConfig(), fieldKey,
                        templateId);
                if (foundField != null) {
                    return new FormFieldSearchResult(foundField, field);
                }
            }
        }

        return null;
    }

    public List<FormField> getAuthorSignatureFields(List<FormField> formFields, String templateId) {
        List<FormField> signatureFields = new ArrayList<>();

        for (FormField field : formFields) {
            if (field.getFieldType() == FieldTypeEnum.SIGNATURE
                    && field.getSigningRole() == UserRoleEnum.GIANG_VIEN) {
                signatureFields.add(field);
            }

            if (field.getChildren() != null && field.getChildren().isArray()) {
                List<FormField> childSignatureFields = getAuthorSignatureFieldsFromChildren(
                        field.getChildren(),
                        templateId);
                signatureFields.addAll(childSignatureFields);
            }

            if (field.getFieldType() == FieldTypeEnum.TABLE && field.getTableConfig() != null) {
                List<FormField> tableSignatureFields = getAuthorSignatureFieldsFromTableColumns(
                        field.getTableConfig(), templateId);
                signatureFields.addAll(tableSignatureFields);
            }
        }

        return signatureFields;
    }

    private List<FormField> getAuthorSignatureFieldsFromChildren(JsonNode childrenNode, String templateId) {
        List<FormField> signatureFields = new ArrayList<>();

        try {
            for (JsonNode childNode : childrenNode) {
                JsonNode typeNode = childNode.get("type");
                if (typeNode != null) {
                    try {
                        FieldTypeEnum fieldType = FieldTypeEnum.valueOf(typeNode.asText());
                        if (fieldType == FieldTypeEnum.SIGNATURE) {
                            JsonNode signingRoleNode = childNode.get("signingRole");
                            if (signingRoleNode != null) {
                                try {
                                    UserRoleEnum signingRole = UserRoleEnum
                                            .valueOf(signingRoleNode
                                                    .asText());
                                    if (signingRole == UserRoleEnum.GIANG_VIEN) {
                                        FormField signatureField = createFormFieldFromJson(
                                                childNode,
                                                templateId);
                                        signatureFields.add(signatureField);
                                    }
                                } catch (IllegalArgumentException e) {
                                }
                            }
                        }
                    } catch (IllegalArgumentException e) {
                    }
                }

                JsonNode childChildrenNode = childNode.get("children");
                if (childChildrenNode != null && childChildrenNode.isArray()) {
                    signatureFields.addAll(
                            getAuthorSignatureFieldsFromChildren(childChildrenNode,
                                    templateId));
                }
            }
        } catch (Exception e) {
            logger.error("Lỗi khi lấy signature fields từ children: {}", e.getMessage(), e);
        }

        return signatureFields;
    }

    private List<FormField> getAuthorSignatureFieldsFromTableColumns(JsonNode tableConfig, String templateId) {
        List<FormField> signatureFields = new ArrayList<>();

        try {
            JsonNode columnsNode = tableConfig.get("columns");
            if (columnsNode != null && columnsNode.isArray()) {
                for (JsonNode columnNode : columnsNode) {
                    JsonNode typeNode = columnNode.get("type");
                    if (typeNode != null) {
                        try {
                            FieldTypeEnum fieldType = FieldTypeEnum
                                    .valueOf(typeNode.asText());
                            if (fieldType == FieldTypeEnum.SIGNATURE) {
                                JsonNode signingRoleNode = columnNode
                                        .get("signingRole");
                                if (signingRoleNode != null) {
                                    try {
                                        UserRoleEnum signingRole = UserRoleEnum
                                                .valueOf(signingRoleNode
                                                        .asText());
                                        if (signingRole == UserRoleEnum.GIANG_VIEN) {
                                            FormField signatureField = createFormFieldFromJson(
                                                    columnNode,
                                                    templateId);
                                            signatureFields.add(
                                                    signatureField);
                                        }
                                    } catch (IllegalArgumentException e) {
                                    }
                                }
                            }
                        } catch (IllegalArgumentException e) {
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Lỗi khi lấy signature fields từ table columns: {}", e.getMessage(), e);
        }

        return signatureFields;
    }

    private FormField createFormFieldFromJson(JsonNode childNode, String templateId) {
        FormField field = new FormField();

        JsonNode idNode = childNode.get("id");
        if (idNode != null) {
            field.setId(idNode.asText());
        }

        if (childNode.get("fieldKey") != null) {
            field.setFieldKey(childNode.get("fieldKey").asText());
        }

        if (childNode.get("label") != null) {
            field.setLabel(childNode.get("label").asText());
        }

        JsonNode typeNode = childNode.get("type");
        if (typeNode != null) {
            try {
                field.setFieldType(FieldTypeEnum.valueOf(typeNode.asText()));
            } catch (IllegalArgumentException e) {
                field.setFieldType(null);
            }
        }

        JsonNode requiredNode = childNode.get("required");
        if (requiredNode != null) {
            field.setRequired(requiredNode.asBoolean());
        }

        JsonNode isReadOnlyNode = childNode.get("isReadOnly");
        if (isReadOnlyNode != null) {
            field.setIsReadOnly(isReadOnlyNode.asBoolean());
        }

        JsonNode optionsNode = childNode.get("options");
        if (optionsNode != null) {
            field.setOptions(optionsNode);
        }

        JsonNode referenceConfigNode = childNode.get("referenceConfig");
        if (referenceConfigNode != null) {
            field.setReferenceConfig(referenceConfigNode);
        }

        JsonNode userDataConfigNode = childNode.get("userDataConfig");
        if (userDataConfigNode != null) {
            field.setUserDataConfig(userDataConfigNode);
        }

        JsonNode innovationDataConfigNode = childNode.get("innovationDataConfig");
        if (innovationDataConfigNode != null) {
            field.setInnovationDataConfig(innovationDataConfigNode);
        }

        JsonNode contributionConfigNode = childNode.get("contributionConfig");
        if (contributionConfigNode != null) {
            field.setContributionConfig(contributionConfigNode);
        }

        JsonNode signingRoleNode = childNode.get("signingRole");
        if (signingRoleNode != null) {
            try {
                field.setSigningRole(UserRoleEnum.valueOf(signingRoleNode.asText()));
            } catch (IllegalArgumentException e) {
                field.setSigningRole(null);
            }
        }

        JsonNode tableConfigNode = childNode.get("tableConfig");
        if (tableConfigNode != null) {
            field.setTableConfig(tableConfigNode);
        }

        JsonNode repeatableNode = childNode.get("repeatable");
        if (repeatableNode != null) {
            field.setRepeatable(repeatableNode.asBoolean());
        }

        if (templateId != null) {
            field.setFormTemplate(formTemplateRepository.findById(templateId).orElse(null));
        }

        return field;
    }

    private FormField findFormFieldInChildren(JsonNode childrenNode, String fieldKey, String templateId) {
        try {
            for (JsonNode childNode : childrenNode) {
                JsonNode childFieldKeyNode = childNode.get("fieldKey");
                if (childFieldKeyNode != null && fieldKey.equals(childFieldKeyNode.asText())) {
                    return createFormFieldFromJson(childNode, templateId);
                }

                JsonNode childChildrenNode = childNode.get("children");
                if (childChildrenNode != null && childChildrenNode.isArray()) {
                    FormField foundField = findFormFieldInChildren(childChildrenNode, fieldKey,
                            templateId);
                    if (foundField != null) {
                        return foundField;
                    }
                }
            }
        } catch (Exception e) {
            throw new IdInvalidException("Lỗi khi tìm FormField trong children: " + e.getMessage());
        }

        return null;
    }

    private FormField findFormFieldInTableColumns(JsonNode tableConfig, String fieldKey, String templateId) {
        try {
            JsonNode columnsNode = tableConfig.get("columns");
            if (columnsNode != null && columnsNode.isArray()) {
                for (JsonNode columnNode : columnsNode) {
                    JsonNode columnKeyNode = columnNode.get("key");
                    if (columnKeyNode != null && fieldKey.equals(columnKeyNode.asText())) {
                        return createFormFieldFromJson(columnNode, templateId);
                    }
                }
            }
        } catch (Exception e) {
            throw new IdInvalidException("Lỗi khi tìm FormField trong tableConfig: " + e.getMessage());
        }

        return null;
    }

    public static class FormFieldSearchResult {
        private FormField formField;
        private FormField parentField;

        public FormFieldSearchResult(FormField formField, FormField parentField) {
            this.formField = formField;
            this.parentField = parentField;
        }

        public FormField getFormField() {
            return formField;
        }

        public FormField getParentField() {
            return parentField;
        }

        public boolean hasParentField() {
            return parentField != null;
        }
    }
}
