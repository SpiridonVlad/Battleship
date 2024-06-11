package battleship;

import javafx.beans.value.ChangeListener;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

public class Settings implements SceneInterface {
    private final MainMenu mainMenu;

    public Settings(MainMenu mainMenu) {
        this.mainMenu = mainMenu;
    }

    public Scene showScene(Stage primaryStage) {
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(10, 10, 10, 10));
        grid.setVgap(5);
        grid.setHgap(5);
        grid.setAlignment(Pos.CENTER);

        TextField userField = new TextField();
        userField.setPromptText("Username");
        GridPane.setConstraints(userField, 1, 0);

        PasswordField passField = new PasswordField();
        passField.setPromptText("Password");
        GridPane.setConstraints(passField, 1, 1);

        Button submitButton = createButton("Submit", e -> {
            //actionHandler.login(userField.getText(), passField.getText());
            //serverHandler.setPlayer(player);
            //ThreadWait();
            primaryStage.setScene(mainMenu.showScene(primaryStage));
        });
        GridPane.setConstraints(submitButton, 1, 2);
        GridPane.setHalignment(submitButton, HPos.CENTER);
        submitButton.setDisable(true);

        Button backButton = createButton("Back", e -> primaryStage.setScene(mainMenu.showScene(primaryStage)));
        GridPane.setConstraints(backButton, 1, 3);
        GridPane.setHalignment(backButton, HPos.CENTER);

        ChangeListener<String> textListener = (observable, oldValue, newValue) -> submitButton.setDisable(userField.getText().isEmpty() || passField.getText().isEmpty());

        userField.textProperty().addListener(textListener);
        passField.textProperty().addListener(textListener);

        grid.getChildren().addAll(userField, passField, submitButton, backButton);

        return new Scene(grid, 500, 400);
    }
}
