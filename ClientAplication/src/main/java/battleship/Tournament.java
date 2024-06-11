package battleship;

import Data.Player;
import Handler.ActionHandler;
import Handler.ServerHandler;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Tournament implements SceneInterface {
    private final ActionHandler actionHandler;
    private final ServerHandler serverHandler;
    private final PlayerInfo playerInfo;
    private final GameModes gameModes;
    private final List<String> tournaments;
    private final Map<String, List<String>> tournamentPlayers;
    private final Player player;
    private final Game game;
    List<String> players;
    private int initialPoints = 0;
    private int matches = 0;

    public Tournament(ActionHandler actionHandler, ServerHandler serverHandler, PlayerInfo playerInfo, GameModes gameModes,
                      List<String> players, List<String> tournaments, Map<String, List<String>> tournamentPlayers,
                      Player player, Game game) {
        this.actionHandler = actionHandler;
        this.serverHandler = serverHandler;
        this.playerInfo = playerInfo;
        this.gameModes = gameModes;
        this.players = players;
        this.tournaments = tournaments;
        this.tournamentPlayers = tournamentPlayers;
        this.player = player;
        this.game = game;
    }

    public Scene showScene(Stage primaryStage) {
        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(20));
        vbox.setAlignment(Pos.CENTER);
        BorderPane playerInfoPane = playerInfo.showPane();
        serverHandler.setPlayersForTournament(tournamentPlayers);

        Button createTournamentButton = createButton("Create", e -> {
            actionHandler.requestAllPlayers();
            serverHandler.setPlayersForTournamentInvite(players);
            ThreadWait();
            primaryStage.setScene(createNewTournamentScene(primaryStage, players));
        });

        Button joinTournamentButton = createButton("Join", e -> {
            actionHandler.requestTournaments();
            ThreadWait();
            primaryStage.setScene(createTournamentsListScene(primaryStage));
        });

        Button backButton = createButton("Back", e -> primaryStage.setScene(gameModes.showScene(primaryStage)));

        vbox.getChildren().addAll(playerInfoPane, createTournamentButton, joinTournamentButton, backButton);

        return new Scene(vbox, 500, 400);
    }

    public Scene createNewTournamentScene(Stage primaryStage, List<String> players) {
        VBox mainVBox = new VBox();
        mainVBox.setPadding(new Insets(10, 10, 10, 10));
        mainVBox.setSpacing(10);
        mainVBox.setAlignment(Pos.CENTER);

        Label titleLabel = new Label("Tournament");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");
        titleLabel.setAlignment(Pos.CENTER);

        mainVBox.getChildren().add(titleLabel);

        TextField tournamentNameField = new TextField();
        tournamentNameField.setPromptText("Enter Tournament Name");

        mainVBox.getChildren().add(tournamentNameField);

        ListView<CheckBox> listView = new ListView<>();
        listView.setPrefHeight(400);

        List<CheckBox> checkBoxes = new ArrayList<>();
        for (String player : players) {
            CheckBox checkBox = new CheckBox(player);
            checkBoxes.add(checkBox);
            listView.getItems().add(checkBox);
        }

        mainVBox.getChildren().add(listView);

        Button createTournamentButton = new Button("Create Tournament");
        createTournamentButton.setDisable(true);

        for (CheckBox checkBox : checkBoxes) {
            checkBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
                long selectedCount = checkBoxes.stream().filter(CheckBox::isSelected).count();
                createTournamentButton.setDisable(selectedCount < 2 || tournamentNameField.getText().isEmpty());
            });
        }

        tournamentNameField.textProperty().addListener((observable, oldValue, newValue) -> {
            long selectedCount = checkBoxes.stream().filter(CheckBox::isSelected).count();
            createTournamentButton.setDisable(selectedCount < 2 || newValue.isEmpty());
        });

        createTournamentButton.setOnAction(e -> {
            List<String> selectedPlayers = new ArrayList<>();
            for (CheckBox checkBox : checkBoxes) {
                if (checkBox.isSelected()) {
                    selectedPlayers.add(checkBox.getText());
                }
            }
            String tournamentName = tournamentNameField.getText();
            actionHandler.createTournament(tournamentName, selectedPlayers);
            primaryStage.setScene(showScene(primaryStage));
        });
        Button backButton = createButton("Back", e -> primaryStage.setScene(showScene(primaryStage)));
        mainVBox.getChildren().addAll(createTournamentButton, backButton);

        return new Scene(mainVBox, 500, 400);
    }

    private Scene createTournamentsListScene(Stage primaryStage) {
        VBox mainVBox = new VBox();
        mainVBox.setPadding(new Insets(10, 10, 10, 10));
        mainVBox.setSpacing(10);
        mainVBox.setAlignment(Pos.CENTER);

        Label titleLabel = new Label("Available Tournaments");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");
        titleLabel.setAlignment(Pos.CENTER);

        mainVBox.getChildren().add(titleLabel);

        ObservableList<String> items = FXCollections.observableArrayList(tournaments);
        ListView<String> listView = new ListView<>(items);
        listView.setPrefHeight(400);

        mainVBox.getChildren().add(listView);

        Button joinTournamentButton = new Button("Join");
        joinTournamentButton.setDisable(true);

        listView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> joinTournamentButton.setDisable(newValue == null));

        joinTournamentButton.setOnAction(e -> {
            String selectedTournament = listView.getSelectionModel().getSelectedItem();
            List<String> playersInTournament = tournamentPlayers.get(selectedTournament);
            matches = playersInTournament.size() - 1;
            if (playersInTournament.contains(player.getName())) {
                System.out.println("Joining tournament: " + selectedTournament);
                primaryStage.setScene(joinTournamentScene(primaryStage, selectedTournament));
            } else {
                showAlert("Not Registered", "You are not registered for this tournament.");
            }
        });

        mainVBox.getChildren().add(joinTournamentButton);

        Button backButton = createButton("Back", e -> primaryStage.setScene(showScene(primaryStage)));
        mainVBox.getChildren().add(backButton);

        initialPoints = player.getPoints();

        return new Scene(mainVBox, 500, 400);
    }

    private Scene joinTournamentScene(Stage primaryStage, String selectedTournament) {
        VBox mainVBox = new VBox();
        mainVBox.setAlignment(Pos.CENTER);
        mainVBox.setSpacing(10);
        mainVBox.setPadding(new Insets(10));

        Label titleLabel = new Label("[ " + selectedTournament + " ]");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");
        mainVBox.getChildren().add(titleLabel);

        List<String> opponents = tournamentPlayers.get(selectedTournament);
        if (opponents.size() > 1) {
            for (String opponentName : opponents) {
                if (!opponentName.equals(player.getName())) {
                    Button joinButton = createButton(opponentName, e -> {
                        System.out.println("Joining tournament against: " + opponentName);
                        player.setOpponent(opponentName);
                        serverHandler.setOpponentName(opponentName);
                        tournamentPlayers.get(selectedTournament).remove(opponentName);
                        opponents.remove(opponentName);
                        primaryStage.setScene(game.createGameGridScene(primaryStage, b -> primaryStage.setScene(joinTournamentScene(primaryStage, selectedTournament))));
                    });
                    mainVBox.getChildren().add(joinButton);
                }
            }
        } else {
            tournaments.remove(selectedTournament);
            Button endButton;
            if (player.getPoints() > initialPoints + matches) {
                endButton = new Button("You won the tournament!");
            } else {
                endButton = new Button("Tournament complete");
            }

            endButton.setStyle("-fx-background-color: #4CAF50; " +
                    "-fx-text-fill: white; " +
                    "-fx-font-size: 16px; " +
                    "-fx-padding: 10px 20px; " +
                    "-fx-border-radius: 5px; " +
                    "-fx-background-radius: 5px;");

            mainVBox.getChildren().add(endButton);
        }

        Button backButton = createButton("Back", e -> primaryStage.setScene(createTournamentsListScene(primaryStage)));
        mainVBox.getChildren().add(backButton);

        return new Scene(mainVBox, 500, 400);
    }
}
