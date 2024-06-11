package battleship;

import Data.Player;
import Handler.ActionHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class MainMenu implements SceneInterface {
    private final ActionHandler actionHandler;
    private final Player player;
    GameModes gameModes;
    PlayerInfo playerInfo;
    Information information;
    Settings settings;
    Login login;
    Register register;

    public MainMenu(ActionHandler actionHandler, Player player) {
        this.actionHandler = actionHandler;
        this.player = player;
    }

    public Scene showScene(Stage primaryStage) {
        VBox vbox = new VBox();
        vbox.setPadding(new Insets(10, 10, 10, 10));
        vbox.setSpacing(10);
        vbox.setAlignment(Pos.CENTER);
        primaryStage.setTitle("Battleship");
        primaryStage.setOnCloseRequest(event -> actionHandler.exitGame());
        Label titleLabel = new Label("Battleship");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");
        titleLabel.setAlignment(Pos.CENTER);

        GridPane grid = new GridPane();
        grid.setPadding(new Insets(10, 10, 10, 10));
        grid.setVgap(10);
        grid.setHgap(10);
        grid.setAlignment(Pos.CENTER);

        if (player.getLogged()) {
            BorderPane playerInfoPane = new BorderPane();
            playerInfoPane.setPrefWidth(200);

            Button playerInfoButton = new Button(player.getName() + " [" + player.getScore() + "]");
            playerInfoButton.setStyle("-fx-font-weight: bold;");
            playerInfoButton.setOnAction(event -> primaryStage.setScene(playerInfo.showScene(primaryStage)));
            playerInfoPane.setCenter(playerInfoButton);

            grid.add(playerInfoPane, 0, 0);
        }

        Button playButton = createButton("Play", e -> primaryStage.setScene(gameModes.showScene(primaryStage)));
        grid.add(playButton, 0, 1);

        if (!player.getLogged()) {
            Button loginButton = createButton("Login", e -> primaryStage.setScene(login.showScene(primaryStage)));
            grid.add(loginButton, 0, 2);
            Button registerButton = createButton("Register", e -> primaryStage.setScene(register.showScene(primaryStage)));
            grid.add(registerButton, 0, 3);
        } else {
            Button logoutButton = createButton("Logout", e -> {
                actionHandler.logout(player.getName());
                player.resetPlayer();
                primaryStage.setScene(showScene(primaryStage));
            });
            grid.add(logoutButton, 0, 2);
        }

        Button settingsButton = createButton("Settings", e -> primaryStage.setScene(settings.showScene(primaryStage)));
        Button infoButton = createButton("Info", e -> primaryStage.setScene(information.showScene(primaryStage)));
        settingsButton.setMinWidth(95);
        infoButton.setMinWidth(95);
        HBox hbox = new HBox();
        hbox.setSpacing(10);
        hbox.setAlignment(Pos.CENTER);
        hbox.getChildren().addAll(settingsButton, infoButton);
        if (!player.getLogged()) {
            grid.add(hbox, 0, 4);
        } else {
            grid.add(hbox, 0, 3);
        }

        Label footerLabel = new Label("Spiridon Vlad");
        footerLabel.setAlignment(Pos.CENTER);
        footerLabel.setPadding(new Insets(20, 0, 0, 0));

        vbox.getChildren().addAll(titleLabel, grid, footerLabel);

        return new Scene(vbox, 500, 400);
    }

    public void setGameModes(GameModes gameModes) {
        this.gameModes = gameModes;
    }

    public void setInformation(Information information) {
        this.information = information;
    }

    public void setLogin(Login login) {
        this.login = login;
    }

    public void setPlayerInfo(PlayerInfo playerInfo) {
        this.playerInfo = playerInfo;
    }

    public void setRegister(Register register) {
        this.register = register;
    }

    public void setSettings(Settings settings) {
        this.settings = settings;
    }
}
