package com.twittersfs.server.repos;

import com.twittersfs.server.entities.TwitterAccount;
import com.twittersfs.server.enums.GroupStatus;
import com.twittersfs.server.enums.TwitterAccountStatus;


import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface TwitterAccountRepo extends JpaRepository<TwitterAccount,Long> {
    Optional<TwitterAccount> findByUsername(String username);
    Page<TwitterAccount> findByModel_User_EmailAndStatus(String userEmail, TwitterAccountStatus status, Pageable pageable);
    Page<TwitterAccount> findByModel_User_Email(String userEmail, Pageable pageable);
    Page<TwitterAccount> findByModel_Id(Long modelId, Pageable pageable);
    @Transactional
    @Modifying
    @Query("UPDATE TwitterAccount t SET t.messagesSent = t.messagesSent + 1 WHERE t.id = :accountId")
    void updateMessagesSent(@Param("accountId") Long accountId);
    @Transactional
    @Modifying
    @Query("UPDATE TwitterAccount t SET t.password = :newPassword WHERE t.id = :accountId")
    void updatePasswordById(@Param("accountId") Long accountId,@Param("newPassword") String newPassword);
    @Transactional
    @Modifying
    @Query("UPDATE TwitterAccount t SET t.csrfToken = :newCsrfToken WHERE t.id = :accountId")
    void updateCsrfToken(@Param("accountId") Long accountId, @Param("newCsrfToken") String newCsrfToken);
    @Transactional
    @Modifying
    @Query("UPDATE TwitterAccount t SET t.cookie = :newCookie WHERE t.id = :accountId")
    void updateCookie(@Param("accountId") Long accountId, @Param("newCookie") String newCookie);
    @Transactional
    @Modifying
    @Query("UPDATE TwitterAccount " +
            "SET friends = :friends, " +
            "retweets = :retweets, " +
            "friendsDifference = :friendsDifference, " +
            "retweetsDifference = :retweetsDifference, " +
            "messagesDifference = :messagesDifference " +
            "WHERE id = :accountId")
    void updateAccountFields(@Param("accountId") Long accountId,
                             @Param("friends") Integer friends,
                             @Param("retweets") Integer retweets,
                             @Param("friendsDifference") Integer friendsDifference,
                             @Param("retweetsDifference") Integer retweetsDifference,
                             @Param("messagesDifference") Integer messagesDifference);
    @Transactional
    @Modifying
    @Query("UPDATE TwitterAccount t SET t.groups = :newGroups WHERE t.id = :accountId")
    void updateGroups(@Param("accountId") Long accountId, @Param("newGroups") Integer newGroups);

    @Transactional
    @Modifying
    @Query("UPDATE TwitterAccount t SET t.restId = :newRestId WHERE t.id = :accountId")
    void updateRestId(@Param("accountId") Long accountId, @Param("newRestId") String newRestId);
    @Transactional
    @Modifying
    @Query("UPDATE TwitterAccount t SET t.status = :newStatus WHERE t.id = :accountId")
    void updateStatus(@Param("accountId") Long accountId, @Param("newStatus") TwitterAccountStatus newStatus);
    @Transactional
    @Modifying
    @Query("UPDATE TwitterAccount t SET t.email = :newEmail WHERE t.id = :accountId")
    void updateEmail(@Param("accountId") Long accountId, @Param("newEmail") String newEmail);
    @Transactional
    @Modifying
    @Query("UPDATE TwitterAccount t SET t.username = :newUsername WHERE t.id = :accountId")
    void updateUsername(@Param("accountId") Long accountId, @Param("newUsername") String newUsername);
    @Transactional
    @Modifying
    @Query("UPDATE TwitterAccount SET messagesDifference = 0 WHERE id = :accountId")
    void resetMessagesDifference(@Param("accountId") Long accountId);
    @Transactional
    @Modifying
    @Query("UPDATE TwitterAccount SET retweetsDifference = 0 WHERE id = :accountId")
    void resetRetweetsDifference(@Param("accountId") Long accountId);
    @Transactional
    @Modifying
    @Query("UPDATE TwitterAccount SET friendsDifference = 0 WHERE id = :accountId")
    void resetFriendsDifference(@Param("accountId") Long accountId);
    @Transactional
    @Modifying
    @Query("UPDATE TwitterAccount t SET t.groupStatus = :groupStatus WHERE t.id = :accountId")
    void updateGroupStatus(@Param("accountId") Long accountId, @Param("groupStatus") GroupStatus groupStatus);

}
