package vn.edu.iuh.fit.innovationmanagementsystem_be.config;

import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.UserRoleEnum;
import vn.edu.iuh.fit.innovationmanagementsystem_be.exception.CustomAccessDeniedHandler;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

        private final String QUAN_TRI_VIEN = UserRoleEnum.QUAN_TRI_VIEN.name();
        private final String THU_KY_QLKH_HTQT = UserRoleEnum.THU_KY_QLKH_HTQT.name();
        private final String TRUONG_KHOA = UserRoleEnum.TRUONG_KHOA.name();
        // private final String THU_KY_KHOA = UserRoleEnum.THU_KY_KHOA.name();
        // private final String TV_HOI_DONG_KHOA = UserRoleEnum.TV_HOI_DONG_KHOA.name();
        // private final String TV_HOI_DONG_TRUONG =
        // UserRoleEnum.TV_HOI_DONG_TRUONG.name();

        private final JwtBlacklistFilter jwtBlacklistFilter;
        private final JwtDecoder jwtDecoder;
        private final JwtAuthenticationConverter jwtAuthenticationConverter;

        public SecurityConfig(JwtBlacklistFilter jwtBlacklistFilter,
                        JwtDecoder jwtDecoder,
                        JwtAuthenticationConverter jwtAuthenticationConverter) {
                this.jwtBlacklistFilter = jwtBlacklistFilter;
                this.jwtDecoder = jwtDecoder;
                this.jwtAuthenticationConverter = jwtAuthenticationConverter;
        }

        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http,
                        CustomAuthenticationEntryPoint customAuthenticationEntryPoint,
                        CustomAccessDeniedHandler customAccessDeniedHandler) throws Exception {

                http
                                .csrf(csrf -> csrf.disable())
                                .cors(Customizer.withDefaults())
                                .authorizeHttpRequests(auth -> auth

                                                // Public endpoints
                                                .requestMatchers(EndpointConstants.AUTH_PUBLIC).permitAll()
                                                .requestMatchers(EndpointConstants.SWAGGER_PUBLIC).permitAll()
                                                .requestMatchers(EndpointConstants.UTILS_PUBLIC).permitAll()

                                                // Auth endpoints (require authentication)
                                                .requestMatchers(HttpMethod.GET, EndpointConstants.AUTH_GET)
                                                .authenticated()
                                                .requestMatchers(HttpMethod.POST, EndpointConstants.USER_PUBLIC[0])
                                                .permitAll()
                                                .requestMatchers(HttpMethod.GET, EndpointConstants.USER_PUBLIC[1])
                                                .permitAll()
                                                .requestMatchers(HttpMethod.PUT, EndpointConstants.USER_PUBLIC[2])
                                                .permitAll()

                                                // User
                                                .requestMatchers(HttpMethod.GET, EndpointConstants.USER_GET)
                                                .hasAnyRole(QUAN_TRI_VIEN, THU_KY_QLKH_HTQT)

                                                // Role Management
                                                .requestMatchers(HttpMethod.POST,
                                                                EndpointConstants.ROLE_MANAGEMENT_POST)
                                                .hasRole(THU_KY_QLKH_HTQT)
                                                .requestMatchers(HttpMethod.DELETE,
                                                                EndpointConstants.ROLE_MANAGEMENT_DELETE)
                                                .hasRole(THU_KY_QLKH_HTQT)

                                                // Department
                                                .requestMatchers(HttpMethod.GET, EndpointConstants.DEPARTMENT_GET)
                                                .hasAnyRole(QUAN_TRI_VIEN, THU_KY_QLKH_HTQT)
                                                .requestMatchers(HttpMethod.POST, EndpointConstants.DEPARTMENT_POST)
                                                .hasAnyRole(QUAN_TRI_VIEN, THU_KY_QLKH_HTQT)
                                                .requestMatchers(HttpMethod.PUT, EndpointConstants.DEPARTMENT_PUT)
                                                .hasAnyRole(QUAN_TRI_VIEN, THU_KY_QLKH_HTQT)

                                                // Innovation Decision
                                                .requestMatchers(HttpMethod.POST,
                                                                EndpointConstants.INNOVATION_DECISION_POST)
                                                .hasAnyRole(QUAN_TRI_VIEN, THU_KY_QLKH_HTQT)
                                                .requestMatchers(HttpMethod.PUT,
                                                                EndpointConstants.INNOVATION_DECISION_PUT)
                                                .hasAnyRole(QUAN_TRI_VIEN, THU_KY_QLKH_HTQT)

                                                // Chapter
                                                .requestMatchers(HttpMethod.POST, EndpointConstants.CHAPTER_POST)
                                                .hasAnyRole(QUAN_TRI_VIEN, THU_KY_QLKH_HTQT)
                                                .requestMatchers(HttpMethod.PUT, EndpointConstants.CHAPTER_PUT)
                                                .hasAnyRole(QUAN_TRI_VIEN, THU_KY_QLKH_HTQT)

                                                // Regulation
                                                .requestMatchers(HttpMethod.POST, EndpointConstants.REGULATION_POST)
                                                .hasAnyRole(QUAN_TRI_VIEN, THU_KY_QLKH_HTQT)
                                                .requestMatchers(HttpMethod.PUT, EndpointConstants.REGULATION_PUT)
                                                .hasAnyRole(QUAN_TRI_VIEN, THU_KY_QLKH_HTQT)

                                                // Innovation Round
                                                .requestMatchers(HttpMethod.POST,
                                                                EndpointConstants.INNOVATION_ROUND_POST)
                                                .hasAnyRole(QUAN_TRI_VIEN, THU_KY_QLKH_HTQT)
                                                .requestMatchers(HttpMethod.PUT, EndpointConstants.INNOVATION_ROUND_PUT)
                                                .hasAnyRole(QUAN_TRI_VIEN, THU_KY_QLKH_HTQT)

                                                // Innovation Phase
                                                .requestMatchers(HttpMethod.POST,
                                                                EndpointConstants.INNOVATION_PHASE_POST)
                                                .hasAnyRole(QUAN_TRI_VIEN, THU_KY_QLKH_HTQT)
                                                .requestMatchers(HttpMethod.PUT, EndpointConstants.INNOVATION_PHASE_PUT)
                                                .hasAnyRole(QUAN_TRI_VIEN, THU_KY_QLKH_HTQT)

                                                // Innovation Department Phase - Only TRUONG_KHOA and THU_KY_QLKH_HTQT
                                                // can access
                                                .requestMatchers(HttpMethod.POST,
                                                                EndpointConstants.DEPARTMENT_PHASE_POST)
                                                .hasAnyRole(TRUONG_KHOA, THU_KY_QLKH_HTQT)
                                                .requestMatchers(HttpMethod.GET, EndpointConstants.DEPARTMENT_PHASE_GET)
                                                .hasAnyRole(TRUONG_KHOA, THU_KY_QLKH_HTQT, QUAN_TRI_VIEN)
                                                .requestMatchers(HttpMethod.PUT, EndpointConstants.DEPARTMENT_PHASE_PUT)
                                                .hasAnyRole(TRUONG_KHOA, THU_KY_QLKH_HTQT)

                                                // Form Template
                                                .requestMatchers(HttpMethod.POST, EndpointConstants.FORM_TEMPLATE_POST)
                                                .hasAnyRole(QUAN_TRI_VIEN, THU_KY_QLKH_HTQT)
                                                .requestMatchers(HttpMethod.PUT, EndpointConstants.FORM_TEMPLATE_PUT)
                                                .hasAnyRole(QUAN_TRI_VIEN, THU_KY_QLKH_HTQT)

                                                // Form Field
                                                .requestMatchers(HttpMethod.POST, EndpointConstants.FORM_FIELD_POST)
                                                .hasAnyRole(QUAN_TRI_VIEN, THU_KY_QLKH_HTQT)
                                                .requestMatchers(HttpMethod.PUT, EndpointConstants.FORM_FIELD_PUT)
                                                .hasAnyRole(QUAN_TRI_VIEN, THU_KY_QLKH_HTQT)
                                                .requestMatchers(HttpMethod.DELETE, EndpointConstants.FORM_FIELD_DELETE)
                                                .hasAnyRole(QUAN_TRI_VIEN, THU_KY_QLKH_HTQT)

                                                .anyRequest().authenticated())
                                .oauth2ResourceServer(oauth2 -> oauth2
                                                .jwt(jwt -> jwt
                                                                .jwtAuthenticationConverter(
                                                                                jwtAuthenticationConverter)
                                                                .decoder(jwtDecoder))
                                                .authenticationEntryPoint(customAuthenticationEntryPoint)
                                                .accessDeniedHandler(customAccessDeniedHandler))
                                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                                .addFilterBefore(jwtBlacklistFilter,
                                                org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter.class);

                return http.build();
        }

        @Bean
        public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
                return config.getAuthenticationManager();
        }

        @Bean
        public PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder();
        }

        // JWT configuration moved to JwtConfig
}
