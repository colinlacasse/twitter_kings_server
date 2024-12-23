//package com.twittersfs.server.telegram.event;
//
//import com.twittersfs.server.telegram.SpaceTraffBot;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Component;
//import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
//import org.telegram.telegrambots.meta.api.objects.Message;
//import org.telegram.telegrambots.meta.api.objects.MessageEntity;
//import org.telegram.telegrambots.meta.api.objects.Update;
//import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
//
//import java.io.IOException;
//import java.util.Optional;
//
//import static java.util.Objects.nonNull;
//
//@Component
//@Slf4j
//public class BotEventDispatcher {
//    private final BotCallbackHandler callbackHandler;
//    private final BotCommandHandler commandHandler;
//    private final BotMessageHandler messageHandler;
//
//    @Autowired
//    public BotEventDispatcher(BotCallbackHandler callbackHandler, BotCommandHandler commandHandler, BotMessageHandler messageHandler) {
//        this.callbackHandler = callbackHandler;
//        this.commandHandler = commandHandler;
//        this.messageHandler = messageHandler;
//    }
//
//    public void handle(SpaceTraffBot bot, Update event) throws TelegramApiException, InterruptedException, IOException {
//        Message message = event.getMessage();
//        CallbackQuery callbackQuery = event.getCallbackQuery();
//        if (isCommand(message)) {
//            commandHandler.handle(bot, event);
//        } else if (nonNull(callbackQuery)) {
//            callbackHandler.handle(bot, event);
//        } else {
//            messageHandler.handle(bot, event);
//        }
//    }
//
//    private boolean isCommand(Message message) {
//        if (message != null) {
//            if (message.hasText() && message.hasEntities()) {
//                Optional<MessageEntity> commandEntity =
//                        message.getEntities().stream().filter(e -> "bot_command".equals(e.getType())).findFirst();
//                return commandEntity.isPresent();
//            }
//        }
//        return false;
//    }
//}
