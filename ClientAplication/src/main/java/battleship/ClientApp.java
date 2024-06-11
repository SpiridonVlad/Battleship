package battleship;

import Connection.ConnectionManager;
import Data.Player;
import Handler.ActionHandler;
import Handler.ServerHandler;
import javafx.application.Application;
import javafx.scene.image.Image;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class ClientApp extends Application {
    public static final int GRID_SIZE = 10;
    private final Player player = new Player();
    private final AtomicBoolean isMyTurn = new AtomicBoolean(false);
    private final List<String> tournaments = new ArrayList<>();
    private final Map<String, List<String>> tournamentPlayers = new HashMap<>();
    ConnectionManager connectionManager = new ConnectionManager();
    GridPane opponentGrid = new GridPane();
    GridPane playerGrid = new GridPane();
    List<String> players = new ArrayList<>();
    MainMenu mainMenu;
    Settings settings;
    Information info;
    Login login;
    Register register;
    Game game;
    Tournament tournament;
    PlayerInfo playerStats;
    GameModes gameModes;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws IOException {
        connectionManager.connect("localhost", 12345);
        ActionHandler actionHandler = new ActionHandler(connectionManager);
        primaryStage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/Battleship.png"))));
        ServerHandler serverHandler = new ServerHandler(connectionManager, playerGrid, opponentGrid);
        serverHandler.setPlayer(player);
        serverHandler.setClientsTurn(isMyTurn);
        serverHandler.setTournaments(tournaments);
        serverHandler.setPlayersForTournamentInvite(players);
        mainMenu = new MainMenu(actionHandler, player);
        settings = new Settings(mainMenu);
        info = new Information(mainMenu);
        login = new Login(actionHandler, serverHandler, player, mainMenu);
        register = new Register(actionHandler, serverHandler, player, mainMenu);
        playerStats = new PlayerInfo(player, mainMenu);
        gameModes = new GameModes(actionHandler, serverHandler, player, playerStats, mainMenu);
        mainMenu.setLogin(login);
        mainMenu.setRegister(register);
        mainMenu.setSettings(settings);
        mainMenu.setInformation(info);
        mainMenu.setPlayerInfo(playerStats);
        mainMenu.setGameModes(gameModes);
        boolean isHorizontal = true;
        game = new Game(actionHandler, gameModes, isHorizontal, opponentGrid, player, playerGrid, serverHandler);
        gameModes.setGame(game);
        tournament = new Tournament(actionHandler, serverHandler, playerStats, gameModes, players, tournaments, tournamentPlayers, player, game);
        gameModes.setTournament(tournament);

        primaryStage.setScene(mainMenu.showScene(primaryStage));
        primaryStage.show();
    }


}
