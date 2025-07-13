/**
 * Controller for the main game scene.
 * Manages pet state, Pomodoro timer, and user interactions.
 */
package at.fhj.msd.ui.controller;

import java.net.URL;
import java.util.ResourceBundle;

import at.fhj.msd.model.GameState;
import at.fhj.msd.model.PetSaveData;
import at.fhj.msd.util.SaveManager;
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
    private boolean isAnimationPlaying = false;
    private boolean isDead = false;

    /**
     * Initializes the game scene, loading pet state and setting up UI components.
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            WindowDragHelper.makeDraggable(background);
            background.setImage(new Image(getClass().getResource("/images/ui/game_scene.png").toExternalForm()));
            loadPetState();
            updatePetAnimation("idle");
            setButtonImages();
            pomodoroTimerLabel.setText("");
            pomodoroTimerLabel.setStyle("-fx-background-color: transparent;");
            startStatsDecrease();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Loads saved pet state from JSON when initializing the game scene.
     */
    private void loadPetState() {
        PetSaveData data = SaveManager.load();
        if (data != null) {
            GameState.setPetName(data.petName);
            GameState.setPetType(data.petType);
            petNameLabel.setText(data.petName);
            hungerBar.setProgress(data.hunger);
            energyBar.setProgress(data.energy);
            moodBar.setProgress(data.mood);
            healthBar.setProgress(data.health);
        } else {
            hungerBar.setProgress(1.0);
            energyBar.setProgress(1.0);
            moodBar.setProgress(1.0);
            healthBar.setProgress(1.0);
            petNameLabel.setText(GameState.getPetName() != null ? GameState.getPetName() : "Unnamed Pet");
        }
        applyLabelEffect();
    }

    /**
     * Saves current pet state to a file.
     */
    private void savePetState() {
        PetSaveData data = new PetSaveData();
        data.petName = GameState.getPetName();
        data.petType = GameState.getPetType();
        data.hunger = hungerBar.getProgress();
        data.energy = energyBar.getProgress();
        data.mood = moodBar.getProgress();
        data.health = healthBar.getProgress();
        SaveManager.save(data);
    }

    /**
     * Applies a drop shadow effect to the pet name label.
     */
    private void applyLabelEffect() {
        DropShadow dropShadow = new DropShadow();
        dropShadow.setColor(Color.BLACK);
        dropShadow.setRadius(3);
        dropShadow.setOffsetX(1);
        dropShadow.setOffsetY(1);
        petNameLabel.setEffect(dropShadow);
    }

    /**
     * Sets images for all pet interaction buttons.
     */
    private void setButtonImages() {
        btnFeed.setImage(new Image(getClass().getResource("/images/ui/food.png").toExternalForm()));
        btnSleep.setImage(new Image(getClass().getResource("/images/ui/sleep.png").toExternalForm()));
        btnPlay.setImage(new Image(getClass().getResource("/images/ui/game.png").toExternalForm()));
        btnHeal.setImage(new Image(getClass().getResource("/images/ui/health.png").toExternalForm()));
        btnTomato.setImage(new Image(getClass().getResource("/images/ui/watch.png").toExternalForm()));
    }

    private void updatePetAnimation(String state) {
        String petType = GameState.getPetType();
        if (petType != null) {
            String imagePath = "/images/pets/" + petType + "/" + petType + "_" + state + ".gif";
            URL petImageUrl = getClass().getResource(imagePath);
            if (petImageUrl != null) {
                petImage.setImage(new Image(petImageUrl.toExternalForm()));
            }
        }
    }

    private void playTemporaryAnimation(String actionState) {
        if (isAnimationPlaying || isDead)
            return;
        isAnimationPlaying = true;
        updatePetAnimation(actionState);
        PauseTransition pause = new PauseTransition(Duration.seconds(5));
        pause.setOnFinished(event -> {
            if (!isPomodoroActive && !isDead) {
                updatePetAnimation("idle");
            }
            isAnimationPlaying = false;
        });
        pause.play();
    }

    private void startStatsDecrease() {
        statsDecreaseTimeline = new Timeline(new KeyFrame(Duration.seconds(5), e -> decreaseStats()));
        statsDecreaseTimeline.setCycleCount(Timeline.INDEFINITE);
        statsDecreaseTimeline.play();
    }

    private void decreaseStats() {
        changeStats(-0.02, -0.02, -0.01, -0.005);
        checkIfDead();
    }

    private void checkIfDead() {
        if (hungerBar.getProgress() <= 0 || energyBar.getProgress() <= 0 ||
                moodBar.getProgress() <= 0 || healthBar.getProgress() <= 0) {
            handleDeath();
        }
    }

    private void handleDeath() {
        if (isDead) return;
        isDead = true;
        isAnimationPlaying = true;
        statsDecreaseTimeline.stop();
        updatePetAnimation("death");
        disableButtons();
        SaveManager.delete();
        PauseTransition pause = new PauseTransition(Duration.seconds(15));
        pause.setOnFinished(event -> loadDeathScene());
        pause.play();
    }

    private void disableButtons() {
        btnFeed.setDisable(true);
        btnSleep.setDisable(true);
        btnPlay.setDisable(true);
        btnHeal.setDisable(true);
        btnTomato.setDisable(true);
    }

    private void loadDeathScene() {
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
    }

    private void changeStats(double hungerDelta, double energyDelta, double moodDelta, double healthDelta) {
        updateProgressBar(hungerBar, hungerBar.getProgress() + hungerDelta);
        updateProgressBar(energyBar, energyBar.getProgress() + energyDelta);
        updateProgressBar(moodBar, moodBar.getProgress() + moodDelta);
        updateProgressBar(healthBar, healthBar.getProgress() + healthDelta);
    }

    private void updateProgressBar(ProgressBar bar, double value) {
        double clampedValue = Math.max(0, Math.min(1.0, value));
        Platform.runLater(() -> {
            bar.setProgress(clampedValue);
            bar.setStyle(clampedValue > 0.3 ? "-fx-accent: green;" : "-fx-accent: red;");
        });
    }

    // Pet interaction methods
    @FXML private void handleFeed() {
        if (!isPomodoroActive) {
            changeStats(+0.2, -0.02, -0.01, 0);
            playTemporaryAnimation("eat");
        }
    }

    @FXML private void handleSleep() {
        if (!isPomodoroActive) {
            changeStats(-0.03, +0.2, -0.01, 0);
            playTemporaryAnimation("sleep");
        }
    }

    @FXML private void handlePlay() {
        if (!isPomodoroActive) {
            changeStats(-0.04, -0.03, +0.2, 0);
            playTemporaryAnimation("play");
        }
    }

    @FXML private void handleHeal() {
        if (!isPomodoroActive) {
            changeStats(-0.05, -0.03, -0.02, +0.1);
            playTemporaryAnimation("heal");
        }
    }

    @FXML private void handleTomato() {
        if (isPomodoroActive) return;
        isPomodoroActive = true;
        remainingSeconds = 25 * 60;
        updatePomodoroLabelStyle();
        pomodoroTimerLabel.setText(formatTime(remainingSeconds));
        statsDecreaseTimeline.stop();
        pomodoroTimeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> updatePomodoro()));
        pomodoroTimeline.setCycleCount(Timeline.INDEFINITE);
        pomodoroTimeline.play();
    }

    private void updatePomodoro() {
        remainingSeconds--;
        pomodoroTimerLabel.setText(formatTime(remainingSeconds));
        if (remainingSeconds <= 0) {
            pomodoroTimeline.stop();
            isPomodoroActive = false;
            statsDecreaseTimeline.play();
            pomodoroTimerLabel.setText(GameState.getBundle().getString("message.pomodoroFinished"));
            PauseTransition hideTimer = new PauseTransition(Duration.seconds(3));
            hideTimer.setOnFinished(event -> hidePomodoroLabel());
            hideTimer.play();
        }
    }

    private void updatePomodoroLabelStyle() {
        Platform.runLater(() -> pomodoroTimerLabel.setStyle(
                "-fx-font-size: 20px; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-color: rgba(0, 0, 0, 0.6); -fx-background-radius: 10; -fx-padding: 5px; -fx-effect: dropshadow(one-pass-box, black, 3, 0.5, 0, 0);"));
    }

    private String formatTime(int totalSeconds) {
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    private void hidePomodoroLabel() {
        Platform.runLater(() -> {
            pomodoroTimerLabel.setText("");
            pomodoroTimerLabel.setStyle("-fx-background-color: transparent;");
        });
    }

    @FXML private void handleBackSelection() {
        SaveManager.delete();
        GameState.setPetName(null);
        GameState.setPetType(null);
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/language_selection.fxml"));
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

    @FXML private void handleClose() {
        savePetState();
        Stage stage = (Stage) background.getScene().getWindow();
        stage.close();
    }

    // UI hover effects
    @FXML private void onHoverImage(MouseEvent event) {
        ImageView btn = (ImageView) event.getSource();
        btn.setOpacity(0.7);
        btn.setScaleX(1.05);
        btn.setScaleY(1.05);
    }

    @FXML private void onExitImage(MouseEvent event) {
        ImageView btn = (ImageView) event.getSource();
        btn.setOpacity(1.0);
        btn.setScaleX(1.0);
        btn.setScaleY(1.0);
    }

    @FXML private void onHoverBackLabel() {
        backLabel.setStyle("-fx-font-size: 20px; -fx-text-fill: white; -fx-background-color: red; -fx-font-weight: bold; -fx-padding: 3px; -fx-background-radius: 5px; -fx-cursor: hand;");
    }

    @FXML private void onBackMove() {
        backLabel.setStyle("-fx-font-size: 20px; -fx-text-fill: red; -fx-background-color: rgba(255,255,255,0.5); -fx-font-weight: bold; -fx-padding: 3px; -fx-background-radius: 5px; -fx-cursor: hand;");
    }

    @FXML private void onHoverClose() {
        closeLabel.setStyle("-fx-font-size: 20px; -fx-text-fill: white; -fx-background-color: red; -fx-font-weight: bold; -fx-padding: 3px; -fx-background-radius: 5px; -fx-cursor: hand;");
    }

    @FXML private void onExitClose() {
        closeLabel.setStyle("-fx-font-size: 20px; -fx-text-fill: red; -fx-background-color: rgba(255,255,255,0.5); -fx-font-weight: bold; -fx-padding: 3px; -fx-background-radius: 5px; -fx-cursor: hand;");
    }
}
