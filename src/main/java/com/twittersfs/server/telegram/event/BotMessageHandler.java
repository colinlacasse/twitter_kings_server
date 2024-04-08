package com.twittersfs.server.telegram.event;

import com.twittersfs.server.entities.TelegramUserEntity;
import com.twittersfs.server.entities.UserEntity;
import com.twittersfs.server.repos.TelegramUserRepo;
import com.twittersfs.server.repos.UserEntityRepo;
import com.twittersfs.server.security.AuthService;
import com.twittersfs.server.telegram.SpaceTraffBot;
import com.twittersfs.server.telegram.markup.TelegramBotMarkups;
import com.twittersfs.server.telegram.service.TelegramBotService;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.nonNull;

@Component
@Slf4j
public class BotMessageHandler {
    private final TelegramBotService telegramBotService;
    private final TelegramUserRepo telegramUserRepo;
    private final AuthService authService;
    private final UserEntityRepo userEntityRepo;
    private final TelegramBotMarkups botMarkups;
    @Value("${main.chat.id}")
    private String mainChatId;

    public BotMessageHandler(TelegramBotService telegramBotService, TelegramUserRepo telegramUserRepo, AuthService authService, UserEntityRepo userEntityRepo, TelegramBotMarkups botMarkups) {
        this.telegramBotService = telegramBotService;
        this.telegramUserRepo = telegramUserRepo;
        this.authService = authService;
        this.userEntityRepo = userEntityRepo;
        this.botMarkups = botMarkups;
    }

    @Transactional
    public void handle(SpaceTraffBot bot, Update event) throws TelegramApiException {
        Message message = event.getMessage();
        Long chatId = message.getChatId();
        Long adminChatId = Long.valueOf(mainChatId);
        TelegramUserEntity telegramUserEntity = telegramUserRepo.findById(chatId).orElseThrow(() -> new RuntimeException("No user with such id"));
        String language = telegramUserEntity.getLanguage();
        if (chatId.equals(adminChatId)) {
            String msg = message.getText();
            TelegramUserEntity user = telegramUserRepo.findByTempId(msg);
            if (nonNull(user)) {
                telegramBotService.approve(bot, chatId, msg);
            } else {
                telegramBotService.sendMessage(bot, chatId, "Temp ID " + msg + " не найдено");
            }
        }

        String msg = message.getText().toLowerCase().trim();
        if (validateEmail(msg)) {
            UserEntity user = userEntityRepo.findByEmail(msg);
            if (nonNull(user)) {
                if (!nonNull(telegramUserEntity.getVerified()) || telegramUserEntity.getVerified().equals(Boolean.FALSE)) {
                    String code = authService.sendVerificationCode(msg);
                    telegramUserRepo.updateCodeByChatId(chatId, code);
                    List<TelegramUserEntity> users = telegramUserRepo.findAll();
                    List<TelegramUserEntity> usersWithEmail = new ArrayList<>();
                    for (TelegramUserEntity telegramUser : users) {
                        if (nonNull(telegramUser.getVerificationEmail())) {
                            if (telegramUser.getVerificationEmail().equals(msg)) {
                                usersWithEmail.add(telegramUser);
                            }
                        }
                    }
                    if (usersWithEmail.isEmpty()) {
                        telegramUserRepo.updateVerificationEmailByChatId(chatId, msg);
                        switch (language) {
                            case "english" ->
                                    telegramBotService.sendMessage(bot, chatId, "Type verification code sent to email : " + msg);
                            default ->
                                    telegramBotService.sendMessage(bot, chatId, "Введите код верификации отправленный на почту : " + msg);
                        }
                    } else {
                        switch (language) {
                            case "english" ->
                                    telegramBotService.sendMessage(bot, chatId, "Email has been already used");
                            default -> telegramBotService.sendMessage(bot, chatId, "Почта уже использовалась");
                        }
                    }
                } else {
                    telegramBotService.setEmail(bot, chatId, msg);
                }
            }
        } else if (validateCode(msg)) {
            if (!nonNull(telegramUserEntity.getVerified()) || telegramUserEntity.getVerified().equals(Boolean.FALSE)) {
                if (telegramUserEntity.getCode().equals(msg)) {
                    UserEntity user = userEntityRepo.findByEmail(telegramUserEntity.getVerificationEmail());
                    if (nonNull(user)) {
                        telegramUserRepo.updateVerifiedByChatId(chatId, Boolean.TRUE);
                        userEntityRepo.updateBalanceById(user.getId(), 3F);
                        switch (language) {
                            case "english" ->
                                    telegramBotService.sendMessageWithMarkup(bot, chatId, "User verified" + "\nFree trial $ added to account" + "\nBalance top up is now available" + "\n\u270C" + " Space-Traff Soft Home Menu", botMarkups.homeKeyboard(language));
                            default ->
                                    telegramBotService.sendMessageWithMarkup(bot, chatId, "Пользователь верифицирован" + "\nФри триал $ добавленны на аккаунт" + "\nПополнение баланса стало доступно" + "\n\u270C" + " Space-Traff Soft Меню", botMarkups.homeKeyboard(language));
                        }
                    } else {
                        switch (language) {
                            case "english" ->
                                    telegramBotService.sendMessage(bot, chatId, "No user with such email at Space-Traff.site : " + telegramUserEntity.getVerificationEmail());
                            default ->
                                    telegramBotService.sendMessage(bot, chatId, "Пользователь с такой почтой на Space-Traff.site не найден : " + telegramUserEntity.getVerificationEmail());
                        }
                    }
                } else {
                    switch (language) {
                        case "english" -> telegramBotService.sendMessage(bot, chatId, "Invalid verification code");
                        default -> telegramBotService.sendMessage(bot, chatId, "Невалидный код верификации");
                    }
                }
            } else {
                switch (language) {
                    case "english" ->
                            telegramBotService.sendMessage(bot, chatId, "User is already verified, Free trial has been given");
                    default ->
                            telegramBotService.sendMessage(bot, chatId, "Пользователь уже верифицирован, Фри триал был выдан ранее");
                }
            }
        } else {
            switch (language) {
                case "english" ->
                        telegramBotService.sendMessage(bot, chatId, "No user with such email at Space-Traff.site sfs soft or Invalid verification code");
                default ->
                        telegramBotService.sendMessage(bot, chatId, "Пользователь с такой почтой на Space-Traff.site не найден или невалидный код верификации");
            }
        }
    }

    private boolean validateEmail(String email) {
        return email != null && email.matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$");
    }

    private boolean validateCode(String code) {
        if (code == null || code.length() != 6) {
            return false;
        }

        for (char c : code.toCharArray()) {
            if (!Character.isDigit(c)) {
                return false;
            }
        }

        return true;
    }

}
