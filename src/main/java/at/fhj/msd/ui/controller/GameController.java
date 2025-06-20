package at.fhj.msd.ui.controller;

import at.fhj.msd.model.GameState;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.util.ResourceBundle;

public class GameController {

    @FXML
    private Label petNameLabel;
    @FXML
    private Label petTypeLabel;
    @FXML
    private ImageView petImageView;

    @FXML
    public void initialize() {
        ResourceBundle bundle = GameState.getBundle();

        String name = GameState.getPetName();
        String type = GameState.getPetType();

        petNameLabel.setText(bundle.getString("label.petName") + ": " + (name != null ? name : "-"));
        petTypeLabel.setText(bundle.getString("label.petType") + ": " + (type != null ? type : "-"));

        if (type != null) {
            String imagePath = "/images/" + type + "/idle.gif";
            Image petImage = new Image(getClass().getResourceAsStream(imagePath));
            petImageView.setImage(petImage);
        } else {
            petImageView.setImage(null); // Если тип не задан
        }
    }
}
