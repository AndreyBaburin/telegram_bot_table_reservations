package com.andrey_baburin.command.menu;

import com.andrey_baburin.bot.BotService;
import com.andrey_baburin.bot.ButtonOrMessage;
import com.andrey_baburin.command.Command;
import com.andrey_baburin.bot.MenuKeyboard;
import com.andrey_baburin.entity.SomeTable;
import com.andrey_baburin.service.SomeTableService;
import com.andrey_baburin.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RequiredArgsConstructor
@Component
@Service
public class CreateSomeTables implements Command {
    private static final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
    private static final String BOOKING_START_FROM = "Бронирование доступно с ";
    private static final String DONE = "Создать столы";
    private static final String TABLES_EXIST = "Столы уже созданы";
    private static final String NUMBER_OF_TABLE = "Стол №";
    private static final String HAVE_BEEN_CREATED_TABLES = " столов созданы";

    private final BotService botService;
    private final UserService userService;
    private final SomeTableService someTableService;

    private boolean isFinished;
    @Value("${bot.default-start-time}")
    private String defaultAvailabilityStart;

    @Value("${bot.default-end-time}")
    private String defaultAvailabilityEnd;

    @Value("${bot.number-of-tables}")
    private int numberOfTables;

    @Override
    public boolean execute(Update update, boolean isBeginning) {
        long chatId = ButtonOrMessage.chatId(update);
        List<SomeTable> someTableList = someTableService.findAll();
        if (someTableList.isEmpty()) {
            autoCreateTable(chatId);
        } else {
            botService.sendMarkup(chatId, TABLES_EXIST,
                    MenuKeyboard.showMenuKeyboard(userService.findById(chatId).get()));
            return isFinished;
        }
        return isFinished;
    }

    @Override
    public StartMenu getName() {
        return StartMenu.EDIT_TABLE;
    }

    private void autoCreateTable(long userId) {
        SomeTable someTable;

        for (int i = 1; i <= numberOfTables; i++) {
            someTable = new SomeTable();
            someTable.setId((long) i);
            someTable.setName(NUMBER_OF_TABLE + i);
            LocalTime availabilityFrom = LocalTime.parse(defaultAvailabilityStart, timeFormatter);
            someTable.setAvailableFrom(availabilityFrom);
            LocalTime availabilityTo = LocalTime.parse(defaultAvailabilityEnd, timeFormatter);
            someTable.setAvailableTo(availabilityTo);
            someTableService.save(someTable);
        }
        botService.sendText(userId, (numberOfTables + HAVE_BEEN_CREATED_TABLES));
        botService.sendText(userId,
                (BOOKING_START_FROM + defaultAvailabilityStart + " до " + defaultAvailabilityEnd));
    }

}
