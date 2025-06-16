package at.fhj.msd.ui.controller;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class PetSelectionController implements Initializable {

    @FXML
    private ImageView catImage;

    @FXML
    private ImageView dogImage;

    @FXML
    private ImageView birdImage;

    @FXML
    private ImageView fishImage;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        loadImage(catImage, "/images/pets/cat/cat_idle.gif");
        loadImage(dogImage, "/images/pets/dog/dog_idle.gif");
        loadImage(birdImage, "/images/pets/bird/bird_idle.gif");
        loadImage(fishImage, "/images/pets/fish/fish_idle.gif");
    }

    private void loadImage(ImageView imageView, String path) {
        try {
            URL imageUrl = getClass().getResource(path);
            if (imageUrl == null) {
                throw new IllegalArgumentException("File not found: " + path);
            }
            imageView.setImage(new Image(imageUrl.toExternalForm()));
        } catch (Exception e) {
            System.err.println("⚠️ Failed to load image: " + path);
            e.printStackTrace();
            // Optional: set a placeholder image
            // imageView.setImage(new Image("/images/default_pet.png"));
        }
    }

    @FXML
    private void handleCatSelection() {
        System.out.println("Cat selected");
        // TODO: implement scene switch logic
    }

    @FXML
    private void handleDogSelection() {
        System.out.println("Dog selected");
    }

    @FXML
    private void handleBirdSelection() {
        System.out.println("Bird selected");
    }

    @FXML
    private void handleFishSelection() {
        System.out.println("Fish selected");
    }
}
