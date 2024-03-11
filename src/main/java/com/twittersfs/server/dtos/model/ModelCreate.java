package com.twittersfs.server.dtos.model;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ModelCreate {
    @NotBlank(message = "Name field must not be empty")
    private String name;
}
