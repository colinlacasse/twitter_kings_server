package com.twittersfs.server.dtos.common;

import lombok.Builder;
import lombok.Data;

import java.util.List;
@Data
@Builder
public class PageableResponse<T> {
    private List<T> elements;
    private Integer page;
    private Integer totalPages;
    private Long totalElements;
}
