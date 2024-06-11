package Battleship;

import Data.Player;
import Database.GameDAO;
import Database.PlayerDAO;
import Database.TournamentDAO;
import Handler.ClientHandler;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class BattleshipServer {
    private static final int PORT = 12345;
    private static final int CORE_POOL_SIZE = 5;
    private static final PlayerDAO playerDAO;
    private static final GameDAO gameDAO;
    private static final TournamentDAO tournamentDAO;
    private static final Map<String, ClientHandler> connectedClients = new HashMap<>();

    static {
        try {
            playerDAO = new PlayerDAO("jdbc:sqlite:identifier.sqlite");
            gameDAO = new GameDAO("jdbc:sqlite:identifier.sqlite");
            tournamentDAO = new TournamentDAO("jdbc:sqlite:identifier.sqlite");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private final ThreadPoolExecutor executor;
    private final Map<String, List<String>> Tournaments = new HashMap<>();

    public BattleshipServer() {
        executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(CORE_POOL_SIZE);
    }

    public static void main(String[] args) {
        BattleshipServer server = new BattleshipServer();
        server.runServer();
    }

    public ClientHandler getClientHandler(String playerName) {
        return connectedClients.get(playerName);
    }

    public void runServer() {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server is listening on port " + PORT);
            retrieveTournaments();
            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("New client connected");
                executor.execute(new ClientHandler(socket, playerDAO, gameDAO, tournamentDAO, this));
            }
        } catch (IOException e) {
            System.out.println("Server exception: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void retrieveTournaments() {
        List<String> tournamentNames = tournamentDAO.getTournament();
        for (String tournamentName : tournamentNames) {
            int tournamentId = tournamentDAO.getTournamentId(tournamentName);
            try {
                List<Player> players = tournamentDAO.getTournamentPlayers(tournamentId);
                List<String> playerNames = new ArrayList<>();
                for (Player player : players) {
                    playerNames.add(player.getName());
                }
                System.out.println("Tournament: " + tournamentName + " Players: " + playerNames);
                Tournaments.put(tournamentName, playerNames);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public void setClientHandlerName(ClientHandler clientHandler, String name) {
        connectedClients.put(name, clientHandler);
    }

    public List<String> retrieveConnectedClients() {
        return new ArrayList<>(connectedClients.keySet());
    }

    public String getClientHandlerName(ClientHandler clientHandler) {
        for (Map.Entry<String, ClientHandler> entry : connectedClients.entrySet()) {
            if (entry.getValue() == clientHandler) {
                return entry.getKey();
            }
        }
        return null;
    }

    public void removeClientHandler(String name) {
        connectedClients.remove(name);
    }

    public Set<String> getConnectedClients() {
        System.out.println("Connected clients: " + connectedClients.keySet());
        return connectedClients.keySet();
    }

    public void addTournament(List<String> players, String tournamentName) throws SQLException {
        Tournaments.put(tournamentName, players);
    }

    public List<String> getTournaments() {
        List<String> tournamentList = new ArrayList<>();

        for (Map.Entry<String, List<String>> entry : Tournaments.entrySet()) {
            String tournamentName = entry.getKey();
            List<String> players = entry.getValue();

            String tournamentEntry = tournamentName + ":" + String.join(":", players);
            tournamentList.add(tournamentEntry);
        }

        return tournamentList;
    }
}
