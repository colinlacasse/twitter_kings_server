package com.twittersfs.server.telegram.event;

import com.twittersfs.server.constants.TelegramCommandConstant;
import com.twittersfs.server.telegram.SpaceTraffBot;
import com.twittersfs.server.telegram.service.TelegramBotService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.MessageEntity;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.IOException;

@Component
@Slf4j
public class BotCommandHandler {
    private final TelegramBotService telegramBotService;

    public BotCommandHandler(TelegramBotService telegramBotService) {
        this.telegramBotService = telegramBotService;
    }

    public void handle(SpaceTraffBot bot, Update event) throws TelegramApiException, IOException, InterruptedException {
        Message message = event.getMessage();
        MessageEntity commandEntity = message.getEntities().stream()
                .filter(e -> "bot_command".equals(e.getType()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Command error"));
        String command = message.getText().substring(commandEntity.getOffset(), commandEntity.getLength());
        switch (command) {
            case TelegramCommandConstant.START -> telegramBotService.startBot(bot,message.getChatId());
        }
    }
}
