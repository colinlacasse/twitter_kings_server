package com.twittersfs.server.telegram;

//import com.twittersfs.server.telegram.event.BotEventDispatcher;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Component;
//import org.telegram.telegrambots.bots.TelegramLongPollingBot;
//import org.telegram.telegrambots.meta.api.objects.Update;
//import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
//
//import java.io.IOException;
//
//@Component
//@Slf4j
//public class SpaceTraffBot extends TelegramLongPollingBot {
//    @Value("${telegram.bot.name}")
//    private String botName;
//
//    private final BotEventDispatcher eventDispatcher;
//
//    public SpaceTraffBot(@Value("${telegram.bot.token}") String botToken, BotEventDispatcher eventDispatcher) {
//        super(botToken);
//        this.eventDispatcher = eventDispatcher;
//    }
//
//
//    @Override
//    public void onUpdateReceived(Update event) {
//        try {
//            eventDispatcher.handle(this, event);
//        } catch (TelegramApiException e) {
//            log.error("Failed to process event: " + e.getMessage());
//        } catch (InterruptedException | IOException e) {
//            throw new RuntimeException(e);
//        }
//
//    }
//
//    @Override
//    public String getBotUsername() {
//        return botName;
//    }
//}
