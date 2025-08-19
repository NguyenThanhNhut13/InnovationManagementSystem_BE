package vn.edu.iuh.fit.innovationmanagementsystem_be.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.security.web.access.AccessDeniedHandler;
import vn.edu.iuh.fit.innovationmanagementsystem_be.utils.RestResponse;

import java.io.IOException;

@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public void handle(HttpServletRequest req, HttpServletResponse res,
            org.springframework.security.access.AccessDeniedException ex) throws IOException {
        res.setStatus(HttpStatus.FORBIDDEN.value());
        res.setContentType("application/json;charset=UTF-8");

        RestResponse<Object> body = new RestResponse<>();
        body.setStatusCode(403);
        body.setError("Forbidden Error");
        body.setMessage("Bạn không có quyền truy cập tài nguyên này !");
        mapper.writeValue(res.getWriter(), body);
    }
}