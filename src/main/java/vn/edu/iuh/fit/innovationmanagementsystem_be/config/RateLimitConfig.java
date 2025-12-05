package vn.edu.iuh.fit.innovationmanagementsystem_be.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "rate-limit")
@Getter
@Setter
public class RateLimitConfig {

    private boolean enabled = true;
    private int requestsPerWindow = 100;
    private int windowDurationSeconds = 60;
}
