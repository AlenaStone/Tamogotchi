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
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class PetSelectionController implements Initializable {

    @FXML
    private ImageView background;

    @FXML
    private ImageView catImage;

    @FXML
    private ImageView dogImage;

    @FXML
    private ImageView birdImage;

    @FXML
    private ImageView fishImage;

    @FXML
    private Label closeLabel;

    @FXML
    private ImageView btnLeft;

    @FXML
    private ImageView btnOK;

    @FXML
    private ImageView btnRight;

    @FXML
    private ImageView btnCat;

    @FXML
    private ImageView btnDog;

    @FXML
    private ImageView btnBird;

    @FXML
    private ImageView btnFish;

    @FXML
    private Label backLabel;

    @FXML
    private Label labelSelectPet;

    private ImageView[] petButtons;
    private int currentPetIndex = 0; // Start with the first pet (cat)

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            WindowDragHelper.makeDraggable(background);
            background.setImage(
                    new Image(getClass().getResource("/images/ui/tamagotchi_pet_selection.png").toExternalForm()));

            labelSelectPet.setText(resourceBundle.getString("label.choosePet"));

            btnCat.setImage(new Image(getClass().getResource("/images/pets/cat/cat_idle.gif").toExternalForm()));
            btnDog.setImage(new Image(getClass().getResource("/images/pets/dog/dog_idle.gif").toExternalForm()));
            btnBird.setImage(new Image(getClass().getResource("/images/pets/bird/bird_idle.gif").toExternalForm()));
            btnFish.setImage(new Image(getClass().getResource("/images/pets/fish/fish_idle.gif").toExternalForm()));

            btnLeft.setImage(new Image(getClass().getResource("/images/ui/button_left.png").toExternalForm()));
            btnOK.setImage(new Image(getClass().getResource("/images/ui/button_OK.png").toExternalForm()));
            btnRight.setImage(new Image(getClass().getResource("/images/ui/button_right.png").toExternalForm()));

            petButtons = new ImageView[] { btnCat, btnDog, btnBird, btnFish };

            for (int i = 0; i < petButtons.length; i++) {
                petButtons[i].setVisible(i == currentPetIndex);
            }

        } catch (Exception e) {
            System.err.println("❌ Failed to load UI images: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleCatSelection() {
        currentPetIndex = 0;
        updatePetVisibility();
    }

    @FXML
    private void handleDogSelection() {
        currentPetIndex = 1;
        updatePetVisibility();
    }

    @FXML
    private void handleBirdSelection() {
        currentPetIndex = 2;
        updatePetVisibility();
    }

    @FXML
    private void handleFishSelection() {
        currentPetIndex = 3;
        updatePetVisibility();
    }

private void updatePetVisibility() {
    for (int i = 0; i < petButtons.length; i++) {
        petButtons[i].setVisible(i == currentPetIndex);
        if (i == currentPetIndex) {
            petButtons[i].setStyle("-fx-effect: dropshadow(gaussian, white, 15, 0.5, 0, 0);");
        } else {
            petButtons[i].setStyle("");  // Снять эффект с остальных
        }
    }
}


    @FXML
    private void handleLeft() {
        petButtons[currentPetIndex].setVisible(false);
        currentPetIndex = (currentPetIndex - 1 + petButtons.length) % petButtons.length;
        petButtons[currentPetIndex].setVisible(true);
    }

    @FXML
    private void handleRight() {
        petButtons[currentPetIndex].setVisible(false);
        currentPetIndex = (currentPetIndex + 1) % petButtons.length;
        petButtons[currentPetIndex].setVisible(true);
    }

    @FXML
    private void handleOKSelection() {
        String[] petTypes = { "cat", "dog", "bird", "fish" };
        GameState.setPetType(petTypes[currentPetIndex]);

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/name_input.fxml"));
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
    private void onExitImage(MouseEvent event) {
        ImageView button = (ImageView) event.getSource();
        button.setOpacity(1.0);
        button.setScaleX(1.0);
        button.setScaleY(1.0);
    }

    @FXML
    private void onHoverImage(MouseEvent event) {
        ImageView button = (ImageView) event.getSource();
        button.setOpacity(0.7);
        button.setScaleX(1.05);
        button.setScaleY(1.05);
    }

    @FXML
    private void handleClose() {
        Stage stage = (Stage) background.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void handleBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/language_selection.fxml"));
            Parent root = loader.load();

            Scene newScene = new Scene(root);
            newScene.setFill(javafx.scene.paint.Color.TRANSPARENT); // <-- вот это важно

            Stage stage = (Stage) background.getScene().getWindow();
            stage.setScene(newScene);
            stage.show();

        } catch (Exception e) {
            System.err.println("⚠️ Failed to load language selection scene: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void onHoverBackLabel() {
        backLabel.setStyle(
                "-fx-font-size: 20px; -fx-text-fill: white; -fx-background-color: red; -fx-font-weight: bold; -fx-padding: 3px; -fx-background-radius: 5px; -fx-cursor: hand;");
    }

    @FXML
    private void onHoverClose() {
        closeLabel.setStyle(
                "-fx-font-size: 20px; -fx-text-fill: white; -fx-background-color: red; -fx-font-weight: bold; -fx-padding: 3px; -fx-background-radius: 5px; -fx-cursor: hand;");
    }

    @FXML
    private void onBackMove() {
        if (backLabel != null) {
            backLabel.setStyle(
                    "-fx-font-size: 20px; -fx-text-fill: red; -fx-background-color: rgba(255,255,255,0.5); -fx-font-weight: bold; -fx-padding: 3px; -fx-background-radius: 5px; -fx-cursor: hand;");
        }
    }

    @FXML
    private void onExitClose() {
        closeLabel.setStyle(
                "-fx-font-size: 20px; -fx-text-fill: red; -fx-background-color: rgba(255,255,255,0.5); -fx-font-weight: bold; -fx-padding: 3px; -fx-background-radius: 5px; -fx-cursor: hand;");
    }
}
