package at.technikum.application.mrp.favorites;

import at.technikum.application.mrp.database.DatabaseConnection;
import at.technikum.application.mrp.favorites.entity.FavoriteEntity;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class FavoritesRepository {

    public FavoriteEntity create(FavoriteEntity entity) {
        String sql = "INSERT INTO favorites (user_id, media_id) VALUES (?, ?) RETURNING favorite_id";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, entity.getUserId());
            stmt.setInt(2, entity.getMediaId());

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                entity.setId(rs.getInt("favorite_id"));
            }
            return entity;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to create favorite", e);
        }
    }

    public boolean delete(int userId, int mediaId) {
        String sql = "DELETE FROM favorites WHERE user_id = ? AND media_id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            stmt.setInt(2, mediaId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete favorite", e);
        }
    }

    public List<FavoriteEntity> findByUserId(int userId) {
        String sql = "SELECT * FROM favorites WHERE user_id = ? ORDER BY created_at DESC";
        List<FavoriteEntity> results = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                results.add(mapResultSetToEntity(rs));
            }
            return results;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find favorites by user id", e);
        }
    }

    public List<FavoriteEntity> findByMediaId(int mediaId) {
        String sql = "SELECT * FROM favorites WHERE media_id = ? ORDER BY created_at DESC";
        List<FavoriteEntity> results = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, mediaId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                results.add(mapResultSetToEntity(rs));
            }
            return results;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find favorites by media id", e);
        }
    }

    public Optional<FavoriteEntity> findByUserIdAndMediaId(int userId, int mediaId) {
        String sql = "SELECT * FROM favorites WHERE user_id = ? AND media_id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            stmt.setInt(2, mediaId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return Optional.of(mapResultSetToEntity(rs));
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find favorite", e);
        }
    }

    public boolean exists(int userId, int mediaId) {
        return findByUserIdAndMediaId(userId, mediaId).isPresent();
    }

    private FavoriteEntity mapResultSetToEntity(ResultSet rs) throws SQLException {
        FavoriteEntity entity = new FavoriteEntity();
        entity.setId(rs.getInt("favorite_id"));
        entity.setUserId(rs.getInt("user_id"));
        entity.setMediaId(rs.getInt("media_id"));

        Timestamp created = rs.getTimestamp("created_at");
        if (created != null) {
            entity.setCreatedAt(created.toLocalDateTime());
        }

        return entity;
    }
}

