package com.twittersfs.server.telegram.service;

import com.twittersfs.server.telegram.SpaceTraffBot;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public interface TelegramBotService {
    void startBot(SpaceTraffBot bot, Long chatId) throws TelegramApiException;
    void cancel(SpaceTraffBot bot, Long chatId, Integer messageId) throws TelegramApiException;
    void sendMessage(SpaceTraffBot bot, Long chatId, String message) throws TelegramApiException;
    void sendMessageWithMarkup(SpaceTraffBot bot, Long chatId, String message, InlineKeyboardMarkup markup) throws TelegramApiException;
    void saveLanguage(SpaceTraffBot bot, Long chatId, String language) throws TelegramApiException;
    void sendBalanceTable(SpaceTraffBot bot, Long chatId) throws TelegramApiException;
    void setRefillAmount(SpaceTraffBot bot, Long chatId, String refillAmount) throws TelegramApiException;
    void handlePayedButton(SpaceTraffBot bot, Long chatId) throws TelegramApiException;
    void setEmail(SpaceTraffBot bot, Long chatId, String email) throws TelegramApiException;
    void approve(SpaceTraffBot bot, Long chatId, String tempId) throws TelegramApiException;
}
