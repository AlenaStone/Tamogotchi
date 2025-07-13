package at.fhj.msd.ui.controller;

import java.net.URL;
import java.util.ResourceBundle;

import at.fhj.msd.model.GameState;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

/**
 * Controller for the death screen scene.
 * Displays death message, pet image, and handles restart/exit actions.
 */
public class DeathController implements Initializable {

    @FXML
    private ImageView backgroundImage;

    @FXML
    private Label deathMessage;

    @FXML
    private ImageView petDeathImage;

    @FXML
    private Button restartButton;

    @FXML
    private Button exitButton;

    /**
     * Initializes the death screen with localized messages and pet image.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        backgroundImage.setImage(new Image(getClass().getResource("/images/ui/game_scene.png").toExternalForm()));

        String petName = GameState.getPetName();
        String petType = GameState.getPetType();

        if (petName != null && resources.containsKey("label.petDied")) {
            String template = resources.getString("label.petDied");
            deathMessage.setText(template.replace("{0}", petName));
        } else {
            deathMessage.setText("Your pet has died.");
        }

        DropShadow dropShadow = new DropShadow();
        dropShadow.setColor(Color.BLACK);
        dropShadow.setRadius(5);
        dropShadow.setOffsetX(2);
        dropShadow.setOffsetY(2);
        deathMessage.setEffect(dropShadow);

        if (petType != null) {
            String imagePath = "/images/pets/" + petType + "/" + petType + "_death.gif";
            URL petImageUrl = getClass().getResource(imagePath);
            if (petImageUrl != null) {
                petDeathImage.setImage(new Image(petImageUrl.toExternalForm()));
            }
        }
    }

    /**
     * Handles the restart button click: returns to pet selection screen.
     */
    @FXML
    private void handleRestart() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/pet_selection.fxml"));
            loader.setResources(GameState.getBundle());
            Parent root = loader.load();

            Stage stage = (Stage) restartButton.getScene().getWindow();
            Scene scene = new Scene(root);
            scene.setFill(Color.TRANSPARENT);

            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Handles the exit button click: closes the application window.
     */
    @FXML
    private void handleExit() {
        Stage stage = (Stage) exitButton.getScene().getWindow();
        stage.close();
    }

    /**
     * Adds hover effect to the buttons.
     */
    @FXML
    private void onHoverButton(MouseEvent event) {
        Button btn = (Button) event.getSource();
        btn.setStyle("-fx-background-color: #FF1493; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 20;");
    }

    /**
     * Resets button style when mouse exits.
     */
    @FXML
    private void onExitButton(MouseEvent event) {
        Button btn = (Button) event.getSource();
        if (btn == restartButton) {
            btn.setStyle("-fx-background-color: #FF69B4; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 20;");
        } else if (btn == exitButton) {
            btn.setStyle("-fx-background-color: #C71585; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 20;");
        }
    }
}
// This controller manages the death screen, allowing users to restart or exit the game.