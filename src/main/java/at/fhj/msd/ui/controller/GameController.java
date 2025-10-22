package at.fhj.msd.ui.controller;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

import at.fhj.msd.model.GameState;
import at.fhj.msd.model.PetSaveData;
import at.fhj.msd.ui.controller.PomodoroSettingsController.PomodoroSettings;
import at.fhj.msd.util.Achievements;
import at.fhj.msd.util.SaveManager;
import at.fhj.msd.util.WindowDragHelper;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
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
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.util.Duration;

public class GameController implements Initializable {

    // ===== UI: main scene =====
    @FXML private ImageView background;
    @FXML private Label petNameLabel;
    @FXML private ImageView petImage;

    @FXML private ProgressBar hungerBar;
    @FXML private ProgressBar energyBar;
    @FXML private ProgressBar moodBar;
    @FXML private ProgressBar healthBar;

    @FXML private Label closeLabel;
    @FXML private Label backLabel;

    @FXML private ImageView btnFeed;
    @FXML private ImageView btnSleep;
    @FXML private ImageView btnPlay;
    @FXML private ImageView btnHeal;
    @FXML private ImageView btnTomato;

    // ===== UI: Pomodoro MENU (matches FXML) =====
    @FXML private VBox pomodoroMenuBox;
    @FXML private Label pomodoroTimeLabel;
    @FXML private Label pomodoroStatusLabel;
    @FXML private ProgressBar pomodoroProgress;
    @FXML private Label btnPomodoroStart;
    @FXML private Label btnPomodoroPause;
    @FXML private Label btnPomodoroReset;
    @FXML private Label btnPomodoroSettings;
    @FXML private Label pomodoroCycleLabel;

    // Achievements entry in the menu (hover styles)
    @FXML private Label btnAchievements;

    @FXML public void onHoverAchievements(Event e) { btnAchievements.setStyle(btnStyle(true)); }
    @FXML public void onExitAchievements(Event e)  { btnAchievements.setStyle(btnStyle(false)); }

    // ===== UI: Pomodoro HUD (compact overlay) =====
    @FXML private VBox pomodoroHudBox;
    @FXML private Label pomodoroTimeHUDLabel;
    @FXML private Label pomodoroStatusHUDLabel;
    @FXML private ProgressBar pomodoroProgressHUD;
    @FXML private Label pomodoroCycleHUDLabel;

    // ===== Pet runtime state (v1.2) =====
    private PetSaveData pet;           // persistent state (xp/coins/achievements etc.)
    private boolean isAnimationPlaying = false; // prevents emotion override while action anim is running
    private boolean isDead = false;

    // Passive stat decay (ticks every 5s)
    private Timeline statsDecreaseTimeline;

    // ===== Pomodoro state =====
    private enum Mode { FOCUS, SHORT_BREAK, LONG_BREAK }

    private Mode mode = Mode.FOCUS;
    private boolean uiMenuOpen = false;       // true while a modal/menu is open
    private Timeline pomodoroTimeline;        // 1s ticking timer
    private int remainingSeconds = 0;         // in current interval
    private int totalSecondsThisInterval = 0; // for progress %
    private boolean pomodoroActive = false;   // currently running
    private boolean pomodoroPaused = false;   // paused by user
    private int completedFocusInCycle = 0;    // counts focus sessions until long break

    // Settings (overridden by settings modal)
    private int focusMinutes = 25;
    private int shortBreakMinutes = 5;
    private int longBreakMinutes = 15;
    private int cyclesBeforeLong = 4;
    private boolean autoStartNext = false;
    private boolean soundOn = true;

    private static final DateTimeFormatter ISO = DateTimeFormatter.ISO_LOCAL_DATE;
    private ResourceBundle bundle;

    private String tr(String key) { return bundle != null ? bundle.getString(key) : key; }

    // End-of-interval sound
    private AudioClip finishClip;

    // Single-shot timer for temporary action animation (eat/sleep/play/heal)
    private PauseTransition actionAnimTimer;

