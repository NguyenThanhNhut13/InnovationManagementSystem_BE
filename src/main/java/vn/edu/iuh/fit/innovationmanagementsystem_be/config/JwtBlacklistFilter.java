package vn.edu.iuh.fit.innovationmanagementsystem_be.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import vn.edu.iuh.fit.innovationmanagementsystem_be.service.RedisTokenService;
import vn.edu.iuh.fit.innovationmanagementsystem_be.utils.RestResponse;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtBlacklistFilter extends OncePerRequestFilter {

    private final RedisTokenService redisTokenService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        try {
            String token = extractTokenFromRequest(request);

            if (StringUtils.hasText(token)) {
                // Check if token is blacklisted
                if (redisTokenService.isAccessTokenBlacklisted(token)) {

                    RestResponse<String> errorResponse = new RestResponse<>();
                    errorResponse.setStatusCode(401);
                    errorResponse.setError("TOKEN_BLACKLISTED");
                    errorResponse.setMessage("Token đã bị vô hiệu hóa");
                    errorResponse.setData(null);

                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                    response.setCharacterEncoding("UTF-8");

                    // Convert RestResponse thành JSON và trả về
                    String jsonResponse = objectMapper.writeValueAsString(errorResponse);
                    response.getWriter().write(jsonResponse);
                    return;
                }
            }

            filterChain.doFilter(request, response);

        } catch (Exception e) {
            filterChain.doFilter(request, response);
        }
    }

    private String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        // Không filter các endpoint public
        return path.startsWith("/api/v1/auth/login") ||
                path.startsWith("/api/v1/auth/refresh") ||
                path.startsWith("/api/v1/auth/logout") ||
                path.startsWith("/api/v1/auth/forgot-password") ||
                path.startsWith("/api/v1/auth/reset-password");
    }
}
