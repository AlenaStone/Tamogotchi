package at.fhj.msd.ui.controller;

import java.net.URL;
import java.util.ResourceBundle;

import at.fhj.msd.model.GameState;
import at.fhj.msd.model.PetSaveData;
import at.fhj.msd.ui.controller.PomodoroSettingsController.PomodoroSettings;
import at.fhj.msd.util.SaveManager;
import at.fhj.msd.util.WindowDragHelper;
import javafx.animation.KeyFrame;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.event.Event;
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
import javafx.scene.layout.VBox;
import javafx.scene.media.AudioClip;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

public class GameController implements Initializable {

    // ===== UI: main scene =====
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

    // ===== UI: Pomodoro MENU (matches FXML) =====
    @FXML
    private VBox pomodoroMenuBox;
    @FXML
    private Label pomodoroTimeLabel;
    @FXML
    private Label pomodoroStatusLabel;
    @FXML
    private ProgressBar pomodoroProgress;
    @FXML
    private Label btnPomodoroStart;
    @FXML
    private Label btnPomodoroPause;
    @FXML
    private Label btnPomodoroReset;
    @FXML
    private Label btnPomodoroSettings;
    @FXML
    private Label pomodoroCycleLabel;

    // ===== UI: Pomodoro HUD (matches FXML) =====
    @FXML
    private VBox pomodoroHudBox;
    @FXML
    private Label pomodoroTimeHUDLabel;
    @FXML
    private Label pomodoroStatusHUDLabel;
    @FXML
    private ProgressBar pomodoroProgressHUD;
    @FXML
    private Label pomodoroCycleHUDLabel;

    // ===== Pet stats =====
    private Timeline statsDecreaseTimeline;
    private boolean isAnimationPlaying = false;
    private boolean isDead = false;

    // ===== Pomodoro state =====
    private enum Mode {
        FOCUS, SHORT_BREAK, LONG_BREAK
    }

    private Mode mode = Mode.FOCUS;

    private Timeline pomodoroTimeline;
    private int remainingSeconds = 0;
    private int totalSecondsThisInterval = 0;
    private boolean pomodoroActive = false; // ticking
    private boolean pomodoroPaused = false; // paused

    // Settings (can be overridden from settings modal)
    private int focusMinutes = 25;
    private int shortBreakMinutes = 5;
    private int longBreakMinutes = 15;
    private int cyclesBeforeLong = 4;
    private boolean autoStartNext = false;
    private boolean soundOn = true;
    private ResourceBundle bundle;

    private String tr(String key) {
        return bundle != null ? bundle.getString(key) : key;
    }

    private int completedFocusInCycle = 0;

    // End-of-interval sound
    private AudioClip finishClip;

    // ===== Init =====
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            // window drag + background
            WindowDragHelper.makeDraggable(background);
            background.setImage(new Image(getClass().getResource("/images/ui/game_scene.png").toExternalForm()));

            // pet state + visuals
            loadPetState();
            updatePetAnimation("idle");
            setButtonImages();

            // ensure timer is off and both containers are hidden
            pomodoroActive = false;
            pomodoroPaused = false;
            if (pomodoroTimeline != null)
                pomodoroTimeline.stop();

            hideMenu();
            hideHud();

            this.bundle = (rb != null) ? rb : GameState.getBundle();

            String defTime = formatTime(focusMinutes * 60);
            // defaults for MENU
            pomodoroStatusLabel.setText(tr("pomodoro.status.focus"));
            pomodoroTimeLabel.setText(defTime);
            pomodoroProgress.setProgress(0);
            pomodoroCycleLabel.setText("0/" + cyclesBeforeLong);

            // HUD defaults
            pomodoroStatusHUDLabel.setText(tr("pomodoro.status.focus"));
            pomodoroTimeHUDLabel.setText(defTime);
            pomodoroProgressHUD.setProgress(0);
            pomodoroCycleHUDLabel.setText("0/" + cyclesBeforeLong);

