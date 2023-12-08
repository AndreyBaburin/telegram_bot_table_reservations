package com.andrey_baburin.command.menu;

import com.andrey_baburin.bot.BotService;
import com.andrey_baburin.bot.ButtonOrMessage;
import com.andrey_baburin.command.Command;
import com.andrey_baburin.bot.MenuKeyboard;
import com.andrey_baburin.entity.Booking;
import com.andrey_baburin.entity.User;
import com.andrey_baburin.repository.UserRepository;
import com.andrey_baburin.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;
import java.util.*;

import static com.andrey_baburin.bot.Button.fourButtons;

@RequiredArgsConstructor
@Component
@Service
public class GuestDatabase implements Command {
    private enum Step {
        BEGIN,
        SHOW_ALL_USERS,
        SELECT,
        USER_SEARCH,
        SELECT_EDIT,
        EDIT_NAME,
        EDIT_PHONE_NUMBER,
        SPAM
    }

    private static final String SAVE = "Сохранить редактирование";
    private static final String SELECT_ACTION = "Выберите действие";
    private static final String EDIT_NAME = "Изменить имя";
    private static final String SHOW_ALL_USERS = "Показать всех гостей";
    private static final String EDIT_PHONE_NUMBER = "Изменить номер телефона";
    private static final String EXIT = "Вернуться в главное меню";
    private static final String DELETE_USER_AND_BOOKING = "Удалить ползьзователя и его брони";
    private static final String CHOOSING_EDITING = "Выбор редактирования";
    private static final String NO_USER = "Пользователь не найден";
    private static final String ENTER_NAME = "Введите Имя";
    private static final String ENTER_PHONE = "Введите номер";
    private static final String NAME_CHANGED = "Имя изменено";
    private static final String PHONE_CHANGED = "Телефон изменён";
    private static final String EVERYTHING_DELETE = "Всё удалено";
    private static final String SAVED = "Сохранено";
    private static final String EDIT = "Редактировать";
    private static final String ENTER_ID = "Введите номер ID";
    private static final String SPAM = "Спам";
    private static final String SEND_SPAM = "Введите ссобщение для рассылки рекламы";
    private static final String ERROR = "Ошибка";
    private static final String EMPTY_DATABASE = "База пуста";
    private static final String FULL_DATABASE = "Полный список наших гостей:";
    private static final String DOME = "Готово";

    private final BotService botService;
    private final UserRepository userDAO;
    private final BookingService bookingService;
    private User user;
    private final Map<Long, GuestDatabase.Step> usersSteps = new HashMap<>();
    private boolean isFinished;

    @Override
    public boolean execute(Update update, boolean isBeginning) {
        long chatId = ButtonOrMessage.chatId(update);
        String messageText = ButtonOrMessage.messageText(update);
        if (!usersSteps.containsKey(chatId) || isBeginning) {
            usersSteps.put(chatId, GuestDatabase.Step.BEGIN);
        }

        switch (usersSteps.get(chatId)) {
            case BEGIN:
                begin(update);
                break;
            case SHOW_ALL_USERS:
                showAllUsers(chatId);
                break;
            case SELECT:
                selectAction(chatId, messageText);
                break;
            case USER_SEARCH:
                searchUser(chatId, messageText);
                break;
            case SELECT_EDIT:
                selectEdit(chatId, messageText);
                break;
            case EDIT_NAME:
                editName(chatId, messageText);
                break;
            case EDIT_PHONE_NUMBER:
                editPhoneNumber(chatId, messageText);
                break;
            case SPAM:
                sendSpam(chatId, messageText);
                break;
        }
        return isFinished;
    }

    @Override
    public StartMenu getName() {
        return StartMenu.GUEST_DATABASE;
    }

    private void begin(Update update) {
        Long chatId = ButtonOrMessage.chatId(update);
        user = new User();
        botService.sendWithKeyboard(chatId, SELECT_ACTION, fourButtons(SHOW_ALL_USERS, EDIT, SPAM, EXIT));
        usersSteps.put(chatId, Step.SELECT);
    }

    private void searchUser(Long chatId, String messageText) {
        try {
            Optional<User> userOpt = userDAO.findById(Long.parseLong(messageText));
            if (userOpt.isPresent()) {
                user = userOpt.get();
                botService.sendWithKeyboard(chatId, CHOOSING_EDITING, fourButtons(EDIT_NAME, EDIT_PHONE_NUMBER,
                        DELETE_USER_AND_BOOKING, SAVE));
                usersSteps.put(chatId, Step.SELECT_EDIT);
            }
        } catch (Exception e) {
            botService.sendText(chatId, NO_USER);
            botService.sendWithKeyboard(chatId, SELECT_ACTION, fourButtons(SHOW_ALL_USERS, EDIT, SPAM, EXIT));
            usersSteps.put(chatId, Step.SELECT);
        }

    }

