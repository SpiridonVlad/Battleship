package Handler;

import Connection.ConnectionManager;
import Data.GameData;
import Data.Player;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

import static battleship.ClientApp.GRID_SIZE;

public class ServerHandler {
    private final ConnectionManager connectionManager;
    private final PlayerBoardHandler playerBoardHandler;
    ComboBox<String> playerComboBox;
    private boolean running = true;

    public ServerHandler(ConnectionManager connectionManager, GridPane playerGrid, GridPane opponentGrid) {
        this.connectionManager = connectionManager;
        playerBoardHandler = new PlayerBoardHandler(playerGrid, opponentGrid);
        listenToServer();
    }

    public void handleIncomingMessage(String message) {
        if (message.startsWith("Attack-ME:")) {
            String[] parts = message.split(":");
            int row = Integer.parseInt(parts[1]);
            int col = Integer.parseInt(parts[2]);
            Platform.runLater(() -> updatePlayerGrid(row, col));
        }
        if (message.startsWith("GameWon")) {
            Platform.runLater(this::gameWonScreen);
            playerBoardHandler.getPlayer().updateScore();
            playerBoardHandler.getPlayer().getGameData().raiseStats();
        }
        if (message.startsWith("Players")) {
            Platform.runLater(() -> updatePlayerComboBox(message));
        }
        if (message.startsWith("Login")) {
            String[] parts = message.split(":");
            playerBoardHandler.getPlayer().setName(parts[1]);
            playerBoardHandler.getPlayer().setGameData(new GameData(Integer.parseInt(parts[3]), Integer.parseInt(parts[4])));
            playerBoardHandler.getPlayer().setLogged(true);
            playerBoardHandler.getPlayer().setScore(Integer.parseInt(parts[2]));
        }
        if (message.startsWith("Register")) {
            playerBoardHandler.getPlayer().setName(message.split(":")[1]);
            playerBoardHandler.getPlayer().setLogged(true);
            playerBoardHandler.getPlayer().setScore(0);
            playerBoardHandler.getPlayer().setGameData(new GameData(0, 0));
        }
        if (message.startsWith("Ships")) {
            Platform.runLater(() -> updateOpponentGrid(message));
        }
        if (message.startsWith("StartTurn")) {
            Platform.runLater(this::updateTurn);
        }
        if (message.startsWith("InvitedGame:")) {
            String[] parts = message.split(":");
            playerBoardHandler.getPlayer().addGame(parts[1]);
        }
        if (message.startsWith("ExitGame")) {
            running = false;
            connectionManager.closeConnection();
        }
        if (message.startsWith("Tournaments:")) {
            String[] tournaments = message.substring(12).split("\\|");

            for (String tournament : tournaments) {
                String[] parts = tournament.split(":");
                String tournamentName = parts[0];
                List<String> players = new ArrayList<>(Arrays.asList(parts).subList(1, parts.length));

                if (!playerBoardHandler.getTournaments().contains(tournamentName)) {
                    playerBoardHandler.addTournament(tournamentName);
                }
                playerBoardHandler.addPlayerToTournament(players, tournamentName);
            }
        }
        if (message.startsWith("AllPlayers")) {
            String[] parts = message.split(":");
            for (int i = 1; i < parts.length; i++) {
                if (!playerBoardHandler.getPlayers().contains(parts[i])) {
                    playerBoardHandler.addPlayer(parts[i]);
                }
            }
        }
        if (message.startsWith("Hit")) {
            String[] parts = message.split(":");
            int row = Integer.parseInt(parts[1]);
            int col = Integer.parseInt(parts[2]);
            Platform.runLater(() -> updateOppGrid(row, col));
        }
    }

    private void updateOppGrid(int row, int col) {
        Button button = getNodeByRowColumnIndex(row, col, playerBoardHandler.getOpponentGrid());
        if (button != null) {
            button.setStyle("-fx-border-color: black; -fx-border-width: 1;-fx-background-color: red;");
            button.setUserData("hit");
            Image shipImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/ship.png")));
            ImageView imageView = new ImageView(shipImage);
            imageView.setFitWidth(60);
            imageView.setFitHeight(30);
            imageView.setPreserveRatio(true);
            imageView.setSmooth(true);
            imageView.setCache(true);
            button.setMaxSize(60, 30);
            button.setGraphic(imageView);
        }
    }

    private void updateTurn() {
        playerBoardHandler.updateTurn(true);
    }

    private void updateOpponentGrid(String message) {
        String[] parts = message.split(":");
        for (int i = 1; i < parts.length; i = i + 2) {
            int row = Integer.parseInt(parts[i]);
            int col = Integer.parseInt(parts[i + 1]);
            Button button = getNodeByRowColumnIndex(row, col, playerBoardHandler.getOpponentGrid());
            assert button != null;
            if (button.getUserData() == "empty") {
                button.setStyle("-fx-border-color: black; -fx-border-width: 1; -fx-background-color: lightblue;");
                button.setUserData("ship");
                button.addEventHandler(ActionEvent.ACTION, event -> {
                    if (button.getUserData().equals("ship") && playerBoardHandler.getIsClientsTurn().get()) {
                        button.setStyle("-fx-border-color: black; -fx-border-width: 1;-fx-background-color: red;");
                        button.setUserData("hit");
                        Image shipImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/ship.png")));
                        ImageView imageView = new ImageView(shipImage);
                        imageView.setFitWidth(60);
                        imageView.setFitHeight(30);
                        imageView.setPreserveRatio(true);
                        imageView.setSmooth(true);
                        imageView.setCache(true);
                        button.setMaxSize(60, 30);
                        button.setGraphic(imageView);
                    }
                });
            }
        }
    }

