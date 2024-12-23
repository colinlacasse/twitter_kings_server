//package com.twittersfs.server.telegram.markup;
//
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.stereotype.Component;
//import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
//import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
//
//import java.util.*;
//
//@Component
//@Slf4j
//public class TelegramBotMarkups {
//
//    public InlineKeyboardMarkup languageKeyboard() {
//        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
//        InlineKeyboardButton english = new InlineKeyboardButton();
//        english.setText("\uD83C\uDDEC\uD83C\uDDE7" + " English language");
//        english.setCallbackData("english");
//        InlineKeyboardButton russian = new InlineKeyboardButton();
//        russian.setText("\uD83C\uDDF7\uD83C\uDDFA" + " Русский язык");
//        russian.setCallbackData("russian");
//        List<InlineKeyboardButton> keyboardButtonsRow1 = new ArrayList<>();
//        keyboardButtonsRow1.add(english);
//        keyboardButtonsRow1.add(russian);
//        List<List<InlineKeyboardButton>> rowList = new ArrayList<>();
//        rowList.add(keyboardButtonsRow1);
//        inlineKeyboardMarkup.setKeyboard(rowList);
//        return inlineKeyboardMarkup;
//    }
//
//    public InlineKeyboardMarkup homeKeyboard(String language) {
//        Map<String, String> englishKeyboard = new LinkedHashMap<>();
//        englishKeyboard.put("topup", "\uD83D\uDCB0 Top up Balance");
//        englishKeyboard.put("sfsgroup", "\u231B Soft Updates");
//        englishKeyboard.put("communityen", "\uD83D\uDCAC Chat");
//        englishKeyboard.put("support", "\uD83D\uDDFF Online Support");
//        Map<String, String> russianKeyboard = new LinkedHashMap<>();
//        russianKeyboard.put("topup", "\uD83D\uDCB0 Пополнить баланс");
//        russianKeyboard.put("sfsgroup", "\u231B Апдейты по софту");
//        russianKeyboard.put("community", "\uD83D\uDCAC Чат");
//        russianKeyboard.put("support", "\uD83D\uDDFF Онлайн поддержка");
//        switch (language) {
//            case "english" -> {
//                return generateKeyBoardMarkup(englishKeyboard);
//            }
//            default -> {
//                return generateKeyBoardMarkup(russianKeyboard);
//            }
//        }
//    }
//
//    public InlineKeyboardMarkup balanceMarkup(String language) {
//        Map<String, String> englishKeyboard = new LinkedHashMap<>();
//        englishKeyboard.put("30", "\uD83D\uDCB8 1 Profile : $29");
//        englishKeyboard.put("120", "\uD83D\uDCB8 3 Profiles + 1 free: $87");
//        englishKeyboard.put("210", "\uD83D\uDCB8 5 Profiles + 2 free: $145");
//        englishKeyboard.put("450", "\uD83D\uDCB8 10 Profiles + 5 free: $290");
//        Map<String, String> russianKeyboard = new LinkedHashMap<>();
//        russianKeyboard.put("30", "\uD83D\uDCB8 1 Профиль : $29");
//        russianKeyboard.put("120", "\uD83D\uDCB8 3 Профиля + 1 бесплатно: $87");
//        russianKeyboard.put("210", "\uD83D\uDCB8 5 Профилей + 2 бесплатно: $145 ");
//        russianKeyboard.put("450", "\uD83D\uDCB8 10 Профилей + 5 бесплатно: $290");
//        switch (language) {
//            case "english" -> {
//                return generateKeyBoardMarkup(englishKeyboard);
//            }
//            default -> {
//                return generateKeyBoardMarkup(russianKeyboard);
//            }
//        }
//    }
//
//    public InlineKeyboardMarkup payedButton(String language) {
//        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
//        InlineKeyboardButton payed = new InlineKeyboardButton();
//        List<InlineKeyboardButton> keyboardButtonsRow = new ArrayList<>();
//        List<List<InlineKeyboardButton>> rowList = new ArrayList<>();
//        payed.setCallbackData("payed");
//        switch (language) {
//            case "english" -> payed.setText("\u2705 Payed");
//            default -> payed.setText("\u2705 Оплачено");
//        }
//        keyboardButtonsRow.add(payed);
//        rowList.add(keyboardButtonsRow);
//        markup.setKeyboard(rowList);
//        return markup;
//    }
//
//
//    private InlineKeyboardMarkup generateKeyBoardMarkup(Map<String, String> buttons) {
//        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
//        List<List<InlineKeyboardButton>> rowList = new ArrayList<>();
//        for (Map.Entry<String, String> entry : buttons.entrySet()) {
//            String callback = entry.getKey();
//            String text = entry.getValue();
//
//            InlineKeyboardButton button = generateButton(callback, text);
//            List<InlineKeyboardButton> buttonRow = generateButtonRow(button);
//
//            rowList.add(buttonRow);
//        }
//        markup.setKeyboard(rowList);
//        return markup;
//    }
//
//    private List<InlineKeyboardButton> generateButtonRow(InlineKeyboardButton button) {
//        List<InlineKeyboardButton> buttonRow = new ArrayList<>();
//        buttonRow.add(button);
//        return buttonRow;
//    }
//
//    private InlineKeyboardButton generateButton(String callback, String text) {
//        InlineKeyboardButton button = new InlineKeyboardButton();
//        button.setCallbackData(callback);
//        button.setText(text);
//        return button;
//    }
//}
