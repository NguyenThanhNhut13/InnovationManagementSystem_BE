package vn.edu.iuh.fit.innovationmanagementsystem_be.config;

import java.util.Arrays;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
public class CorsConfig {

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // cho phép các URL nào có thể kết nối tới backend
        configuration.setAllowedOrigins(
                Arrays.asList(
                        "http://localhost:3000",
                        "https://localhost:3000",
                        "http://localhost:9000",
                        "https://localhost:9000",
                        "http://localhost:9001",
                        "https://localhost:9001",
                        "http://innovation-management-system-fe.vercel.app",
                        "https://innovation-management-system-fe.vercel.app"));

        // các method nào đc kết nối
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));

        // các phần header được phép gửi lên
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "Accept"));

        // gửi kèm cookies hay không
        configuration.setAllowCredentials(true);

        // thời gian pre-flight request có thể cache (tính theo seconds)
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // cấu hình cors cho tất cả api
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

}
