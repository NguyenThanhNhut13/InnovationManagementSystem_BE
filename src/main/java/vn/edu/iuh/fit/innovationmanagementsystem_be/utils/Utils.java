package vn.edu.iuh.fit.innovationmanagementsystem_be.utils;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class Utils {

    public static <T> ResultPaginationDTO toResultPaginationDTO(Page<T> page, Pageable pageable) {
        ResultPaginationDTO resultPaginationDTO = new ResultPaginationDTO();
        ResultPaginationDTO.Meta meta = resultPaginationDTO.new Meta();

        meta.setPage(pageable.getPageNumber() + 1);
        meta.setPageSize(pageable.getPageSize());
        meta.setPages(page.getTotalPages());
        meta.setTotal(page.getTotalElements());

        resultPaginationDTO.setMeta(meta);
        resultPaginationDTO.setResult(page.getContent());

        return resultPaginationDTO;
    }

    public static String encode(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        return Base64.getEncoder().encodeToString(input.getBytes(StandardCharsets.UTF_8));
    }

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
