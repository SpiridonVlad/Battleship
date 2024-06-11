package battleship;

import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import static java.lang.Thread.sleep;

public interface SceneInterface {
    default void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    default Button createButton(String text, javafx.event.EventHandler<javafx.event.ActionEvent> event) {
        Button button = new Button(text);
        button.setMinWidth(200);
        button.setMinHeight(40);
        button.setOnAction(event);
        return button;
    }

    default void ThreadWait() {
        try {
            sleep(100);
        } catch (InterruptedException ex) {
            throw new RuntimeException(ex);
        }
    }

    default Button getNodeByRowColumnIndex(final int row, final int col, GridPane gridPane) {
        for (javafx.scene.Node node : gridPane.getChildren()) {
            if (GridPane.getRowIndex(node) == row && GridPane.getColumnIndex(node) == col) {
                return (Button) node;
            }
        }
        return null;
    }

    default Scene showScene(Stage primaryStage) {
        return null;
    }
}