    private void selectEdit(Long chatId, String messageText) {
        switch (messageText) {
            case EDIT_NAME:
                usersSteps.put(chatId, Step.EDIT_NAME);
                botService.sendText(chatId, ENTER_NAME);
                break;
            case SAVE:
                saveEdit(chatId);
                break;
            case EDIT_PHONE_NUMBER:
                usersSteps.put(chatId, Step.EDIT_PHONE_NUMBER);
                botService.sendText(chatId, ENTER_PHONE);
                break;
            case DELETE_USER_AND_BOOKING:
                deleteUserAndBooking(chatId);
                break;
            default:
                botService.sendText(chatId, ERROR);
                botService.sendWithKeyboard(chatId, SELECT_ACTION, fourButtons(SHOW_ALL_USERS, EDIT, SPAM, EXIT));
                usersSteps.put(chatId, Step.SELECT);
                break;
        }
    }

    private void editName(Long chatId, String messageText) {
        user.setUserName(messageText);
        botService.sendText(chatId, NAME_CHANGED);
        botService.sendWithKeyboard(chatId, CHOOSING_EDITING, fourButtons(EDIT_NAME, EDIT_PHONE_NUMBER,
                DELETE_USER_AND_BOOKING, SAVE));
        usersSteps.put(chatId, Step.SELECT_EDIT);
    }

    private void editPhoneNumber(Long chatId, String messageText) {
        user.setUserNumberPhone(messageText);
        botService.sendText(chatId, PHONE_CHANGED);
        botService.sendWithKeyboard(chatId, CHOOSING_EDITING, fourButtons(EDIT_NAME, EDIT_PHONE_NUMBER,
                DELETE_USER_AND_BOOKING, SAVE));
        usersSteps.put(chatId, Step.SELECT_EDIT);
    }

    private void deleteUserAndBooking(Long chatId) {
        List<Booking> bookingList = bookingService.findByUserId(user.getId());
        for (Booking booking : bookingList) {
            bookingService.delete(booking);
        }
        userDAO.deleteById(user.getId());
        botService.sendText(chatId, EVERYTHING_DELETE);

    }

    private void saveEdit(Long chatId) {
        userDAO.save(user);
        botService.sendMarkup(chatId, SAVED, MenuKeyboard.showMenuKeyboard(user));
        isFinished = true;
    }

    private void selectAction(Long chatId, String messageText) {
        switch (messageText) {
            case SHOW_ALL_USERS:
                showAllUsers(chatId);
                break;
            case EDIT:
                botService.sendText(chatId, ENTER_ID);
                usersSteps.put(chatId, Step.USER_SEARCH);
                break;
            case SPAM:
                botService.sendText(chatId, SEND_SPAM);
                usersSteps.put(chatId, Step.SPAM);
                break;
            case EXIT:
                botService.sendMarkup(chatId, DOME, MenuKeyboard.showMenuKeyboard(userDAO.findById(chatId).get()));
                break;
            default:
                botService.sendText(chatId, ERROR);
                botService.sendWithKeyboard(chatId, SELECT_ACTION, fourButtons(SHOW_ALL_USERS, EDIT, SPAM, EXIT));
                usersSteps.put(chatId, Step.SELECT);
                break;
        }
    }

    private void sendSpam(Long chatId, String messageText) {
        List<User> allUsers = userDAO.findAll();
        for (User user : allUsers) {
            botService.sendText(user.getId(), messageText);
        }
        botService.sendWithKeyboard(chatId, SELECT_ACTION, fourButtons(SHOW_ALL_USERS, EDIT, SPAM, EXIT));
        usersSteps.put(chatId, Step.SELECT);
    }


    private void showAllUsers(long chatId) {
        List<User> allUsers = userDAO.findAll();
        if (allUsers.isEmpty()) {
            botService.sendText(chatId, EMPTY_DATABASE);
        } else {
            botService.sendText(chatId, FULL_DATABASE);
            for (User user : allUsers) {
                botService.sendText(chatId, userInformation(user));
            }
            botService.sendWithKeyboard(chatId, SELECT_ACTION, fourButtons(SHOW_ALL_USERS, EDIT, SPAM, EXIT));
            usersSteps.put(chatId, Step.SELECT);
        }
    }


    private String userInformation(User user) {
        String inf = "ID: " + user.getId() + "\n" + "Имя: " + user.getUserName() +
                "\n" + "тел: " + user.getUserNumberPhone();
        return inf;
    }
}
