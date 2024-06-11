package Data;

public class GameData {
    private int gamesPlayed;
    private int gamesWon;

    public GameData(int gamesPlayed, int gamesWon) {
        this.gamesPlayed = gamesPlayed;
        this.gamesWon = gamesWon;
    }

    public int getGamesPlayed() {
        return gamesPlayed;
    }

    public void raiseStats() {
        gamesPlayed++;
        gamesWon++;
    }

    public double getWinRate() {
        double winRate = (double) gamesWon / gamesPlayed * 100;
        return Double.parseDouble(String.format("%.2f", winRate));
    }

    public int getPoints() {
        return gamesWon;
    }
}