    // Achievement toast guard (so toasts don’t stack)
    private boolean achievementShowing = false;

    // ===== Init =====
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            // Make undecorated window draggable by grabbing the background
            WindowDragHelper.makeDraggable(background);

            // Background image of the main game scene
            URL bgUrl = getClass().getResource("/images/ui/game_scene.png");
            if (bgUrl != null) {
                background.setImage(new Image(bgUrl.toExternalForm()));
            }

            // i18n: prefer provided bundle, fallback to GameState bundle
            this.bundle = (rb != null) ? rb : GameState.getBundle();

            // Load/save state & initial visuals
            loadPetState();             // fills bars/name/type and defaults for v1.2 fields
            rollOverDayIfNeeded();      // daily counters (streaks, mins)
            updatePetAnimation((pet != null && pet.emotion != null) ? pet.emotion : "idle");
            setButtonImages();

            // Ensure pomodoro starts idle and both containers are hidden
            pomodoroActive = false;
            pomodoroPaused = false;
            if (pomodoroTimeline != null) pomodoroTimeline.stop();

            hideMenu();
            hideHud();

            // Default labels/progress for menu & HUD
            String defTime = formatTime(focusMinutes * 60);
            pomodoroStatusLabel.setText(tr("pomodoro.status.focus"));
            pomodoroTimeLabel.setText(defTime);
            pomodoroProgress.setProgress(0);
            pomodoroCycleLabel.setText("0/" + cyclesBeforeLong);

            pomodoroStatusHUDLabel.setText(tr("pomodoro.status.focus"));
            pomodoroTimeHUDLabel.setText(defTime);
            pomodoroProgressHUD.setProgress(0);
            pomodoroCycleHUDLabel.setText("0/" + cyclesBeforeLong);

            // Prepare end sound (optional)
            URL soundUrl = getClass().getResource("/sounds/finish.wav");
            if (soundUrl != null) finishClip = new AudioClip(soundUrl.toExternalForm());

            // Start passive decay loop (5s)
            startStatsDecrease();

            updatePomodoroButtonsState();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ===== Pet state load/save =====
    private void loadPetState() {
        PetSaveData data = SaveManager.load();
        if (data == null) {
            // First-time defaults
            data = new PetSaveData();
            data.petName = (GameState.getPetName() != null) ? GameState.getPetName() : "Unnamed Pet";
            data.petType = GameState.getPetType();
            if (data.petType != null) data.petType = data.petType.toLowerCase();

            data.hunger = 1.0;
            data.energy = 1.0;
            data.mood   = 1.0;
            data.health = 1.0;

            // v1.2 fields
            data.emotion = "idle";
            data.affection = 0.2;
            data.xp = 0;
            data.coins = 0;
            data.achievements = new java.util.HashSet<>();
            data.lastDayIso = LocalDate.now().format(ISO);
            data.fedToday = false;
            data.feedStreakDays = 0;
            data.moodAbove80Secs = 0;
            data.energyDayMin = 1.0;
            data.consecutivePomos = 0;
        } else {
            // Backfill v1.2 fields for older saves
            if (data.petType != null) data.petType = data.petType.toLowerCase();
            if (data.emotion == null) data.emotion = "idle";
            if (data.achievements == null) data.achievements = new java.util.HashSet<>();
            if (data.lastDayIso == null) data.lastDayIso = LocalDate.now().format(ISO);
        }
        this.pet = data;

        // Reflect core stats to UI
        GameState.setPetName(data.petName);
        GameState.setPetType(data.petType);
        petNameLabel.setText(data.petName);

        hungerBar.setProgress(safe01(data.hunger));
        energyBar.setProgress(safe01(data.energy));
        moodBar.setProgress(safe01(data.mood));
        healthBar.setProgress(safe01(data.health));

        // Soft black outline for name for readability on light backgrounds
        DropShadow ds = new DropShadow();
        ds.setColor(Color.BLACK);
        ds.setRadius(3);
        ds.setOffsetX(1);
        ds.setOffsetY(1);
        petNameLabel.setEffect(ds);
    }

