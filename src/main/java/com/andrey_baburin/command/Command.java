package com.andrey_baburin.command;

import com.andrey_baburin.command.menu.StartMenu;
import org.telegram.telegrambots.meta.api.objects.Update;


public interface Command {

    boolean execute(Update update, boolean isBeginning);
    StartMenu getName();
}
