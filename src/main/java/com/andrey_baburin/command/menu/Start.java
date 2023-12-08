package com.andrey_baburin.command.menu;

import com.andrey_baburin.bot.BotService;
import com.andrey_baburin.bot.ButtonOrMessage;
import com.andrey_baburin.command.Command;
import com.andrey_baburin.command.menu.StartMenu;
import com.vdurmont.emoji.EmojiParser;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.io.File;

@RequiredArgsConstructor
@Component
@Scope("prototype")
public class Start implements Command {

    @Value("${bot.startPhoto}")
    private String startPhoto;

    private final BotService botService;

    @Override
    public boolean execute(Update update, boolean isBeginning) {
        Long chatId = ButtonOrMessage.chatId(update);
        String name = update.getMessage().getChat().getFirstName();
        botService.sendPhoto(getPhotoMessage(chatId, name));
        return true;
    }

    @Override
    public StartMenu getName() {
        return StartMenu.START;
    }

    private String startingText(String name) {
        return EmojiParser.parseToUnicode("Приветствуем Вас *" + name + "* :wave:" +
                "\nДобро пожаловать в чат-бот нашего заведения," +
                "\nРежим работы: ежедневно с 10.00 до 23.00" + " :clock9:" +
                "\nтел: +7-999-888-77-66" + " :telephone_receiver:" +
                "\nЕсли хотите забронировать стол, пройдете регистрацию" + " :memo:");

    }


    private SendPhoto getPhotoMessage(Long chatId, String name) {
        SendPhoto sendPhoto = new SendPhoto();
        sendPhoto.setChatId(String.valueOf(chatId));
        sendPhoto.setCaption(startingText(name));
        sendPhoto.setParseMode(ParseMode.MARKDOWN);
        String filePath = startPhoto;
        File file = new File(filePath);
        sendPhoto.setPhoto(new InputFile(file));
        return sendPhoto;
    }
}