    private void updatePlayerComboBox(String message) {
        playerComboBox.getItems().clear();
        String[] players = message.split(":");
        String[] parts = players[1].split(",");
        for (int i = 1; i < parts.length; i++) {
            if (!parts[i].equals(playerBoardHandler.getPlayer().getName()))
                playerComboBox.getItems().add(parts[i]);
        }
    }

    private void gameWonScreen() {
        playerBoardHandler.getOpponentGrid().getChildren().clear();

        Label gameOverLabel = new Label("Game Over");
        gameOverLabel.setFont(new Font("Arial", 48));
        gameOverLabel.setTextAlignment(TextAlignment.CENTER);

        playerBoardHandler.getOpponentGrid().add(gameOverLabel, 0, 0);
        GridPane.setColumnSpan(gameOverLabel, GRID_SIZE);
        GridPane.setRowSpan(gameOverLabel, GRID_SIZE);
        playerBoardHandler.getOpponentGrid().setAlignment(Pos.CENTER);
    }

    private void updatePlayerGrid(int row, int col) {
        Button button = getNodeByRowColumnIndex(row, col, playerBoardHandler.getPlayerGrid());
        if (button != null) {
            button.setStyle("-fx-border-color: black; -fx-border-width: 1;-fx-background-color: red;");
            button.setUserData("hit");
        }
        if (!shipsExists()) {
            gameOverScreen();
            connectionManager.sendMessage("GameWon:" + playerBoardHandler.getOpponentName());

        }
    }

    private void gameOverScreen() {
        playerBoardHandler.getPlayerGrid().getChildren().clear();

        Label gameOverLabel = new Label("Game Over");
        gameOverLabel.setFont(new Font("Arial", 48));
        gameOverLabel.setTextAlignment(TextAlignment.CENTER);

        playerBoardHandler.getPlayerGrid().add(gameOverLabel, 0, 0);
        GridPane.setColumnSpan(gameOverLabel, GRID_SIZE);
        GridPane.setRowSpan(gameOverLabel, GRID_SIZE);
        playerBoardHandler.getPlayerGrid().setAlignment(Pos.CENTER);
    }

    public boolean shipsExists() {
        for (javafx.scene.Node node : playerBoardHandler.getPlayerGrid().getChildren()) {
            if (node instanceof Button squareButton) {
                Object userData = squareButton.getUserData();
                if ("ship".equals(userData)) {
                    return true;
                }
            }
        }
        return false;
    }

    private Button getNodeByRowColumnIndex(final int row, final int col, GridPane gridPane) {
        for (javafx.scene.Node node : gridPane.getChildren()) {
            if (GridPane.getRowIndex(node) == row && GridPane.getColumnIndex(node) == col) {
                return (Button) node;
            }
        }
        return null;
    }

    private void listenToServer() {
        new Thread(() -> {
            try {
                if (!connectionManager.isConnected()) {
                    System.out.println("Connection not established. Exiting listenToServer.");
                    return;
                }

                System.out.println("Listening to server...");
                while (running && connectionManager.isConnected()) {
                    String message = connectionManager.receiveMessage();
                    if (message != null) {
                        System.out.println("Received from server: " + message);
                        handleIncomingMessage(message);
                    }
                }
                System.out.println("Connection to server closed or stopped. Exiting listenToServer.");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    public void setPlayerComboBox(ComboBox<String> playerComboBox) {
        this.playerComboBox = playerComboBox;
    }

    public void setPlayerGrid(GridPane playerGrid) {
        playerBoardHandler.setPlayerGrid(playerGrid);
    }

    public void setOpponentName(String opponentName) {
        playerBoardHandler.setOpponentName(opponentName);
    }

    public void setOpponentGrid(GridPane opponentGrid) {
        playerBoardHandler.setOpponentGrid(opponentGrid);
    }

    public void setPlayer(Player player) {
        playerBoardHandler.setPlayer(player);
    }

    public void setClientsTurn(AtomicBoolean clientsTurn) {
        playerBoardHandler.setIsClientsTurn(clientsTurn);
    }

    public void setTournaments(List<String> tournaments) {
        playerBoardHandler.setTournaments(tournaments);
    }

    public void setPlayersForTournamentInvite(List<String> players) {
        playerBoardHandler.setPlayers(players);
    }

    public void setPlayersForTournament(Map<String, List<String>> playerTournament) {
        playerBoardHandler.setPlayerTournament(playerTournament);
    }

}

