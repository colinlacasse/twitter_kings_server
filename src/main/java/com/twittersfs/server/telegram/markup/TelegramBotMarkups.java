package com.twittersfs.server.telegram.markup;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.*;

@Component
@Slf4j
public class TelegramBotMarkups {

    public InlineKeyboardMarkup languageKeyboard() {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        InlineKeyboardButton english = new InlineKeyboardButton();
        english.setText("\uD83C\uDDEC\uD83C\uDDE7" + " English language");
        english.setCallbackData("english");
        InlineKeyboardButton russian = new InlineKeyboardButton();
        russian.setText("\uD83C\uDDF7\uD83C\uDDFA" + " Русский язык");
        russian.setCallbackData("russian");
        List<InlineKeyboardButton> keyboardButtonsRow1 = new ArrayList<>();
        keyboardButtonsRow1.add(english);
        keyboardButtonsRow1.add(russian);
        List<List<InlineKeyboardButton>> rowList = new ArrayList<>();
        rowList.add(keyboardButtonsRow1);
        inlineKeyboardMarkup.setKeyboard(rowList);
        return inlineKeyboardMarkup;
    }

    public InlineKeyboardMarkup homeKeyboard(String language) {
        Map<String, String> englishKeyboard = new HashMap<>();
        englishKeyboard.put("topup", "\uD83D\uDCB0 Top up Balance");
        englishKeyboard.put("sfsgroup", "\uD83D\uDCAC Soft Updates");
        englishKeyboard.put("support", "\uD83D\uDDFF Online Support");
        Map<String, String> russianKeyboard = new HashMap<>();
        russianKeyboard.put("topup", "\uD83D\uDCB0 Пополнить баланс");
        russianKeyboard.put("sfsgroup", "\uD83D\uDCAC Апдейты по софту");
        russianKeyboard.put("support", "\uD83D\uDDFF Онлайн поддержка");
        switch (language) {
            case "english" -> {
                return generateKeyBoardMarkup(englishKeyboard);
            }
            default -> {
                return generateKeyBoardMarkup(russianKeyboard);
            }
        }
    }

    public InlineKeyboardMarkup balanceMarkup(String language) {
        Map<String, String> englishKeyboard = new LinkedHashMap<>();
        englishKeyboard.put("11", "Pay 10 \uD83D\uDCB8 -> Get 10 \uD83D\uDCB8");
        englishKeyboard.put("35", "Pay 30 \uD83D\uDCB8 -> Get 35 \uD83D\uDCB8");
        englishKeyboard.put("60", "Pay 50 \uD83D\uDCB8 -> Get 60 \uD83D\uDCB8");
        englishKeyboard.put("180", "Pay 150 \uD83D\uDCB8 -> Get 180 \uD83D\uDCB8");
        englishKeyboard.put("250", "Pay 200 \uD83D\uDCB8 -> Get 250 \uD83D\uDCB8");
        Map<String, String> russianKeyboard = new LinkedHashMap<>();
        russianKeyboard.put("11", "Платишь 10 \uD83D\uDCB8 -> Получаешь 10 \uD83D\uDCB8");
        russianKeyboard.put("35", "Платишь 30 \uD83D\uDCB8 -> Получаешь 35 \uD83D\uDCB8");
        russianKeyboard.put("60", "Платишь 50 \uD83D\uDCB8 -> Получаешь 60 \uD83D\uDCB8");
        russianKeyboard.put("180", "Платишь 150 \uD83D\uDCB8 -> Получаешь 180 \uD83D\uDCB8");
        russianKeyboard.put("250", "Платишь 200 \uD83D\uDCB8 -> Получаешь 250 \uD83D\uDCB8");
        switch (language) {
            case "english" -> {
                return generateKeyBoardMarkup(englishKeyboard);
            }
            default -> {
                return generateKeyBoardMarkup(russianKeyboard);
            }
        }
    }

    public InlineKeyboardMarkup payedButton(String language) {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        InlineKeyboardButton payed = new InlineKeyboardButton();
        List<InlineKeyboardButton> keyboardButtonsRow = new ArrayList<>();
        List<List<InlineKeyboardButton>> rowList = new ArrayList<>();
        payed.setCallbackData("payed");
        switch (language) {
            case "english" -> payed.setText("\u2705 Payed");
            default -> payed.setText("\u2705 Оплачено");
        }
        keyboardButtonsRow.add(payed);
        rowList.add(keyboardButtonsRow);
        markup.setKeyboard(rowList);
        return markup;
    }


    private InlineKeyboardMarkup generateKeyBoardMarkup(Map<String, String> buttons) {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowList = new ArrayList<>();
        for (Map.Entry<String, String> entry : buttons.entrySet()) {
            String callback = entry.getKey();
            String text = entry.getValue();

            InlineKeyboardButton button = generateButton(callback, text);
            List<InlineKeyboardButton> buttonRow = generateButtonRow(button);

            rowList.add(buttonRow);
        }
        markup.setKeyboard(rowList);
        return markup;
    }

    private List<InlineKeyboardButton> generateButtonRow(InlineKeyboardButton button) {
        List<InlineKeyboardButton> buttonRow = new ArrayList<>();
        buttonRow.add(button);
        return buttonRow;
    }

    private InlineKeyboardButton generateButton(String callback, String text) {
        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setCallbackData(callback);
        button.setText(text);
        return button;
    }
}
