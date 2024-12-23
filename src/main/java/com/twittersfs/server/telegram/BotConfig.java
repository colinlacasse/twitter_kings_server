package com.twittersfs.server.telegram;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.BotSession;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

//@Configuration
//public class BotConfig {
//    @Bean
//    public BotSession botSession(SpaceTraffBot bot) {
//        try {
//            TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
//            return telegramBotsApi.registerBot(bot);
//        } catch (TelegramApiException e) {
//            throw new RuntimeException(e);
//        }
//    }
//}
