package com.twittersfs.server.repos;

import com.twittersfs.server.entities.TwitterChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TwitterChatMessageRepo extends JpaRepository<TwitterChatMessage, Long> {
}
