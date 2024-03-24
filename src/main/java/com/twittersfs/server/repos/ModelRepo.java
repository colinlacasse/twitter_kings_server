package com.twittersfs.server.repos;

import com.twittersfs.server.entities.ModelEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ModelRepo extends JpaRepository<ModelEntity, Long> {

}
