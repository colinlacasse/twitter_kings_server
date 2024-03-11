package com.twittersfs.server.services;

import com.twittersfs.server.dtos.model.ModelCreate;
import com.twittersfs.server.dtos.model.ModelData;

public interface ModelService {
    void create(String email, ModelCreate model);
    void delete(Long id);
    void changeName(Long id, ModelCreate model);
}
