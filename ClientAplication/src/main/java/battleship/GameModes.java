package battleship;

import Data.Player;
import Handler.ActionHandler;
import Handler.ServerHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class GameModes implements SceneInterface {
    private final Player player;
    private final PlayerInfo playerInfo;
    private final ActionHandler actionHandler;
    private final ServerHandler serverHandler;
    private final MainMenu mainMenu;
    private Game game;
    private Tournament tournament;

    public GameModes(ActionHandler actionHandler, ServerHandler serverHandler, Player player,
                     PlayerInfo playerInfo, MainMenu mainMenu) {
        this.actionHandler = actionHandler;
        this.serverHandler = serverHandler;
        this.player = player;
        this.playerInfo = playerInfo;
        this.mainMenu = mainMenu;
    }

    public void setGame(Game game) {
        this.game = game;
    }

    public void setTournament(Tournament tournament) {
        this.tournament = tournament;
    }

    public Scene showScene(Stage primaryStage) {
        VBox vbox = new VBox();
        vbox.setPadding(new Insets(10, 10, 10, 10));
        vbox.setSpacing(10);
        vbox.setAlignment(Pos.CENTER);

        BorderPane playerInfoPane = playerInfo.showPane();

        ComboBox<String> playerComboBox = new ComboBox<>();
        playerComboBox.setPromptText("Fetch Players");
        playerComboBox.setMinWidth(200);
        playerComboBox.setOnMouseClicked(e -> {
            actionHandler.fetchPlayers();
            serverHandler.setPlayerComboBox(playerComboBox);
        });


        Button createGameButton = createButton("Create Game", e -> {
            actionHandler.inviteToGame(playerComboBox.getValue(), player.getName());
            player.getGames().add(playerComboBox.getValue());
            primaryStage.setScene(game.showScene(primaryStage));
        });
        createGameButton.setMinWidth(95);
        createGameButton.setDisable(true);

        Button joinGameButton = createButton("Join Game", e -> primaryStage.setScene(game.showScene(primaryStage)));
        joinGameButton.setMinWidth(95);

        playerComboBox.setOnAction(event -> {
            String selectedPlayer = playerComboBox.getValue();
            if (selectedPlayer != null) {
                createGameButton.setDisable(false);
                joinGameButton.setDisable(false);
            } else {
                createGameButton.setDisable(true);
                joinGameButton.setDisable(true);
            }
        });

        HBox buttonBox = new HBox();
        buttonBox.setSpacing(10);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.getChildren().addAll(createGameButton, joinGameButton);

        Button backButton = createButton("Back", e -> primaryStage.setScene(mainMenu.showScene(primaryStage)));
        Button tournamentButton = createButton("Tournament", e -> primaryStage.setScene(tournament.showScene(primaryStage)));
        Button aiButton = createButton("AI", e -> {
            player.setOpponent("AI");
            primaryStage.setScene(game.createAiGridScene(primaryStage, b -> primaryStage.setScene(showScene(primaryStage))));
        });

        vbox.getChildren().addAll(playerInfoPane, playerComboBox, buttonBox, aiButton, tournamentButton, backButton);

        return new Scene(vbox, 500, 400);
    }
}
