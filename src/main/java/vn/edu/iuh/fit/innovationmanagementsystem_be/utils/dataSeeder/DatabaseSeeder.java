package vn.edu.iuh.fit.innovationmanagementsystem_be.utils.dataSeeder;

public interface DatabaseSeeder {
    void seed();

    int getOrder();

    default boolean isEnabled() {
        return true; // Mặc định bật
    }

    default boolean isForce() {
        return false; // Mặc định không force
    }

    default String getSeederName() {
        return this.getClass().getSimpleName();
    }

    default String getConfigPrefix() {
        // Tự động tạo config prefix từ tên class
        // Ví dụ: DepartmentSeeder -> department
        String className = getSeederName();
        if (className.endsWith("Seeder")) {
            className = className.substring(0, className.length() - 7);
        }
        return className.toLowerCase();
    }
}