package at.fhj.msd.ui.controller;

import java.util.Comparator;
import java.util.ResourceBundle;

import at.fhj.msd.model.PetSaveData;
import at.fhj.msd.util.Achievements;
import at.fhj.msd.util.SaveManager;
import at.fhj.msd.util.WindowDragHelper;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

public class AchievementsController {

    @FXML
    private StackPane root; // Root node of the modal (fx:id="root")
    @FXML
    private VBox grid; // Container for achievement cards
    @FXML
    private Label countLabel; // "unlocked/total"
    @FXML
    private Label coinsLabel; // Shows current coins
    @FXML
    private ResourceBundle resources; // i18n bundle injected via FXMLLoader

    @FXML
    private void onClose() {
        // Close the modal window safely
        Stage st = (Stage) root.getScene().getWindow();
        if (st != null) {
            st.close();
        }
    }

    @FXML
    public void initialize() {
        // ——— Visual setup for the frameless modal ———

        // Make the undecorated window draggable by dragging the content
        WindowDragHelper.makeDraggable(root);

        // Rounded clip so the drop shadow doesn’t "shine" on white corners
        Rectangle clip = new Rectangle();
        clip.setArcWidth(18);
        clip.setArcHeight(18);
        root.layoutBoundsProperty().addListener((obs, o, n) -> {
            clip.setWidth(n.getWidth());
            clip.setHeight(n.getHeight());
        });
        root.setClip(clip);

        // Close on Esc and focus inside the window once scene is ready
        Platform.runLater(() -> {
            if (root.getScene() != null) {
                root.getScene().setOnKeyPressed(e -> {
                    if (e.getCode() == KeyCode.ESCAPE)
                        onClose();
                });
            }
            root.requestFocus();
        });

        // ——— Data population ———
        if (grid != null)
            grid.getChildren().clear();

        PetSaveData loaded = SaveManager.load();
        final PetSaveData data = (loaded != null) ? loaded : new PetSaveData();
        if (data.achievements == null) {
            data.achievements = new java.util.HashSet<>();
        }

        // Coins label
        if (coinsLabel != null) {
            int coins = Math.max(0, data.coins);
            coinsLabel.setText(coins + " \uD83D\uDCB0"); // 💰
        }

        // Sort achievements: unlocked → locked, then by enum ordinal
        var types = java.util.Arrays.stream(Achievements.Type.values())
                .sorted(Comparator
                        // unlocked first: has==true → !true==false sorts before !false==true
                        .comparing((Achievements.Type t) -> !Achievements.has(data, t.id))
                        .thenComparing(Enum::ordinal))
                .toList();

        int unlockedCount = 0;
        for (Achievements.Type t : types) {
            boolean unlocked = Achievements.has(data, t.id);
            if (unlocked)
                unlockedCount++;
            if (grid != null)
                grid.getChildren().add(makeCard(t, unlocked));
        }

        // "X/Y" counter
        if (countLabel != null) {
            countLabel.setText(unlockedCount + "/" + Achievements.Type.values().length);
        }
    }

    /** Builds a single achievement card (icon + title + description). */
    private VBox makeCard(Achievements.Type a, boolean unlocked) {
        ImageView iv = new ImageView(loadIcon(a.iconPath)); // iconPath should start with "/"
        iv.setFitWidth(96);
        iv.setFitHeight(96);
        iv.setPreserveRatio(true);
        iv.setSmooth(false); // keep pixel-art crisp (no smoothing)

        Label title = new Label(tr(a.titleKey, a.id));
        title.getStyleClass().add("achievement-title");

        // If specific descKey not found, fallback to a generic description
        Label desc = new Label(tr(a.descKey, tr("ach.generic.desc", "")));
        desc.setWrapText(true);
        desc.setMaxWidth(220);
        desc.getStyleClass().add("achievement-desc");

        VBox box = new VBox(8, iv, title, desc);
        box.getStyleClass().add("achievement-card");

        // Apply grayscale for locked achievements and add CSS hook
        if (!unlocked) {
            ColorAdjust gray = new ColorAdjust();
            gray.setSaturation(-1.0);
            gray.setBrightness(-0.25);
            iv.setEffect(gray);
            box.getStyleClass().add("locked");
        }

        return box;
    }

    /**
     * Loads an icon by classpath; returns a 1×1 transparent placeholder if missing.
     */
    private Image loadIcon(String classpath) {
        var url = getClass().getResource(classpath);
        if (url == null) {
            System.err.println("Missing icon resource: " + classpath);
            // Use a transparent 1×1 image as a safe fallback (data: URLs are not
            // guaranteed)
            return new WritableImage(1, 1);
        }
        return new Image(url.toExternalForm());
    }

    /** i18n helper: return resources[key] or default string if missing. */
    private String tr(String key, String def) {
        return (resources != null && resources.containsKey(key)) ? resources.getString(key) : def;
    }
}
