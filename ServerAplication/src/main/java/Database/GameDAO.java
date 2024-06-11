package Database;

import java.sql.*;

public class GameDAO {
    private final Connection connection;

    public GameDAO(String dbUrl) throws SQLException {
        connection = DriverManager.getConnection(dbUrl);
    }

    public void insertGame(String player1, String player2, String winner) throws SQLException {
        String sql = "INSERT INTO Games (player1, player2, winner) VALUES (?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, player1);
            pstmt.setString(2, player2);
            pstmt.setString(3, winner);
            pstmt.executeUpdate();
        }
    }

    public int getNumberOfGamesWon(String player) throws SQLException {
        String sql = "SELECT COUNT(*) AS count FROM Games WHERE winner = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, player);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("count");
                }
            }
        }
        return 0;
    }

    public int getNumberOfGamesPlayed(String player) throws SQLException {
        String sql = "SELECT COUNT(*) AS count FROM Games WHERE player1 = ? OR player2 = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, player);
            pstmt.setString(2, player);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("count");
                }
            }
        }
        return 0;
    }
}


