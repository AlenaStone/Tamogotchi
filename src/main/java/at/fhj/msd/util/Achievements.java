package at.fhj.msd.util;

import java.util.HashSet;

import at.fhj.msd.model.PetSaveData;

/**
 * Utility class for handling achievements.
 * Provides methods to check and grant achievements.
 */
public final class Achievements {

    // Private constructor 
    private Achievements() {
    }

    /**
     * Checks if the given PetSaveData already has a specific achievement.
     *
     * @param s  the save data
     * @param id the achievement ID
     * @return true if already unlocked
     */
    public static boolean has(PetSaveData s, String id) {
        return s != null && s.achievements != null && s.achievements.contains(id);
    }

    /**
     * Grants an achievement to the pet and adds a coin reward if not already
     * unlocked.
     *
     * @param s           the save data
     * @param id          the achievement ID
     * @param rewardCoins number of coins to reward
     */
    public static void grant(PetSaveData s, String id, int rewardCoins) {
        if (s == null || id == null)
            return;

        if (!has(s, id)) {
            if (s.achievements == null)
                s.achievements = new HashSet<>();

            s.achievements.add(id);
            s.coins += rewardCoins;
        }
    }

    /**
     * Enum representing all possible achievements.
     * Each achievement has an ID, localization keys, and icon path.
     */
    public enum Type {
        POMO5(
                "pomo5",
                "ach.pomo5.title",
                "ach.pomo5.desc",
                "/images/ach/pomo5.png"),
        CARE3D(
                "care3d",
                "ach.care3d.title",
                "ach.care3d.desc",
                "/images/ach/3days.png"),
        MOOD80(
                "mood80",
                "ach.mood80.title",
                "ach.mood80.desc",
                "/images/ach/mood.png"),
        AFF70(
                "aff70",
                "ach.aff70.title",
                "ach.aff70.desc",
                "/images/ach/heart.png"),
        ENERGYS(
                "energys",
                "ach.energys.title",
                "ach.energys.desc",
                "/images/ach/energy.png");

        public final String id;
        public final String titleKey;
        public final String descKey;
        public final String iconPath;

        Type(String id, String titleKey, String descKey, String iconPath) {
            this.id = id;
            this.titleKey = titleKey;
            this.descKey = descKey;
            this.iconPath = iconPath;
        }
    }

}
