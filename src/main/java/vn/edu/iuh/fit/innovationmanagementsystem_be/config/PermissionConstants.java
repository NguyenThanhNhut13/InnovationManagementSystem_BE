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
                        "/api/v1/users" // Method POST
        };

        // CA Endpoints - chỉ Admin mới có quyền truy cập
        public static final String[] CA_ADMIN_ENDPOINTS = {
                        "/api/ca/csr/*/verify",
                        "/api/ca/csr/*/issue",
                        "/api/ca/certificate/*/revoke",
                        "/api/ca/csr/pending",
                        "/api/ca/certificates/issued"
        };

        // CA Endpoints - User có thể truy cập
        public static final String[] CA_USER_ENDPOINTS = {
                        "/api/ca/csr/create",
                        "/api/ca/certificate/*/status",
                        "/api/ca/csr/*"
        };
}
