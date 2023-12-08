package com.andrey_baburin.command;

import com.andrey_baburin.command.menu.StartMenu;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class CommandFactory {
    private final ApplicationContext context;
    private final Map<String, Class<? extends Command>> commandClasses = new HashMap<>();

    public CommandFactory(ApplicationContext context) {
        this.context = context;

        for (StartMenu startMenu : StartMenu.values()) {
            commandClasses.put(startMenu.getText(), startMenu.getClassName());
        }
    }
    public Command getCommand(String commandName) {
        Class<? extends Command> commandClass = commandClasses.get(commandName);
        if (commandClass != null) {
            return context.getBean(commandClass);
        }
        return null;
    }

    public boolean hasCommand(String name) {
        return commandClasses.containsKey(name);
    }
}
