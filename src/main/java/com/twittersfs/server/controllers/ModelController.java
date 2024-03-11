package com.twittersfs.server.controllers;

import com.twittersfs.server.dtos.model.ModelCreate;
import com.twittersfs.server.dtos.model.ModelData;
import com.twittersfs.server.dtos.model.ModelDto;
import com.twittersfs.server.services.ModelService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/model")
public class ModelController {
    private final ModelService modelService;

    public ModelController(ModelService modelService) {
        this.modelService = modelService;
    }

    @PostMapping
    public void create(Authentication authentication,
                       @Valid @NotNull(message = "Request body must not be null") @RequestBody ModelCreate dto) {
        modelService.create(authentication.getPrincipal().toString(), dto);
    }

    @PatchMapping("/{id}")
    public void changeName(@PathVariable Long id,
                           @Valid @NotNull(message = "Request body must not be null") @RequestBody ModelCreate dto) {
        modelService.changeName(id, dto);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        modelService.delete(id);
    }
}
