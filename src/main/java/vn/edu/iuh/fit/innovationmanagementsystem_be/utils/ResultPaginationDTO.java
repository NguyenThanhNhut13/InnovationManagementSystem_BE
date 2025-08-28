package vn.edu.iuh.fit.innovationmanagementsystem_be.utils;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResultPaginationDTO {
    private Meta meta;
    private Object result;

    @Getter
    @Setter
    public class Meta {
        private int page;
        private int pageSize;
        private int pages;
        private long total;
    }

}
