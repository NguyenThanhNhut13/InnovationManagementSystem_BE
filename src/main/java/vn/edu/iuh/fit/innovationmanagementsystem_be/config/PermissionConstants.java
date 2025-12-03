package vn.edu.iuh.fit.innovationmanagementsystem_be.config;

public class PermissionConstants {

        public static final String[] PUBLIC = {
                        "/api/v1/auth/login",
                        "/api/v1/auth/refresh",
                        "/api/v1/auth/forgot-password",
                        "/api/v1/auth/reset-password",
                        "/api/v1/utils/ping",
                        "/swagger-ui/**",
                        "/swagger-ui.html",
                        "/v3/api-docs/**",
                        "/api-docs/**",
                        "/swagger-resources/**",
                        "/webjars/**",
                        "/api/v1/users", // Method POST
                        "/ws/**",
                        "https://minio9000.silenthero.xyz/innovation-management/**"
        };
}
