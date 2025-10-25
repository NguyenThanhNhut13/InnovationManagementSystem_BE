package vn.edu.iuh.fit.innovationmanagementsystem_be.utils.dataSeeder;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class TemplateSeeder implements DatabaseSeeder {

    private final JdbcTemplate jdbcTemplate;

    @Value("${seeder.template.enabled:true}")
    private boolean enabled;

    @Value("${seeder.template.force:false}")
    private boolean force;

    @Override
    public void seed() {
        if (!isEnabled()) {
            log.info("{} seeding bị tắt.", getSeederName());
            return;
        }

        log.info("Bắt đầu seed dữ liệu {}...", getConfigPrefix());

        if (!isForce() && isDataExists()) {
            log.info("Dữ liệu {} đã tồn tại, bỏ qua seeding.", getConfigPrefix());
            return;
        }

        if (isForce()) {
            log.info("Force seeding: Xóa dữ liệu cũ và tạo mới...");
            clearTemplateData();
        }

        try {
            importTemplateLibrary();
            log.info("Đã seed thành công dữ liệu template từ template-library.sql");
        } catch (Exception e) {
            log.error("Lỗi khi import template-library.sql: {}", e.getMessage(), e);
            throw new RuntimeException("Không thể import template-library.sql", e);
        }
    }

    @Override
    public int getOrder() {
        return 3; // Chạy sau RoleSeeder, DepartmentSeeder, UserSeeder
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public boolean isForce() {
        return force;
    }

    private boolean isDataExists() {
        try {
            // Kiểm tra xem đã có dữ liệu template chưa
            String countQuery = "SELECT COUNT(*) FROM form_templates";
            Integer count = jdbcTemplate.queryForObject(countQuery, Integer.class);
            return count != null && count > 0;
        } catch (Exception e) {
            log.debug("Bảng form_templates chưa tồn tại hoặc chưa có dữ liệu: {}", e.getMessage());
            return false;
        }
    }

    @Transactional
    private void importTemplateLibrary() throws IOException {
        ClassPathResource resource = new ClassPathResource("template-library.sql");

        if (!resource.exists()) {
            log.warn("File template-library.sql không tồn tại trong resources");
            return;
        }

        log.info("Đang import dữ liệu từ template-library.sql...");

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {

            List<String> statements = parseSqlStatements(reader);

            for (String statement : statements) {
                if (!statement.trim().isEmpty()) {
                    try {
                        jdbcTemplate.execute(statement);
                        log.debug("Đã thực thi SQL statement thành công");
                    } catch (Exception e) {
                        log.warn("Lỗi khi thực thi SQL statement: {} - {}",
                                statement.substring(0, Math.min(100, statement.length())), e.getMessage());
                        // Tiếp tục với statement tiếp theo thay vì dừng lại
                    }
                }
            }
        }
    }

    private List<String> parseSqlStatements(BufferedReader reader) throws IOException {
        List<String> statements = new ArrayList<>();
        StringBuilder currentStatement = new StringBuilder();
        String line;

        while ((line = reader.readLine()) != null) {
            // Bỏ qua comment và empty lines
            if (line.trim().startsWith("--") || line.trim().isEmpty()) {
                continue;
            }

            // Bỏ qua MySQL specific statements
            if (line.trim().startsWith("/*!") || line.trim().startsWith("SET @")) {
                continue;
            }

            currentStatement.append(line).append("\n");

            // Kết thúc statement khi gặp dấu ;
            if (line.trim().endsWith(";")) {
                String statement = currentStatement.toString().trim();
                if (!statement.isEmpty()) {
                    statements.add(statement);
                }
                currentStatement = new StringBuilder();
            }
        }

        // Thêm statement cuối cùng nếu có
        String finalStatement = currentStatement.toString().trim();
        if (!finalStatement.isEmpty()) {
            statements.add(finalStatement);
        }

        return statements;
    }

    @Transactional
    private void clearTemplateData() {
        try {
            log.info("Đang xóa dữ liệu template cũ...");

            // Xóa theo thứ tự để tránh foreign key constraint
            jdbcTemplate.execute("DELETE FROM form_fields");
            jdbcTemplate.execute("DELETE FROM form_templates");

            log.info("Đã xóa dữ liệu template cũ thành công");
        } catch (Exception e) {
            log.warn("Lỗi khi xóa dữ liệu template cũ: {}", e.getMessage());
        }
    }
}
