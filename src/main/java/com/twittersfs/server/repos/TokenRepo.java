package com.twittersfs.server.repos;

import com.twittersfs.server.entities.TokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TokenRepo extends JpaRepository<TokenEntity, Long> {
    TokenEntity findByEmail(String email);
}
