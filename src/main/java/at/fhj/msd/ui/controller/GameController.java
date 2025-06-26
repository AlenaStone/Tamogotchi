package at.fhj.msd.ui.controller;

import java.net.URL;
import java.util.ResourceBundle;

import at.fhj.msd.model.GameState;
import at.fhj.msd.util.WindowDragHelper;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class GameController implements Initializable {

    @FXML
    private ImageView background;

    @FXML
    private Label petNameLabel;

    @FXML
    private ImageView petImage;

    @FXML
    private ProgressBar hungerBar;
    @FXML
    private ProgressBar energyBar;
    @FXML
    private ProgressBar moodBar;
    @FXML
    private ProgressBar healthBar;

    @FXML
    private Label closeLabel;
    @FXML
    private Label backLabel;

    @FXML
    private ImageView btnFeed;
    @FXML
    private ImageView btnSleep;
    @FXML
    private ImageView btnPlay;
    @FXML
    private ImageView btnHeal;
    @FXML
    private ImageView btnTomato;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            WindowDragHelper.makeDraggable(background);
            background.setImage(new Image(getClass().getResource("/images/ui/game_scene.png").toExternalForm()));

            // Pet name with shadow
            String petName = GameState.getPetName();
            petNameLabel.setText(petName != null ? petName : "Unnamed Pet");

            DropShadow dropShadow = new DropShadow();
            dropShadow.setColor(Color.BLACK);
            dropShadow.setRadius(3);
            dropShadow.setOffsetX(1);
            dropShadow.setOffsetY(1);
            petNameLabel.setEffect(dropShadow);

            // Pet image
            String petType = GameState.getPetType();
            if (petType != null) {
                String imagePath = "/images/pets/" + petType + "/" + petType + "_idle.gif";
                URL petImageUrl = getClass().getResource(imagePath);
                if (petImageUrl != null) {
                    petImage.setImage(new Image(petImageUrl.toExternalForm()));
                } else {
                    System.err.println("❌ Pet image not found: " + imagePath);
                }
            }

            // Button images
            btnFeed.setImage(new Image(getClass().getResource("/images/ui/food.png").toExternalForm()));
            btnSleep.setImage(new Image(getClass().getResource("/images/ui/sleep.png").toExternalForm()));
            btnPlay.setImage(new Image(getClass().getResource("/images/ui/game.png").toExternalForm()));
            btnHeal.setImage(new Image(getClass().getResource("/images/ui/health.png").toExternalForm()));
            btnTomato.setImage(new Image(getClass().getResource("/images/ui/watch.png").toExternalForm()));

            // Dummy progress values
            hungerBar.setProgress(0.7);
            energyBar.setProgress(0.5);
            moodBar.setProgress(0.9);
            healthBar.setProgress(0.8);

        } catch (Exception e) {
            System.err.println("❌ GameController initialization failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleMouseClick(MouseEvent event) {
        double x = event.getX();
        double y = event.getY();
        System.out.println("👉 Clicked at: X=" + x + ", Y=" + y);
    }

    @FXML
    private void handleFeed() {
        hungerBar.setProgress(Math.min(1.0, hungerBar.getProgress() + 0.1));
        System.out.println("🍴 Feeding pet");
    }

    @FXML
    private void handleSleep() {
        energyBar.setProgress(Math.min(1.0, energyBar.getProgress() + 0.1));
        System.out.println("😴 Pet sleeping");
    }

    @FXML
    private void handlePlay() {
        moodBar.setProgress(Math.min(1.0, moodBar.getProgress() + 0.1));
        System.out.println("😊 Pet playing");
    }

    @FXML
    private void handleHeal() {
        healthBar.setProgress(Math.min(1.0, healthBar.getProgress() + 0.1));
        System.out.println("💊 Healing pet");
    }

    @FXML
    private void handleTomato() {
        System.out.println("⏳ Pomodoro Timer - to be implemented");
    }

    @FXML
    private void handleBackSelection() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/name_input.fxml"));
            loader.setResources(GameState.getBundle());
            Parent root = loader.load();

            Scene scene = new Scene(root);
            scene.setFill(Color.TRANSPARENT);

            Stage stage = (Stage) background.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleClose() {
        Stage stage = (Stage) background.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void onHoverImage(MouseEvent event) {
        ImageView btn = (ImageView) event.getSource();
        btn.setOpacity(0.7);
        btn.setScaleX(1.05);
        btn.setScaleY(1.05);
    }

    @FXML
    private void onExitImage(MouseEvent event) {
        ImageView btn = (ImageView) event.getSource();
        btn.setOpacity(1.0);
        btn.setScaleX(1.0);
        btn.setScaleY(1.0);
    }

    @FXML
    private void onHoverBackLabel() {
        backLabel.setStyle("-fx-font-size: 20px; -fx-text-fill: white; -fx-background-color: red; "
                + "-fx-font-weight: bold; -fx-padding: 3px; -fx-background-radius: 5px; -fx-cursor: hand;");
    }

    @FXML
    private void onBackMove() {
        backLabel.setStyle("-fx-font-size: 20px; -fx-text-fill: red; -fx-background-color: rgba(255,255,255,0.5); "
                + "-fx-font-weight: bold; -fx-padding: 3px; -fx-background-radius: 5px; -fx-cursor: hand;");
    }

    @FXML
    private void onHoverClose() {
        closeLabel.setStyle("-fx-font-size: 20px; -fx-text-fill: white; -fx-background-color: red; "
                + "-fx-font-weight: bold; -fx-padding: 3px; -fx-background-radius: 5px; -fx-cursor: hand;");
    }

    @FXML
    private void onExitClose() {
        closeLabel.setStyle("-fx-font-size: 20px; -fx-text-fill: red; -fx-background-color: rgba(255,255,255,0.5); "
                + "-fx-font-weight: bold; -fx-padding: 3px; -fx-background-radius: 5px; -fx-cursor: hand;");
    }
}
