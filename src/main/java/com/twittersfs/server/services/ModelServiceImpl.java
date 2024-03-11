package com.twittersfs.server.services;

import com.twittersfs.server.dtos.model.ModelCreate;
import com.twittersfs.server.entities.ModelEntity;
import com.twittersfs.server.entities.UserEntity;
import com.twittersfs.server.repos.ModelRepo;
import com.twittersfs.server.repos.UserEntityRepo;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import static java.util.Objects.nonNull;

@Service
public class ModelServiceImpl implements ModelService {
    private final ModelRepo modelRepo;
    private final UserEntityRepo userRepo;

    public ModelServiceImpl(ModelRepo modelRepo, UserEntityRepo userRepo, TwitterAccountServiceImpl twitterAccountService) {
        this.modelRepo = modelRepo;
        this.userRepo = userRepo;
    }

    @Override
    @Transactional
    public void create(String email, ModelCreate dto) {
        UserEntity user = userRepo.findByEmail(email);
        modelRepo.save(fromModelCreate(user, dto));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        ModelEntity modelEntity = modelRepo.findById(id).orElseThrow(()-> new RuntimeException("Model with such id does not exist"));
        modelEntity.setUser(null);
        modelRepo.delete(modelEntity);
    }

    @Override
    @Transactional
    public void changeName(Long id, ModelCreate dto) {
        ModelEntity modelEntity = modelRepo.findById(id).orElseThrow(()-> new RuntimeException("Model with such id does not exist"));
        if(nonNull(dto.getName())){
            modelEntity.setName(dto.getName());
        }
        modelRepo.save(modelEntity);
    }

    private ModelEntity fromModelCreate(UserEntity user, ModelCreate dto) {
        return ModelEntity.builder()
                .name(dto.getName())
                .user(user)
                .build();
    }
}
