package at.fhj.msd.ui.controller;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.function.UnaryOperator;

import at.fhj.msd.model.GameState;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.media.AudioClip;
import javafx.stage.Window;
import javafx.util.StringConverter;
import javafx.util.converter.IntegerStringConverter;

public class PomodoroSettingsController implements Initializable {

    // ===== FXML controls (must match fx:id in pomodoro_settings.fxml) =====
    @FXML private TextField focusMinutesField;
    @FXML private TextField shortBreakMinutesField;
    @FXML private TextField longBreakMinutesField;
    @FXML private TextField cyclesBeforeLongField;

    @FXML private ComboBox<Preset> presetCombo;

    @FXML private CheckBox autoStartCheck;
    @FXML private CheckBox soundOnCheck;

    @FXML private Button btnSoundPreview;
    @FXML private Button btnSave;
    @FXML private Button btnCancel;

    // Result state
    private boolean saved = false;
    private PomodoroSettings result;

    // Sound preview (resource should exist at src/main/resources/sounds/finish.wav)
    private static final String SOUND_PATH = "/sounds/finish.wav";
    private AudioClip previewClip;

    // Constraints
    private static final int MIN_MINUTES = 1;
    private static final int MAX_MINUTES = 180;
    private static final int MIN_CYCLES  = 1;
    private static final int MAX_CYCLES  = 12;

    // ===== Preset enum with i18n keys (labels resolved via ResourceBundle) =====
    public enum Preset {
        CLASSIC(25, 5, 15, "pomodoro.preset.classic"),
        SHORT  (15, 3,  9, "pomodoro.preset.short"),
        DEEP   (50,10, 30, "pomodoro.preset.deep"),
        CUSTOM (null,null,null,"pomodoro.preset.custom");

        private final Integer focusMinutes;
        private final Integer shortBreakMinutes;
        private final Integer longBreakMinutes;
        private final String  i18nKey;

        Preset(Integer focusMinutes, Integer shortBreakMinutes, Integer longBreakMinutes, String i18nKey) {
            this.focusMinutes = focusMinutes;
            this.shortBreakMinutes = shortBreakMinutes;
            this.longBreakMinutes = longBreakMinutes;
            this.i18nKey = i18nKey;
        }

        public Integer getFocusMinutes()      { return focusMinutes; }
        public Integer getShortBreakMinutes() { return shortBreakMinutes; }
        public Integer getLongBreakMinutes()  { return longBreakMinutes; }
        public String  getI18nKey()           { return i18nKey; }

        @Override public String toString() {
            // Localized label for UI (ComboBox uses this by default)
            ResourceBundle b = GameState.getBundle();
            return (b != null && b.containsKey(i18nKey)) ? b.getString(i18nKey) : i18nKey;
        }
    }

    // ===== Immutable result DTO =====
    public static final class PomodoroSettings {
        private final int     focusMinutes;
        private final int     shortBreakMinutes;
        private final int     longBreakMinutes;
        private final int     cyclesBeforeLong;
        private final boolean autoStart;
        private final boolean soundOn;
        private final Preset  preset;

        public PomodoroSettings(int focusMinutes, int shortBreakMinutes, int longBreakMinutes,
                                int cyclesBeforeLong, boolean autoStart, boolean soundOn, Preset preset) {
            this.focusMinutes = focusMinutes;
            this.shortBreakMinutes = shortBreakMinutes;
            this.longBreakMinutes = longBreakMinutes;
            this.cyclesBeforeLong = cyclesBeforeLong;
            this.autoStart = autoStart;
            this.soundOn = soundOn;
            this.preset = preset;
        }

        public int getFocusMinutes()         { return focusMinutes; }
        public int getShortBreakMinutes()    { return shortBreakMinutes; }
        public int getLongBreakMinutes()     { return longBreakMinutes; }
        public int getCyclesBeforeLong()     { return cyclesBeforeLong; }
        public boolean isAutoStart()         { return autoStart; }
        public boolean isSoundOn()           { return soundOn; }
        public Preset getPreset()            { return preset; }

        /** Backward-compat alias used elsewhere in code. */
        public boolean autoStartNext() { return autoStart; }
    }

