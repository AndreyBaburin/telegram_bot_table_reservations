package com.andrey_baburin.command.menu;

import com.andrey_baburin.bot.BotService;
import com.andrey_baburin.bot.ButtonOrMessage;
import com.andrey_baburin.command.Command;
import com.andrey_baburin.bot.MenuKeyboard;
import com.andrey_baburin.entity.Booking;
import com.andrey_baburin.entity.SomeTable;
import com.andrey_baburin.entity.User;
import com.andrey_baburin.service.BookingService;
import com.andrey_baburin.service.SomeTableService;
import com.andrey_baburin.service.UserService;
import com.vdurmont.emoji.EmojiParser;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

@RequiredArgsConstructor
@Component
@Service
public class NewBooking implements Command {

    @Value("${bot.tablesPhoto}")
    private String tablesPhoto;
    private enum Step {
        BEGIN,
        SELECT_TABLE,
        SELECT_DATE,
        ENTER_TIME_START,
        ENTER_TIME_END
    }

    private static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private static final DateTimeFormatter dateForButton = DateTimeFormatter.ofPattern("dd.MM");
    private static final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");


    private static final String NEED_REGISTRATION = "Вы ещё не зарегестрировались";
    private static final String WRONG_TABLE = "Не удалось найти стол" +
            " \n Выберите номер ещё раз";
    private static final String CHOOSE_TABLE = "Выберите столик:";
    private static final String PAST_DAY = "Данный день уже прошёл, поробуйте выбрать снова";
    private static final String CHOOSE_DATE = "Выберите дату:";
    private static final String INVALID_DAY = "Формат даты некорректен, выбирите дату";
    private static final String AVAILABLE_TIME = "Доступное время бронирования:";
    private static final String ADD_TIME_START = "Введите время начала \nВ формате *10:30*";
    private static final String EARLY_TIME_START = "Время должно быть не раньше текущего.\nВведите время заного";
    private static final String ADD_TIME_END = "Введите время окончания \nВ формате *17:30*";
    private static final String WRONG_TIME_FORMAT = "Формат времени некорректен \nПример формата *17:30*";
    private static final String EARLY_TIME_END = "Время окончания не должно быть раньше времени начала.\nВведите время заного";
    private static final String TIME_IS_BUSY = "Время пересекается с занятым \nВведите время заного";

    private final BotService botService;
    private final UserService userService;
    private final BookingService bookingService;
    private final SomeTableService someTableService;
    private Booking booking;

    private final Map<Long, Step> usersSteps = new HashMap<>();
    private boolean isFinished;


    @Override
    public boolean execute(Update update, boolean isBeginning) {
        long chatId = ButtonOrMessage.chatId(update);
        String messageText = ButtonOrMessage.messageText(update);
        if (userService.existsById(chatId)) {
            if (!usersSteps.containsKey(chatId) || isBeginning) {
                usersSteps.put(chatId, Step.BEGIN);
            }
        } else {
            botService.sendText(chatId, NEED_REGISTRATION);
        }

        switch (usersSteps.get(chatId)) {
            case BEGIN:
                begin(update);
                break;
            case SELECT_TABLE:
                selectSomeTable(chatId, messageText);
                break;
            case SELECT_DATE:
                selectDate(chatId, messageText);
                break;
            case ENTER_TIME_START:
                addTimeStart(chatId, messageText);
                break;
            case ENTER_TIME_END:
                addTimeEnd(chatId, messageText);
                break;
        }
        return isFinished;
    }

    @Override
    public StartMenu getName() {
        return StartMenu.NEW_BOOKING;
    }

    private void begin(Update update) {
        long chatId = ButtonOrMessage.chatId(update);
        botService.sendPhoto(getPhotoMessage(chatId));
        User user = userService.findById(chatId).get();
        booking = new Booking();
        booking.setUser(user);
        List<SomeTable> someTableList = someTableService.findAll();
        botService.sendMarkup(chatId, CHOOSE_TABLE, showAllTables(someTableList));
        usersSteps.put(chatId, Step.SELECT_TABLE);
    }

    private void selectSomeTable(Long chatId, String messageText) {
        try {
            Optional<SomeTable> tableOpt = someTableService.findById(messageText);
            booking.setSomeTable(tableOpt.get());
            botService.sendMarkup(chatId, CHOOSE_DATE, showDates());
            usersSteps.put(chatId, Step.SELECT_DATE);
        } catch (Exception e) {
            botService.sendText(chatId, WRONG_TABLE);
        }
    }

    private void selectDate(Long chatId, String messageText) {
        try {
            LocalDate selectedDate = LocalDate.parse(messageText, dateFormatter);
            if (selectedDate.isBefore(LocalDate.now())) {
                botService.sendText(chatId, PAST_DAY);
            } else {
                booking.setTimeStart(selectedDate.atStartOfDay());
                botService.sendText(chatId, AVAILABLE_TIME +
                        "\n" + showFreeTime(booking.getSomeTable(), selectedDate));
                botService.sendText(chatId, ADD_TIME_START);
                usersSteps.put(chatId, Step.ENTER_TIME_START);
            }
        } catch (Exception e) {
            botService.sendText(chatId, INVALID_DAY);
        }

    }

