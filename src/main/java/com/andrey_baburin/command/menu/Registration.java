package com.andrey_baburin.command.menu;

import com.andrey_baburin.bot.BotService;
import com.andrey_baburin.bot.ButtonOrMessage;
import com.andrey_baburin.command.Command;
import com.andrey_baburin.bot.MenuKeyboard;
import com.andrey_baburin.entity.User;
import com.andrey_baburin.repository.UserRepository;
import com.vdurmont.emoji.EmojiParser;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;
import java.util.*;

@RequiredArgsConstructor
@Component
@Service
public class Registration implements Command {
    @Value("${bot.admin-phoneNumber}")
    private String adminPhoneNumber;

    private enum Step {
        BEGIN,
        ADD_NAME,
        ADD_NUMBER_PHONE,
    }

    private static final String ADD_NAME = "Введите имя";
    private static final String ADD_NUMBER_PHONE = "Введите номер телефона";
    private static final String FORMAT_NUMBER_PHONE = "В формате - *89997774455*";
    private static final String INCORRECT_NUMBER = "Номер телефона неподходит по формату";
    private static final String USER_EXIST = "Вы уже зарегестрированны";
    private static final String NEXT = EmojiParser.parseToUnicode("Можете перейти в" +
            " раздел \"Забронировать\"" + " :round_pushpin:");
    private static final String SUCCESSFUL = EmojiParser.parseToUnicode("Вы успешно зарегестрировались" +
            " :white_check_mark:");

    private final BotService botService;
    private final Map<Long, Step> usersSteps = new HashMap<>();
    private boolean isFinished;

    private User user;

    private final UserRepository userDAO;

    @Override
    public boolean execute(Update update, boolean isBeginning) {
        long chatId = ButtonOrMessage.chatId(update);
        String messageText = ButtonOrMessage.messageText(update);
        if (!usersSteps.containsKey(chatId) || isBeginning) {
            usersSteps.put(chatId, Step.BEGIN);
        }
        switch (usersSteps.get(chatId)) {
            case BEGIN:
                begin(update);
                break;
            case ADD_NAME:
                addName(chatId, messageText);
                break;
            case ADD_NUMBER_PHONE:
                addNumberPhone(chatId, messageText);
                break;
        }
        return isFinished;
    }

    @Override
    public StartMenu getName() {
        return StartMenu.REGISTRATION;
    }

    public boolean begin(Update update) {
        long chatId = ButtonOrMessage.chatId(update);
        if(userDAO.existsById(chatId)) {
            botService.sendMarkup(chatId, USER_EXIST,
                    MenuKeyboard.showMenuKeyboard(userDAO.findById(chatId).get()));
            return isFinished;
        }
        user = new User();
        user.setId(chatId);
        botService.sendMarkup(chatId, ADD_NAME, MenuKeyboard.userMenuKeyboard());
        usersSteps.put(chatId, Step.ADD_NAME);
        return true;
    }

    public void addName(Long chatId, String messageText) {
        user.setUserName(messageText);
        botService.sendText(chatId, ADD_NUMBER_PHONE + "\n" + FORMAT_NUMBER_PHONE);
        usersSteps.put(chatId, Step.ADD_NUMBER_PHONE);
    }

    public void addNumberPhone(long chatId, String messageText) {
        if (messageText.length() == 11 && messageText.charAt(0) == '8') {
            user.setUserNumberPhone(messageText);
            user.setIsAdmin(messageText.equals(adminPhoneNumber));
            userDAO.save(user);
            botService.sendText(chatId, SUCCESSFUL);
            botService.sendMarkup(chatId, NEXT, MenuKeyboard.showMenuKeyboard(user));
        } else {
            botService.sendText(chatId, INCORRECT_NUMBER);
        }
    }


}
