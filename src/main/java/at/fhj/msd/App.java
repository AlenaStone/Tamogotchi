/**
 * Main application class for the Tamagotchi desktop widget.
 * 
 * This class initializes the application window and loads the correct scene 
 * based on the saved language settings and pet data.
 * 
 * Author: Alena Vodopianova
 * Version: 1.0
 * Since: 2025-07-14
 */

package at.fhj.msd;

import java.util.Locale;
import java.util.ResourceBundle;

import at.fhj.msd.model.GameState;
import at.fhj.msd.model.PetSaveData;
import at.fhj.msd.util.SaveManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class App extends Application {

    /**
     * Entry point for the JavaFX application.
     * Loads the initial scene based on saved language and pet data.
     *
     * @param stage the main application window
     * @throws Exception if FXML loading fails
     */
    @Override
    public void start(Stage stage) throws Exception {
        // Load saved language from file
        String savedLanguage = SaveManager.loadLanguage();

        // If no language is saved, show language selection screen
        if (savedLanguage == null) {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/language_selection.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            setupStage(stage, scene);
            return;
        }

        // Load localized resource bundle
        ResourceBundle bundle = ResourceBundle.getBundle("i18n.messages", new Locale(savedLanguage));

        // Load saved pet data
        PetSaveData data = SaveManager.load();

        FXMLLoader loader;
        if (data != null) {
            GameState.setPetName(data.petName);
            GameState.setPetType(data.petType);
            loader = new FXMLLoader(getClass().getResource("/fxml/game_scene.fxml"), bundle);
        } else {
            loader = new FXMLLoader(getClass().getResource("/fxml/language_selection.fxml"), bundle);
        }

        Parent root = loader.load();
        Scene scene = new Scene(root);
        setupStage(stage, scene);
    }

    /**
     * Configures the stage with the given scene and window settings.
     *
     * @param stage the application stage
     * @param scene the scene to set
     */
    private void setupStage(Stage stage, Scene scene) {
        scene.setFill(Color.TRANSPARENT);
        stage.initStyle(StageStyle.TRANSPARENT);
        stage.setAlwaysOnTop(true);
        stage.setScene(scene);
        stage.setWidth(1024);
        stage.setHeight(1024);
        stage.show();
    }

    /**
     * Main method to launch the application.
     *
     * @param args command-line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
}

// To run the application, use the following command:
// mvn clean javafx:run