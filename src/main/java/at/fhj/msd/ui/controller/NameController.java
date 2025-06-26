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
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class NameController implements Initializable {

    @FXML
    private ImageView background;
    @FXML
    private ImageView btnOK;
    @FXML
    private ImageView btnBack;
    @FXML
    private ImageView btnClose;
    @FXML
    private TextField nameField;
    @FXML
    private Label labelSelectName;
    @FXML
    private Label closeLabel;
    @FXML
    private Label backLabel;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            WindowDragHelper.makeDraggable(background);
            background.setImage(new Image(getClass().getResource("/images/ui/tamagotchi_name_selection.png").toExternalForm()));
            btnOK.setImage(new Image(getClass().getResource("/images/ui/button_OK_name.png").toExternalForm()));

            labelSelectName.setText(resources.getString("label.enterName"));
            nameField.setPromptText(resources.getString("placeholder.petName"));
        } catch (Exception e) {
            System.err.println("❌ Error initializing NameController: " + e.getMessage());
        }
    }

    @FXML
    private void handleNameConfirm() {
        String name = nameField.getText().trim();
        if (name.isEmpty())
            return;

        GameState.setPetName(name);

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/game_scene.fxml"));
            loader.setResources(GameState.getBundle());
            Parent root = loader.load();

            Scene scene = new Scene(root);
            scene.setFill(Color.TRANSPARENT);

            Stage stage = (Stage) nameField.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleBackSelection() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/pet_selection.fxml"));
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
