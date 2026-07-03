package com.yabo.addressbook.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import org.springframework.data.domain.Page;

@Schema(description = "分页响应数据")
public class PageDTO<T> {

    @Schema(description = "数据列表")
    private List<T> content;

    @Schema(description = "总页数", example = "10")
    private int totalPages;

    @Schema(description = "总记录数", example = "100")
    private long totalElements;

    @Schema(description = "当前页码", example = "1")
    private int currentPage;

    @Schema(description = "每页大小", example = "10")
    private int size;

    public PageDTO() {
    }

    public PageDTO(Page<T> page) {
        this.content = page.getContent();
        this.totalPages = page.getTotalPages();
        this.totalElements = page.getTotalElements();
        this.currentPage = page.getNumber() + 1;
        this.size = page.getSize();
    }

    public List<T> getContent() {
        return content;
    }

    public void setContent(List<T> content) {
        this.content = content;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }

    public long getTotalElements() {
        return totalElements;
    }

    public void setTotalElements(long totalElements) {
        this.totalElements = totalElements;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }
}