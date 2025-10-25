package vn.edu.iuh.fit.innovationmanagementsystem_be.utils;

import org.springframework.stereotype.Component;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.FormField;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.core.type.TypeReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class HtmlTemplateUtils {

    // Pattern để tìm data-field-id và data-section-id trong HTML
    private static final Pattern FIELD_ID_PATTERN = Pattern.compile("data-field-id=\"([^\"]+)\"");
    private static final Pattern SECTION_ID_PATTERN = Pattern.compile("data-section-id=\"([^\"]+)\"");

    // Cập nhật HTML template content với field IDs mới
    public String updateFieldIdsInHtml(String htmlContent, List<FormField> fields) {
        if (htmlContent == null || fields == null || fields.isEmpty()) {
            return htmlContent;
        }

        List<FormField> allFields = flattenAllFields(fields);
        Map<String, String> oldIdToNewIdMap = new HashMap<>();
        List<String> oldFieldIds = extractOldFieldIds(htmlContent);

        int maxMapping = Math.min(oldFieldIds.size(), allFields.size());
        for (int i = 0; i < maxMapping; i++) {
            String oldId = oldFieldIds.get(i);
            String newId = allFields.get(i).getId();
            oldIdToNewIdMap.put(oldId, newId);
        }

        return updateFieldIds(htmlContent, oldIdToNewIdMap);
    }

    // Flatten tất cả fields bao gồm cả children trong sections
    public static List<FormField> flattenAllFields(List<FormField> fields) {
        List<FormField> flattenedFields = new ArrayList<>();
        ObjectMapper objectMapper = new ObjectMapper();

        for (FormField field : fields) {
            flattenedFields.add(field);

            if (field.getChildren() != null && !field.getChildren().isNull()) {
                try {
                    List<FormField> children = null;

                    if (field.getChildren().isArray()) {
                        children = objectMapper.convertValue(
                                field.getChildren(),
                                new TypeReference<List<FormField>>() {
                                });
                    } else {
                        String childrenJson = field.getChildren().toString();
                        children = objectMapper.readValue(childrenJson, new TypeReference<List<FormField>>() {
                        });
                    }

                    flattenedFields.addAll(children);
                } catch (Exception e) {
                    // Fallback: Parse thủ công từng child
                    try {
                        if (field.getChildren().isArray()) {
                            for (int i = 0; i < field.getChildren().size(); i++) {
                                JsonNode childNode = field.getChildren().get(i);
                                FormField child = objectMapper.treeToValue(childNode, FormField.class);
                                flattenedFields.add(child);
                            }
                        }
                    } catch (Exception e2) {
                        System.err.println(
                                "Lỗi parse children của field " + field.getFieldKey() + ": " + e2.getMessage());
                    }
                }
            }
        }

        return flattenedFields;
    }

    // Trích xuất tất cả field IDs và section IDs từ HTML content
    // Bao gồm cả data-field-id và data-section-id
    private static List<String> extractOldFieldIds(String htmlContent) {
        List<String> fieldIds = new ArrayList<>();

        // Tìm tất cả data-field-id
        Matcher fieldMatcher = FIELD_ID_PATTERN.matcher(htmlContent);
        while (fieldMatcher.find()) {
            String fieldId = fieldMatcher.group(1);
            fieldIds.add(fieldId);
        }

        // Tìm tất cả data-section-id
        Matcher sectionMatcher = SECTION_ID_PATTERN.matcher(htmlContent);
        while (sectionMatcher.find()) {
            String sectionId = sectionMatcher.group(1);
            fieldIds.add(sectionId);
        }

        return fieldIds;
    }

    // Cập nhật các data-field-id và data-section-id trong HTML từ oldFieldId ->
    // newFieldId (UUID)
    private static String updateFieldIds(String htmlContent, Map<String, String> oldIdToNewIdMap) {
        if (htmlContent == null) {
            return htmlContent;
        }

        String result = htmlContent;

        // Cập nhật data-field-id
        Matcher fieldMatcher = FIELD_ID_PATTERN.matcher(result);
        StringBuffer sb = new StringBuffer();

        while (fieldMatcher.find()) {
            String oldFieldId = fieldMatcher.group(1);
            String newFieldId = oldIdToNewIdMap.get(oldFieldId);

            if (newFieldId != null) {
                String replacement = "data-field-id=\"" + newFieldId + "\"";
                fieldMatcher.appendReplacement(sb, replacement);
            } else {
                fieldMatcher.appendReplacement(sb, fieldMatcher.group(0));
            }
        }
        fieldMatcher.appendTail(sb);
        result = sb.toString();

        // Cập nhật data-section-id
        Matcher sectionMatcher = SECTION_ID_PATTERN.matcher(result);
        sb = new StringBuffer();

        while (sectionMatcher.find()) {
            String oldSectionId = sectionMatcher.group(1);
            String newSectionId = oldIdToNewIdMap.get(oldSectionId);

            if (newSectionId != null) {
                String replacement = "data-section-id=\"" + newSectionId + "\"";
                sectionMatcher.appendReplacement(sb, replacement);
            } else {
                sectionMatcher.appendReplacement(sb, sectionMatcher.group(0));
            }
        }
        sectionMatcher.appendTail(sb);
        result = sb.toString();

        return result;
    }

}