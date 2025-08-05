package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageResponseDTO<T> {

    private List<T> content;

    private Integer currentPage;

    private Integer pageSize;

    private Integer totalPages;

    private Long totalElements;

    private Boolean hasPrevious;

    private Boolean hasNext;

    private String sortBy;

    private String sortDirection;

    // Constructor từ Spring Page
    public PageResponseDTO(org.springframework.data.domain.Page<T> page, String sortBy, String sortDirection) {
        this.content = page.getContent();
        this.currentPage = page.getNumber();
        this.pageSize = page.getSize();
        this.totalPages = page.getTotalPages();
        this.totalElements = page.getTotalElements();
        this.hasPrevious = page.hasPrevious();
        this.hasNext = page.hasNext();
        this.sortBy = sortBy;
        this.sortDirection = sortDirection;
    }

    // Constructor tùy chỉnh
    public PageResponseDTO(List<T> content, Integer currentPage, Integer pageSize,
            Long totalElements, String sortBy, String sortDirection) {
        this.content = content;
        this.currentPage = currentPage;
        this.pageSize = pageSize;
        this.totalElements = totalElements;
        this.totalPages = (int) Math.ceil((double) totalElements / pageSize);
        this.hasPrevious = currentPage > 0;
        this.hasNext = currentPage < totalPages - 1;
        this.sortBy = sortBy;
        this.sortDirection = sortDirection;
    }
}