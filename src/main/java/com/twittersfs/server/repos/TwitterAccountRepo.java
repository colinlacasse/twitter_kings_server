package com.twittersfs.server.repos;

import com.twittersfs.server.entities.TwitterAccount;
import com.twittersfs.server.enums.TwitterAccountStatus;


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
    @Modifying
    @Query("UPDATE TwitterAccount t SET t.messagesSent = t.messagesSent + 1 WHERE t.id = :accountId")
    void updateMessagesSent(@Param("accountId") Long accountId);

    @Modifying
    @Query("UPDATE TwitterAccount t SET t.password = :newPassword WHERE t.id = :accountId")
    void updatePasswordById(@Param("accountId") Long accountId,@Param("newPassword") String newPassword);

    @Modifying
    @Query("UPDATE TwitterAccount t SET t.csrfToken = :newCsrfToken WHERE t.id = :accountId")
    void updateCsrfToken(@Param("accountId") Long accountId, @Param("newCsrfToken") String newCsrfToken);

    @Modifying
    @Query("UPDATE TwitterAccount t SET t.cookie = :newCookie WHERE t.id = :accountId")
    void updateCookie(@Param("accountId") Long accountId, @Param("newCookie") String newCookie);

    @Modifying
    @Query("UPDATE TwitterAccount t SET t.status = :newStatus WHERE t.id = :accountId")
    void updateStatus(@Param("accountId") Long accountId, @Param("newStatus") TwitterAccountStatus newStatus);

    @Modifying
    @Query("UPDATE TwitterAccount t SET t.email = :newEmail WHERE t.id = :accountId")
    void updateEmail(@Param("accountId") Long accountId, @Param("newEmail") String newEmail);

    @Modifying
    @Query("UPDATE TwitterAccount t SET t.username = :newUsername WHERE t.id = :accountId")
    void updateUsername(@Param("accountId") Long accountId, @Param("newUsername") String newUsername);


}
