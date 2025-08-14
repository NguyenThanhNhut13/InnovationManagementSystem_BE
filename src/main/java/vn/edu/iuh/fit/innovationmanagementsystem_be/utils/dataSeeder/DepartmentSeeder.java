package vn.edu.iuh.fit.innovationmanagementsystem_be.utils.dataSeeder;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.Department;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.DepartmentRepository;

import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DepartmentSeeder implements DatabaseSeeder {

    private final DepartmentRepository departmentRepository;

    @Value("${app.seeding.department.enabled:true}")
    private boolean enabled;

    @Value("${app.seeding.department.force:false}")
    private boolean force;

    @Override
    public void seed() {
        if (!enabled) {
            log.info("Department seeding bị tắt.");
            return;
        }

        log.info("Bắt đầu seed dữ liệu khoa viện...");

        if (!force && isDataExists()) {
            log.info("Dữ liệu khoa viện đã tồn tại, bỏ qua seeding.");
            return;
        }

        if (force) {
            log.info("Force seeding: Xóa dữ liệu cũ và tạo mới...");
            departmentRepository.deleteAll();
        }

        List<Department> departments = createDefaultDepartments();
        departmentRepository.saveAll(departments);

        log.info("Đã seed thành công {} khoa viện.", departments.size());
    }

    @Override
    public int getOrder() {
        return 1;
    }

    private boolean isDataExists() {
        return departmentRepository.count() > 0;
    }

    private List<Department> createDefaultDepartments() {
        return Arrays.asList(
                // Khoa Luật và Chính trị
                createDepartment("Khoa Luật", "LUAT"),
                createDepartment("Khoa Lý luận Chính trị", "LLCT"),
                createDepartment("Khoa Luật và Khoa học chính trị", "LKHCT"),

                // Khoa Kỹ thuật
                createDepartment("Khoa Kỹ thuật Xây dựng", "KTXD"),
                createDepartment("Khoa Công nghệ Cơ khí", "CNCK"),
                createDepartment("Khoa Công nghệ Điện", "CND"),
                createDepartment("Khoa Công nghệ Điện tử", "CNDT"),
                createDepartment("Khoa Công nghệ Động lực", "CNDL"),
                createDepartment("Khoa Công nghệ Nhiệt - Lạnh", "CNNL"),
                createDepartment("Khoa Công nghệ May - Thời trang", "CNMT"),
                createDepartment("Khoa Công nghệ Hóa học", "CNHH"),

                // Khoa Công nghệ Thông tin
                createDepartment("Khoa Công nghệ Thông tin", "CNTT"),

                // Khoa Kinh tế và Quản trị
                createDepartment("Khoa Quản trị Kinh doanh", "QTKD"),
                createDepartment("Khoa Thương mại - Du lịch", "TMDL"),

                // Khoa Ngoại ngữ và Giáo dục
                createDepartment("Khoa Ngoại ngữ", "NN"),
                createDepartment("Khoa Giáo dục thường xuyên", "GDTX"),

                // Viện nghiên cứu
                createDepartment("Viện Công nghệ Sinh học và Thực phẩm", "VCNSTP"),
                createDepartment("Viện Khoa học Công nghệ và Quản lý Môi trường", "VKHCNQLMT"),
                createDepartment("Viện Tài chính - Kế toán", "VTCKT"),

                // Khoa cơ bản
                createDepartment("Khoa Khoa học Cơ bản", "KHCB"),

                // Trung tâm
                createDepartment("Trung tâm Giáo dục quốc phòng và Thể chất", "TTGDQPTC"));
    }

    private Department createDepartment(String name, String code) {
        Department department = new Department();
        department.setDepartmentName(name);
        department.setDepartmentCode(code);
        return department;
    }
}