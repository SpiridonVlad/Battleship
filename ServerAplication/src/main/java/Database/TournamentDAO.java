package Database;

import Data.Player;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TournamentDAO {
    private final Connection connection;

    public TournamentDAO(String dbUrl) throws SQLException {
        connection = DriverManager.getConnection(dbUrl);
    }

    public List<Player> getTournamentPlayers(int tournamentId) throws SQLException {
        String query = """
            SELECT p.PlayerID, p.Name
            FROM Players p
            JOIN TournamentPlayers tp ON p.PlayerID = tp.PlayerID
            WHERE tp.TournamentID = ?
            """;
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, tournamentId);
            ResultSet rs = stmt.executeQuery();
            List<Player> players = new ArrayList<>();
            while (rs.next()) {
                Player player = new Player(rs.getString("Name"), rs.getInt("PlayerID"));
                players.add(player);
            }
            return players;
        }
    }

    public int addTournament(String tournamentName) throws SQLException {
        String query = "INSERT INTO Tournament (TournamentName) VALUES (?)";
        try (PreparedStatement stmt = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, tournamentName);
            stmt.executeUpdate();
            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            } else {
                throw new SQLException("Creating tournament failed, no ID obtained.");
            }
        }
    }

    public void addPlayerToTournament(int tournamentId, int playerId) throws SQLException {
        String query = "INSERT INTO TournamentPlayers (TournamentID, PlayerID) VALUES (?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, tournamentId);
            stmt.setInt(2, playerId);
            stmt.executeUpdate();
        }
    }

    public void removePlayerFromTournament(int tournamentId, int playerId) throws SQLException {
        String query = "DELETE FROM TournamentPlayers WHERE TournamentID = ? AND PlayerID = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, tournamentId);
            stmt.setInt(2, playerId);
            stmt.executeUpdate();
        }
    }

    public void close() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }

    public int getTournamentId(String tournamentName) {
        String query = "SELECT TournamentID FROM Tournament WHERE TournamentName = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, tournamentName);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("TournamentID");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public List<String> getTournament() {
        String query = "SELECT TournamentName FROM Tournament";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            ResultSet rs = stmt.executeQuery();
            List<String> tournaments = new ArrayList<>();
            while (rs.next()) {
                tournaments.add(rs.getString("TournamentName"));
            }
            return tournaments;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
