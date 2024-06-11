package Handler;

import Connection.ConnectionManager;
import javafx.scene.control.Button;
import javafx.scene.layout.GridPane;

import java.util.List;


public class ActionHandler {
    private final ConnectionManager connectionManager;

    public ActionHandler(ConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    private static StringBuilder getAllShips(GridPane playerGrid) {
        StringBuilder message = new StringBuilder("Ships:");
        for (javafx.scene.Node node : playerGrid.getChildren()) {
            if (node instanceof Button squareButton) {
                Object userData = squareButton.getUserData();
                if ("ship".equals(userData)) {
                    int row = GridPane.getRowIndex(node);
                    int col = GridPane.getColumnIndex(node);
                    message.append(row).append(":").append(col).append(":");
                }
            }
        }
        return message;
    }

    public void sendAttack(int row, int col, String opponent, GridPane playerGrid) {
        requestShips(opponent);
        if (shipsExists(playerGrid) || isGameOver(playerGrid)) {
            String message = "Attack-OP:" + row + ":" + col + ":" + opponent;
            connectionManager.sendMessage(message);
            turnFinished(opponent);
        }
    }

    public boolean shipsExists(GridPane playerGrid) {
        int shipCount = 0;
        for (javafx.scene.Node node : playerGrid.getChildren()) {
            if (node instanceof Button squareButton) {
                Object userData = squareButton.getUserData();
                if ("ship".equals(userData)) {
                    shipCount++;
                    if (shipCount >= 15) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public boolean isGameOver(GridPane playerGrid) {
        for (javafx.scene.Node node : playerGrid.getChildren()) {
            if (node instanceof Button squareButton) {
                Object userData = squareButton.getUserData();
                if ("ship".equals(userData)) {
                    return true;
                }
            }
        }
        return false;
    }

    public void fetchPlayers() {
        connectionManager.sendMessage("GetPlayers");
    }

    public void login(String username, String password) {
        connectionManager.sendMessage("Login:" + username + ":" + password);
    }

    public void register(String username, String password) {
        connectionManager.sendMessage("Register:" + username + ":" + password);
    }

    public void logout(String username) {
        connectionManager.sendMessage("Logout:" + username);
    }

    public void sendShips(GridPane playerGrid) {
        StringBuilder message = getAllShips(playerGrid);
        connectionManager.sendMessage(message.toString());
    }

    public void turnFinished(String username) {
        connectionManager.sendMessage("TurnFinished:" + username);
    }

    public void requestShips(String opponent) {
        connectionManager.sendMessage("RequestShips:" + opponent);
    }

    public void inviteToGame(String opponent, String username) {
        connectionManager.sendMessage("InviteGame:" + opponent + ":" + username);
    }

    public void exitGame() {
        connectionManager.sendMessage("ExitGame");
    }

    public void createTournament(String tournamentName, List<String> selectedPlayers) {
        StringBuilder message = new StringBuilder("Tournament:" + tournamentName + ":");
        for (String player : selectedPlayers) {
            message.append(player).append(":");
        }
        connectionManager.sendMessage(message.toString());
    }

    public void requestTournaments() {
        connectionManager.sendMessage("GetTournaments");
    }

    public void requestAllPlayers() {
        connectionManager.sendMessage("GetAllPlayers");
    }

    public void sendAIAttack(int finalRow, int finalCol, String opponent, GridPane playerGrid) {
        if (shipsExists(playerGrid) || isGameOver(playerGrid)) {
            String message = "AI:" + finalRow + ":" + finalCol;
            connectionManager.sendMessage(message);
        }
    }
}
