package at.fhj.msd.model;

import java.util.Locale;
import java.util.ResourceBundle;

/**
 * GameState holds the current state of the game such as pet name, pet type, and selected language.
 * This class provides static methods for managing game-wide settings and localization.
 */

public class GameState {
    /**
     * The name of the pet.
     */
    private static String petName;

    /**
     * The type of the pet (e.g., cat, dog).
     */
    private static String petType;

    /**
     * The selected locale for the game's language.
     */
    private static Locale locale;

     /**
     * Sets the pet's name.
     * @param name the name of the pet
     */
    public static void setPetName(String name) {
        petName = name;
    }

     /**
     * Returns the pet's name.
     * @return the name of the pet
     */
    public static String getPetName() {
        return petName;
    }

    /**
     * Sets the type of the pet.
     * @param type the type of the pet
     */
    public static void setPetType(String type) {
        petType = type;
    }


    /**
     * Returns the type of the pet.
     * @return the type of the pet
     */
    public static String getPetType() {
        return petType;
    }

      /**
     * Sets the game's locale for language.
     * @param loc the locale to set
     */
    public static void setLocale(Locale loc) {
        locale = loc;
    }

    /**
     * Returns the currently set locale.
     * @return the locale of the game
     */
    public static Locale getLocale() {
        return locale;
    }

    /**
     * Returns the ResourceBundle for the currently set locale.
     * Defaults to English if no locale is set.
     * @return the resource bundle for localization
     */
    public static ResourceBundle getBundle() {
        if (locale == null) {
            locale = Locale.ENGLISH;
        }
        return ResourceBundle.getBundle("i18n.messages", locale);
    }
}
