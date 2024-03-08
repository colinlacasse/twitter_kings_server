package com.twittersfs.server.dtos.model;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class ModelDto {
    private Long id;
    private String name;
}
