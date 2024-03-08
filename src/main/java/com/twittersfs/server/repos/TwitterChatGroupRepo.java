package com.twittersfs.server.repos;

import com.twittersfs.server.entities.TwitterChatGroup;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TwitterChatGroupRepo extends JpaRepository<TwitterChatGroup, Long> {
    TwitterChatGroup findByTwitterAccount_IdAndGroupId(Long id, String groupId);
}
