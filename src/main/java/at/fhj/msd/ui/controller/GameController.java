package at.fhj.msd.ui.controller;

import at.fhj.msd.model.GameState;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class GameController {
    @FXML
    private Label petNameLabel;
    @FXML
    private Label petTypeLabel;
    @FXML
    private ImageView petImageView;

    @FXML
    public void initialize() {
        String name = GameState.getPetName();
        String type = GameState.getPetType();

        petNameLabel.setText("Name for pet: " + (name != null ? name : "not set"));
        petTypeLabel.setText("Type: " + (type != null ? type : "not set"));

        if (type != null) {
            String imagePath = "/images/" + type + "/idle.gif";
            Image petImage = new Image(getClass().getResourceAsStream(imagePath));
            petImageView.setImage(petImage);
        } else {
            petImageView.setImage(null); // Clear image if type is not set
        }
    }

}
