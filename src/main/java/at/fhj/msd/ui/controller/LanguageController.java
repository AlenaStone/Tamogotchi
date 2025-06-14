package at.fhj.msd.ui.controller;

import java.io.IOException;
import java.util.Locale;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class LanguageController {
    @FXML
    private void handleRussian(ActionEvent event) {
        loadNextScene(new Locale("ru"));
    }

     @FXML
    private void handleEnglish(ActionEvent event) {
        loadNextScene(new Locale("en"));
    }

private void loadNextScene(Locale locale, ActionEvent event) {
    try {
        ResourceBundle bundle = ResourceBundle.getBundle("i18n.messages", locale);
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/pet_selection.fxml"), bundle);
        Parent root = loader.load();

        Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();

        // можно сохранить выбранный язык в GameState
    } catch (IOException e) {
        e.printStackTrace();
    }
}
}
