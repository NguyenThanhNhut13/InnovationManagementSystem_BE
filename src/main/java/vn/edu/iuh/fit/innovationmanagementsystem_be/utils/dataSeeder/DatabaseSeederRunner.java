package vn.edu.iuh.fit.innovationmanagementsystem_be.utils.dataSeeder;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
@Order(1)
public class DatabaseSeederRunner implements CommandLineRunner {

    private final List<DatabaseSeeder> seeders;

    @Override
    public void run(String... args) throws Exception {
        log.info("Bắt đầu chạy Database Seeders...");

        // Sắp xếp seeders theo thứ tự
        seeders.sort(Comparator.comparingInt(DatabaseSeeder::getOrder));

        for (DatabaseSeeder seeder : seeders) {
            try {
                log.info("Đang chạy seeder: {}", seeder.getClass().getSimpleName());
                seeder.seed();
            } catch (Exception e) {
                log.error("Lỗi khi chạy seeder {}: {}", seeder.getClass().getSimpleName(), e.getMessage(), e);
            }
        }

        log.info("Hoàn thành chạy Database Seeders.");
    }
}