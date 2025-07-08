package at.fhj.msd.ui.controller;

import java.net.URL;
import java.util.ResourceBundle;

import at.fhj.msd.model.GameState;
import at.fhj.msd.util.WindowDragHelper;
import javafx.animation.KeyFrame;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.application.Platform;
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
import javafx.util.Duration;

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
    @FXML
    private Label pomodoroTimerLabel;

    private Timeline statsDecreaseTimeline;
    private Timeline pomodoroTimeline;
    private int remainingSeconds;
    private boolean isPomodoroActive = false;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            WindowDragHelper.makeDraggable(background);
            background.setImage(new Image(getClass().getResource("/images/ui/game_scene.png").toExternalForm()));

            String petName = GameState.getPetName();
            petNameLabel.setText(petName != null ? petName : "Unnamed Pet");

            DropShadow dropShadow = new DropShadow();
            dropShadow.setColor(Color.BLACK);
            dropShadow.setRadius(3);
            dropShadow.setOffsetX(1);
            dropShadow.setOffsetY(1);
            petNameLabel.setEffect(dropShadow);

            updatePetAnimation("idle");

            btnFeed.setImage(new Image(getClass().getResource("/images/ui/food.png").toExternalForm()));
            btnSleep.setImage(new Image(getClass().getResource("/images/ui/sleep.png").toExternalForm()));
            btnPlay.setImage(new Image(getClass().getResource("/images/ui/game.png").toExternalForm()));
            btnHeal.setImage(new Image(getClass().getResource("/images/ui/health.png").toExternalForm()));
            btnTomato.setImage(new Image(getClass().getResource("/images/ui/watch.png").toExternalForm()));

            hungerBar.setProgress(1.0);
            energyBar.setProgress(1.0);
            moodBar.setProgress(1.0);
            healthBar.setProgress(1.0);

            // Initialize Pomodoro label as invisible with transparent background
            pomodoroTimerLabel.setText("");
            pomodoroTimerLabel.setStyle("-fx-background-color: transparent;");

            startStatsDecrease();

        } catch (Exception e) {
            System.err.println("❌ GameController initialization failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void updatePetAnimation(String state) {
        String petType = GameState.getPetType();
        if (petType != null) {
            String imagePath = "/images/pets/" + petType + "/" + petType + "_" + state + ".gif";
            URL petImageUrl = getClass().getResource(imagePath);
            if (petImageUrl != null) {
                petImage.setImage(new Image(petImageUrl.toExternalForm()));
            } else {
                System.err.println("❌ Image for state '" + state + "' not found: " + imagePath);
            }
        }
    }

    private void playTemporaryAnimation(String actionState) {
        updatePetAnimation(actionState);
        PauseTransition pause = new PauseTransition(Duration.seconds(5));

        pause.setOnFinished(event -> updatePetAnimation("idle"));

        pause.play();
    }

    private void startStatsDecrease() {
        statsDecreaseTimeline = new Timeline(new KeyFrame(Duration.seconds(5), e -> {
            decreaseStats();
        }));
        statsDecreaseTimeline.setCycleCount(Timeline.INDEFINITE);
        statsDecreaseTimeline.play();
    }

    private void decreaseStats() {
        changeStats(-0.02, -0.02, -0.01, -0.005);
        System.out.println("Hunger: " + hungerBar.getProgress() + ", Energy: " + energyBar.getProgress());
        checkIfDead();
    }

    private void checkIfDead() {
        if (hungerBar.getProgress() <= 0 || energyBar.getProgress() <= 0 ||
                moodBar.getProgress() <= 0 || healthBar.getProgress() <= 0) {
            handleDeath();
        }
    }

    private void handleDeath() {
        statsDecreaseTimeline.stop();

        // Show death animation
        updatePetAnimation("death");

        // Disable all buttons during death animation
        btnFeed.setDisable(true);
        btnSleep.setDisable(true);
        btnPlay.setDisable(true);
        btnHeal.setDisable(true);
        btnTomato.setDisable(true);

        PauseTransition pause = new PauseTransition(Duration.seconds(15));
        pause.setOnFinished(event -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/death_scene.fxml"));
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
        });
        pause.play();
    }

    private void changeStats(double hungerDelta, double energyDelta, double moodDelta, double healthDelta) {
        updateProgressBar(hungerBar, hungerBar.getProgress() + hungerDelta);
        updateProgressBar(energyBar, energyBar.getProgress() + energyDelta);
        updateProgressBar(moodBar, moodBar.getProgress() + moodDelta);
        updateProgressBar(healthBar, healthBar.getProgress() + healthDelta);
    }

    private void updateProgressBar(ProgressBar bar, double value) {
        double clampedValue = Math.max(0, Math.min(1.0, value)); // Clamp between 0 and 1
        Platform.runLater(() -> {
            bar.setProgress(clampedValue);
            bar.setStyle(clampedValue > 0.3 ? "-fx-accent: green;" : "-fx-accent: red;");
        });
    }

    @FXML
    private void handleFeed() {
        if (!isPomodoroActive) {
            changeStats(+0.2, -0.02, -0.01, 0);
            playTemporaryAnimation("eat");
            System.out.println("🍴 Feeding pet");
        }
    }

    @FXML
    private void handleSleep() {
        if (!isPomodoroActive) {
            changeStats(-0.03, +0.2, -0.01, 0);
            playTemporaryAnimation("sleep");
            System.out.println("😴 Pet sleeping");
        }
    }

    @FXML
    private void handlePlay() {
        if (!isPomodoroActive) {
            changeStats(-0.04, -0.03, +0.2, 0);
            playTemporaryAnimation("play");
            System.out.println("😊 Pet playing");
        }
    }

    @FXML
    private void handleHeal() {
        if (!isPomodoroActive) {
            changeStats(-0.05, -0.03, -0.02, +0.1);
            playTemporaryAnimation("heal");
            System.out.println("💊 Healing pet");
        }
    }

    @FXML
    private void handleTomato() {
        if (isPomodoroActive) {
            // Pomodoro is already running
            System.out.println(GameState.getBundle().getString("message.pomodoroAlreadyRunning"));
            return;
        }

        System.out.println(GameState.getBundle().getString("message.pomodoroStarted"));

        isPomodoroActive = true;
        remainingSeconds = 25 * 60; // 25 minutes

        // Make Pomodoro timer label visible with background and initial time
        Platform.runLater(() -> {
            pomodoroTimerLabel.setStyle("-fx-font-size: 20px; -fx-text-fill: white; -fx-font-weight: bold; "
                    + "-fx-background-color: rgba(0, 0, 0, 0.6); -fx-background-radius: 10; -fx-padding: 5px; "
                    + "-fx-effect: dropshadow(one-pass-box, black, 3, 0.5, 0, 0);");
            pomodoroTimerLabel.setText(formatTime(remainingSeconds));
        });

        // Stop stats decrease during Pomodoro
        statsDecreaseTimeline.stop();

        pomodoroTimeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            remainingSeconds--;
            Platform.runLater(() -> pomodoroTimerLabel.setText(formatTime(remainingSeconds)));

            if (remainingSeconds <= 0) {
                pomodoroTimeline.stop();
                isPomodoroActive = false;

                // Resume stats decrease
                statsDecreaseTimeline.play();

                // Show Pomodoro finished message
                Platform.runLater(() -> pomodoroTimerLabel.setText(GameState.getBundle().getString("message.pomodoroFinished")));
                System.out.println(GameState.getBundle().getString("message.pomodoroFinishedLog"));

                // Hide timer label after 3 seconds
                PauseTransition hideTimer = new PauseTransition(Duration.seconds(3));
                hideTimer.setOnFinished(event -> hidePomodoroLabel());
                hideTimer.play();
            }
        }));

        pomodoroTimeline.setCycleCount(Timeline.INDEFINITE);
        pomodoroTimeline.play();
    }

    // Format time as MM:SS
    private String formatTime(int totalSeconds) {
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    // Hide Pomodoro label and remove background
    private void hidePomodoroLabel() {
        Platform.runLater(() -> {
            pomodoroTimerLabel.setText("");
            pomodoroTimerLabel.setStyle("-fx-background-color: transparent;");
        });
    }

    @FXML
    private void handleMouseClick(MouseEvent event) {
        double x = event.getX();
        double y = event.getY();
        System.out.println("👉 Clicked at: X=" + x + ", Y=" + y);
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
