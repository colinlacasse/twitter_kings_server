package com.twittersfs.server.repos;

import com.twittersfs.server.entities.TwitterAccount;
import com.twittersfs.server.entities.UserEntity;
import org.hibernate.query.Page;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserEntityRepo extends JpaRepository<UserEntity, Long> {
    UserEntity findByEmail(String email);

}
