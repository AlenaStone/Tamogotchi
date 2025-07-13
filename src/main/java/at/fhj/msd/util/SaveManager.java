package at.fhj.msd.util;

import at.fhj.msd.model.PetSaveData;
import com.google.gson.Gson;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class SaveManager {
    private static final String SAVE_PATH = "save_data.json";
    private static final String LANGUAGE_PATH = "language_data.json";

    public static void save(PetSaveData data) {
        try (FileWriter writer = new FileWriter(SAVE_PATH)) {
            new Gson().toJson(data, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static PetSaveData load() {
        try (FileReader reader = new FileReader(SAVE_PATH)) {
            return new Gson().fromJson(reader, PetSaveData.class);
        } catch (IOException e) {
            return null;
        }
    }

    public static void saveLanguage(String language) {
        try (FileWriter writer = new FileWriter(LANGUAGE_PATH)) {
            new Gson().toJson(language, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String loadLanguage() {
        try (FileReader reader = new FileReader(LANGUAGE_PATH)) {
            return new Gson().fromJson(reader, String.class);
        } catch (IOException e) {
            return null;
        }
    }

    public static void delete() {
        File file = new File(SAVE_PATH);
        if (file.exists()) {
            file.delete();
        }
    }

    public static void deleteLanguage() {
        File file = new File(LANGUAGE_PATH);
        if (file.exists()) {
            file.delete();
        }
    }
}
