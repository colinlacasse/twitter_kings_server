package com.twittersfs.server.repos;

import com.twittersfs.server.entities.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserEntityRepo extends JpaRepository<UserEntity, Long> {
    UserEntity findByEmail(String email);
    @Modifying
    @Query("UPDATE UserEntity u SET u.balance = :newBalance WHERE u.id = :userId")
    void updateBalanceById(@Param("userId") Long userId,@Param("newBalance") Float newBalance);
}
