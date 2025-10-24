package vn.edu.iuh.fit.innovationmanagementsystem_be.utils;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Utility class để encode và decode Base64
 */
public class Base64Utils {

    /**
     * Encode string thành Base64
     * 
     * @param input String cần encode
     * @return String đã được encode Base64
     */
    public static String encode(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        return Base64.getEncoder().encodeToString(input.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Decode Base64 thành string
     * 
     * @param encoded Base64 string cần decode
     * @return String đã được decode
     */
    public static String decode(String encoded) {
        if (encoded == null || encoded.isEmpty()) {
            return encoded;
        }
        try {
            byte[] decodedBytes = Base64.getDecoder().decode(encoded);
            return new String(decodedBytes, StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            // Nếu không phải Base64 hợp lệ, trả về string gốc
            return encoded;
        }
    }

    /**
     * Kiểm tra string có phải là Base64 hợp lệ không
     * 
     * @param input String cần kiểm tra
     * @return true nếu là Base64 hợp lệ
     */
    public static boolean isValidBase64(String input) {
        if (input == null || input.isEmpty()) {
            return false;
        }
        try {
            Base64.getDecoder().decode(input);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
