package vn.edu.iuh.fit.innovationmanagementsystem_be.service.similarity;

import com.fasterxml.jackson.databind.JsonNode;
import org.jsoup.Jsoup;
import org.springframework.stereotype.Component;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.FormData;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.Innovation;

import java.text.Normalizer;
import java.util.*;
import java.util.regex.Pattern;

@Component
public class InnovationTextExtractor {

    private static final Pattern VIETNAMESE_PATTERN = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
    private static final Pattern SPECIAL_CHARS = Pattern.compile("[^a-zA-Z0-9\\s\\p{L}]");
    private static final Pattern MULTIPLE_SPACES = Pattern.compile("\\s+");

    public String extractText(Innovation innovation) {
        StringBuilder text = new StringBuilder();

        if (innovation.getInnovationName() != null) {
            text.append(innovation.getInnovationName()).append(" ");
        }

        if (innovation.getBasisText() != null) {
            text.append(innovation.getBasisText()).append(" ");
        }

        if (innovation.getFormDataList() != null) {
            for (FormData formData : innovation.getFormDataList()) {
                String fieldText = extractFieldValue(formData.getFieldValue());
                if (fieldText != null && !fieldText.trim().isEmpty()) {
                    text.append(fieldText).append(" ");
                }
            }
        }

        return text.toString().trim();
    }

    public String extractTextFromRequest(String innovationName, List<Map<String, Object>> templates) {
        StringBuilder text = new StringBuilder();

        if (innovationName != null) {
            text.append(innovationName).append(" ");
        }

        if (templates != null) {
            for (Map<String, Object> template : templates) {
                Object formData = template.get("formData");
                if (formData instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> formDataMap = (Map<String, Object>) formData;
                    for (Object value : formDataMap.values()) {
                        String fieldText = extractValueAsString(value);
                        if (fieldText != null && !fieldText.trim().isEmpty()) {
                            text.append(fieldText).append(" ");
                        }
                    }
                }
            }
        }

        return text.toString().trim();
    }

    private String extractFieldValue(JsonNode fieldValue) {
        if (fieldValue == null) {
            return null;
        }

        if (fieldValue.isTextual()) {
            return stripHtml(fieldValue.asText());
        }

        if (fieldValue.isNumber()) {
            return fieldValue.asText();
        }

        if (fieldValue.isBoolean()) {
            return fieldValue.asBoolean() ? "Có" : "Không";
        }

        if (fieldValue.isArray()) {
            StringBuilder arrayContent = new StringBuilder();
            for (JsonNode item : fieldValue) {
                String itemText = extractFieldValue(item);
                if (itemText != null && !itemText.isEmpty()) {
                    arrayContent.append(itemText).append(" ");
                }
            }
            return arrayContent.toString().trim();
        }

        if (fieldValue.isObject()) {
            StringBuilder objectContent = new StringBuilder();
            Iterator<String> fieldNames = fieldValue.fieldNames();
            while (fieldNames.hasNext()) {
                String fieldName = fieldNames.next();
                String valueText = extractFieldValue(fieldValue.get(fieldName));
                if (valueText != null && !valueText.isEmpty()) {
                    objectContent.append(valueText).append(" ");
                }
            }
            return objectContent.toString().trim();
        }

        return null;
    }

    private String extractValueAsString(Object value) {
        if (value == null) {
            return null;
        }

        if (value instanceof String) {
            return stripHtml((String) value);
        }

        if (value instanceof Number || value instanceof Boolean) {
            return value.toString();
        }

        if (value instanceof List) {
            StringBuilder listContent = new StringBuilder();
            for (Object item : (List<?>) value) {
                String itemText = extractValueAsString(item);
                if (itemText != null && !itemText.isEmpty()) {
                    listContent.append(itemText).append(" ");
                }
            }
            return listContent.toString().trim();
        }

        if (value instanceof Map) {
            StringBuilder mapContent = new StringBuilder();
            @SuppressWarnings("unchecked")
            Map<String, Object> map = (Map<String, Object>) value;
            for (Object v : map.values()) {
                String vText = extractValueAsString(v);
                if (vText != null && !vText.isEmpty()) {
                    mapContent.append(vText).append(" ");
                }
            }
            return mapContent.toString().trim();
        }

        return value.toString();
    }

    private String stripHtml(String html) {
        if (html == null || html.isEmpty()) {
            return html;
        }
        return Jsoup.parse(html).text();
    }

    public List<String> tokenize(String text) {
        if (text == null || text.isEmpty()) {
            return Collections.emptyList();
        }

        String normalized = text.toLowerCase().trim();
        normalized = SPECIAL_CHARS.matcher(normalized).replaceAll(" ");
        normalized = MULTIPLE_SPACES.matcher(normalized).replaceAll(" ");

        String[] words = normalized.split("\\s+");

        List<String> tokens = new ArrayList<>();
        for (String word : words) {
            if (word.length() >= 2) {
                tokens.add(word);
            }
        }

        return tokens;
    }

    public String removeVietnameseDiacritics(String text) {
        if (text == null) {
            return null;
        }
        String normalized = Normalizer.normalize(text, Normalizer.Form.NFD);
        return VIETNAMESE_PATTERN.matcher(normalized).replaceAll("")
                .replace('đ', 'd')
                .replace('Đ', 'D');
    }
}
