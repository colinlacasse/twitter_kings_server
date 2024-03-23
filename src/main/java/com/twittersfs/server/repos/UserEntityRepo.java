package com.twittersfs.server.repos;

import com.twittersfs.server.entities.UserEntity;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface UserEntityRepo extends JpaRepository<UserEntity, Long> {
    UserEntity findByEmail(String email);
    @Modifying
    @Query("UPDATE UserEntity u SET u.balance = :newBalance WHERE u.id = :userId")
    void updateBalanceById(Long userId, Float newBalance);
}
