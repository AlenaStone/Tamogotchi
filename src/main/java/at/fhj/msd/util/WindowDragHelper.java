package at.fhj.msd.util;


import javafx.scene.Node;
import javafx.stage.Stage;

public class WindowDragHelper {
    private static double xOffset = 0;
    private static double yOffset = 0;

    public static void makeDraggable(Node node) {
        node.setOnMousePressed(event -> {
            xOffset = event.getSceneX();
            yOffset = event.getSceneY();
        });

        node.setOnMouseDragged(event -> {
            Stage stage = (Stage) node.getScene().getWindow();
            stage.setX(event.getScreenX() - xOffset);
            stage.setY(event.getScreenY() - yOffset);
        });
    }
}


