package com.andrey_baburin.bot;


import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;

import java.util.List;

public interface BotService {
    void sendText(Long chatId, String text);
    void sendWithKeyboard(Long chatId, String text, List<Button> buttons);
    void sendMarkup(Long chatId, String text, ReplyKeyboard keyboard);
    void sendPhoto(SendPhoto sendPhoto);
}
