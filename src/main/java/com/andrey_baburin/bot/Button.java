package com.andrey_baburin.bot;

import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Data
public class Button {
    String text;
    String callBack;

    public Button(String text, String callBack) {
        this.text = text;
        this.callBack = callBack;
    }

    public static List<Button> twoButtons (String first, String second) {
        List<Button> buttonList = new ArrayList<>();
        Button button;
        button = new Button(first, first);
        buttonList.add(button);
        button = new Button(second, second);
        buttonList.add(button);
        return buttonList;
        }

    public static List<Button> threeButtons (String first, String second, String third) {
        List<Button> buttonList = twoButtons(first, second);
        Button button;
        button = new Button(third, third);
        buttonList.add(button);
        return buttonList;
    }
    public static List<Button> fourButtons(String first, String second, String third, String fourth) {
        List<Button> buttonList = threeButtons(first, second, third);
        Button button;
        button = new Button(fourth, fourth);
        buttonList.add(button);
        return buttonList;
    }
}
