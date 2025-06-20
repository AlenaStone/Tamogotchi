package at.fhj.msd.model;

import java.util.Locale;
import java.util.ResourceBundle;

public class GameState {
    private static String petName;
    private static String petType;
    private static Locale locale;

    public static void setPetName(String name) {
        petName = name;
    }

    public static String getPetName() {
        return petName;
    }

    public static void setPetType(String type) {
        petType = type;
    }

    public static String getPetType() {
        return petType;
    }

    public static void setLocale(Locale loc) {
        locale = loc;
    }

    public static Locale getLocale() {
        return locale;
    }

    public static ResourceBundle getBundle() {
        return ResourceBundle.getBundle("i18n.messages", locale);
    }
}
