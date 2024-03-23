package com.twittersfs.server.telegram.service;

import com.twittersfs.server.entities.TelegramUserEntity;
import com.twittersfs.server.entities.UserEntity;
import com.twittersfs.server.repos.TelegramUserRepo;
import com.twittersfs.server.repos.UserEntityRepo;
import com.twittersfs.server.telegram.SpaceTraffBot;
import com.twittersfs.server.telegram.markup.TelegramBotMarkups;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class TelegramBotServiceImpl implements TelegramBotService {
    private final TelegramBotMarkups botMarkups;
    private final TelegramUserRepo telegramUserRepo;
    private final UserEntityRepo userEntityRepo;
    @Value("${usdt.wallet}")
    private String wallet;
    @Value("${main.chat.id}")
    private String mainChatId;

    public TelegramBotServiceImpl(TelegramBotMarkups botMarkups, TelegramUserRepo telegramUserRepo, UserEntityRepo userEntityRepo) {
        this.botMarkups = botMarkups;
        this.telegramUserRepo = telegramUserRepo;
        this.userEntityRepo = userEntityRepo;
    }

    @Override
    public void startBot(SpaceTraffBot bot, Long chatId) throws TelegramApiException {
        sendMessageWithMarkup(bot, chatId, "\uD83D\uDCAD" + " Choose your language / Выберете язык", botMarkups.languageKeyboard());
    }

    @Override
    public void saveLanguage(SpaceTraffBot bot, Long chatId, String language) throws TelegramApiException {
        saveTelegramUser(chatId, language);
        switch (language) {
            case "english" ->
                    sendMessageWithMarkup(bot, chatId, "\u270C" + " Space-Traff Soft Home Menu", botMarkups.homeKeyboard(language));
            default ->
                    sendMessageWithMarkup(bot, chatId, "\u270C" + " Space-Traff Soft Меню", botMarkups.homeKeyboard(language));
        }
    }

    public void sendBalanceTable(SpaceTraffBot bot, Long chatId) throws TelegramApiException {
        TelegramUserEntity user = telegramUserRepo.findById(chatId).orElseThrow(() -> new RuntimeException("User with such Id does not exist"));
        String language = user.getLanguage();
        switch (language) {
            case "english" ->
                    sendMessageWithMarkup(bot, chatId, "\u270C" + " Choose amount of refill", botMarkups.balanceMarkup(language));
            default ->
                    sendMessageWithMarkup(bot, chatId, "\u270C" + " Выберети сумму пополнения", botMarkups.balanceMarkup(language));
        }
    }

    public void setRefillAmount(SpaceTraffBot bot, Long chatId, String refillAmount) throws TelegramApiException {
        TelegramUserEntity user = telegramUserRepo.findById(chatId).orElseThrow(() -> new RuntimeException("User with such Id does not exist"));
        String language = user.getLanguage();
        telegramUserRepo.updateRefillAmountByChatId(chatId, Integer.valueOf(refillAmount));
        switch (language) {
            case "english" ->
                    sendMessage(bot, chatId, "\u2705" + " Add user email on Space-Traff.site (NOT twiiter account email");
            default ->
                    sendMessage(bot, chatId, "\u2705" + " Введите имеил пользователя на сайте Space-Traff.site (НЕ твиттер аккаунта)");
        }
    }

    public void setEmail(SpaceTraffBot bot, Long chatId, String email) throws TelegramApiException {
        TelegramUserEntity user = telegramUserRepo.findById(chatId).orElseThrow(() -> new RuntimeException("User with such Id does not exist"));
        String language = user.getLanguage();
        String paymentAmount = calculatePayedAmount(user.getRefillAmount());
        UUID uuid = UUID.randomUUID();
        String tempId = uuid.toString();
        telegramUserRepo.updateEmailByChatId(chatId, email);
        telegramUserRepo.updateTempIdByChatId(chatId, tempId);
        switch (language) {
            case "english" ->
                    sendMessageWithMarkup(bot, chatId, "\uD83D\uDCB5" + " Payment amount " + paymentAmount + " $" + "\nSend money via USDT TRC20 and press PAYED button \n Wallet : " + wallet, botMarkups.payedButton(language));
            default ->
                    sendMessageWithMarkup(bot, chatId, "\uD83D\uDCB5" + " Сумма пополения " + paymentAmount + " $" + "\nСделайте перевод через USDT TRC20 и нажмите кнопку ОПЛАЧЕНО \n Кошелек : " + wallet, botMarkups.payedButton(language));
        }
    }

    public void handlePayedButton(SpaceTraffBot bot, Long chatId) throws TelegramApiException {
        TelegramUserEntity user = telegramUserRepo.findById(chatId).orElseThrow(() -> new RuntimeException("User with such Id does not exist"));
        Long adminChatId = Long.valueOf(mainChatId);
        String language = user.getLanguage();
        LocalDateTime now = LocalDateTime.now();
        switch (language) {
            case "english" ->
                    sendMessage(bot, chatId, "\uD83D\uDCB5" + " Balance will be refilled automatically after we review your payment");
            default ->
                    sendMessage(bot, chatId, "\uD83D\uDCB5" + " Баланс будет пополнен автоматически после рассмотрения платежа");
        }
        sendMessage(bot, adminChatId, " User : " + user.getEmail() + "\n Refill : " + user.getRefillAmount() + "\n Time : " + now + "\n Temp ID : " + user.getTempId());
    }

    public void approve(SpaceTraffBot bot, Long chatId, String tempId) throws TelegramApiException {
        TelegramUserEntity tgUser = telegramUserRepo.findByTempId(tempId);
        String language = tgUser.getLanguage();
        UserEntity user = userEntityRepo.findByEmail(tgUser.getEmail());
        Float currentBalance = user.getBalance();
        Float newBalance = currentBalance + tgUser.getRefillAmount();
        userEntityRepo.updateBalanceById(user.getId(), newBalance);
        switch (language) {
            case "english" ->
                    sendMessage(bot, chatId, "\uD83D\uDCB5" + " Balance refilled on " + tgUser.getRefillAmount().toString() + " $");
            default ->
                    sendMessage(bot, chatId, "\uD83D\uDCB5" + " Баланс пополнен на " + tgUser.getRefillAmount().toString() + " $");
        }

    }

    @Override
    public void cancel(SpaceTraffBot bot, Long chatId, Integer messageId) throws TelegramApiException {
        DeleteMessage deleteMessage = new DeleteMessage(chatId.toString(), messageId);
        bot.execute(deleteMessage);
    }

    @Override
    public void sendMessage(SpaceTraffBot bot, Long chatId, String message) throws TelegramApiException {
        bot.execute(SendMessage.builder()
                .chatId(chatId)
                .text(message)
                .build());
    }

    @Override
    public void sendMessageWithMarkup(SpaceTraffBot bot, Long chatId, String message, InlineKeyboardMarkup markup) throws TelegramApiException {
        bot.execute(SendMessage.builder()
                .chatId(chatId)
                .text(message)
                .replyMarkup(markup)
                .build());
    }

    private void saveTelegramUser(Long userId, String language) {
        Optional<TelegramUserEntity> user = telegramUserRepo.findById(userId);
        if (user.isEmpty()) {
            telegramUserRepo.save(TelegramUserEntity.builder()
                    .chatId(userId)
                    .language(language)
                    .build());
        } else {
            TelegramUserEntity updated = user.get();
            updated.setLanguage(language);
            telegramUserRepo.save(updated);
        }
    }

    private String calculatePayedAmount(Integer amount) {
        switch (amount) {
            case 12 -> {
                return "10";
            }
            case 35 -> {
                return "30";
            }
            case 60 -> {
                return "50";
            }
            case 120 -> {
                return "100";
            }
            case 200 -> {
                return "150";
            }
        }
        return "0";
    }

}
