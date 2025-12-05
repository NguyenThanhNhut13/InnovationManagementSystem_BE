package vn.edu.iuh.fit.innovationmanagementsystem_be.utils;

import org.springframework.stereotype.Component;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.FormField;

import java.util.List;

@Component
public class HtmlTemplateUtils {

    // ✅ Không cần update IDs nữa - chỉ dùng data-field-key
    // Method này giữ lại để tương thích nhưng không làm gì
    public String updateFieldIdsInHtml(String htmlContent, List<FormField> fields) {
        // Return HTML không đổi vì không còn dùng data-field-id/data-section-id
        return htmlContent;
    }

}