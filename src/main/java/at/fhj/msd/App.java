package at.fhj.msd;

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

import java.util.Locale;
import java.util.ResourceBundle;

public class App extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        String savedLanguage = SaveManager.loadLanguage();

        if (savedLanguage == null) {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/language_selection.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            stage.initStyle(StageStyle.TRANSPARENT);
            stage.setAlwaysOnTop(true);
            stage.setScene(scene);
            stage.setWidth(1024);
            stage.setHeight(1024);
            stage.show();
            return;
        }

        ResourceBundle bundle = ResourceBundle.getBundle("i18n.messages", new Locale(savedLanguage));

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
        scene.setFill(Color.TRANSPARENT);

        stage.initStyle(StageStyle.TRANSPARENT);
        stage.setAlwaysOnTop(true);
        stage.setScene(scene);
        stage.setWidth(1024);
        stage.setHeight(1024);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
