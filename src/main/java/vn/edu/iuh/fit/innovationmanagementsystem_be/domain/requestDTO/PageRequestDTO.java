package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageRequestDTO {

    private Integer page = 0;

    private Integer size = 10;

    private String sortBy = "createdAt";

    private String sortDirection = "desc";

    // Validation
    public Integer getPage() {
        return page != null ? page : 0;
    }

    public Integer getSize() {
        if (size == null || size <= 0) {
            return 10;
        }
        if (size > 100) {
            return 100; // Giới hạn tối đa 100 items per page
        }
        return size;
    }

    public String getSortBy() {
        return sortBy != null ? sortBy : "createdAt";
    }

    public String getSortDirection() {
        return "asc".equalsIgnoreCase(sortDirection) ? "asc" : "desc";
    }
}