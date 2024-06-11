package battleship;

import Data.Player;
import Handler.ActionHandler;
import Handler.ServerHandler;
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

public class Login implements SceneInterface {

    private final ActionHandler actionHandler;
    private final ServerHandler serverHandler;
    private Player player;
    private MainMenu mainMenu;

    public Login(ActionHandler actionHandler, ServerHandler serverHandler, Player player, MainMenu mainMenu) {
        this.actionHandler = actionHandler;
        this.serverHandler = serverHandler;
        this.player = player;
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

        Button loginButton = createButton("Login", e -> {
            actionHandler.login(userField.getText(), passField.getText());
            serverHandler.setPlayer(player);
            ThreadWait();
            primaryStage.setScene(mainMenu.showScene(primaryStage));
        });
        GridPane.setConstraints(loginButton, 1, 2);
        GridPane.setHalignment(loginButton, HPos.CENTER);
        loginButton.setDisable(true);

        Button backButton = createButton("Back", e -> primaryStage.setScene(mainMenu.showScene(primaryStage)));
        GridPane.setConstraints(backButton, 1, 3);
        GridPane.setHalignment(backButton, HPos.CENTER);

        ChangeListener<String> textListener = (observable, oldValue, newValue) -> loginButton.setDisable(userField.getText().isEmpty() || passField.getText().isEmpty());
        userField.textProperty().addListener(textListener);
        passField.textProperty().addListener(textListener);

        grid.getChildren().addAll(userField, passField, loginButton, backButton);

        return new Scene(grid, 500, 400);
    }
}
