/**
 * Utility class to make JavaFX windows draggable by mouse.
 * Allows dragging by holding a specific Node.
 */
package at.fhj.msd.util;

import javafx.scene.Node;
import javafx.stage.Stage;

public class WindowDragHelper {
    private static double xOffset = 0;
    private static double yOffset = 0;

    /**
     * Enables dragging functionality for the given Node.
     * Clicking and dragging this Node will move the entire Stage.
     * 
     * @param node the Node that will act as the draggable area
     */
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