    private void savePetState() {
        // Pull the current UI values back into the save (keeps things in sync)
        pet.hunger = hungerBar.getProgress();
        pet.energy = energyBar.getProgress();
        pet.mood   = moodBar.getProgress();
        pet.health = healthBar.getProgress();
        pet.petName = GameState.getPetName();
        pet.petType = GameState.getPetType() != null ? GameState.getPetType().toLowerCase() : null;

        SaveManager.save(pet);
    }

    // Called at init and every naturalTick to advance day and check dailies
    private void rollOverDayIfNeeded() {
        if (pet == null) return;

        String today = LocalDate.now().format(ISO);
        if (pet.lastDayIso == null) {
            pet.lastDayIso = today;
            pet.energyDayMin = energyBar.getProgress();
            savePetState();
            return;
        }

        if (!today.equals(pet.lastDayIso)) {
            // --- previous-day checks ---

            // ACH_ENERGY_SMART: never drop below 20% energy during the whole day
            if (pet.energyDayMin >= 0.20 && !Achievements.has(pet, Achievements.Type.ENERGYS.id)) {
                Achievements.grant(pet, Achievements.Type.ENERGYS.id, 10);
                showAchievementToast("ach.energys.title", "ach.energys.desc", 10);
            }

            // ACH_CARE_3D: fed 3 days in a row
            if (pet.fedToday) pet.feedStreakDays++;
            else              pet.feedStreakDays = 0;
            if (pet.feedStreakDays >= 3 && !Achievements.has(pet, Achievements.Type.CARE3D.id)) {
                Achievements.grant(pet, Achievements.Type.CARE3D.id, 15);
                showAchievementToast("ach.care3d.title", "ach.care3d.desc", 15);
            }

            // reset daily trackers for the new day
            pet.lastDayIso = today;
            pet.fedToday = false;
            pet.moodAbove80Secs = 0;
            pet.energyDayMin = energyBar.getProgress();
            savePetState();
        }
    }

    // ===== Pet visuals & stats =====
    private void setButtonImages() {
        btnFeed.setImage(new Image(getClass().getResource("/images/ui/food.png").toExternalForm()));
        btnSleep.setImage(new Image(getClass().getResource("/images/ui/sleep.png").toExternalForm()));
        btnPlay.setImage(new Image(getClass().getResource("/images/ui/game.png").toExternalForm()));
        btnHeal.setImage(new Image(getClass().getResource("/images/ui/health.png").toExternalForm()));
        btnTomato.setImage(new Image(getClass().getResource("/images/ui/watch.png").toExternalForm()));
    }

    // Robust sprite loader: prefers GIF, then PNG; if state missing, falls back to idle
    private void updatePetAnimation(String state) {
        String type = GameState.getPetType();
        if (type == null || type.isBlank()) return;

        String t = type.toLowerCase();
        String base = "/images/pets/" + t + "/" + t + "_" + state;

        URL u = getClass().getResource(base + ".gif");
        if (u == null) u = getClass().getResource(base + ".png");

        if (u == null && !"idle".equals(state)) {
            String idleBase = "/images/pets/" + t + "/" + t + "_idle";
            u = getClass().getResource(idleBase + ".gif");
            if (u == null) u = getClass().getResource(idleBase + ".png");
        }

        if (u != null) {
            petImage.setImage(new Image(u.toExternalForm()));
        } else {
            System.out.println("Missing sprite: " + base + ".[gif|png]");
        }
    }

    private void startStatsDecrease() {
        // Ticks every 5 seconds: small decay + affection drift + achievement checks
        statsDecreaseTimeline = new Timeline(new KeyFrame(Duration.seconds(5), e -> naturalTick()));
        statsDecreaseTimeline.setCycleCount(Timeline.INDEFINITE);
        statsDecreaseTimeline.play();
    }

