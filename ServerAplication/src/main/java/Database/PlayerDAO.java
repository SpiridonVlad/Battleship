package Database;

import Data.Player;

import java.sql.*;

public class PlayerDAO {
    private final Connection connection;

    public PlayerDAO(String dbUrl) throws SQLException {
        connection = DriverManager.getConnection(dbUrl);
    }

    public void addPlayer(Player player) throws SQLException {
        String sql = "INSERT INTO Players (playerid,name, score) VALUES (?,?,?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, getHighestPlayerId() + 1);
            stmt.setString(2, player.getName());
            stmt.setInt(3, player.getScore());
            stmt.executeUpdate();
        }
    }

    public int getHighestPlayerId() throws SQLException {
        String sql = "SELECT MAX(playerid) AS max FROM Players";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt("max");
            }
        }
        return 0;
    }

    public boolean doesPlayerExist(String name) throws SQLException {
        String sql = "SELECT COUNT(*) AS count FROM Players WHERE name = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, name);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                int count = rs.getInt("count");
                return count > 0;
            }
        }
        return false;
    }

    public void close() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }

    public int returnPlayerScore(String playerName) {
        String sql = "SELECT score FROM Players WHERE name = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, playerName);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("score");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public void updatePlayerScore(String playerName) {
        String sql = "UPDATE Players SET score = score + 1 WHERE name = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, playerName);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public int getPlayerId(String playerName) {
        String sql = "SELECT playerid FROM Players WHERE name = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, playerName);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("playerid");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public String getAllPlayers() {
        String sql = "SELECT name FROM Players";
        StringBuilder players = new StringBuilder();
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                players.append(rs.getString("name")).append(":");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return players.toString();
    }
}
