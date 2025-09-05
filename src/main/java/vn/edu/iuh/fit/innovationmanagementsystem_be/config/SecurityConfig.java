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
        // private final String TRUONG_KHOA = UserRoleEnum.TRUONG_KHOA.name();
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

                                                .requestMatchers(EndpointConstants.AUTH_PUBLIC).permitAll()
                                                .requestMatchers(HttpMethod.POST, EndpointConstants.USER_PUBLIC[0])
                                                .permitAll()
                                                .requestMatchers(HttpMethod.GET, EndpointConstants.USER_PUBLIC[1])
                                                .permitAll()
                                                .requestMatchers(HttpMethod.PUT, EndpointConstants.USER_PUBLIC[2])
                                                .permitAll()

                                                .requestMatchers(HttpMethod.GET, EndpointConstants.USER_GET)
                                                .hasAnyRole(QUAN_TRI_VIEN, THU_KY_QLKH_HTQT)
                                                .requestMatchers(HttpMethod.POST, EndpointConstants.USER_POST)
                                                .hasAnyRole(QUAN_TRI_VIEN, THU_KY_QLKH_HTQT)
                                                .requestMatchers(HttpMethod.DELETE, EndpointConstants.USER_DELETE)
                                                .hasAnyRole(QUAN_TRI_VIEN, THU_KY_QLKH_HTQT)

                                                .requestMatchers(HttpMethod.GET, EndpointConstants.DEPARTMENT_GET)
                                                .hasAnyRole(QUAN_TRI_VIEN, THU_KY_QLKH_HTQT)
                                                .requestMatchers(HttpMethod.POST, EndpointConstants.DEPARTMENT_POST)
                                                .hasAnyRole(QUAN_TRI_VIEN, THU_KY_QLKH_HTQT)
                                                .requestMatchers(HttpMethod.PUT, EndpointConstants.DEPARTMENT_PUT)
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