    // ===== Initialize UI =====
    @Override
    public void initialize(URL url, ResourceBundle bundle) {
        // Numeric formatters for inputs
        focusMinutesField.setTextFormatter(integerFormatter(MIN_MINUTES, MAX_MINUTES));
        shortBreakMinutesField.setTextFormatter(integerFormatter(MIN_MINUTES, MAX_MINUTES));
        longBreakMinutesField.setTextFormatter(integerFormatter(MIN_MINUTES, MAX_MINUTES));
        cyclesBeforeLongField.setTextFormatter(integerFormatter(MIN_CYCLES, MAX_CYCLES));

        // Defaults (match Classic preset)
        focusMinutesField.setText("25");
        shortBreakMinutesField.setText("5");
        longBreakMinutesField.setText("15");
        cyclesBeforeLongField.setText("4");

        autoStartCheck.setSelected(false);
        soundOnCheck.setSelected(true);

        // Presets in ComboBox
        presetCombo.getItems().setAll(Preset.CLASSIC, Preset.SHORT, Preset.DEEP, Preset.CUSTOM);

        // Localized rendering and reverse mapping
        presetCombo.setConverter(new StringConverter<>() {
            @Override public String toString(Preset p) {
                return (p == null) ? "" : p.toString(); // toString() already localized
            }
            @Override public Preset fromString(String s) {
                if (s == null) return null;
                for (Preset p : presetCombo.getItems()) {
                    if (p.toString().equals(s)) return p;
                }
                return Preset.CUSTOM;
            }
        });

        // Optional prompt text
        if (bundle != null && bundle.containsKey("pomodoro.preset")) {
            presetCombo.setPromptText(bundle.getString("pomodoro.preset"));
        }

        // Select Classic by default and apply fields
        presetCombo.getSelectionModel().select(Preset.CLASSIC);
        applyPresetToFields(Preset.CLASSIC);

        // Load preview sound if present
        URL soundUrl = getClass().getResource(SOUND_PATH);
        if (soundUrl != null) {
            previewClip = new AudioClip(soundUrl.toExternalForm());
        } else {
            btnSoundPreview.setDisable(true);
        }

        // Listeners
        presetCombo.valueProperty().addListener((obs, oldV, newV) -> {
            if (newV != null) applyPresetToFields(newV);
            validate();
        });

        focusMinutesField.textProperty().addListener((o, a, b) -> validate());
        shortBreakMinutesField.textProperty().addListener((o, a, b) -> validate());
        longBreakMinutesField.textProperty().addListener((o, a, b) -> validate());
        cyclesBeforeLongField.textProperty().addListener((o, a, b) -> validate());

        validate();
    }

    // Apply preset values into fields. If CUSTOM, enable manual editing.
    private void applyPresetToFields(Preset preset) {
        boolean custom = (preset == Preset.CUSTOM);
        if (!custom) {
            // Safe unwrap since non-CUSTOM presets have non-null values
            focusMinutesField.setText(String.valueOf(preset.getFocusMinutes()));
            shortBreakMinutesField.setText(String.valueOf(preset.getShortBreakMinutes()));
            longBreakMinutesField.setText(String.valueOf(preset.getLongBreakMinutes()));
        }
        focusMinutesField.setDisable(!custom);
        shortBreakMinutesField.setDisable(!custom);
        longBreakMinutesField.setDisable(!custom);
        // cyclesBeforeLong stays editable for any preset (commonly customized)
    }

    // Allow only integers, basic length guard; semantic range checked in validate()
    private TextFormatter<Integer> integerFormatter(int min, int max) {
        StringConverter<Integer> converter = new IntegerStringConverter();
        UnaryOperator<TextFormatter.Change> filter = change -> {
            String newText = change.getControlNewText();
            if (newText.isEmpty()) return change;
            if (!newText.matches("\\d{0,3}")) return null;
            try {
                int val = Integer.parseInt(newText);
                if (val < 0 || val > 999) return null;
            } catch (NumberFormatException e) {
                return null;
            }
            return change;
        };
        return new TextFormatter<>(converter, null, filter);
    }

    // Validate ranges and toggle Save state
    private boolean validate() {
        boolean ok = true;
        ok &= inRange(focusMinutesField.getText(), MIN_MINUTES, MAX_MINUTES);
        ok &= inRange(shortBreakMinutesField.getText(), MIN_MINUTES, MAX_MINUTES);
        ok &= inRange(longBreakMinutesField.getText(), MIN_MINUTES, MAX_MINUTES);
        ok &= inRange(cyclesBeforeLongField.getText(), MIN_CYCLES, MAX_CYCLES);
        btnSave.setDisable(!ok);
        return ok;
    }

    private boolean inRange(String text, int min, int max) {
        try {
            int v = Integer.parseInt(text);
            return v >= min && v <= max;
        } catch (Exception e) {
            return false;
        }
    }

    // ===== FXML handlers =====
    @FXML
    private void onSoundPreview() {
        if (previewClip != null && soundOnCheck.isSelected()) {
            previewClip.stop();
            previewClip.play();
        }
    }

    @FXML
    private void onSave() {
        if (!validate()) return;

        int focus  = Integer.parseInt(focusMinutesField.getText());
        int sBreak = Integer.parseInt(shortBreakMinutesField.getText());
        int lBreak = Integer.parseInt(longBreakMinutesField.getText());
        int cycles = Integer.parseInt(cyclesBeforeLongField.getText());
        boolean auto  = autoStartCheck.isSelected();
        boolean sound = soundOnCheck.isSelected();
        Preset preset  = presetCombo.getValue() == null ? Preset.CUSTOM : presetCombo.getValue();

        this.result = new PomodoroSettings(focus, sBreak, lBreak, cycles, auto, sound, preset);
        this.saved = true;
        closeWindow();
    }

    @FXML
    private void onCancel() {
        this.saved = false;
        this.result = null;
        closeWindow();
    }

    private void closeWindow() {
        Window w = (btnCancel.getScene() != null) ? btnCancel.getScene().getWindow() : null;
        if (w != null) w.hide();
    }

    // ===== Public API for caller =====
    public boolean wasSaved()   { return saved; }
    public PomodoroSettings getResult() { return result; }
}
