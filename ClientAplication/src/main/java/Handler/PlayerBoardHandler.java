package Handler;

import Data.Player;
import javafx.scene.layout.GridPane;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class PlayerBoardHandler {
    List<String> players = new ArrayList<>();
    private GridPane playerGrid;
    private GridPane opponentGrid;
    private Player player;
    private AtomicBoolean isClientsTurn;
    private List<String> tournaments = new ArrayList<>();
    private Map<String, List<String>> playerTournament = new HashMap<>();

    public PlayerBoardHandler(GridPane playerGrid, GridPane opponentGrid) {
        this.playerGrid = playerGrid;
        this.opponentGrid = opponentGrid;
        this.isClientsTurn = new AtomicBoolean(true);
    }

    public List<String> getPlayers() {
        return players;
    }

    public void setPlayers(List<String> players) {
        this.players = players;
    }

    public void addPlayer(String player) {
        players.add(player);
    }

    public List<String> getTournaments() {
        return tournaments;
    }

    public void setTournaments(List<String> tournaments) {
        this.tournaments = tournaments;
    }

    public void addTournament(String tournament) {
        tournaments.add(tournament);
    }

    public AtomicBoolean getIsClientsTurn() {
        return isClientsTurn;
    }

    public void setIsClientsTurn(AtomicBoolean isClientsTurn) {
        this.isClientsTurn = isClientsTurn;
    }

    public GridPane getOpponentGrid() {
        return opponentGrid;
    }

    public void setOpponentGrid(GridPane opponentGrid) {
        this.opponentGrid = opponentGrid;
    }

    public String getOpponentName() {
        return player.getOpponent();
    }

    public void setOpponentName(String opponentName) {
        player.setOpponent(opponentName);
    }

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public GridPane getPlayerGrid() {
        return playerGrid;
    }

    public void setPlayerGrid(GridPane playerGrid) {
        this.playerGrid = playerGrid;
    }

    public void updateTurn(boolean isClientsTurn) {
        this.isClientsTurn.set(isClientsTurn);
    }

    public void setPlayerTournament(Map<String, List<String>> playerTournament) {
        this.playerTournament = playerTournament;
    }

    public void addPlayerToTournament(List<String> players, String tournament) {
        playerTournament.put(tournament, players);
    }

}
