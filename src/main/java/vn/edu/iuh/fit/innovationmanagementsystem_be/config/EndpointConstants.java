package vn.edu.iuh.fit.innovationmanagementsystem_be.config;

public final class EndpointConstants {

        private EndpointConstants() {
        }

        // ================== AUTH ==================
        public static final String[] AUTH_PUBLIC = {
                        "/api/v1/auth/login",
                        "/api/v1/auth/refresh",
                        "/api/v1/auth/forgot-password",
                        "/api/v1/auth/reset-password"
        };

        public static final String[] AUTH_GET = {
                        "/api/v1/auth/me"
        };

        // ================== USER ==================
        public static final String[] USER_PUBLIC = {
                        "/api/v1/users", // POST
                        "/api/v1/users/{id}", // GET
                        "/api/v1/users/{id}" // PUT
        };

        public static final String[] USER_GET = {
                        "/api/v1/users",
                        "/api/v1/users/status",
                        "/api/v1/roles/{roleId}/users",
                        "/api/v1/users/departments/{departmentId}/users"
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
                        "/api/v1/departments/users/statistics",
                        "/api/v1/departments/{id}/users/statistics",
                        "/api/v1/departments/{id}/users",
                        "/api/v1/departments/{id}/users/active",
                        "/api/v1/departments/{id}/users/inactive",
                        "/api/v1/departments/{id}/merge-history"
        };

        public static final String[] DEPARTMENT_POST = {
                        "/api/v1/departments",
                        "/api/v1/departments/merge",
                        "/api/v1/departments/split"
        };

        public static final String[] DEPARTMENT_PUT = {
                        "/api/v1/departments/{id}"
        };

        // ================== INNOVATION DECISION ==================
        public static final String[] INNOVATION_DECISION_POST = {
                        "/api/v1/innovation-decisions"
        };

        public static final String[] INNOVATION_DECISION_PUT = {
                        "/api/v1/innovation-decisions/{id}"
        };

        // ================== INNOVATION ROUND ==================
        public static final String[] INNOVATION_ROUND_POST = {
                        "/api/v1/innovation-rounds"
        };

        public static final String[] INNOVATION_ROUND_PUT = {
                        "/api/v1/innovation-rounds/{roundId}",
                        "/api/v1/innovation-rounds/{roundId}/toggle-status"
        };

        // ================== INNOVATION PHASE ==================
        public static final String[] INNOVATION_PHASE_POST = {
                        "/api/v1/innovation-phases/round/{roundId}/create-phases",
                        "/api/v1/innovation-phases/round/{roundId}/create-phase"
        };

        public static final String[] INNOVATION_PHASE_PUT = {
                        "/api/v1/innovation-phases/{phaseId}/dates",
                        "/api/v1/innovation-phases/{phaseId}/toggle-status",
                        "/api/v1/innovation-phases/{phaseId}",
                        "/api/v1/innovation-phases/{phaseId}/transition",
                        "/api/v1/innovation-phases/{phaseId}/complete",
                        "/api/v1/innovation-phases/{phaseId}/suspend",
                        "/api/v1/innovation-phases/{phaseId}/cancel"
        };

        // ================== FORM TEMPLATE ==================
        public static final String[] FORM_TEMPLATE_POST = {
                        "/api/v1/form-templates",
                        "/api/v1/form-templates/bulk"
        };

        public static final String[] FORM_TEMPLATE_PUT = {
                        "/api/v1/form-templates/{id}"
        };

        // ================== FORM FIELD ==================
        public static final String[] FORM_FIELD_POST = {
                        "/api/v1/form-fields",
                        "/api/v1/form-fields/bulk"
        };

        public static final String[] FORM_FIELD_PUT = {
                        "/api/v1/form-fields/{id}",
                        "/api/v1/form-fields/{id}/reorder"
        };

        public static final String[] FORM_FIELD_DELETE = {
                        "/api/v1/form-fields/{id}"
        };

        // ================== DEPARTMENT PHASE ==================
        public static final String[] DEPARTMENT_PHASE_POST = {
                        "/api/v1/department-phases/department/{departmentId}/create-phase",
                        "/api/v1/department-phases/department/{departmentId}/copy-from-innovation-phase/{innovationPhaseId}",
                        "/api/v1/department-phases/department/{departmentId}/round/{roundId}/create-all-phases",
                        "/api/v1/department-phases/department/{departmentId}/round/{roundId}/create-phase/{phaseType}"
        };

        public static final String[] DEPARTMENT_PHASE_GET = {
                        "/api/v1/department-phases/department/{departmentId}/phase/{phaseId}",
                        "/api/v1/department-phases/department/{departmentId}/phase/{phaseId}/current",
                        "/api/v1/department-phases/department/{departmentId}/round/{roundId}",
                        "/api/v1/department-phases/department/{departmentId}/round/{roundId}/current"
        };

        public static final String[] DEPARTMENT_PHASE_PUT = {
                        "/api/v1/department-phases/{phaseId}/dates",
                        "/api/v1/department-phases/{phaseId}",
                        "/api/v1/department-phases/{phaseId}/toggle-status"
        };

        // ================== UTILS ==================
        public static final String[] UTILS_PUBLIC = {
                        "/api/v1/utils/download/{fileName}",
                        "/api/v1/utils/info/{fileName}",
                        "/api/v1/utils/ping"
        };

        // ================== SWAGGER ==================
        public static final String[] SWAGGER_PUBLIC = {
                        "/swagger-ui/**",
                        "/swagger-ui.html",
                        "/v3/api-docs/**",
                        "/api-docs",
                        "/api-docs/**",
                        "/swagger-resources/**",
                        "/webjars/**"
        };
}
