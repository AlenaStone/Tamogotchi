package at.fhj.msd.model;

import java.util.Locale;

/**
 * Класс для хранения глобального состояния игры
 */
public class GameState {
    private static String petName;
    private static String petType;
    private static Locale locale;
    
    // Добавляем статистику питомца
    private static int hunger = 50;
    private static int happiness = 50;
    private static int energy = 50;
    private static int cleanliness = 50;

    // Методы для имени питомца
    public static void setPetName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Имя питомца не может быть пустым");
        }
        petName = name.trim();
    }

    public static String getPetName() {
        return petName != null ? petName : "Unnamed";
    }

    // Методы для типа питомца
    public static void setPetType(String type) {
        if (type == null || type.trim().isEmpty()) {
            throw new IllegalArgumentException("Тип питомца не может быть пустым");
        }
        petType = type.trim();
    }

    public static String getPetType() {
        return petType != null ? petType : "Unknown";
    }

    // Методы для локализации
    public static void setLocale(Locale loc) {
        if (loc == null) {
            throw new IllegalArgumentException("Локаль не может быть null");
        }
        locale = loc;
    }

    public static Locale getLocale() {
        return locale != null ? locale : Locale.getDefault();
    }

    // Методы для статистики питомца (значения от 0 до 100)
    public static int getHunger() {
        return hunger;
    }

    public static void setHunger(int value) {
        hunger = clamp(value);
    }

    public static int getHappiness() {
        return happiness;
    }

    public static void setHappiness(int value) {
        happiness = clamp(value);
    }

    public static int getEnergy() {
        return energy;
    }

    public static void setEnergy(int value) {
        energy = clamp(value);
    }

    public static int getCleanliness() {
        return cleanliness;
    }

    public static void setCleanliness(int value) {
        cleanliness = clamp(value);
    }

    // Вспомогательный метод для ограничения значений
    private static int clamp(int value) {
        return Math.max(0, Math.min(100, value));
    }

    // Сброс состояния игры
    public static void reset() {
        petName = null;
        petType = null;
        locale = null;
        hunger = 50;
        happiness = 50;
        energy = 50;
        cleanliness = 50;
    }

    // Получение текущего состояния в виде строки (для отладки)
    public static String getStatus() {
        return String.format(
            "Pet: %s (%s) | Locale: %s | Stats: Hng %d, Hpp %d, En %d, Cln %d",
            getPetName(), getPetType(), getLocale(),
            hunger, happiness, energy, cleanliness
        );
    }
}