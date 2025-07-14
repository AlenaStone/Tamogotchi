/**
 * Controller for the language selection scene.
 * Manages language choice, updates application locale and scene transitions.
 */
package at.fhj.msd.ui.controller;

import java.io.IOException;
import java.util.Locale;
import java.util.ResourceBundle;

import at.fhj.msd.model.GameState;
import at.fhj.msd.util.SaveManager;
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

    /**
     * Initializes the language selection scene, loading UI images and setting up drag support.
     */
    public void initialize() {
        try {
            WindowDragHelper.makeDraggable(background);
            background.setImage(
                    new Image(getClass().getResource("/images/ui/tamagotchi_frame_final.png").toExternalForm()));
            btnEnglish.setImage(new Image(getClass().getResource("/images/ui/button_en.png").toExternalForm()));
            btnRussian.setImage(new Image(getClass().getResource("/images/ui/button_ru.png").toExternalForm()));
        } catch (Exception e) {
            System.err.println("❌ Failed to load UI images: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Handles hover effect on language buttons.
     */
    @FXML
    private void onHoverImage(MouseEvent event) {
        ImageView button = (ImageView) event.getSource();
        button.setOpacity(0.7);
        button.setScaleX(1.05);
        button.setScaleY(1.05);
    }

    /**
     * Resets button style when hover ends.
     */
    @FXML
    private void onExitImage(MouseEvent event) {
        ImageView button = (ImageView) event.getSource();
        button.setOpacity(1.0);
        button.setScaleX(1.0);
        button.setScaleY(1.0);
    }

    /**
     * Selects English language and applies it.
     */
    @FXML
    private void onEnglishClick() {
        animateClick(btnEnglish);
        switchLanguage("en");
    }

    /**
     * Selects Russian language and applies it.
     */
    @FXML
    private void onRussianClick() {
        animateClick(btnRussian);
        switchLanguage("ru");
    }

    /**
     * Adds a click animation effect to the button.
     */
    private void animateClick(ImageView button) {
        button.setScaleX(0.95);
        button.setScaleY(0.95);
        new Thread(() -> {
            try {
                Thread.sleep(100);
            } catch (InterruptedException ignored) {
            }
            Platform.runLater(() -> {
                button.setScaleX(1.0);
                button.setScaleY(1.0);
            });
        }).start();
    }

    /**
     * Switches application language and updates the scene.
     * @param lang The language code (e.g., "en", "ru").
     */
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

    /**
     * Closes the application window.
     */
    @FXML
    private void handleClose() {
        Stage stage = (Stage) background.getScene().getWindow();
        stage.close();
    }

    /**
     * Adds hover effect to the close label.
     */
    @FXML
    private void onHoverClose() {
        closeLabel.setStyle(
                "-fx-font-size: 20px; -fx-text-fill: white; -fx-background-color: red; -fx-font-weight: bold; -fx-padding: 3px; -fx-background-radius: 5px; -fx-cursor: hand;");
    }

    /**
     * Resets close label style after hover.
     */
    @FXML
    private void onExitClose() {
        closeLabel.setStyle(
                "-fx-font-size: 20px; -fx-text-fill: red; -fx-background-color: rgba(255,255,255,0.5); -fx-font-weight: bold; -fx-padding: 3px; -fx-background-radius: 5px; -fx-cursor: hand;");
    }
}