    private void addTimeStart(Long chatId, String messageText) {
        try {
            LocalTime timeStart = LocalTime.parse(messageText, timeFormatter);
            if (booking.getTimeStart().toLocalDate().atTime(timeStart).isBefore(LocalDateTime.now())) {
                botService.sendText(chatId, EARLY_TIME_START);
            } else {
                booking.setTimeStart(booking.getTimeStart().toLocalDate().atTime(timeStart));
                botService.sendText(chatId, ADD_TIME_END);
                usersSteps.put(chatId, Step.ENTER_TIME_END);
            }
        } catch (Exception e) {
            botService.sendText(chatId, WRONG_TIME_FORMAT);
        }
    }

    private void addTimeEnd(Long chatId, String messageText) {
        try {
            LocalTime timeEnd = LocalTime.parse(messageText, timeFormatter);
            LocalTime timeStart = booking.getTimeStart().toLocalTime();
            if (timeEnd.isBefore(timeStart)) {
                botService.sendText(chatId, EARLY_TIME_END);
            } else if (!checkFreeTime(timeStart, timeEnd)) {
                botService.sendText(chatId, TIME_IS_BUSY);
                usersSteps.put(chatId, Step.ENTER_TIME_END);
            } else {
                booking.setTimeEnd(LocalDateTime.of(booking.getTimeStart().toLocalDate(), timeEnd));
                bookingService.save(booking);
                usersSteps.put(chatId, Step.BEGIN);
                botService.sendMarkup(chatId, fullInfAboutBooking(),
                        MenuKeyboard.showMenuKeyboard(userService.findById(chatId).get()));
                isFinished = true;
            }
        } catch (DateTimeParseException e) {
            botService.sendText(chatId, WRONG_TIME_FORMAT);
        }
    }

    private String fullInfAboutBooking() {
        return EmojiParser.parseToUnicode("Успешно " + ":white_check_mark:" +
                "\nВаша бронь " + booking.getTimeStart().format(dateFormatter) + " :calendar:"
                + "\nc " + booking.getTimeStart().format(timeFormatter) +
                " до " + booking.getTimeEnd().format(timeFormatter) + " :clock2:" +
                "\n" + "на имя: *" + booking.getUser().getUserName() +
                "*\n" + "тел: " + booking.getUser().getUserNumberPhone() + " :telephone_receiver:" +
                "\n" + booking.getSomeTable().getName());
    }

    private boolean checkFreeTime(LocalTime timeStart, LocalTime timeEnd) {
        List<Booking> bookingList = bookingService.findByDateTable(booking.getTimeStart().toLocalDate(),
                booking.getSomeTable());
        for (Booking booking : bookingList) {
            LocalTime start = booking.getTimeStart().toLocalTime();
            LocalTime end = booking.getTimeEnd().toLocalTime();
            if (timeStart.isBefore(end) || timeEnd.isBefore(start)) {
                return false;
            }
        }
        return true;
    }

    private List<String> showFreeTime(SomeTable someTable, LocalDate date) {
        List<Booking> bookingList = bookingService.findByDateTable(date, booking.getSomeTable());
        List<String> time = new ArrayList<>();
        List<String> freeTimes = new ArrayList<>();
        time.add(someTable.getAvailableFrom().toString());
        for (Booking booking : bookingList) {
            time.add(booking.getTimeStart().toLocalTime().toString());
            time.add(booking.getTimeEnd().toLocalTime().toString());
        }
        time.add(someTable.getAvailableTo().toString());

        for (int i = 0; i < time.size(); i++) {
            freeTimes.add(time.get(i++) + " - " + time.get(i));
        }
        return freeTimes;
    }

    private SendPhoto getPhotoMessage(Long chatId) {
        SendPhoto sendPhoto = new SendPhoto();
        sendPhoto.setChatId(String.valueOf(chatId));
        sendPhoto.setParseMode(ParseMode.MARKDOWN);
        String filePath = tablesPhoto;
        File file = new File(filePath);
        sendPhoto.setPhoto(new InputFile(file));
        return sendPhoto;
    }


    private InlineKeyboardMarkup showAllTables(List<SomeTable> someTableList) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> totalList = new ArrayList<>();

        int buttonCount = 0;
        List<InlineKeyboardButton> keyboardButtonRow = new ArrayList<>();

        for (SomeTable table : someTableList) {
            InlineKeyboardButton button = new InlineKeyboardButton();
            button.setText(String.valueOf(table.getId()));
            button.setCallbackData(String.valueOf(table.getId()));
            keyboardButtonRow.add(button);

            buttonCount++;
            if (buttonCount == 5) {
                totalList.add(keyboardButtonRow);
                keyboardButtonRow = new ArrayList<>();
                buttonCount = 0;
            }
        }

        inlineKeyboardMarkup.setKeyboard(totalList);
        return inlineKeyboardMarkup;
    }

    private InlineKeyboardMarkup showDates() {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> totalList = new ArrayList<>();

        LocalDate today = LocalDate.now();
        LocalDate day = today.minusDays(today.getDayOfWeek().getValue() - 1);

        for (int calendarRow = 0; calendarRow < 2; calendarRow++) {
            List<InlineKeyboardButton> keyboardButtonRow = new ArrayList<>();
            for (int calendarColumn = 0; calendarColumn < 7; calendarColumn++) {
                InlineKeyboardButton button = new InlineKeyboardButton();
                button.setText(day.format(dateForButton));
                button.setCallbackData(day.format(dateFormatter));
                keyboardButtonRow.add(button);
                day = day.plusDays(1);
            }
            totalList.add(keyboardButtonRow);
        }
        inlineKeyboardMarkup.setKeyboard(totalList);
        return inlineKeyboardMarkup;
    }
}