    private void naturalTick() {
        // Passive decay (small)
        changeStats(-0.02, -0.02, -0.01, -0.005);

        // Affection drift & daily counters
        if (pet != null) {
            // Affection rises slowly when mood > 0.6, otherwise drifts down a bit
            double delta = (moodBar.getProgress() > 0.6) ? +0.002 : -0.001;
            pet.affection = clamp01(pet.affection + delta);

            // Count seconds with mood > 0.80 (to award MOOD80 at 3600s)
            if (moodBar.getProgress() > 0.80) {
                pet.moodAbove80Secs += 5;
                if (pet.moodAbove80Secs >= 3600 && !Achievements.has(pet, Achievements.Type.MOOD80.id)) {
                    Achievements.grant(pet, Achievements.Type.MOOD80.id, 25);
                    showAchievementToast("ach.mood80.title", "ach.mood80.desc", 25);
                }
            }

            // Track today's min energy for the ENERGY-smart achievement
            pet.energyDayMin = Math.min(pet.energyDayMin, energyBar.getProgress());
        }

        updateEmotionAndAnimation();
        checkIfDead();
        rollOverDayIfNeeded();
    }

    // Legacy hook (kept for compatibility)
    private void decreaseStats() {
        changeStats(-0.02, -0.02, -0.01, -0.005);
        updateEmotionAndAnimation();
        checkIfDead();
    }

    private void changeStats(double hungerDelta, double energyDelta, double moodDelta, double healthDelta) {
        updateProgressBar(hungerBar, hungerBar.getProgress() + hungerDelta);
        updateProgressBar(energyBar, energyBar.getProgress() + energyDelta);
        updateProgressBar(moodBar,   moodBar.getProgress()   + moodDelta);
        updateProgressBar(healthBar, healthBar.getProgress() + healthDelta);
    }

    private void updateProgressBar(ProgressBar bar, double value) {
        double clamped = clamp01(value);
        Platform.runLater(() -> {
            bar.setProgress(clamped);
            // Green when healthy enough, red when critical
            bar.setStyle(clamped > 0.3 ? "-fx-accent: green;" : "-fx-accent: red;");
        });
    }

    private void updateEmotionAndAnimation() {
        if (pet == null || isDead) return;
        // Do not override temporary action animation while it plays
        if (isAnimationPlaying) return;

        double h = hungerBar.getProgress();
        double e = energyBar.getProgress();
        double m = moodBar.getProgress();

        String newEmo;
        if      (m < 0.3)                 newEmo = "sad";
        else if (e < 0.3)                 newEmo = "sleepy";
        else if (h < 0.3)                 newEmo = "hungry";
        else if (h > 0.6 && e > 0.5 && m > 0.6) newEmo = "happy";
        else                               newEmo = "idle";

        if (!newEmo.equals(pet.emotion)) {
            pet.emotion = newEmo;
            // While pomodoro is running we keep the focus sprite (HUD), so only update when idle
            if (!pomodoroActive) updatePetAnimation(newEmo);
        }
    }

    private void checkIfDead() {
        if (hungerBar.getProgress() <= 0 || energyBar.getProgress() <= 0
         || moodBar.getProgress()   <= 0 || healthBar.getProgress()  <= 0) {
            handleDeath();
        }
    }

    private void handleDeath() {
        if (isDead) return;
        isDead = true;
        isAnimationPlaying = true;

        if (statsDecreaseTimeline != null) statsDecreaseTimeline.stop();
        if (pomodoroTimeline != null) pomodoroTimeline.stop();

        updatePetAnimation("death");
        disableButtons();

        // Delete save immediately and move to death scene after a short delay
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
        if (pomodoroActive) return;

        changeStats(+0.20, -0.02, -0.01, 0);
        if (pet != null) {
            pet.affection = clamp01(pet.affection + 0.03);
            pet.xp += 5;
            pet.fedToday = true;
        }
        updateEmotionAndAnimation();
        playTempAnim("eat");

        if (pet != null && pet.affection >= 0.70 && !Achievements.has(pet, Achievements.Type.AFF70.id)) {
            Achievements.grant(pet, Achievements.Type.AFF70.id, 20);
            showAchievementToast("ach.aff70.title", "ach.aff70.desc", 20);
        }
        if (pet != null) savePetState();
    }

