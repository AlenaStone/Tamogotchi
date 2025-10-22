package at.fhj.msd.model;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Data model for saving pet state.
 * Stores pet name, type and current stat values.
 * <p>
 * NOTE: achievements are stored as a Set<String> to avoid duplicates.
 */
public class PetSaveData {

    /** The name of the pet. */
    public String petName;

    /** The type of the pet (cat, dog, fish, etc.). */
    public String petType;

    /** Hunger level from 0.0 to 1.0. */
    public double hunger;

    /** Energy level from 0.0 to 1.0. */
    public double energy;

    /** Mood level from 0.0 to 1.0. */
    public double mood;

    /** Health level from 0.0 to 1.0. */
    public double health;

    // ===== new fields for version 1.2 =====

    /** Current pet emotion: "idle", "happy", "sleepy", "hungry", "sad", etc. */
    public String emotion;

    /** Affection 0.0..1.0 — increases when you care for the pet. */
    public double affection;

    /** Experience points (gained from focus sessions and care). */
    public int xp;

    /** In-game currency (earned from achievements). */
    public int coins;

    /** Set of unlocked achievement IDs (e.g. "first_feed"). */
    public Set<String> achievements = new HashSet<>();

    // achievements tracking (daily + counters)

    /** ISO date "YYYY-MM-DD" to detect day change. */
    public String lastDayIso;

    /** True when the pet was fed today. */
    public boolean fedToday;

    /** Consecutive days you fed the pet. */
    public int feedStreakDays;

    /** Accumulated seconds with mood > 0.8 (resets daily). */
    public int moodAbove80Secs;

    /** Today's minimum energy (start == current). */
    public double energyDayMin;

    /** Consecutive finished focus intervals (Pomodoro). */
    public int consecutivePomos;

    // ===== Helpers (quality-of-life) =====

    /** Safe check for an unlocked achievement. */
    public boolean hasAchievement(String id) {
        return id != null && achievements != null && achievements.contains(id);
    }

    /**
     * Add an achievement id into the set.
     * @return true if it was added first time, false if it already existed.
     */
    public boolean addAchievement(String id) {
        Objects.requireNonNull(id, "achievement id");
        if (achievements == null) achievements = new HashSet<>();
        return achievements.add(id);
    }

    /** Add coins safely (prevents negative overflow by mistake). */
    public void addCoins(int delta) {
        this.coins = Math.max(0, this.coins + delta);
    }

    /** Add XP safely (non-negative). */
    public void addXp(int delta) {
        this.xp = Math.max(0, this.xp + Math.max(0, delta));
    }

    /** Call on app start / day switch to reset daily stats. */
    public void ensureDayInitialized() {
        String today = LocalDate.now().toString(); // ISO-8601
        if (!today.equals(lastDayIso)) {
            lastDayIso = today;
            fedToday = false;
            moodAbove80Secs = 0;
            consecutivePomos = 0; // если нужно сбрасывать по дню — оставь; иначе убери
            energyDayMin = energy; // старт дня = текущей энергии
        }
    }

    /** Mark that pet was fed now; updates streak. */
    public void markFedNow() {
        String today = LocalDate.now().toString();
        if (!today.equals(lastDayIso)) {
            // если день сменился — проинициализируем новый день
            ensureDayInitialized();
        }
        if (!fedToday) {
            fedToday = true;
            feedStreakDays = Math.max(1, feedStreakDays + 1);
        }
    }

    /** Track min energy across the day. */
    public void updateEnergyDayMin() {
        if (energyDayMin == 0) energyDayMin = energy;
        energyDayMin = Math.min(energyDayMin, energy);
    }

    /** Accumulate mood>0.8 time (call from game loop / timer). */
    public void addMoodAbove80Seconds(int seconds) {
        if (mood > 0.8) {
            moodAbove80Secs += Math.max(0, seconds);
        }
    }

    /** Increment consecutive Pomodoro counter. */
    public void incConsecutivePomos() {
        consecutivePomos = Math.max(0, consecutivePomos + 1);
    }

    @Override
    public String toString() {
        return "PetSaveData{" +
                "petName='" + petName + '\'' +
                ", petType='" + petType + '\'' +
                ", hunger=" + hunger +
                ", energy=" + energy +
                ", mood=" + mood +
                ", health=" + health +
                ", emotion='" + emotion + '\'' +
                ", affection=" + affection +
                ", xp=" + xp +
                ", coins=" + coins +
                ", achievements=" + (achievements == null ? 0 : achievements.size()) +
                ", lastDayIso='" + lastDayIso + '\'' +
                ", fedToday=" + fedToday +
                ", feedStreakDays=" + feedStreakDays +
                ", moodAbove80Secs=" + moodAbove80Secs +
                ", energyDayMin=" + energyDayMin +
                ", consecutivePomos=" + consecutivePomos +
                '}';
    }
}
