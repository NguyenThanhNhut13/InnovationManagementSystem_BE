package vn.edu.iuh.fit.innovationmanagementsystem_be.utils;

import org.springframework.core.MethodParameter;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import jakarta.servlet.http.HttpServletResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.utils.annotation.ApiMessage;

@RestControllerAdvice
public class FormatRestResponse implements ResponseBodyAdvice<Object> {

    @Override
    public boolean supports(@NonNull MethodParameter returnType,
            @NonNull Class<? extends HttpMessageConverter<?>> converterType) {
        // Exclude OpenAPI endpoints from response formatting
        String methodName = returnType.getMethod() != null ? returnType.getMethod().getName() : "";
        String className = returnType.getContainingClass() != null ? returnType.getContainingClass().getSimpleName()
                : "";

        // Skip formatting for SpringDoc OpenAPI endpoints
        if (className.contains("OpenApi") || methodName.contains("openapi") ||
                methodName.contains("swagger") || methodName.contains("api-docs")) {
            return false;
        }

        // Skip formatting for file conversion/proxy endpoints that return raw content
        if (methodName.contains("convertDocToHtml")
                || methodName.contains("convertWordToHtmlViaThirdParty")
                || methodName.contains("downloadFile")) {
            return false;
        }

        // Skip formatting for base64 endpoints that return raw strings
        if (methodName.contains("encodeBase64") || methodName.contains("decodeBase64")) {
            return false;
        }

        // Skip formatting for digital signature endpoints that return raw strings
        if (methodName.contains("generateSignature") || methodName.contains("generateDocumentHash")) {
            return false;
        }

        return true;
    }

    @Override
    public Object beforeBodyWrite(@Nullable Object body, @NonNull MethodParameter returnType,
            @NonNull MediaType selectedContentType,
            @NonNull Class<? extends HttpMessageConverter<?>> selectedConverterType, @NonNull ServerHttpRequest request,
            @NonNull ServerHttpResponse response) {

        HttpServletResponse servletResponse = ((ServletServerHttpResponse) response).getServletResponse();
        int status = servletResponse.getStatus();

        RestResponse<Object> res = new RestResponse<Object>();
        res.setStatusCode(status);

        if (body instanceof Resource || status >= 400) {
            return body;
        } else if (body instanceof String) {
            res.setData(body);
            ApiMessage message = returnType.getMethodAnnotation(ApiMessage.class);
            res.setMessage(message != null ? message.value() : "Thành công");
            return res;
        } else {
            res.setData(body);
            ApiMessage message = returnType.getMethodAnnotation(ApiMessage.class);
            res.setMessage(message != null ? message.value() : "Thành công");
            return res;
        }
    }

}