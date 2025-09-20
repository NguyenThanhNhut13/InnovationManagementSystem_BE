package vn.edu.iuh.fit.innovationmanagementsystem_be.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
@Slf4j
public class RedisConfig {

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // Key/HashKey dùng String serializer
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());

        // Value/HashValue dùng String serializer (lưu giá trị thuần như userId,
        // "blacklisted")
        template.setValueSerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new StringRedisSerializer());

        template.afterPropertiesSet();
        return template;
    }

    @Bean
    public ApplicationRunner redisHealthLogger(RedisConnectionFactory connectionFactory) {
        return new ApplicationRunner() {
            @Override
            public void run(ApplicationArguments args) {
                try (var connection = connectionFactory.getConnection()) {
                    String ping = connection.ping();
                    log.info("Redis ping: {} (kết nối Redis thành công)", ping);
                } catch (Exception e) {
                    log.error("Không thể kết nối Redis: {}", e.getMessage(), e);
                }
            }
        };
    }
}