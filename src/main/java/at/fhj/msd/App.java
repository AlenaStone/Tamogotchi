/**
 * Main application class for the Tamagotchi desktop widget.
 * 
 * This class initializes the application window and loads the correct scene 
 * based on the saved language settings and pet data.
 * 
 * Author: Alena Vodopianova
 * Version: 1.1
 * Since: 2025-08-11
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

/**
 * Main application entry point.
 *
 * - Restores language from storage and sets the global ResourceBundle.
 * - Loads either the game scene (if a pet save exists) or the language selector.
 * - Configures the stage as a transparent, always-on-top widget.
 */
public class App extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        // 1) Restore language (fallback to English)
        String savedLang = SaveManager.loadLanguage();        // e.g. "ru" / "en" / null
        Locale locale = (savedLang == null || savedLang.isBlank())
                ? Locale.ENGLISH
                : Locale.forLanguageTag(savedLang);

        // Load bundle and store it globally so controllers can reuse it
        ResourceBundle bundle = ResourceBundle.getBundle("i18n.messages", locale);
        GameState.setBundle(bundle);

        // 2) Decide which scene to show
        PetSaveData save = SaveManager.load();

        String fxml = (save != null)
                ? "/fxml/game_scene.fxml"
                : "/fxml/language_selection.fxml";

        if (save != null) {
            // Pre-fill GameState so GameController sees the pet immediately
            GameState.setPetName(save.petName);
            GameState.setPetType(save.petType);
        }

        // 3) Load FXML with the bundle (IMPORTANT for i18n in %keys)
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml), bundle);
        Parent root = loader.load();

        // 4) Stage setup
        Scene scene = new Scene(root);
        scene.setFill(Color.TRANSPARENT);

        stage.initStyle(StageStyle.TRANSPARENT);
        stage.setAlwaysOnTop(true);
        stage.setScene(scene);

        // Let the FXML define the size; don’t hard-code 1024x1024
        stage.sizeToScene();
        stage.centerOnScreen();

        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

// To run the application, use the following command:
// mvn clean javafx:run