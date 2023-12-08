package com.andrey_baburin.command.menu;

import com.andrey_baburin.bot.BotService;
import com.andrey_baburin.bot.ButtonOrMessage;
import com.andrey_baburin.command.Command;
import com.andrey_baburin.bot.MenuKeyboard;
import com.andrey_baburin.entity.Booking;
import com.andrey_baburin.entity.User;
import com.andrey_baburin.service.BookingService;
import com.andrey_baburin.service.UserService;
import com.vdurmont.emoji.EmojiParser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static com.andrey_baburin.bot.Button.twoButtons;

@RequiredArgsConstructor
@Component
public class MyBooking implements Command {
    private enum Step {
        BEGIN,
        CHOOSE,
        DELETE
    }

    private static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private static final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
    private static final String NO_BOOKINGS = "У вас пока нет бронирований";
    private static final String CHANGE_SOMETHING = "Желаете, что то изменить?";
    private static final String BOOKING = "Бронь №";
    private static final String WAITING_YOU = "Ждём Вас";
    private static final String GOOD = "Всё отлично";
    private static final String DELETE_BOOKING = "Удалить бронь";
    private static final String DELETE = " удалена";
    private static final String NO_RESERVED = "У Вас нет зарезервированных столов";
    private static final String BOOKING_FOR_DELETE = "Введите номер брони для удаления";
    private static final String NEED_REGISTRATION = "Вы ещё не зарегестрировались";
    private static final String INCORRECT_DATA = "Неверные данные";


    private final BotService botService;
    private final BookingService bookingService;
    private final UserService userService;

    private final Map<Long, Step> usersSteps = new HashMap<>();
    private boolean isFinished;

    public boolean execute(Update update, boolean isBeginning) {
        long chatId = ButtonOrMessage.chatId(update);
        String messageText = ButtonOrMessage.messageText(update);

        if (userService.existsById(chatId)) {
            if (!usersSteps.containsKey(chatId) || isBeginning) {
                usersSteps.put(chatId, MyBooking.Step.BEGIN);
            }
        } else {
            botService.sendText(chatId, NEED_REGISTRATION);
        }

        switch (usersSteps.get(chatId)) {
            case BEGIN:
                begin(update);
                break;
            case CHOOSE:
                continueOrDelete(chatId, messageText);
                break;
            case DELETE:
                setDeleteBooking(chatId, messageText);
                break;
        }

        return isFinished;
    }

    @Override
    public StartMenu getName() {
        return StartMenu.MY_BOOKINGS;
    }

    private void begin(Update update) {
        Long chatId = ButtonOrMessage.chatId(update);
        List<Booking> bookingList = allBookings(chatId);
        if (bookingList.isEmpty()) {
            botService.sendText(chatId, NO_BOOKINGS);
        } else {
            for (Booking booking : bookingList) {
                String fullText = fullInfAboutBooking(booking);
                botService.sendText(chatId, fullText);
            }
        }
        botService.sendWithKeyboard(chatId, CHANGE_SOMETHING, twoButtons(GOOD,DELETE_BOOKING));
        usersSteps.put(chatId, Step.CHOOSE);

    }

    public List<Booking> allBookings(long chatId) {
        Optional<User> userOpt = userService.findById(chatId);
        if (userOpt.isPresent()) {
            List<Booking> bookingList;
            User user = userOpt.get();
            if (user.getIsAdmin()) {
                bookingList = bookingService.findAll();
            } else {
                bookingList = bookingService.findByUserId(chatId);
            }
            return bookingList;
        } else {
            return null;
        }
    }

    private void continueOrDelete(Long chatId, String messageText) {
        if (messageText.equals(DELETE_BOOKING)) {
            List<Booking> bookingList = allBookings(chatId);
            botService.sendText(chatId, BOOKING_FOR_DELETE);
            for (Booking booking : bookingList) {
                String fullText = "\n" + BOOKING + booking.getId() + "\n" + fullInfAboutBooking(booking);
                botService.sendText(chatId, fullText);
                usersSteps.put(chatId, Step.DELETE);
            }
        } else if (messageText.equals(GOOD)) {
            botService.sendMarkup(chatId, WAITING_YOU,
                    MenuKeyboard.showMenuKeyboard(userService.findById(chatId).get()));
            isFinished = true;
        } else {
            botService.sendText(chatId, INCORRECT_DATA);
        }

    }

    private void setDeleteBooking (Long chatId, String messageText) {
       try {
           List<Booking> bookingList = allBookings(chatId);
           if(bookingList.size()>=Integer.parseInt(messageText)) {
               bookingService.deleteById(Long.valueOf(messageText));
               botService.sendText(chatId, BOOKING + messageText + DELETE);
           } else {
               botService.sendText(chatId, NO_RESERVED);
           }
       } catch (Exception e) {
           botService.sendText(chatId, INCORRECT_DATA);
       }
    }

    private String fullInfAboutBooking(Booking booking) {
        return EmojiParser.parseToUnicode("Ваша бронь "
                + booking.getTimeStart().format(dateFormatter) + " :calendar:"
                + "\nc " + booking.getTimeStart().format(timeFormatter) +
                " до " + booking.getTimeEnd().format(timeFormatter) + " :clock2:" +
                "\n" + "на имя: *" + booking.getUser().getUserName() +
                "*\n" + "тел: " + booking.getUser().getUserNumberPhone() + " :telephone_receiver:" +
                "\n" + booking.getSomeTable().getName());
    }
}
