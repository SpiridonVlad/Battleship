package battleship;

import Data.Player;
import Handler.ActionHandler;
import Handler.ServerHandler;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

public class Game implements SceneInterface {
    public static final int GRID_SIZE = 10;
    private static final int SHIP_LENGTH = 3;
    private final Player player;
    private final ServerHandler serverHandler;
    private final ActionHandler actionHandler;
    private final AtomicBoolean isMyTurn = new AtomicBoolean(false);
    GridPane opponentGrid = new GridPane();
    GridPane playerGrid = new GridPane();
    private GameModes gameModes;
    private boolean isHorizontal = true;


    public Game(ActionHandler actionHandler, GameModes gameModes, boolean isHorizontal, GridPane opponentGrid,
                Player player, GridPane playerGrid, ServerHandler serverHandler) {
        this.actionHandler = actionHandler;
        this.gameModes = gameModes;
        this.isHorizontal = isHorizontal;
        this.opponentGrid = opponentGrid;
        this.player = player;
        this.playerGrid = playerGrid;
        this.serverHandler = serverHandler;
    }

    public Scene showScene(Stage primaryStage) {
        VBox vbox = new VBox();
        vbox.setPadding(new Insets(20, 20, 20, 20));
        vbox.setSpacing(20);
        vbox.setAlignment(Pos.CENTER);

        Label titleLabel = new Label("Your Games");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");
        vbox.getChildren().add(titleLabel);

        ListView<String> gamesListView = new ListView<>();
        gamesListView.setPrefSize(350, 250);
        gamesListView.setStyle("-fx-font-size: 16px; -fx-border-color: #ccc; -fx-border-radius: 5px; -fx-padding: 10px;");

        List<String> gamesList = player.getGames();
        gamesListView.getItems().addAll(gamesList);
        vbox.getChildren().add(gamesListView);

        gamesListView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                String selectedGame = gamesListView.getSelectionModel().getSelectedItem();
                if (selectedGame != null) {
                    System.out.println("Opp:" + selectedGame);
                    player.setOpponent(selectedGame);
                    serverHandler.setOpponentName(selectedGame);
                    primaryStage.setScene(createGameGridScene(primaryStage, e -> primaryStage.setScene(gameModes.showScene(primaryStage))));
                }
            }
        });

        Button backButton = createButton("Back", e -> primaryStage.setScene(gameModes.showScene(primaryStage)));
        vbox.getChildren().add(backButton);

        VBox.setMargin(backButton, new Insets(20, 0, 0, 0));

        return new Scene(vbox, 500, 500);
    }

    public Scene createGameGridScene(Stage primaryStage, EventHandler<ActionEvent> backButtonHandler) {
        createPlayerGrid(primaryStage);
        createOppGrid(actionHandler);

        Button opponentNameButton = new Button(player.getOpponent());
        opponentNameButton.setFont(new Font("Arial", 20));
        opponentNameButton.setAlignment(Pos.CENTER);

        Button timeButton = new Button("20");
        timeButton.setFont(new Font("Arial", 20));
        timeButton.setAlignment(Pos.CENTER);

        Runnable startCountdown = getTimer(timeButton);

        Task<Void> monitorShipPlacementTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                while (!actionHandler.shipsExists(playerGrid)) {
                    Thread.sleep(500);
                }
                Platform.runLater(startCountdown);
                return null;
            }
        };

        Thread monitorShipPlacementThread = new Thread(monitorShipPlacementTask);
        monitorShipPlacementThread.setDaemon(true);
        monitorShipPlacementThread.start();

        for (javafx.scene.Node node : opponentGrid.getChildren()) {
            if (node instanceof Button attackButton) {
                attackButton.addEventHandler(ActionEvent.ACTION, event -> {
                    if (isMyTurn.get()) {
                        startCountdown.run();
                    }
                });
            }
        }

        HBox nameLayout = new HBox(10);
        nameLayout.setAlignment(Pos.CENTER);
        nameLayout.getChildren().addAll(opponentNameButton, timeButton);
        serverHandler.setOpponentGrid(opponentGrid);
        serverHandler.setPlayerGrid(playerGrid);

        HBox shipsPanel = createShipsPanel();

        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(10));
        vbox.setAlignment(Pos.CENTER);
        vbox.getChildren().addAll(shipsPanel, opponentGrid, nameLayout, playerGrid);

        Button backButton = createButton("Back", backButtonHandler);
        vbox.getChildren().add(backButton);

        return new Scene(vbox, 900, 900);
    }

    private void createPlayerGrid(Stage primaryStage) {
        playerGrid = new GridPane();
        playerGrid.setVgap(1);
        playerGrid.setHgap(1);
        playerGrid.setAlignment(Pos.CENTER);

        for (int row = 0; row < GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE; col++) {
                Button squareButton = new Button();
                squareButton.setStyle("-fx-border-color: black; -fx-border-width: 1; -fx-background-color: lightblue;");
                squareButton.setMinSize(60, 30);
                playerGrid.add(squareButton, col, row);

                squareButton.setOnDragOver(event -> {
                    if (event.getGestureSource() != squareButton && event.getDragboard().hasString()) {
                        event.acceptTransferModes(TransferMode.MOVE);
                    }
                    event.consume();
                });

                squareButton.setOnDragDropped(event -> {
                    Dragboard db = event.getDragboard();
                    boolean success = false;
                    if (db.hasString()) {
                        String shipId = db.getString();
                        int colIndex = GridPane.getColumnIndex(squareButton);
                        int rowIndex = GridPane.getRowIndex(squareButton);
                        if (canPlaceShip(playerGrid, colIndex, rowIndex, isHorizontal)) {
                            placeShip(playerGrid, colIndex, rowIndex, shipId, isHorizontal);
                            success = true;
                            removeShipById(primaryStage, shipId);
                        }
                    }
                    event.setDropCompleted(success);
                    event.consume();
                });
            }
        }
    }

    private void createOppGrid(ActionHandler actionHandler) {
        opponentGrid = new GridPane();
        opponentGrid.setVgap(1);
        opponentGrid.setHgap(1);
        opponentGrid.setAlignment(Pos.CENTER);

        for (int row = 0; row < GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE; col++) {
                Button squareButton = new Button();
                squareButton.setStyle("-fx-border-color: black; -fx-border-width: 1; -fx-background-color: lightblue;");
                squareButton.setMinSize(60, 30);
                squareButton.setUserData("empty");
                int finalRow = row;
                int finalCol = col;
                squareButton.setOnAction(e -> {
                    if (isMyTurn.get()) {
                        actionHandler.sendAttack(finalRow, finalCol, player.getOpponent(), playerGrid);
                        squareButton.setStyle("-fx-background-color: red;");
                        squareButton.setUserData("hit");
                        isMyTurn.set(false);
                    }
                });
                opponentGrid.add(squareButton, col, row);
            }
        }
        serverHandler.setOpponentGrid(opponentGrid);
        serverHandler.setClientsTurn(isMyTurn);
    }

    private Runnable getTimer(Button timeButton) {
        final Thread[] timeThread = {null};

        return () -> {
            if (timeThread[0] != null && timeThread[0].isAlive()) {
                timeThread[0].interrupt();
            }

            timeThread[0] = new Thread(() -> {
                int countdown = 20;
                while (countdown >= 0) {
                    int finalCountdown = countdown;
                    Platform.runLater(() -> timeButton.setText(String.valueOf(finalCountdown)));

                    if (countdown == 0) {
                        Platform.runLater(() -> actionHandler.turnFinished(player.getOpponent()));
                        Platform.runLater(() -> isMyTurn.set(false));
                        break;
                    }

                    countdown--;
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            });

            timeThread[0].setDaemon(true);
            timeThread[0].start();
        };
    }

    private HBox createShipsPanel() {
        HBox shipsPanel = new HBox(10);
        shipsPanel.setPadding(new Insets(10));
        shipsPanel.setAlignment(Pos.CENTER);

        List<Button> ships = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            Button ship = createShip();
            ships.add(ship);
            shipsPanel.getChildren().add(ship);

            ship.setOnDragDetected(event -> {
                Dragboard db = ship.startDragAndDrop(TransferMode.MOVE);
                ClipboardContent content = new ClipboardContent();
                content.putString(ship.getId());
                db.setContent(content);
                event.consume();
            });
        }

        Button orientationButton = new Button("Toggle Orientation");
        orientationButton.setOnAction(e -> isHorizontal = !isHorizontal);
        shipsPanel.getChildren().add(orientationButton);
        return shipsPanel;
    }

    private Button createShip() {
        Image shipImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/ship.png")));
        ImageView imageView = new ImageView(shipImage);
        imageView.setFitWidth(30);
        imageView.setPreserveRatio(true);

        Button shipButton = new Button();
        shipButton.setGraphic(imageView);
        shipButton.setId("ship" + SHIP_LENGTH + "_" + System.currentTimeMillis());

        shipButton.setOnDragDetected(event -> {
            Dragboard db = shipButton.startDragAndDrop(TransferMode.MOVE);
            ClipboardContent content = new ClipboardContent();
            content.putString(shipButton.getId());
            db.setContent(content);
            event.consume();
        });

        return shipButton;
    }

    private boolean canPlaceShip(GridPane grid, int col, int row, boolean isHorizontal) {
        if (isHorizontal) {
            if (col + SHIP_LENGTH > GRID_SIZE) {
                return false;
            }
            for (int i = 0; i < SHIP_LENGTH; i++) {
                Button button = getNodeByRowColumnIndex(row, col + i, grid);
                if (button == null || button.getStyle().equals("-fx-background-color: gray;")) {
                    return false;
                }
            }
        } else {
            if (row + SHIP_LENGTH > GRID_SIZE) {
                return false;
            }
            for (int i = 0; i < SHIP_LENGTH; i++) {
                Button button = getNodeByRowColumnIndex(row + i, col, grid);
                if (button == null || button.getStyle().equals("-fx-background-color: gray;")) {
                    return false;
                }
            }
        }
        return true;
    }

    private void placeShip(GridPane grid, int col, int row, String shipId, boolean isHorizontal) {
        Image shipImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/ship.png")));

        for (int i = 0; i < SHIP_LENGTH; i++) {
            Button button;
            if (isHorizontal) {
                button = getNodeByRowColumnIndex(row, col + i, grid);
            } else {
                button = getNodeByRowColumnIndex(row + i, col, grid);
            }

            if (button != null) {
                ImageView imageView = new ImageView(shipImage);
                imageView.setFitWidth(60);
                imageView.setFitHeight(30);
                imageView.setPreserveRatio(true);
                imageView.setSmooth(true);
                imageView.setCache(true);

                button.setGraphic(imageView);
                button.setMaxSize(60, 30);
                button.setUserData("ship");
                button.setId(shipId);
            }
        }
    }

    private void removeShipById(Stage primaryStage, String shipId) {
        VBox root = (VBox) primaryStage.getScene().getRoot();
        HBox shipsPanel = (HBox) root.getChildren().getFirst();
        for (Node ship : shipsPanel.getChildren()) {
            if (shipId.equals(ship.getId())) {
                shipsPanel.getChildren().remove(ship);
                actionHandler.sendShips(playerGrid);
                if (actionHandler.shipsExists(playerGrid)) {
                    shipsPanel.getChildren().clear();
                    actionHandler.turnFinished(player.getOpponent());
                }
                break;
            }
        }
    }

    public Scene createAiGridScene(Stage primaryStage, EventHandler<ActionEvent> backButtonHandler) {
        createPlayerGrid(primaryStage);
        createAIGrid(actionHandler);

        Button opponentNameButton = new Button(player.getOpponent());
        opponentNameButton.setFont(new Font("Arial", 20));
        opponentNameButton.setAlignment(Pos.CENTER);

        Button timeButton = new Button("20");
        timeButton.setFont(new Font("Arial", 20));
        timeButton.setAlignment(Pos.CENTER);

        Runnable startCountdown = getTimer(timeButton);

        Task<Void> monitorShipPlacementTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                while (!actionHandler.shipsExists(playerGrid)) {
                    Thread.sleep(500);
                }
                Platform.runLater(startCountdown);
                return null;
            }
        };

        Thread monitorShipPlacementThread = new Thread(monitorShipPlacementTask);
        monitorShipPlacementThread.setDaemon(true);
        monitorShipPlacementThread.start();

        for (javafx.scene.Node node : opponentGrid.getChildren()) {
            if (node instanceof Button attackButton) {
                attackButton.addEventHandler(ActionEvent.ACTION, event -> {
                    if (isMyTurn.get()) {
                        startCountdown.run();
                    }
                });
            }
        }

        HBox nameLayout = new HBox(10);
        nameLayout.setAlignment(Pos.CENTER);
        nameLayout.getChildren().addAll(opponentNameButton, timeButton);
        serverHandler.setOpponentGrid(opponentGrid);
        serverHandler.setPlayerGrid(playerGrid);

        HBox shipsPanel = createShipsPanel();

        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(10));
        vbox.setAlignment(Pos.CENTER);
        vbox.getChildren().addAll(shipsPanel, opponentGrid, nameLayout, playerGrid);

        Button backButton = createButton("Back", backButtonHandler);
        vbox.getChildren().add(backButton);
        isMyTurn.set(true);
        return new Scene(vbox, 900, 900);
    }

    private void createAIGrid(ActionHandler actionHandler) {
        opponentGrid = new GridPane();
        opponentGrid.setVgap(1);
        opponentGrid.setHgap(1);
        opponentGrid.setAlignment(Pos.CENTER);

        for (int row = 0; row < GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE; col++) {
                Button squareButton = new Button();
                squareButton.setStyle("-fx-border-color: black; -fx-border-width: 1; -fx-background-color: lightblue;");
                squareButton.setMinSize(60, 30);
                squareButton.setUserData("empty");
                int finalRow = row;
                int finalCol = col;
                squareButton.setOnAction(e -> {
                    if (isMyTurn.get()) {
                        actionHandler.sendAIAttack(finalRow, finalCol, player.getOpponent(), playerGrid);
                        squareButton.setStyle("-fx-background-color: red;");
                        squareButton.setUserData("hit");
                        squareButton.setDisable(true);
                        isMyTurn.set(false);
                    }
                });
                opponentGrid.add(squareButton, col, row);
            }
        }
        serverHandler.setOpponentGrid(opponentGrid);
        serverHandler.setClientsTurn(isMyTurn);
    }
}
