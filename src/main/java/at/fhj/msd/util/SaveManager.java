package at.fhj.msd.util;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import com.google.gson.Gson;

import at.fhj.msd.model.PetSaveData;

/**
 * Utility class for saving and loading application state.
 * Handles saving and loading pet data and language settings using JSON files
 * stored in the user's home directory under a dedicated Tamogotchi folder.
 */
public class SaveManager {
    /**
     * Base folder path for saving data files.
     */
    private static final String BASE_FOLDER = System.getProperty("user.home") + File.separator + "Tamogotchi";

    /**
     * Path to the pet save data JSON file.
     */
    private static final String SAVE_PATH = BASE_FOLDER + File.separator + "save_data.json";

    /**
     * Path to the language settings JSON file.
     */
    private static final String LANGUAGE_PATH = BASE_FOLDER + File.separator + "language_data.json";

    static {
        new File(BASE_FOLDER).mkdirs();
    }

    /**
     * Saves pet data to a JSON file.
     * @param data The PetSaveData object to save.
     */
    public static void save(PetSaveData data) {
        try (FileWriter writer = new FileWriter(SAVE_PATH)) {
            new Gson().toJson(data, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Loads pet data from a JSON file.
     * @return The loaded PetSaveData object, or null if not found.
     */
    public static PetSaveData load() {
        try (FileReader reader = new FileReader(SAVE_PATH)) {
            return new Gson().fromJson(reader, PetSaveData.class);
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * Saves the selected language code to a JSON file.
     * @param language The language code as String (e.g., "en", "ru").
     */
    public static void saveLanguage(String language) {
        try (FileWriter writer = new FileWriter(LANGUAGE_PATH)) {
            new Gson().toJson(language, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Loads the selected language code from a JSON file.
     * @return The language code as String, or null if not found.
     */
    public static String loadLanguage() {
        try (FileReader reader = new FileReader(LANGUAGE_PATH)) {
            return new Gson().fromJson(reader, String.class);
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * Deletes the pet save data file.
     */
    public static void delete() {
        new File(SAVE_PATH).delete();
    }

    /**
     * Deletes the language settings file.
     */
    public static void deleteLanguage() {
        new File(LANGUAGE_PATH).delete();
    }
}
