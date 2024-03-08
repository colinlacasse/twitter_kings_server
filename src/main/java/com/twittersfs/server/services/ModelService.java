package com.twittersfs.server.services;

import com.twittersfs.server.dtos.model.ModelCreate;

public interface ModelService {
    void create(String email, ModelCreate model);
    void delete(Long id);
    void changeName(Long id, ModelCreate model);
}
