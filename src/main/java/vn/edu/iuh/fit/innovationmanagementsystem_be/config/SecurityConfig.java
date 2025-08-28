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
                                                .requestMatchers("/api/v1/auth/**").permitAll()

                                                .requestMatchers("/api/v1/innovation-decisions/**",
                                                                "/api/v1/regulations/**", "/api/v1/chapters/**",
                                                                "/api/v1/innovation-rounds/**",
                                                                "/api/form-templates/**",
                                                                "/api/v1/form-fields/**")
                                                .hasAnyRole(QUAN_TRI_VIEN, THU_KY_QLKH_HTQT)

                                                .requestMatchers(HttpMethod.GET,
                                                                "/api/v1//departments/users/statistics",
                                                                "/api/v1//departments/{id}/users/statistics",
                                                                "/api/v1/departments/{id}/users",
                                                                "/api/v1/departments/{id}/users/active",
                                                                "/api/v1/departments/{id}/users/inactive")
                                                .hasAnyRole(QUAN_TRI_VIEN, THU_KY_QLKH_HTQT, TRUONG_KHOA)

                                                .requestMatchers(HttpMethod.POST,
                                                                "/api/v1/departments",
                                                                "/api/v1/departments/merge",
                                                                "/api/v1/departments/split",
                                                                "/api/v1/departments/{id}/merge-history",
                                                                // Form Field
                                                                "/api/v1/form-fields",
                                                                "/api/v1/form-fields/bulk")
                                                .hasAnyRole(QUAN_TRI_VIEN, THU_KY_QLKH_HTQT)

                                                .requestMatchers(HttpMethod.PUT,
                                                                "/api/v1/departments/{id}",
                                                                // Form Field
                                                                "/api/v1/form-fields/{id}",
                                                                "/api/v1/form-fields/{id}/reorder")
                                                .hasAnyRole(QUAN_TRI_VIEN, THU_KY_QLKH_HTQT)

                                                .requestMatchers(HttpMethod.DELETE,
                                                                "/api/v1/departments/{departmentId}/users/{userId}",
                                                                // Form Field
                                                                "/api/v1/form-fields/{id}")
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
