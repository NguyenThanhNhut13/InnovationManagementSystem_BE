package vn.edu.iuh.fit.innovationmanagementsystem_be.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.data.redis.connection.RedisConnectionFactory;

@Component
@Slf4j
public class RedisHealthLogger implements ApplicationRunner {

    private final RedisConnectionFactory connectionFactory;

    public RedisHealthLogger(RedisConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    @Override
    public void run(ApplicationArguments args) {
        try (var connection = connectionFactory.getConnection()) {
            String ping = connection.ping();
            log.info("Redis ping: {} (kết nối Redis thành công)", ping);
        } catch (Exception e) {
            log.error("Không thể kết nối Redis: {}", e.getMessage(), e);
        }
    }
}
