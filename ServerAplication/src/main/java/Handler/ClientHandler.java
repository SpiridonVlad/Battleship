package Handler;

import Battleship.BattleshipServer;
import Data.Player;
import Database.GameDAO;
import Database.PlayerDAO;
import Database.TournamentDAO;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class ClientHandler extends Thread {
    private static final int TABLE_SIZE = 10;
    private static final int SHIP_SIZE = 3;
    private final Socket socket;
    private final PlayerDAO playerDAO;
    private final GameDAO gameDAO;
    private final TournamentDAO tournamentDAO;
    private final BattleshipServer server;
    List<List<Boolean>> robotTable = new ArrayList<>();
    private String shipPositions = "Ships";
    private boolean running = true;

    public ClientHandler(Socket socket, PlayerDAO playerDAO, GameDAO gameDAO, TournamentDAO tournamentDAO, BattleshipServer server) {
        this.socket = socket;
        this.playerDAO = playerDAO;
        this.gameDAO = gameDAO;
        this.tournamentDAO = tournamentDAO;
        this.server = server;
    }

    public void run() {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter writer = new PrintWriter(socket.getOutputStream(), true)) {
            String text;
            while (running) {
                text = reader.readLine();
                System.out.println("Received from client: " + text);
                String[] parts = text.split(":");
                String action = parts[0];
                System.out.println("Action: " + action);
                switch (action) {
                    case "InviteGame":
                        handleInviteGame(parts);
                        System.out.println("Inviting player to game");
                        break;
                    case "InvitedGame":
                        handleInvitedToGame(parts, writer);
                        System.out.println("Invited to game");
                        break;
                    case "GetPlayers":
                        handleGetPlayers(writer);
                        break;
                    case "Login":
                        handleLogin(parts, writer);
                        break;
                    case "Register":
                        handleRegister(parts, writer);
                        break;
                    case "Attack-OP":
                        handleAttackOp(parts, writer);
                        break;
                    case "Attack-ME":
                        handleAttackMe(parts, writer);
                        break;
                    case "GameWon":
                        handleGameWon(parts);
                        break;
                    case "Logout":
                        System.out.println("Logging out: " + parts[1]);
                        server.removeClientHandler(parts[1]);
                    case "Ships":
                        handleShips(parts);
                        break;
                    case "RequestShips":
                        handleSendShips(parts, writer);
                        break;
                    case "TurnFinished":
                        handleTurnFinished(parts);
                        break;
                    case "ExitGame":
                        handleExit(writer);
                        break;
                    case "Tournament":
                        handleTournament(parts);
                        break;
                    case "GetTournaments":
                        handleGetTournaments(writer);
                        break;
                    case "GetAllPlayers":
                        handleAllPlayers(writer);
                        break;
                    case "AI":
                        createRobotTable();
                        handleAi(parts, writer);
                        break;
                    default:
                        System.out.println("Unknown command: " + action);
                        writer.println("Unknown command");
                        break;
                }
            }

        } catch (IOException ex) {
            System.out.println("Server exception: " + ex.getMessage());
            ex.printStackTrace();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void handleAi(String[] parts, PrintWriter writer) {
        if (parts.length >= 3) {
            try {
                int row = Integer.parseInt(parts[1]);
                int col = Integer.parseInt(parts[2]);

                if (isValidPosition(row, col)) {
                    if (robotTable.get(row).get(col)) {
                        writer.println("Hit:" + row + ":" + col);
                        robotTable.get(row).set(col, false);

                        if (!hasShipsLeft()) {
                            writer.println("GameWon");
                        }
                    } else {
                        writer.println("Miss:" + row + ":" + col + ":");
                    }
                } else {
                    writer.println("Invalid position");
                }
                sendAttackToOpponent(writer);
            } catch (NumberFormatException e) {
                writer.println("Invalid input");
            }
        }
    }

    private boolean hasShipsLeft() {
        for (List<Boolean> row : robotTable) {
            for (Boolean cell : row) {
                if (cell) {
                    return true;
                }
            }
        }
        createRobotTable();
        return false;
    }

    private void sendAttackToOpponent(PrintWriter writer) {
        Random random = new Random();
        int row = random.nextInt(TABLE_SIZE);
        int col = random.nextInt(TABLE_SIZE);
        writer.println("Attack-ME:" + row + ":" + col);
        writer.println("StartTurn");
    }

    private boolean isValidPosition(int row, int col) {
        return row >= 0 && row < TABLE_SIZE && col >= 0 && col < TABLE_SIZE;
    }

    private void createRobotTable() {
        for (int i = 0; i < TABLE_SIZE; i++) {
            List<Boolean> row = new ArrayList<>();
            for (int j = 0; j < TABLE_SIZE; j++) {
                row.add(false);
            }
            robotTable.add(row);
        }

        Random random = new Random();
        int numberOfShips = 5;

        while (numberOfShips > 0) {
            boolean horizontal = random.nextBoolean();
            int x = random.nextInt(TABLE_SIZE);
            int y = random.nextInt(TABLE_SIZE);

            if (horizontal) {
                if (y + SHIP_SIZE <= TABLE_SIZE && canPlaceShip(x, y, horizontal)) {
                    for (int j = 0; j < SHIP_SIZE; j++) {
                        robotTable.get(x).set(y + j, true);
                    }
                    numberOfShips--;
                }
            } else {
                if (x + SHIP_SIZE <= TABLE_SIZE && canPlaceShip(x, y, horizontal)) {
                    for (int i = 0; i < SHIP_SIZE; i++) {
                        robotTable.get(x + i).set(y, true);
                    }
                    numberOfShips--;
                }
            }
        }
    }

    private boolean canPlaceShip(int x, int y, boolean horizontal) {
        for (int i = 0; i < SHIP_SIZE; i++) {
            if (horizontal) {
                if (robotTable.get(x).get(y + i)) {
                    return false;
                }
            } else {
                if (robotTable.get(x + i).get(y)) {
                    return false;
                }
            }
        }
        return true;
    }

    private void handleAllPlayers(PrintWriter writer) {
        List<String> players;
        players = Collections.singletonList(playerDAO.getAllPlayers());
        StringBuilder allPlayers = new StringBuilder("AllPlayers:");
        for (String player : players) {
            allPlayers.append(player);
        }
        System.out.println(allPlayers);
        writer.println(allPlayers);
    }

    private void handleGetTournaments(PrintWriter writer) {
        StringBuilder tournaments = new StringBuilder("Tournaments:");
        List<String> tournamentList = server.getTournaments();

        for (String tournament : tournamentList) {
            tournaments.append(tournament).append("|");
        }

        if (tournaments.length() > 12) {
            tournaments.setLength(tournaments.length() - 1);
        }

        writer.println(tournaments.toString());
    }

    private void handleTournament(String[] parts) throws SQLException {
        List<String> players = new ArrayList<>();
        List<Integer> playerId = new ArrayList<>();
        String tournamentName = parts[1];
        for (int i = 2; i < parts.length; i++) {
            playerId.add(playerDAO.getPlayerId(parts[i]));
            players.add(parts[i]);
        }
        tournamentDAO.addTournament(tournamentName);
        int tournamentId = tournamentDAO.getTournamentId(tournamentName);
        for (int i = 0; i < players.size(); i++) {
            tournamentDAO.addPlayerToTournament(tournamentId, playerId.get(i));
        }
        server.addTournament(players, tournamentName);
    }

    private void handleExit(PrintWriter writer) {
        running = false;
        writer.println("ExitGame");
        server.removeClientHandler(server.getClientHandlerName(this));
        try {
            socket.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void handleInvitedToGame(String[] parts, PrintWriter writer) {
        String opponent = parts[1];
        writer.println("InvitedGame:" + opponent);
    }

    private void handleInviteGame(String[] parts) {
        forwardMessage(parts[1], "InvitedGame:" + parts[2]);
    }

    private void handleTurnFinished(String[] parts) throws IOException {
        ClientHandler recipientHandler = server.getClientHandler(parts[1]);
        if (recipientHandler != null) {
            PrintWriter writer = new PrintWriter(recipientHandler.socket.getOutputStream(), true);
            writer.println("StartTurn");
        } else {
            System.out.println("Recipient not found: " + parts[1]);
        }
    }

    private void handleSendShips(String[] parts, PrintWriter writer) {
        ClientHandler recipientHandler = server.getClientHandler(parts[1]);
        if (recipientHandler != null) {
            writer.println(recipientHandler.getShipPositions());
        } else {
            System.out.println("Recipient not found: " + parts[1]);
        }
    }

    private void handleGameWon(String[] parts) {
        String opponentName = parts[1];
        try {
            String player1 = server.getClientHandlerName(this);
            gameDAO.insertGame(player1, opponentName, opponentName);
            playerDAO.updatePlayerScore(opponentName);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        forwardMessage(opponentName, "GameWon");
    }

    private void handleGetPlayers(PrintWriter writer) {
        try {
            writer.println("Players:" + getPlayerList());
        } catch (SQLException e) {
            writer.println("Error fetching players");
            e.printStackTrace();
        }
    }

    private void handleLogin(String[] parts, PrintWriter writer) {
        if (parts.length >= 3) {
            String username = parts[1];
            try {
                if (playerDAO.doesPlayerExist(username)) {
                    if (!server.getConnectedClients().contains(username)) {
                        server.setClientHandlerName(this, username);
                        writer.println("Login:" + username + ":" + playerDAO.returnPlayerScore(username) + ":" + gameDAO.getNumberOfGamesPlayed(username) + ":" + gameDAO.getNumberOfGamesWon(username));
                    } else {
                        writer.println("User already logged in");
                    }
                } else {
                    writer.println("User does not exist");
                }
            } catch (SQLException e) {
                writer.println("Error during login");
                e.printStackTrace();
            }
        } else {
            writer.println("Missing username or password");
        }
    }

    private void handleRegister(String[] parts, PrintWriter writer) {
        if (parts.length >= 3) {
            String username = parts[1];
            String password = parts[2];
            try {
                if (playerDAO.doesPlayerExist(username)) {
                    writer.println("User already exists");
                    return;
                }
                playerDAO.addPlayer(new Player(username, 0));
                server.setClientHandlerName(this, username);
                writer.println("Register:" + username + ":password: " + password);
            } catch (SQLException e) {
                writer.println("Error during registration");
                e.printStackTrace();
            }
        } else {
            writer.println("Missing username or password");
        }
    }

    private void handleAttackOp(String[] parts, PrintWriter writer) {
        if (parts.length >= 4) {
            String row = parts[1];
            String col = parts[2];
            String opponent = parts[3];
            forwardMessage(opponent, "Attack-ME:" + row + ":" + col);
        } else {
            writer.println("Missing position or opponent");
        }
    }

    private void handleAttackMe(String[] parts, PrintWriter writer) {
        if (parts.length >= 3) {
            String row = parts[1];
            String col = parts[2];
            writer.println("Attack-Me: " + row + ":" + col);
        } else {
            writer.println("Missing position");
        }
    }

    private void forwardMessage(String recipient, String message) {
        ClientHandler recipientHandler = server.getClientHandler(recipient);
        System.out.println("Recipient: " + recipient + " Handler: " + recipientHandler);
        if (recipientHandler != null) {
            try {
                PrintWriter writer = new PrintWriter(recipientHandler.socket.getOutputStream(), true);
                System.out.println("Forwarding message to " + recipient + ": " + message);
                writer.println(message);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("Recipient not found: " + recipient);
        }
    }

    private String getPlayerList() throws SQLException {
        StringBuilder players = new StringBuilder();
        for (String player : server.retrieveConnectedClients()) {
            players.append(player).append(",");
        }
        return players.toString();
    }

    public String getShipPositions() {
        return shipPositions;
    }

    private void handleShips(String[] parts) {
        StringBuilder message = new StringBuilder("Ships:");
        if (parts.length >= 2) {
            for (int i = 1; i < parts.length - 1; i = i + 2) {
                String row = parts[i];
                String col = parts[i + 1];
                message.append(row).append(":").append(col).append(":");
            }
        }
        if (message.length() > 1)
            shipPositions = message.toString();
        System.out.println("Ship positions: " + shipPositions);
    }

}
