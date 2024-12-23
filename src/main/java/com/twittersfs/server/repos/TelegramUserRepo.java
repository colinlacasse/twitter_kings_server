//package com.twittersfs.server.repos;
//
//import com.twittersfs.server.entities.TelegramUserEntity;
//import jakarta.transaction.Transactional;
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.data.jpa.repository.Modifying;
//import org.springframework.data.jpa.repository.Query;
//import org.springframework.data.repository.query.Param;
//
//public interface TelegramUserRepo extends JpaRepository<TelegramUserEntity, Long> {
//    @Modifying
//    @Query("UPDATE TelegramUserEntity u SET u.refillAmount = :refillAmount WHERE u.chatId = :chatId")
//    void updateRefillAmountByChatId(@Param("chatId") Long chatId, @Param("refillAmount") Integer refillAmount);
//
//    @Modifying
//    @Query("UPDATE TelegramUserEntity u SET u.email = :email WHERE u.chatId = :chatId")
//    void updateEmailByChatId(@Param("chatId") Long chatId, @Param("email") String email);
//    @Modifying
//    @Query("UPDATE TelegramUserEntity u SET u.verificationEmail = :verificationEmail WHERE u.chatId = :chatId")
//    void updateVerificationEmailByChatId(@Param("chatId") Long chatId, @Param("verificationEmail") String email);
//
//    @Modifying
//    @Query("UPDATE TelegramUserEntity u SET u.tempId = :tempId WHERE u.chatId = :chatId")
//    void updateTempIdByChatId(@Param("chatId") Long chatId, @Param("tempId") String tempId);
//    @Modifying
//    @Query("UPDATE TelegramUserEntity u SET u.code = :code WHERE u.chatId = :chatId")
//    void updateCodeByChatId(Long chatId, String code);
//    @Modifying
//    @Query("UPDATE TelegramUserEntity u SET u.verified = :verified WHERE u.chatId = :chatId")
//    void updateVerifiedByChatId(Long chatId, boolean verified);
//
//    TelegramUserEntity findByTempId(String tempId);
//}
