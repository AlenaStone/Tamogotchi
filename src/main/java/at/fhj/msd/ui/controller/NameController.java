package at.fhj.msd.ui.controller;

import java.io.IOException;
import java.util.ResourceBundle;

import at.fhj.msd.model.GameState;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class NameController {

    @FXML
    private TextField nameInput;

    @FXML
    private Label errorLabel;

    @FXML
    private void handleNameSubmission(ActionEvent event) {
        String name = nameInput.getText().trim();
        if (name.isEmpty()) {
            errorLabel.setText("Name cannot be empty!");
        } else {
            GameState.setPetName(name);
            loadNextScene(event);
        }
    }

    @FXML
    private void handleBack(ActionEvent event) {
        loadPreviousScene(event);
    }

    private void loadNextScene(ActionEvent event) {
        try {
            ResourceBundle bundle = ResourceBundle.getBundle("i18n.messages", GameState.getLocale());
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/game_scene.fxml"), bundle);
            Parent root = loader.load();
            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadPreviousScene(ActionEvent event) {
        try {
            ResourceBundle bundle = ResourceBundle.getBundle("i18n.messages", GameState.getLocale());
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/pet_selection.fxml"), bundle);
            Parent root = loader.load();
            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