            // end sound
            URL soundUrl = getClass().getResource("/sounds/finish.wav");
            if (soundUrl != null)
                finishClip = new AudioClip(soundUrl.toExternalForm());

            // passive pet decay
            startStatsDecrease();

            updatePomodoroButtonsState();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ===== Pet state load/save =====
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
        DropShadow ds = new DropShadow();
        ds.setColor(Color.BLACK);
        ds.setRadius(3);
        ds.setOffsetX(1);
        ds.setOffsetY(1);
        petNameLabel.setEffect(ds);
    }

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

    // ===== Pet visuals & stats =====
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
            String path = "/images/pets/" + petType + "/" + petType + "_" + state + ".gif";
            URL u = getClass().getResource(path);
            if (u != null)
                petImage.setImage(new Image(u.toExternalForm()));
        }
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

    private void changeStats(double hungerDelta, double energyDelta, double moodDelta, double healthDelta) {
        updateProgressBar(hungerBar, hungerBar.getProgress() + hungerDelta);
        updateProgressBar(energyBar, energyBar.getProgress() + energyDelta);
        updateProgressBar(moodBar, moodBar.getProgress() + moodDelta);
        updateProgressBar(healthBar, healthBar.getProgress() + healthDelta);
    }

    private void updateProgressBar(ProgressBar bar, double value) {
        double clamped = Math.max(0, Math.min(1.0, value));
        Platform.runLater(() -> {
            bar.setProgress(clamped);
            bar.setStyle(clamped > 0.3 ? "-fx-accent: green;" : "-fx-accent: red;");
        });
    }

    private void checkIfDead() {
        if (hungerBar.getProgress() <= 0 || energyBar.getProgress() <= 0
                || moodBar.getProgress() <= 0 || healthBar.getProgress() <= 0) {
            handleDeath();
        }
    }

    private void handleDeath() {
        if (isDead)
            return;
        isDead = true;
        isAnimationPlaying = true;
        if (statsDecreaseTimeline != null)
            statsDecreaseTimeline.stop();
        if (pomodoroTimeline != null)
            pomodoroTimeline.stop();
        updatePetAnimation("death");
        disableButtons();
        SaveManager.delete();
        PauseTransition pause = new PauseTransition(Duration.seconds(15));
        pause.setOnFinished(e -> loadDeathScene());
        pause.play();
    }

    private void disableButtons() {
        btnFeed.setDisable(true);
        btnSleep.setDisable(true);
        btnPlay.setDisable(true);
        btnHeal.setDisable(true);
        btnTomato.setDisable(true);
        btnPomodoroStart.setDisable(true);
        btnPomodoroPause.setDisable(true);
        btnPomodoroReset.setDisable(true);
        btnPomodoroSettings.setDisable(true);
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

    // ===== Pet actions =====
    @FXML
    private void handleFeed() {
        if (!pomodoroActive) {
            changeStats(+0.2, -0.02, -0.01, 0);
            playTempAnim("eat");
        }
    }

    @FXML
    private void handleSleep() {
        if (!pomodoroActive) {
            changeStats(-0.03, +0.2, -0.01, 0);
            playTempAnim("sleep");
        }
    }

    @FXML
    private void handlePlay() {
        if (!pomodoroActive) {
            changeStats(-0.04, -0.03, +0.2, 0);
            playTempAnim("play");
        }
    }

    @FXML
    private void handleHeal() {
        if (!pomodoroActive) {
            changeStats(-0.05, -0.03, -0.02, +0.1);
            playTempAnim("heal");
        }
    }

    private void playTempAnim(String state) {
        if (isAnimationPlaying || isDead)
            return;
        isAnimationPlaying = true;
        updatePetAnimation(state);
        PauseTransition pause = new PauseTransition(Duration.seconds(5));
        pause.setOnFinished(e -> {
            if (!pomodoroActive && !isDead)
                updatePetAnimation("idle");
            isAnimationPlaying = false;
        });
        pause.play();
    }

    // ===== Navigation / close =====
    @FXML
    private void handleBackSelection() {
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

    @FXML
    private void handleClose() {
        savePetState();
        ((Stage) background.getScene().getWindow()).close();
    }

    // ===== Hover for image buttons =====
    @FXML
    private void onHoverImage(MouseEvent e) {
        ImageView b = (ImageView) e.getSource();
        b.setOpacity(0.7);
        b.setScaleX(1.05);
        b.setScaleY(1.05);
    }

    @FXML
    private void onExitImage(MouseEvent e) {
        ImageView b = (ImageView) e.getSource();
        b.setOpacity(1.0);
        b.setScaleX(1.0);
        b.setScaleY(1.0);
    }

    @FXML
    private void onHoverBackLabel() {
        backLabel.setStyle(
                "-fx-font-size: 20px; -fx-text-fill: white; -fx-background-color: red; -fx-font-weight: bold; -fx-padding: 3; -fx-background-radius: 5; -fx-cursor: hand;");
    }

    @FXML
    private void onBackMove() {
        backLabel.setStyle(
                "-fx-font-size: 20px; -fx-text-fill: red; -fx-background-color: rgba(255,255,255,0.5); -fx-font-weight: bold; -fx-padding: 3; -fx-background-radius: 5; -fx-cursor: hand;");
    }

    @FXML
    private void onHoverClose() {
        closeLabel.setStyle(
                "-fx-font-size: 20px; -fx-text-fill: white; -fx-background-color: red; -fx-font-weight: bold; -fx-padding: 3; -fx-background-radius: 5; -fx-cursor: hand;");
    }

    @FXML
    private void onExitClose() {
        closeLabel.setStyle(
                "-fx-font-size: 20px; -fx-text-fill: red; -fx-background-color: rgba(255,255,255,0.5); -fx-font-weight: bold; -fx-padding: 3; -fx-background-radius: 5; -fx-cursor: hand;");
    }

    // ===== Pomodoro: menu toggle =====
    @FXML
    private void togglePomodoroPanel() {
        if (!pomodoroMenuBox.isVisible()) {
            if (pomodoroTimeline != null && pomodoroActive && !pomodoroPaused) {
                pomodoroTimeline.pause();
                pomodoroPaused = true; // важно
            }
            showMenu();
            hideHud();
        } else {
            hideMenu();
            if (pomodoroActive)
                showHud();
            else
                hideHud();
        }
        updatePomodoroButtonsState();
    }

    // ===== Pomodoro: buttons =====
    @FXML
    private void handlePomodoroStart() {
        // 1) Resume if paused
        if (pomodoroTimeline != null && pomodoroPaused) {
            pomodoroPaused = false;
            pomodoroActive = true;
            pomodoroTimeline.play();
            hideMenu();
            showHud();
            updatePomodoroButtonsState();
            return;
        }

        // 2) Fresh start
        if (!pomodoroActive) {
            startMode(mode);
            startTicker();
            hideMenu();
            showHud();
            updatePomodoroButtonsState();
        }
    }

    @FXML
    private void handlePomodoroPause() {
        if (!pomodoroActive || pomodoroPaused)
            return;
        pomodoroPaused = true;
        if (pomodoroTimeline != null)
            pomodoroTimeline.pause();
        showMenu();
        hideHud();
        updatePomodoroButtonsState();
    }

    @FXML
    private void handlePomodoroReset() {
        stopTicker();
        pomodoroActive = false;
        pomodoroPaused = false;
        mode = Mode.FOCUS;
        remainingSeconds = focusMinutes * 60;
        totalSecondsThisInterval = remainingSeconds;
        completedFocusInCycle = 0;
        syncPomodoroUI();
        if (statsDecreaseTimeline != null)
            statsDecreaseTimeline.play(); // resume passive decay
        showMenu();
        hideHud();
        updatePomodoroButtonsState();
    }

    @FXML
    private void handlePomodoroSettings() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/pomodoro_settings.fxml"),
                    GameState.getBundle());
            Parent root = loader.load();
            PomodoroSettingsController controller = loader.getController();

            Stage modal = new Stage();
            modal.initModality(Modality.WINDOW_MODAL);
            modal.initOwner(background.getScene().getWindow());
            modal.setScene(new Scene(root));
            modal.setTitle("Pomodoro Settings");
            modal.showAndWait();

            if (controller.wasSaved()) {
                PomodoroSettings s = controller.getResult(); // <-- возвращает твой POJO

                // apply new settings
                focusMinutes = s.getFocusMinutes();
                shortBreakMinutes = s.getShortBreakMinutes(); // FIX: раньше брал из longBreak
                longBreakMinutes = s.getLongBreakMinutes();
                cyclesBeforeLong = s.getCyclesBeforeLong();
                autoStartNext = s.isAutoStart(); // boolean -> is*
                soundOn = s.isSoundOn();

                // refresh UI baselines
                if (!pomodoroActive) {
                    mode = Mode.FOCUS;
                    remainingSeconds = focusMinutes * 60;
                    totalSecondsThisInterval = remainingSeconds;
                    completedFocusInCycle = 0;
                }
                syncPomodoroUI();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ===== Pomodoro core =====
    private void startMode(Mode m) {
        mode = m;
        switch (mode) {
            case FOCUS -> {
                totalSecondsThisInterval = focusMinutes * 60;
            }
            case SHORT_BREAK -> {
                totalSecondsThisInterval = shortBreakMinutes * 60;
            }
            case LONG_BREAK -> {
                totalSecondsThisInterval = longBreakMinutes * 60;
            }
        }
        remainingSeconds = totalSecondsThisInterval;

        // Stop pet decay while timer is running (decay resumes on pause/stop)
        if (statsDecreaseTimeline != null)
            statsDecreaseTimeline.stop();

        syncPomodoroUI();
        updatePomodoroButtonsState();
    }

    private void startTicker() {
        pomodoroActive = true;
        pomodoroPaused = false;
        if (pomodoroTimeline != null)
            pomodoroTimeline.stop();
        pomodoroTimeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> onTick()));
        pomodoroTimeline.setCycleCount(Timeline.INDEFINITE);
        pomodoroTimeline.play();
        updatePomodoroButtonsState();
    }

    private void stopTicker() {
        if (pomodoroTimeline != null)
            pomodoroTimeline.stop();
    }

    private void onTick() {
        remainingSeconds--;
        if (remainingSeconds < 0)
            remainingSeconds = 0;
        syncPomodoroUI();
        if (remainingSeconds <= 0)
            onIntervalFinished();
    }

    private void onIntervalFinished() {
        stopTicker();
        pomodoroActive = false;
        pomodoroPaused = false;

        // ring once
        if (soundOn && finishClip != null) {
            finishClip.stop();
            finishClip.play();
        }

        // decide next mode
        Mode next;
        if (mode == Mode.FOCUS) {
            completedFocusInCycle++;
            next = (completedFocusInCycle >= cyclesBeforeLong) ? Mode.LONG_BREAK : Mode.SHORT_BREAK;
            if (next == Mode.LONG_BREAK)
                completedFocusInCycle = 0;
        } else {
            next = Mode.FOCUS;
        }

        pomodoroCycleLabel.setText(completedFocusInCycle + "/" + cyclesBeforeLong);
        pomodoroCycleHUDLabel.setText(completedFocusInCycle + "/" + cyclesBeforeLong);

        if (autoStartNext) {
            startMode(next);
            startTicker();
            showHud();
            hideMenu();
        } else {
            // preload next mode but wait for user
            startMode(next); // sets remainingSeconds/labels for next phase
            if (statsDecreaseTimeline != null)
                statsDecreaseTimeline.play();
            showMenu(); // encourage user to press Start
            hideHud();
            updatePomodoroButtonsState();
        }
    }

    private void updatePomodoroButtonsState() {
        // Start is disabled only when running and not paused
        btnPomodoroStart.setDisable(pomodoroActive && !pomodoroPaused);
        // Pause is enabled only when running and not paused
        btnPomodoroPause.setDisable(!pomodoroActive || pomodoroPaused);
        // Reset is enabled when there is a session (running or paused)
        btnPomodoroReset.setDisable(!pomodoroActive && !pomodoroPaused);
    }

    // ===== Helpers: view + formatting =====
    private void showMenu() {
        pomodoroMenuBox.setVisible(true);
        pomodoroMenuBox.setManaged(true);
    }

    private void hideMenu() {
        pomodoroMenuBox.setVisible(false);
        pomodoroMenuBox.setManaged(false);
    }

    private void showHud() {
        pomodoroHudBox.setVisible(true);
        pomodoroHudBox.setManaged(true);
    }

    private void hideHud() {
        pomodoroHudBox.setVisible(false);
        pomodoroHudBox.setManaged(false);
    }

    private void syncPomodoroUI() {
        String t = formatTime(remainingSeconds);
        String statusKey = switch (mode) {
            case FOCUS -> "pomodoro.status.focus";
            case SHORT_BREAK -> "pomodoro.status.short";
            case LONG_BREAK -> "pomodoro.status.long";
        };
        String status = tr(statusKey);

        double p = (totalSecondsThisInterval == 0) ? 0
                : Math.max(0, Math.min(1, 1.0 - (remainingSeconds / (double) totalSecondsThisInterval)));
        String cycle = completedFocusInCycle + "/" + cyclesBeforeLong;

        // menu
        pomodoroTimeLabel.setText(t);
        pomodoroStatusLabel.setText(status);
        pomodoroProgress.setProgress(p);
        pomodoroCycleLabel.setText(cycle);

        // hud
        pomodoroTimeHUDLabel.setText(t);
        pomodoroStatusHUDLabel.setText(status);
        pomodoroProgressHUD.setProgress(p);
        pomodoroCycleHUDLabel.setText(cycle);
    }

    private String formatTime(int totalSeconds) {
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    // Hover on
    @FXML
    public void onHoverPomodoroStart(Event e) {
        btnPomodoroStart.setStyle(btnStyle(true));
    }

    @FXML
    public void onHoverPomodoroPause(Event e) {
        btnPomodoroPause.setStyle(btnStyle(true));
    }

    @FXML
    public void onHoverPomodoroReset(Event e) {
        btnPomodoroReset.setStyle(btnStyle(true));
    }

    @FXML
    public void onHoverPomodoroSettings(Event e) {
        btnPomodoroSettings.setStyle(btnStyle(true));
    }

    // Hover off
    @FXML
    public void onExitPomodoroStart(Event e) {
        btnPomodoroStart.setStyle(btnStyle(false));
    }

    @FXML
    public void onExitPomodoroPause(Event e) {
        btnPomodoroPause.setStyle(btnStyle(false));
    }

    @FXML
    public void onExitPomodoroReset(Event e) {
        btnPomodoroReset.setStyle(btnStyle(false));
    }

    @FXML
    public void onExitPomodoroSettings(Event e) {
        btnPomodoroSettings.setStyle(btnStyle(false));
    }

    // Returns CSS for Pomodoro menu buttons. Use hover=true for highlighted state.
    private String btnStyle(boolean hover) {
        return hover
                ? "-fx-font-size: 14px; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-color: rgba(120,65,110,0.8); -fx-background-radius: 10; -fx-padding: 4 8; -fx-cursor: hand;"
                : "-fx-font-size: 14px; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-color: rgba(87,46,81,0.6); -fx-background-radius: 10; -fx-padding: 4 8; -fx-cursor: hand;";
    }

}
