package at.technikum.application.mrp.leaderboard;

import at.technikum.application.mrp.database.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LeaderboardRepository {

    /**
     * Get top users by rating count
     * @param limit Maximum number of results to return
     * @param offset Number of results to skip
     * @return List of user statistics
     */
    public List<Map<String, Object>> getTopUsersByRatings(int limit, int offset) {
        String sql = """
            SELECT
                u.user_id,
                u.username,
                COUNT(r.rating_id) as rating_count,
                COUNT(DISTINCT r.media_id) as media_rated_count,
                AVG(r.rating_value) as avg_rating_given
            FROM users u
            LEFT JOIN ratings r ON u.user_id = r.user_id
            GROUP BY u.user_id, u.username
            ORDER BY rating_count DESC, u.username ASC
            LIMIT ? OFFSET ?
            """;

        List<Map<String, Object>> results = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, limit);
            stmt.setInt(2, offset);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Map<String, Object> entry = new HashMap<>();
                entry.put("userId", rs.getInt("user_id"));
                entry.put("username", rs.getString("username"));
                entry.put("ratingCount", rs.getInt("rating_count"));
                entry.put("mediaRatedCount", rs.getInt("media_rated_count"));

                // Handle AVG which can be NULL
                double avgRating = rs.getDouble("avg_rating_given");
                if (rs.wasNull()) {
                    entry.put("avgRatingGiven", 0.0);
                } else {
                    entry.put("avgRatingGiven", Math.round(avgRating * 100.0) / 100.0); // Round to 2 decimals
                }

                results.add(entry);
            }
            return results;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get leaderboard", e);
        }
    }
}
