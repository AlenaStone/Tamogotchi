package at.fhj.msd.model;

import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Global game state (singleton-style via static fields).
 * Holds current pet data and the active i18n bundle.
 *
 * You can either:
 * - call {@link #setLocale(Locale)} to switch language by locale, or
 * - call {@link #setBundle(ResourceBundle)} to inject a specific bundle.
 *
 * Controllers should read strings via {@link #getBundle()} or
 * {@link #i18n(String)}.
 *
 * @author Alena Vodopianova
 * @version 2.0
 * @since 2025-07-14
 */
public final class GameState {

    // ---------------- Pet data ----------------
    private static String petName;
    private static String petType;

    // ---------------- I18N ----------------
    /** Current locale (defaults to ENGLISH). */
    private static Locale locale = Locale.ENGLISH;

    /** Active bundle; if null, it will be (re)loaded from {@link #locale}. */
    private static ResourceBundle bundle = null;

    private GameState() {
        /* no instances */ }

    // ---------- Pet getters/setters ----------
    /** Set current pet name. */
    public static void setPetName(String name) {
        petName = name;
    }

    /** Get current pet name. */
    public static String getPetName() {
        return petName;
    }

    /** Set current pet type (e.g., "cat"). */
    public static void setPetType(String type) {
        petType = type;
    }

    /** Get current pet type. */
    public static String getPetType() {
        return petType;
    }

    // ---------- I18N: bundle + locale ----------
    /**
     * Explicitly set the active ResourceBundle.
     * Use this from App.start() after you load the bundle once.
     */
    public static void setBundle(ResourceBundle b) {
        bundle = b;
    }

    /**
     * Get the active ResourceBundle.
     * If none was explicitly set, it will be loaded from the current
     * {@link #locale}.
     */
    public static ResourceBundle getBundle() {
        ensureBundle();
        return bundle;
    }

    /**
     * Convenience helper: translate a key or return the key if missing.
     */
    public static String i18n(String key) {
        ResourceBundle b = getBundle();
        return (b != null && b.containsKey(key)) ? b.getString(key) : key;
    }

    /**
     * Set application locale and (re)load the bundle from it.
     * Calling this overrides any previously injected bundle.
     */
    public static void setLocale(Locale loc) {
        locale = (loc == null) ? Locale.ENGLISH : loc;
        // force reload from the new locale
        bundle = ResourceBundle.getBundle("i18n.messages", locale);
    }

    /** Get current locale. */
    public static Locale getLocale() {
        return locale;
    }

    // ---------- Internals ----------
    private static void ensureBundle() {
        if (bundle == null) {
            bundle = ResourceBundle.getBundle("i18n.messages", (locale == null) ? Locale.ENGLISH : locale);
        }
    }

    public static void initDefaultsIfEmpty(PetSaveData s) {
        if (s == null)
            return;
        if (s.emotion == null)
            s.emotion = "idle";
        if (s.achievements == null)
            s.achievements = new java.util.HashSet<>();
    }

}