    @FXML
    private void handleSleep() {
        if (pomodoroActive) return;

        changeStats(-0.03, +0.20, -0.01, 0);
        if (pet != null) pet.xp += 3;
        updateEmotionAndAnimation();
        playTempAnim("sleep");
    }

    @FXML
    private void handlePlay() {
        if (pomodoroActive) return;

        changeStats(-0.04, -0.03, +0.20, 0);
        if (pet != null) {
            pet.affection = clamp01(pet.affection + 0.04);
            pet.xp += 7;
        }
        updateEmotionAndAnimation();
        playTempAnim("play");

        if (pet != null && pet.affection >= 0.70 && !Achievements.has(pet, Achievements.Type.AFF70.id)) {
            Achievements.grant(pet, Achievements.Type.AFF70.id, 20);
            showAchievementToast("ach.aff70.title", "ach.aff70.desc", 20);
            savePetState();
        }
    }

    @FXML
    private void handleHeal() {
        if (pomodoroActive) return;

        changeStats(-0.05, -0.03, -0.02, +0.10);
        updateEmotionAndAnimation();
        playTempAnim("heal");
    }

    private void playTempAnim(String state) {
        if (isDead) return;

        // Cancel any running temp animation to avoid stacking/zombie states
        if (actionAnimTimer != null) {
            actionAnimTimer.stop();
            actionAnimTimer = null;
        }

        isAnimationPlaying = true;
        updatePetAnimation(state);

        actionAnimTimer = new PauseTransition(Duration.seconds(5));
        actionAnimTimer.setOnFinished(e -> {
            isAnimationPlaying = false;
            // Hard return to baseline so GIF never gets stuck
            updatePetAnimation("idle");
            actionAnimTimer = null;
            // Alternative: return to current emotion
            // updatePetAnimation(safeEmotion());
        });
        actionAnimTimer.playFromStart();
    }

    // ===== Navigation / close =====
    @FXML
    private void handleBackSelection() {
        // Wipes save and returns to language selection
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
        Stage st = (Stage) background.getScene().getWindow();
        if (st != null) st.close();
    }

