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

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            // Set background image
            backgroundImage.setImage(new Image(getClass().getResource("/images/ui/game_scene.png").toExternalForm()));

            // Set death message with pet name (localized)
            String petName = GameState.getPetName();
            String template = resources.getString("label.petDied");
            String message = template.replace("{0}", petName);
            deathMessage.setText(message);

            // Add shadow to death message
            DropShadow dropShadow = new DropShadow();
            dropShadow.setColor(Color.BLACK);
            dropShadow.setRadius(5);
            dropShadow.setOffsetX(2);
            dropShadow.setOffsetY(2);
            deathMessage.setEffect(dropShadow);

            // Set death GIF
            String petType = GameState.getPetType();
            if (petType != null) {
                String imagePath = "/images/pets/" + petType + "/" + petType + "_death.gif";
                URL petImageUrl = getClass().getResource(imagePath);
                if (petImageUrl != null) {
                    petDeathImage.setImage(new Image(petImageUrl.toExternalForm()));
                } else {
                    System.err.println("❌ Pet death GIF not found: " + imagePath);
                }
            }

        } catch (Exception e) {
            System.err.println("❌ DeathController initialization error: " + e.getMessage());
            e.printStackTrace();
        }
    }

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

    @FXML
    private void handleExit() {
        Stage stage = (Stage) exitButton.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void onHoverButton(MouseEvent event) {
        Button btn = (Button) event.getSource();
        btn.setStyle("-fx-background-color: #FF1493; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 20;");
    }

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
