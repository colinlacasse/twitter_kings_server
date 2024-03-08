package com.twittersfs.server.dtos.common;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PageableRequest {
    private final Integer page;
    private final Integer size;
}
