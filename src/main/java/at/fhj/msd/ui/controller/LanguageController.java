package at.fhj.msd.ui.controller;

import java.io.IOException;
import java.util.Locale;
import java.util.ResourceBundle;
import at.fhj.msd.util.SaveManager;

import at.fhj.msd.model.GameState;
import at.fhj.msd.util.WindowDragHelper;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

public class LanguageController {

    @FXML
    private ImageView background;

    @FXML
    private ImageView btnEnglish;

    @FXML
    private ImageView btnRussian;

    @FXML
    private Label closeLabel;


    public void initialize() {
        try {
            WindowDragHelper.makeDraggable(background);
            background.setImage(new Image(getClass().getResource("/images/ui/tamagotchi_frame_final.png").toExternalForm()));
            btnEnglish.setImage(new Image(getClass().getResource("/images/ui/button_en.png").toExternalForm()));
            btnRussian.setImage(new Image(getClass().getResource("/images/ui/button_ru.png").toExternalForm()));
        } catch (Exception e) {
            System.err.println("❌ Failed to load UI images: " + e.getMessage());
            e.printStackTrace();
        }
    }



    @FXML
    private void onHoverImage(MouseEvent event) {
        ImageView button = (ImageView) event.getSource();
        button.setOpacity(0.7);
        button.setScaleX(1.05);
        button.setScaleY(1.05);
    }

    @FXML
    private void onExitImage(MouseEvent event) {
        ImageView button = (ImageView) event.getSource();
        button.setOpacity(1.0);
        button.setScaleX(1.0);
        button.setScaleY(1.0);
    }

    @FXML
    private void onEnglishClick() {
        animateClick(btnEnglish);
        switchLanguage("en");
    }

    @FXML
    private void onRussianClick() {
        animateClick(btnRussian);
        switchLanguage("ru");
    }

    private void animateClick(ImageView button) {
        button.setScaleX(0.95);
        button.setScaleY(0.95);
        new Thread(() -> {
            try {
                Thread.sleep(100);
            } catch (InterruptedException ignored) {}
            Platform.runLater(() -> {
                button.setScaleX(1.0);
                button.setScaleY(1.0);
            });
        }).start();
    }

    private void switchLanguage(String lang) {
        try {
          

            GameState.setLocale(new Locale(lang));
              SaveManager.saveLanguage(lang);
              
            ResourceBundle bundle = ResourceBundle.getBundle("i18n.messages", new Locale(lang));

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/pet_selection.fxml"), bundle);
            Parent root = loader.load();

            Scene scene = new Scene(root);
            scene.setFill(javafx.scene.paint.Color.TRANSPARENT);

            Stage stage = (Stage) background.getScene().getWindow();
            stage.setScene(scene);
            stage.sizeToScene();

        } catch (IOException e) {
            System.err.println("❌ Failed to switch language: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleClose() {
        Stage stage = (Stage) background.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void onHoverClose() {
        closeLabel.setStyle("-fx-font-size: 20px; -fx-text-fill: white; -fx-background-color: red; -fx-font-weight: bold; -fx-padding: 3px; -fx-background-radius: 5px; -fx-cursor: hand;");
    }

    @FXML
    private void onExitClose() {
        closeLabel.setStyle("-fx-font-size: 20px; -fx-text-fill: red; -fx-background-color: rgba(255,255,255,0.5); -fx-font-weight: bold; -fx-padding: 3px; -fx-background-radius: 5px; -fx-cursor: hand;");
    }
}


// mvn clean javafx:run 