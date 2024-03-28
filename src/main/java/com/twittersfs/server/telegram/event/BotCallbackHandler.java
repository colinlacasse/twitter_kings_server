package com.twittersfs.server.telegram.event;

import com.twittersfs.server.telegram.SpaceTraffBot;
import com.twittersfs.server.telegram.service.TelegramBotService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Component
@Slf4j
public class BotCallbackHandler {
    private final TelegramBotService telegramBotService;

    public BotCallbackHandler(TelegramBotService telegramBotService) {
        this.telegramBotService = telegramBotService;
    }

    public void handle(SpaceTraffBot bot, Update event) throws TelegramApiException{
        CallbackQuery query = event.getCallbackQuery();
        String callbackData = query.getData();

        switch (callbackData) {
            case "english", "russian" ->
                    telegramBotService.saveLanguage(bot, query.getMessage().getChatId(), callbackData);
            case "topup" -> telegramBotService.sendBalanceTable(bot, query.getMessage().getChatId());
            case "30", "117", "204", "436" ->
                    telegramBotService.setRefillAmount(bot, query.getMessage().getChatId(), callbackData);
            case "payed" -> telegramBotService.handlePayedButton(bot, query.getMessage().getChatId());
            case "sfsgroup" -> telegramBotService.openNewsGroup(bot, query.getMessage().getChatId());
            case "community" -> telegramBotService.openChat(bot, query.getMessage().getChatId());
            case "communityen" -> telegramBotService.openEnChat(bot, query.getMessage().getChatId());
            case "support" -> telegramBotService.openSupport(bot,query.getMessage().getChatId());
            case "cancel" ->
                    telegramBotService.cancel(bot, event.getCallbackQuery().getMessage().getChatId(), event.getCallbackQuery().getMessage().getMessageId());

            default -> {
            }
        }
    }
}
