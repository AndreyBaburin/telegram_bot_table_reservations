package com.andrey_baburin.bot;

import com.andrey_baburin.command.Command;
import com.andrey_baburin.command.CommandFactory;
import com.vdurmont.emoji.EmojiParser;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
@Component
public class BotTelegram extends TelegramLongPollingBot {
    @Value("${bot.name}")
    private String botName;

    @Value("${bot.token}")
    private String botToken;

    @Override
    public String getBotUsername() {
        return botName;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }


    static final String WELCOME = EmojiParser.parseToUnicode("Выберете кнопу \"Старт\" из меню"
            + " :point_down:");

    private final CommandFactory commandFactory;
    private final Map<Long, Command> currentUserCommands = new HashMap<>();

    @Override
    public void onUpdateReceived(Update update) {

        long chatId = ButtonOrMessage.chatId(update);
        String messageText = ButtonOrMessage.messageText(update);

        boolean isFinished = false;
        if (commandFactory.hasCommand(messageText)) {
            Command command = commandFactory.getCommand(messageText);
            currentUserCommands.put(chatId, command);
            isFinished = command.execute(update, true);

        } else if (currentUserCommands.containsKey(chatId)) {
            isFinished = currentUserCommands.get(chatId).execute(update, false);
        } else {
            sendKeyBoard(chatId);
        }
        if (isFinished) {
            currentUserCommands.remove(chatId);
        }
    }

    private void sendKeyBoard(long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(WELCOME);

        ReplyKeyboardMarkup keyboardMarkup = MenuKeyboard.userMenuKeyboard();

        message.setReplyMarkup(keyboardMarkup);

        try {
            execute(message);
        } catch (TelegramApiException e) {

        }
    }

}

