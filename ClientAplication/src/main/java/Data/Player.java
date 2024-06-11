package Data;

import java.util.ArrayList;
import java.util.List;

public class Player {
    private final List<String> games = new ArrayList<>();
    private String name;
    private int score;
    private boolean logged = false;
    private String opponent;
    private GameData gameData;

    public GameData getGameData() {
        return gameData;
    }

    public void setGameData(GameData gameData) {
        this.gameData = gameData;
    }

    public List<String> getGames() {
        return games;
    }

    public void addGame(String game) {
        games.add(game);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public boolean getLogged() {
        return logged;
    }

    public void setLogged(boolean logged) {
        this.logged = logged;
    }

    public String getOpponent() {
        return opponent;
    }

    public void setOpponent(String opponent) {
        this.opponent = opponent;
    }

    public void updateScore() {
        this.score++;
    }

    public void updateGameData() {
        gameData.raiseStats();
    }

    public String getGamesPlayed() {
        return String.valueOf(gameData.getGamesPlayed());
    }

    public String getWinLossRatio() {
        return gameData.getWinRate() + "%";
    }

    public void resetPlayer() {
        this.name = null;
        this.score = 0;
        this.logged = false;
        this.opponent = null;
        this.gameData = null;
        this.games.clear();
    }

    public int getPoints() {
        return gameData.getPoints();
    }
}
