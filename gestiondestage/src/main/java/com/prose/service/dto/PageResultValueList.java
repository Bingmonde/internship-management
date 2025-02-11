package com.prose.service.dto;

import com.prose.entity.ResultValue;
import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
public class PageResultValueList<T> extends ResultValue<List<T>> {
    private int pageNumber;
    private int pageSize;
    private int totalPages;

    public void setValue(Page<T> page) {
        this.pageNumber = page.getNumber();
        this.pageSize = page.getSize();
        this.totalPages = page.getTotalPages();
        this.setValue(page.getContent());
    }

    public void setValue(List<T> content,int pageNumber, int pageSize, int totalPages) {
        this.pageNumber = pageNumber;
        this.pageSize = pageSize;
        this.totalPages = totalPages;
        this.setValue(content);
    }
}
