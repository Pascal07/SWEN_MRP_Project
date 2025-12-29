package at.technikum.application.mrp.user;

import at.technikum.application.mrp.auth.AuthService;
import at.technikum.application.mrp.database.DatabaseConnection;
import at.technikum.application.mrp.favorites.entity.FavoriteEntity;
import at.technikum.application.mrp.rating.entity.RatingEntity;
import at.technikum.application.mrp.user.entity.UserEntity;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserRepository {
    private final AuthService authService;

    public UserRepository(AuthService authService) {
        this.authService = authService;
    }

    public Optional<UserEntity> findByToken(String token) {
        return authService.getUserByToken(token);
    }

    public List<RatingEntity> findRatingsByUserId(Integer userId) {
        String sql = "SELECT * FROM ratings WHERE user_id = ? ORDER BY created_at DESC";
        List<RatingEntity> results = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                RatingEntity entity = new RatingEntity();
                entity.setId(rs.getInt("rating_id"));
                entity.setUserId(rs.getInt("user_id"));
                entity.setMediaId(rs.getInt("media_id"));
                entity.setScore(rs.getInt("rating_value"));
                entity.setComment(rs.getString("comment"));
                entity.setConfirmed(rs.getBoolean("confirmed"));

                Timestamp created = rs.getTimestamp("created_at");
                if (created != null) {
                    entity.setTimestamp(created.getTime());
                }

                results.add(entity);
            }
            return results;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find ratings by user id", e);
        }
    }

    public List<FavoriteEntity> findFavoritesByUserId(Integer userId) {
        String sql = "SELECT * FROM favorites WHERE user_id = ? ORDER BY created_at DESC";
        List<FavoriteEntity> results = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                FavoriteEntity entity = new FavoriteEntity();
                entity.setId(rs.getInt("favorite_id"));
                entity.setUserId(rs.getInt("user_id"));
                entity.setMediaId(rs.getInt("media_id"));

                Timestamp created = rs.getTimestamp("created_at");
                if (created != null) {
                    entity.setCreatedAt(created.toLocalDateTime());
                }

                results.add(entity);
            }
            return results;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find favorites by user id", e);
        }
    }

    public boolean updateProfile(Integer userId, String email) {
        String sql = "UPDATE users SET email = ?, updated_at = CURRENT_TIMESTAMP WHERE user_id = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);
            stmt.setInt(2, userId);

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update user profile", e);
        }
    }
}

