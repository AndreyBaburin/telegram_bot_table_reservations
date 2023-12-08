package com.andrey_baburin.command.menu;

import com.andrey_baburin.command.*;
import com.vdurmont.emoji.EmojiParser;
import lombok.Getter;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
@Getter
public enum StartMenu {
    START(EmojiParser.parseToUnicode("Старт"+ " :rocket:"), Start.class),
    REGISTRATION(EmojiParser.parseToUnicode("Регистрация" + " :man_technologist:"), Registration.class),
    NEW_BOOKING(EmojiParser.parseToUnicode("Забронировать" + " :round_pushpin:"), NewBooking.class),
    MY_BOOKINGS(EmojiParser.parseToUnicode("Мои бронирования" + " :green_book:"), MyBooking.class),
    EDIT_TABLE(EmojiParser.parseToUnicode("Создать столы" + " :hammer_and_wrench:"), CreateSomeTables.class),
    GUEST_DATABASE(EmojiParser.parseToUnicode("База гостей" + " :card_index_dividers:"), GuestDatabase.class);

    private final String text;
    private final Class<? extends Command> className;
}
