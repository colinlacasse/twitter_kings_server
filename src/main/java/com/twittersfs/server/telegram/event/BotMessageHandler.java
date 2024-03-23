package com.twittersfs.server.telegram.event;

import com.twittersfs.server.entities.TelegramUserEntity;
import com.twittersfs.server.entities.UserEntity;
import com.twittersfs.server.repos.TelegramUserRepo;
import com.twittersfs.server.repos.UserEntityRepo;
import com.twittersfs.server.telegram.SpaceTraffBot;
import com.twittersfs.server.telegram.service.TelegramBotService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import static java.util.Objects.nonNull;

@Component
@Slf4j
public class BotMessageHandler {
    private final TelegramBotService telegramBotService;
    private final TelegramUserRepo telegramUserRepo;
    private final UserEntityRepo userEntityRepo;
    @Value("${main.chat.id}")
    private String mainChatId;

    public BotMessageHandler(TelegramBotService telegramBotService, TelegramUserRepo telegramUserRepo, UserEntityRepo userEntityRepo) {
        this.telegramBotService = telegramBotService;
        this.telegramUserRepo = telegramUserRepo;
        this.userEntityRepo = userEntityRepo;
    }

    public void handle(SpaceTraffBot bot, Update event) throws TelegramApiException {
        Message message = event.getMessage();
        Long chatId = message.getChatId();
        Long adminChatId = Long.valueOf(mainChatId);
        TelegramUserEntity telegramUserEntity = telegramUserRepo.findById(chatId).orElseThrow(()-> new RuntimeException("No user with such id"));
        String language = telegramUserEntity.getLanguage();
        if (chatId.equals(adminChatId)) {
            String msg = message.getText();
            TelegramUserEntity user = telegramUserRepo.findByTempId(msg);
            if(nonNull(user)){
                telegramBotService.approve(bot, chatId, msg);
            } else {
                telegramBotService.sendMessage(bot,chatId, "Temp ID " + msg + " не найдено");
            }
        }

        String msg = message.getText().toLowerCase().trim();
        if (validateEmail(msg)) {
            UserEntity user = userEntityRepo.findByEmail(msg);
            if (nonNull(user)) {
                telegramBotService.setEmail(bot, chatId, msg);
            } else {
                switch (language) {
                    case "english" ->
                            telegramBotService.sendMessage(bot,chatId, "No user with such email at Space-Traff.site sfs soft");
                    default ->
                            telegramBotService.sendMessage(bot,chatId, "Пользователь с такой почтой на Space-Traff.site не найден");
                }
            }
        }
    }

    private boolean validateEmail(String email) {
        return email != null && email.matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$");
    }

}
