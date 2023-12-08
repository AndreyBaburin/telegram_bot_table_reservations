package com.andrey_baburin.bot;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Component
public class BotExecution implements BotService {
    private final BotTelegram bot;

    @Override
    public void sendText(Long chatId, String text) {
        SendMessage send = new SendMessage();
        send.setChatId(chatId.toString());
        send.setText(text);
        send.setParseMode(ParseMode.MARKDOWN);
        try {
            bot.execute(send);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void sendWithKeyboard(Long chatId, String text, List<Button> buttons) {
        SendMessage send = new SendMessage();
        send.setChatId(chatId.toString());
        send.setText(text);
        send.setReplyMarkup(inlineKeyboard(buttons));
        try {
            bot.execute(send);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void sendMarkup(Long chatId, String text, ReplyKeyboard markup) {
        SendMessage send = new SendMessage();
        send.setChatId(chatId.toString());
        send.setText(text);
        send.setParseMode(ParseMode.MARKDOWN);
        send.setReplyMarkup(markup);
        try {
            bot.execute(send);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void sendPhoto(SendPhoto sendPhoto) {
        sendPhoto.setParseMode(ParseMode.MARKDOWN);

        try {
            bot.execute(sendPhoto);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private InlineKeyboardMarkup inlineKeyboard(List<Button> buttons) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> totalList = new ArrayList<>();

        for (Button button : buttons) {
            List<InlineKeyboardButton> keyboardButtonRow = new ArrayList<>();

            InlineKeyboardButton inlineButton = new InlineKeyboardButton(button.getText());
            inlineButton.setCallbackData(button.getCallBack());
            keyboardButtonRow.add(inlineButton);
            totalList.add(keyboardButtonRow);
        }
        inlineKeyboardMarkup.setKeyboard(totalList);

        return inlineKeyboardMarkup;
    }
}