    // ===== Hover effects for image buttons =====
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
            // Opening the menu: pause the ticking and pause passive decay
            if (pomodoroTimeline != null && pomodoroActive && !pomodoroPaused) {
                pomodoroTimeline.pause();
                pomodoroPaused = true;
            }
            uiMenuOpen = true;
            pauseDecay(); // pause bars while UI is open
            showMenu();
            hideHud();
        } else {
            // Closing the menu
            hideMenu();
            uiMenuOpen = false;
            if (pomodoroActive) {
                showHud(); // show compact HUD while the timer is active
            } else {
                hideHud();
                // If timer is not active, resume passive decay after menus are gone
                resumeDecayIfAllowed();
            }
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
        if (!pomodoroActive || pomodoroPaused) return;
        pomodoroPaused = true;
        if (pomodoroTimeline != null) pomodoroTimeline.pause();
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

        // Resume passive decay when no active timer and no menus
        if (statsDecreaseTimeline != null) statsDecreaseTimeline.play();

        showMenu();
        hideHud();
        updatePomodoroButtonsState();
    }

    @FXML
    private void handleOpenAchievements() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/achievements.fxml"),
                    GameState.getBundle());
            Parent root = loader.load();

            Scene scene = new Scene(root);
            scene.setFill(Color.TRANSPARENT);

            Stage owner = (Stage) background.getScene().getWindow();
            Stage modal = new Stage(javafx.stage.StageStyle.TRANSPARENT);
            modal.initModality(Modality.WINDOW_MODAL);
            modal.initOwner(owner);
            modal.setScene(scene);

            // Make the modal draggable (frameless)
            WindowDragHelper.makeDraggable(root);

            // Pause passive decay while the modal is open
            uiMenuOpen = true;
            pauseDecay();
            modal.setOnHidden(ev -> {
                uiMenuOpen = false;
                resumeDecayIfAllowed();
            });

            modal.show();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @FXML
    private void handlePomodoroSettings() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/pomodoro_settings.fxml"),
                    GameState.getBundle());
            Parent root = loader.load();
            PomodoroSettingsController controller = loader.getController();

            Stage owner = (Stage) background.getScene().getWindow();
            Stage modal = new Stage();
            modal.initModality(Modality.WINDOW_MODAL);
            modal.initOwner(owner);
            modal.setScene(new Scene(root));
            modal.setTitle("Pomodoro Settings");

            // Pause passive decay while settings modal is open
            uiMenuOpen = true;
            pauseDecay();
            try {
                modal.showAndWait(); // blocking call
            } finally {
                uiMenuOpen = false;
                resumeDecayIfAllowed();
            }

            if (controller.wasSaved()) {
                PomodoroSettings s = controller.getResult();

                // === FIX: actually apply user settings ===
    this.focusMinutes      = Math.max(1, s.getFocusMinutes());
    this.shortBreakMinutes = Math.max(1, s.getShortBreakMinutes());
    this.longBreakMinutes  = Math.max(1, s.getLongBreakMinutes());
    this.cyclesBeforeLong  = Math.max(1, s.getCyclesBeforeLong());
    this.autoStartNext     = s.isAutoStart();   // или s.autoStartNext()
    this.soundOn           = s.isSoundOn();

                // If timer is not active, reset counters to reflect new settings
                if (!pomodoroActive) {
                    mode = Mode.FOCUS;
                    remainingSeconds = focusMinutes * 60;
                    totalSecondsThisInterval = remainingSeconds;
                    completedFocusInCycle = 0;
                }
                syncPomodoroUI();
                updatePomodoroButtonsState();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ===== Pomodoro core =====
    private void startMode(Mode m) {
        mode = m;
        switch (mode) {
            case FOCUS       -> totalSecondsThisInterval = focusMinutes * 60;
            case SHORT_BREAK -> totalSecondsThisInterval = shortBreakMinutes * 60;
            case LONG_BREAK  -> totalSecondsThisInterval = longBreakMinutes * 60;
        }
        remainingSeconds = totalSecondsThisInterval;

        // Stop passive decay while a timer interval is running
        if (statsDecreaseTimeline != null) statsDecreaseTimeline.stop();

        // Tiny positive nudge when starting focus — feels supportive
        if (mode == Mode.FOCUS) updateProgressBar(moodBar, moodBar.getProgress() + 0.01);

        syncPomodoroUI();
        updatePomodoroButtonsState();
    }

    private void startTicker() {
        pomodoroActive = true;
        pomodoroPaused = false;

        if (pomodoroTimeline != null) pomodoroTimeline.stop();
        pomodoroTimeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> onTick()));
        pomodoroTimeline.setCycleCount(Timeline.INDEFINITE);
        pomodoroTimeline.play();

        updatePomodoroButtonsState();
    }

    private void stopTicker() {
        if (pomodoroTimeline != null) pomodoroTimeline.stop();
    }

    private void onTick() {
        remainingSeconds--;
        if (remainingSeconds < 0) remainingSeconds = 0;

        syncPomodoroUI();
        if (remainingSeconds <= 0) onIntervalFinished();
    }

    private void onIntervalFinished() {
        stopTicker();

        Mode finished = mode;
        pomodoroActive = false;
        pomodoroPaused = false;

        // One-shot ding
        if (soundOn && finishClip != null) {
            finishClip.stop();
            finishClip.play();
        }

        // Reward & achievements for finishing a focus interval
        if (finished == Mode.FOCUS && pet != null) {
            pet.consecutivePomos++;
            pet.xp += 10;
            if (pet.consecutivePomos >= 5 && !Achievements.has(pet, Achievements.Type.POMO5.id)) {
                Achievements.grant(pet, Achievements.Type.POMO5.id, 20);
                showAchievementToast("ach.pomo5.title", "ach.pomo5.desc", 20);
            }
        }

        // Decide next mode
        Mode next;
        if (finished == Mode.FOCUS) {
            completedFocusInCycle++;
            next = (completedFocusInCycle >= cyclesBeforeLong) ? Mode.LONG_BREAK : Mode.SHORT_BREAK;
            if (next == Mode.LONG_BREAK) completedFocusInCycle = 0;
        } else {
            next = Mode.FOCUS;
            // Break ended: reset chain counter (only for that achievement logic)
            if (pet != null) pet.consecutivePomos = 0;
        }

        pomodoroCycleLabel.setText(completedFocusInCycle + "/" + cyclesBeforeLong);
        pomodoroCycleHUDLabel.setText(completedFocusInCycle + "/" + cyclesBeforeLong);

        if (autoStartNext) {
            // Immediately start next interval and keep passive decay stopped
            startMode(next);
            startTicker();
            showHud();
            hideMenu();
        } else {
            // Preload next mode but wait for user; resume passive decay for idle time
            startMode(next);
            if (statsDecreaseTimeline != null) statsDecreaseTimeline.play();
            showMenu();
            hideHud();
            updatePomodoroButtonsState();
        }

        updateEmotionAndAnimation();
        savePetState();
    }

    private void updatePomodoroButtonsState() {
        // Start disabled only when running (and not paused)
        btnPomodoroStart.setDisable(pomodoroActive && !pomodoroPaused);
        // Pause enabled only when running (and not paused)
        btnPomodoroPause.setDisable(!pomodoroActive || pomodoroPaused);
        // Reset enabled when there is a session (running or paused)
        btnPomodoroReset.setDisable(!pomodoroActive && !pomodoroPaused);
    }

    // ===== Helpers: view + formatting =====
    private void showMenu() { pomodoroMenuBox.setVisible(true);  pomodoroMenuBox.setManaged(true); }
    private void hideMenu() { pomodoroMenuBox.setVisible(false); pomodoroMenuBox.setManaged(false); }

    private void showHud()  { pomodoroHudBox.setVisible(true);   pomodoroHudBox.setManaged(true); }
    private void hideHud()  { pomodoroHudBox.setVisible(false);  pomodoroHudBox.setManaged(false); }

    private void syncPomodoroUI() {
        String t = formatTime(remainingSeconds);
        String statusKey = switch (mode) {
            case FOCUS       -> "pomodoro.status.focus";
            case SHORT_BREAK -> "pomodoro.status.short";
            case LONG_BREAK  -> "pomodoro.status.long";
        };
        String status = tr(statusKey);

        double p = (totalSecondsThisInterval == 0) ? 0
                : Math.max(0, Math.min(1, 1.0 - (remainingSeconds / (double) totalSecondsThisInterval)));
        String cycle = completedFocusInCycle + "/" + cyclesBeforeLong;

        // Menu
        pomodoroTimeLabel.setText(t);
        pomodoroStatusLabel.setText(status);
        pomodoroProgress.setProgress(p);
        pomodoroCycleLabel.setText(cycle);

        // HUD
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

    // Pause passive decay (UI/menu use this; unlike stop(), pause keeps internal state)
    private void pauseDecay() {
        if (statsDecreaseTimeline != null) statsDecreaseTimeline.pause();
    }

    // Resume passive decay only when timer is not running and no UI menu is open
    private void resumeDecayIfAllowed() {
        if (!pomodoroActive && !uiMenuOpen && statsDecreaseTimeline != null) {
            statsDecreaseTimeline.play();
        }
    }

    // ===== Hover styles for Pomodoro labels (styled as buttons) =====
    @FXML public void onHoverPomodoroStart(Event e)     { btnPomodoroStart.setStyle(btnStyle(true)); }
    @FXML public void onHoverPomodoroPause(Event e)     { btnPomodoroPause.setStyle(btnStyle(true)); }
    @FXML public void onHoverPomodoroReset(Event e)     { btnPomodoroReset.setStyle(btnStyle(true)); }
    @FXML public void onHoverPomodoroSettings(Event e)  { btnPomodoroSettings.setStyle(btnStyle(true)); }

    @FXML public void onExitPomodoroStart(Event e)      { btnPomodoroStart.setStyle(btnStyle(false)); }
    @FXML public void onExitPomodoroPause(Event e)      { btnPomodoroPause.setStyle(btnStyle(false)); }
    @FXML public void onExitPomodoroReset(Event e)      { btnPomodoroReset.setStyle(btnStyle(false)); }
    @FXML public void onExitPomodoroSettings(Event e)   { btnPomodoroSettings.setStyle(btnStyle(false)); }

    // Returns inline CSS for Pomodoro menu buttons. Use hover=true for highlighted state.
    private String btnStyle(boolean hover) {
        return hover
            ? "-fx-font-size: 14px; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-color: rgba(120,65,110,0.8); -fx-background-radius: 10; -fx-padding: 4 8; -fx-cursor: hand;"
            : "-fx-font-size: 14px; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-color: rgba(87,46,81,0.6); -fx-background-radius: 10; -fx-padding: 4 8; -fx-cursor: hand;";
    }

    // ===== Achievement toast (popup near the top-right) =====
    private void showAchievementToast(String titleKey, String descKey, int rewardCoins) {
        if (achievementShowing) return;
        achievementShowing = true;

        Label title = new Label("🏆 " + tr(titleKey));
        title.setStyle("-fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;");

        Label desc = new Label(tr(descKey));
        desc.setWrapText(true);
        desc.setStyle("-fx-text-fill: #eaeaea; -fx-font-size: 12px;");

        Label reward = new Label("+" + rewardCoins + " coins");
        reward.setStyle("-fx-text-fill: #ffd166; -fx-font-size: 12px; -fx-font-weight: bold;");

        VBox box = new VBox(4, title, desc, reward);
        box.setPadding(new Insets(10, 14, 10, 14));
        box.setStyle("-fx-background-color: rgba(30,30,30,0.92);"
                + "-fx-background-radius: 10;"
                + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.6), 16, 0.2, 0, 4);");

        Popup popup = new Popup();
        popup.getContent().add(box);
        popup.setAutoFix(true);
        popup.setAutoHide(false);
        popup.setHideOnEscape(false);

        Stage stage = (Stage) background.getScene().getWindow();
        double x = stage.getX() + stage.getWidth() - 320;
        double y = stage.getY() + 180;
        popup.show(stage, x, y);

        // Simple in/out animation sequence
        box.setOpacity(0);
        box.setTranslateY(-20);

        FadeTransition fIn = new FadeTransition(Duration.millis(220), box);
        fIn.setFromValue(0);
        fIn.setToValue(1);

        TranslateTransition tIn = new TranslateTransition(Duration.millis(220), box);
        tIn.setFromY(-20);
        tIn.setToY(0);

        PauseTransition hold = new PauseTransition(Duration.seconds(2.0));

        FadeTransition fOut = new FadeTransition(Duration.millis(240), box);
        fOut.setFromValue(1);
        fOut.setToValue(0);

        TranslateTransition tOut = new TranslateTransition(Duration.millis(240), box);
        tOut.setFromY(0);
        tOut.setToY(-16);

        fIn.setOnFinished(e -> hold.play());
        hold.setOnFinished(e -> { fOut.play(); tOut.play(); });
        fOut.setOnFinished(e -> {
            popup.hide();
            achievementShowing = false;
        });

        fIn.play();
        tIn.play();
    }

    // ===== Small utils =====
    private double safe01(Double v) { return v == null ? 0.0 : clamp01(v); }
    private double clamp01(double v){ return Math.max(0, Math.min(1.0, v)); }
    private String safeEmotion()    { return (pet != null && pet.emotion != null) ? pet.emotion : "idle"; }
}
