package vn.edu.iuh.fit.innovationmanagementsystem_be.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

@Configuration
@EnableJpaAuditing
public class AuditingConfig {

    @Bean
    public AuditorAware<String> auditorAware() {
        return () -> {
            try {
                Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                if (auth != null && auth.isAuthenticated()) {
                    String name = auth.getName();
                    if (name != null && !"anonymousUser".equalsIgnoreCase(name)) {
                        return Optional.of(name);
                    }
                }
            } catch (Exception ignored) {
            }
            return Optional.of("SYSTEM");
        };
    }
}
