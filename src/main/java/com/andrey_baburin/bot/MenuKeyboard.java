package com.andrey_baburin.bot;

import com.andrey_baburin.command.menu.StartMenu;
import com.andrey_baburin.entity.User;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import java.util.ArrayList;
import java.util.List;

public class MenuKeyboard {

    public static ReplyKeyboardMarkup showMenuKeyboard(User user) {
        if (user.getIsAdmin()){
            return adminMenuKeyboard();
        } else {
            return userMenuKeyboard();
        }
    }

    public static ReplyKeyboardMarkup adminMenuKeyboard() {
        ReplyKeyboardMarkup adminKeyboard = new ReplyKeyboardMarkup();

        List<KeyboardRow> keyboardRows = new ArrayList<>();

        KeyboardRow row = new KeyboardRow();
        row.add(StartMenu.START.getText());
        row.add(StartMenu.REGISTRATION.getText());

        keyboardRows.add(row);

        row = new KeyboardRow();
        row.add(StartMenu.NEW_BOOKING.getText());
        row.add(StartMenu.MY_BOOKINGS.getText());

        keyboardRows.add(row);
        row = new KeyboardRow();

        row.add(StartMenu.EDIT_TABLE.getText());
        row.add(StartMenu.GUEST_DATABASE.getText());
        keyboardRows.add(row);

        adminKeyboard.setKeyboard(keyboardRows);
        return adminKeyboard;
    }

    public static ReplyKeyboardMarkup userMenuKeyboard() {
        ReplyKeyboardMarkup userKeyboard = new ReplyKeyboardMarkup();

        List<KeyboardRow> keyboardRows = new ArrayList<>();

        KeyboardRow row = new KeyboardRow();
        row.add(StartMenu.START.getText());
        row.add(StartMenu.REGISTRATION.getText());

        keyboardRows.add(row);

        row = new KeyboardRow();
        row.add(StartMenu.NEW_BOOKING.getText());
        row.add(StartMenu.MY_BOOKINGS.getText());

        keyboardRows.add(row);

        userKeyboard.setKeyboard(keyboardRows);
        return userKeyboard;
    }
}
