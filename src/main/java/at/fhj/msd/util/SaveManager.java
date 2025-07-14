/**
 * Utility class for saving and loading game and language data using JSON files.
 * Handles pet save data and selected language preferences.
 */
package at.fhj.msd.util;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import com.google.gson.Gson;

import at.fhj.msd.model.PetSaveData;

public class SaveManager {

    private static final String SAVE_PATH = "save_data.json";
    private static final String LANGUAGE_PATH = "language_data.json";

    /**
     * Saves the given pet state data to a JSON file.
     * @param data PetSaveData object containing the pet's state.
     */
    public static void save(PetSaveData data) {
        try (FileWriter writer = new FileWriter(SAVE_PATH)) {
            new Gson().toJson(data, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Loads the saved pet state from the JSON file.
     * @return PetSaveData object if file exists, otherwise null.
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
     * @param language Language code as a String (e.g., "en", "ru").
     */
    public static void saveLanguage(String language) {
        try (FileWriter writer = new FileWriter(LANGUAGE_PATH)) {
            new Gson().toJson(language, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Loads the saved language code from the JSON file.
     * @return Language code as a String, or null if file does not exist.
     */
    public static String loadLanguage() {
        try (FileReader reader = new FileReader(LANGUAGE_PATH)) {
            return new Gson().fromJson(reader, String.class);
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * Deletes the saved pet state file if it exists.
     */
    public static void delete() {
        File file = new File(SAVE_PATH);
        if (file.exists()) {
            file.delete();
        }
    }

    /**
     * Deletes the saved language file if it exists.
     */
    public static void deleteLanguage() {
        File file = new File(LANGUAGE_PATH);
        if (file.exists()) {
            file.delete();
        }
    }
}
