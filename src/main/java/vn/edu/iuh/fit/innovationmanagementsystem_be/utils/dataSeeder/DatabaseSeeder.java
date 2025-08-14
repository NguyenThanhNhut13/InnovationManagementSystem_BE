package vn.edu.iuh.fit.innovationmanagementsystem_be.utils.dataSeeder;

public interface DatabaseSeeder {
    void seed();

    int getOrder(); // Để sắp xếp thứ tự chạy seeder
}