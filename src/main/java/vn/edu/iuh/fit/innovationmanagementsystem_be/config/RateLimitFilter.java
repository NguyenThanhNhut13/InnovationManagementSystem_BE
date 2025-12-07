package vn.edu.iuh.fit.innovationmanagementsystem_be.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import vn.edu.iuh.fit.innovationmanagementsystem_be.service.RateLimitService;
import vn.edu.iuh.fit.innovationmanagementsystem_be.utils.RestResponse;

import java.io.IOException;

@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private final RateLimitService rateLimitService;
    private final RateLimitConfig rateLimitConfig;
    private final ObjectMapper objectMapper;

    public RateLimitFilter(RateLimitService rateLimitService, RateLimitConfig rateLimitConfig,
            ObjectMapper objectMapper) {
        this.rateLimitService = rateLimitService;
        this.rateLimitConfig = rateLimitConfig;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        if (!rateLimitConfig.isEnabled()) {
            filterChain.doFilter(request, response);
            return;
        }

        String clientKey = getClientKey(request);

        if (!rateLimitService.isAllowed(clientKey)) {
            int remaining = rateLimitService.getRemainingRequests(clientKey);
            long retryAfter = rateLimitService.getRetryAfterSeconds(clientKey);

            response.setHeader("X-RateLimit-Limit", String.valueOf(rateLimitConfig.getRequestsPerWindow()));
            response.setHeader("X-RateLimit-Remaining", String.valueOf(remaining));
            response.setHeader("Retry-After", String.valueOf(retryAfter));

            RestResponse<String> errorResponse = new RestResponse<>();
            errorResponse.setStatusCode(429);
            errorResponse.setMessage("Quá nhiều request đến server. Vui lòng thử lại sau " + retryAfter + " giây.");
            errorResponse.setData(null);

            response.setStatus(429);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding("UTF-8");

            String jsonResponse = objectMapper.writeValueAsString(errorResponse);
            response.getWriter().write(jsonResponse);
            return;
        }

        int remaining = rateLimitService.getRemainingRequests(clientKey);
        response.setHeader("X-RateLimit-Limit", String.valueOf(rateLimitConfig.getRequestsPerWindow()));
        response.setHeader("X-RateLimit-Remaining", String.valueOf(remaining));

        filterChain.doFilter(request, response);
    }

    private String getClientKey(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/actuator/health") ||
                path.startsWith("/api-docs") ||
                path.startsWith("/swagger-ui");
    }
}
