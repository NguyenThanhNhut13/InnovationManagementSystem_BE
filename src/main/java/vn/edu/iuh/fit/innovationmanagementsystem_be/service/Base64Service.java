package vn.edu.iuh.fit.innovationmanagementsystem_be.service;

import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Service
public class Base64Service {

    // 1. Encode text to Base64
    public String encode(String plainText) {
        if (plainText == null) {
            return "";
        }
        return Base64.getEncoder().encodeToString(plainText.getBytes(StandardCharsets.UTF_8));
    }

    // 2. Decode Base64 to text
    public String decode(String base64Text) {
        if (base64Text == null || base64Text.isBlank()) {
            return "";
        }
        byte[] decodedBytes = Base64.getDecoder().decode(base64Text);
        return new String(decodedBytes, StandardCharsets.UTF_8);
    }
}
