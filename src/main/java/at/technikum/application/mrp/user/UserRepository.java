package at.technikum.application.mrp.user;

import at.technikum.application.mrp.auth.AuthTokenStore;
import at.technikum.application.mrp.database.DatabaseConnection;
import at.technikum.application.mrp.favorites.entity.FavoriteEntity;
import at.technikum.application.mrp.rating.entity.RatingEntity;
import at.technikum.application.mrp.user.entity.UserEntity;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserRepository {

    public Optional<UserEntity> findByToken(String token) {
        return AuthTokenStore.getUser(token);
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
}

