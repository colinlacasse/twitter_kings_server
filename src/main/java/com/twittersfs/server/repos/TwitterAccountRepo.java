package com.twittersfs.server.repos;

import com.twittersfs.server.entities.TwitterAccount;
import com.twittersfs.server.enums.TwitterAccountStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface TwitterAccountRepo extends JpaRepository<TwitterAccount,Long> {
    Optional<TwitterAccount> findByUsernameAndModel_Id(String username, Long modelId);
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