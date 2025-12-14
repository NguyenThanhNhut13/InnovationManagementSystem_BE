package vn.edu.iuh.fit.innovationmanagementsystem_be.utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Utils Unit Tests")
class UtilsTest {

    // ==================== encode Tests ====================

    @Test
    @DisplayName("1. encode - Should encode string to Base64")
    void testEncode_Success() {
        String input = "Hello World";

        String encoded = Utils.encode(input);

        assertNotNull(encoded);
        assertEquals("SGVsbG8gV29ybGQ=", encoded);
    }

    @Test
    @DisplayName("2. encode - Should handle Vietnamese characters")
    void testEncode_VietnameseCharacters() {
        String input = "Xin chào Việt Nam";

        String encoded = Utils.encode(input);

        assertNotNull(encoded);
        assertFalse(encoded.isEmpty());
        assertNotEquals(input, encoded);
    }

    @Test
    @DisplayName("3. encode - Null input should return null")
    void testEncode_NullInput_ReturnsNull() {
        String result = Utils.encode(null);

        assertNull(result);
    }

    @Test
    @DisplayName("4. encode - Empty input should return empty")
    void testEncode_EmptyInput_ReturnsEmpty() {
        String result = Utils.encode("");

        assertEquals("", result);
    }

    // ==================== decode Tests ====================

    @Test
    @DisplayName("5. decode - Should decode Base64 to string")
    void testDecode_Success() {
        String encoded = "SGVsbG8gV29ybGQ=";

        String decoded = Utils.decode(encoded);

        assertEquals("Hello World", decoded);
    }

    @Test
    @DisplayName("6. decode - Should handle Vietnamese characters")
    void testDecode_VietnameseCharacters() {
        String original = "Xin chào Việt Nam";
        String encoded = Utils.encode(original);

        String decoded = Utils.decode(encoded);

        assertEquals(original, decoded);
    }

    @Test
    @DisplayName("7. decode - Null input should return null")
    void testDecode_NullInput_ReturnsNull() {
        String result = Utils.decode(null);

        assertNull(result);
    }

    @Test
    @DisplayName("8. decode - Empty input should return empty")
    void testDecode_EmptyInput_ReturnsEmpty() {
        String result = Utils.decode("");

        assertEquals("", result);
    }

    @Test
    @DisplayName("9. decode - Invalid Base64 should return original string")
    void testDecode_InvalidBase64_ReturnsOriginal() {
        String invalidBase64 = "not-valid-base64!!!";

        String result = Utils.decode(invalidBase64);

        assertEquals(invalidBase64, result);
    }

    // ==================== Encode-Decode Round Trip Tests ====================

    @Test
    @DisplayName("10. Encode-Decode Round Trip - Should preserve original value")
    void testEncodeDecode_RoundTrip() {
        String[] testCases = {
                "Simple text",
                "Tiếng Việt có dấu",
                "Special chars: !@#$%^&*()_+-=[]{}|;':\",./<>?",
                "Numbers: 12345",
                "Mixed: Hello 123 Xin chào!"
        };

        for (String original : testCases) {
            String encoded = Utils.encode(original);
            String decoded = Utils.decode(encoded);
            assertEquals(original, decoded, "Round trip should preserve: " + original);
        }
    }

    // ==================== isValidBase64 Tests ====================

    @Test
    @DisplayName("11. isValidBase64 - Valid Base64 should return true")
    void testIsValidBase64_ValidInput_ReturnsTrue() {
        String validBase64 = "SGVsbG8gV29ybGQ=";

        assertTrue(Utils.isValidBase64(validBase64));
    }

    @Test
    @DisplayName("12. isValidBase64 - Invalid Base64 should return false")
    void testIsValidBase64_InvalidInput_ReturnsFalse() {
        String invalidBase64 = "not-valid-base64!!!";

        assertFalse(Utils.isValidBase64(invalidBase64));
    }

    @Test
    @DisplayName("13. isValidBase64 - Null input should return false")
    void testIsValidBase64_NullInput_ReturnsFalse() {
        assertFalse(Utils.isValidBase64(null));
    }

    @Test
    @DisplayName("14. isValidBase64 - Empty input should return false")
    void testIsValidBase64_EmptyInput_ReturnsFalse() {
        assertFalse(Utils.isValidBase64(""));
    }

    // ==================== toResultPaginationDTO Tests ====================

    @Test
    @DisplayName("15. toResultPaginationDTO - Should create pagination DTO correctly")
    void testToResultPaginationDTO_Success() {
        List<String> content = Arrays.asList("Item1", "Item2", "Item3");
        Pageable pageable = PageRequest.of(0, 10);
        Page<String> page = new PageImpl<>(content, pageable, 25);

        ResultPaginationDTO result = Utils.toResultPaginationDTO(page, pageable);

        assertNotNull(result);
        assertNotNull(result.getMeta());
        assertEquals(1, result.getMeta().getPage());
        assertEquals(10, result.getMeta().getPageSize());
        assertEquals(3, result.getMeta().getPages());
        assertEquals(25, result.getMeta().getTotal());
        assertEquals(content, result.getResult());
    }

    @Test
    @DisplayName("16. toResultPaginationDTO - Page number should be 1-indexed")
    void testToResultPaginationDTO_PageNumber_OneIndexed() {
        List<String> content = Arrays.asList("Item1");
        Pageable pageable = PageRequest.of(2, 5);
        Page<String> page = new PageImpl<>(content, pageable, 100);

        ResultPaginationDTO result = Utils.toResultPaginationDTO(page, pageable);

        assertEquals(3, result.getMeta().getPage(), "Page should be 1-indexed (0 -> 1, 2 -> 3)");
    }

    @Test
    @DisplayName("17. toResultPaginationDTO - Empty page should work correctly")
    void testToResultPaginationDTO_EmptyPage() {
        List<String> content = Collections.emptyList();
        Pageable pageable = PageRequest.of(0, 10);
        Page<String> page = new PageImpl<>(content, pageable, 0);

        ResultPaginationDTO result = Utils.toResultPaginationDTO(page, pageable);

        assertNotNull(result);
        assertEquals(1, result.getMeta().getPage());
        assertEquals(10, result.getMeta().getPageSize());
        assertEquals(0, result.getMeta().getPages());
        assertEquals(0, result.getMeta().getTotal());
        assertTrue(((List<?>) result.getResult()).isEmpty());
    }
}
