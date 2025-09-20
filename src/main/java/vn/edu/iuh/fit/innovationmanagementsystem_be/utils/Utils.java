package vn.edu.iuh.fit.innovationmanagementsystem_be.utils;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

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
}
