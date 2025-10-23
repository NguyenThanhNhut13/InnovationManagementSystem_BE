package vn.edu.iuh.fit.innovationmanagementsystem_be.config;

public final class EndpointConstants {

        private EndpointConstants() {
        }

        // ================== AUTH ==================
        public static final String[] PUBLIC = {
                        "/api/v1/auth/login",
                        "/api/v1/auth/refresh",
                        "/api/v1/auth/forgot-password",
                        "/api/v1/auth/reset-password",
                        // SWAGGER ENDPOINTS
                        "/swagger-ui/**",
                        "/swagger-ui.html",
                        "/v3/api-docs/**",
                        "/api-docs",
                        "/api-docs/**",
                        "/swagger-resources/**",
                        "/webjars/**",
                        // UTILS ENDPOINTS
                        "/api/v1/utils/download/{fileName}",
                        "/api/v1/utils/info/{fileName}",
                        "/api/v1/utils/ping",
                        "/api/v1/utils/exists/{fileName}",
                        "/api/v1/utils/view/{fileName}"

        };

        public static final String[] AUTH_POST = {
                        "/api/v1/auth/logout",
                        "/api/v1/auth/change-password"
        };

        // ================== USER ==================
        public static final String[] USER_POST = {
                        "/api/v1/users"
        };

        public static final String[] USER_PUT = {
                        "/api/v1/users/profile"
        };

        // ================== ROLE MANAGEMENT ==================
        public static final String[] ROLE_MANAGEMENT_POST = {
                        "/api/v1/users/{userId}/roles/{roleId}"
        };

        public static final String[] ROLE_MANAGEMENT_DELETE = {
                        "/api/v1/users/{userId}/roles/{roleId}"
        };

        // ================== DEPARTMENT ==================
        public static final String[] DEPARTMENT_GET = {
                        "/api/v1/departments",
                        "/api/v1/departments/innovations/statistics"
        };

        // ================== INNOVATION DECISION ==================
        public static final String[] INNOVATION_DECISION_GET = {
                        "/api/v1/innovation-decisions",
                        "/api/v1/innovation-decisions/{id}"
        };
        // ================== INNOVATION ==================
        public static final String[] INNOVATION_GET = {
                        "/api/v1/innovations/statistics"
        };
        // ================== INNOVATION ROUND ==================
        public static final String[] INNOVATION_ROUND_GET = {
                        "/api/v1/innovation-rounds",
                        "/api/v1/innovation-rounds/list",
                        "/api/v1/innovation-rounds/current"
        };

        public static final String[] INNOVATION_ROUND_POST = {
                        "/api/v1/innovation-rounds"
        };

        public static final String[] INNOVATION_ROUND_PUT = {
                        "/api/v1/innovation-rounds/{roundId}"
        };

        // ================== FORM TEMPLATE ==================
        public static final String[] FORM_TEMPLATE_GET = {
                        "/api/v1/form-templates/{id}",
                        "/api/v1/form-templates/innovation-round/current",
                        "/api/v1/form-templates",
                        "/api/v1/form-templates/library"
        };

        public static final String[] FORM_TEMPLATE_POST = {
                        "/api/v1/form-templates/with-fields",
                        "/api/v1/form-templates"
        };

        public static final String[] FORM_TEMPLATE_PUT = {
                        "/api/v1/form-templates/{id}"
        };

        public static final String[] FORM_TEMPLATE_DELETE = {
                        "/api/v1/form-templates/{id}"
        };

        public static final String[] UTILS_POST = {
                        "/api/v1/utils/upload",
                        "/api/v1/utils/upload-multiple",
                        "/api/v1/utils/doc-to-html",
                        "/api/v1/utils/convert-word-to-html"
        };

        public static final String[] UTILS_DELETE = {
                        "/api/v1/utils/delete/{fileName}"
        };

}
