package Data;

public class Player {
    private int playerid;
    private String name;
    private int score;

    public Player(int playerid, String name, int score) {
        this.playerid = playerid;
        this.name = name;
        this.score = score;
    }
    public Player(String name, int score) {
        this.name = name;
        this.score = score;
    }

    public int getPlayerid() {
        return playerid;
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
}
