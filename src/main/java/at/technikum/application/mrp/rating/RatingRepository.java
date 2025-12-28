package at.technikum.application.mrp.rating;

import at.technikum.application.mrp.database.DatabaseConnection;
import at.technikum.application.mrp.rating.entity.RatingEntity;

import java.sql.*;
import java.util.*;

public class RatingRepository {

    public RatingEntity create(RatingEntity e) {
        String sql = """
            INSERT INTO ratings (user_id, media_id, rating_value, comment, confirmed)
            VALUES (?, ?, ?, ?, ?) RETURNING rating_id
            """;

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, e.getUserId());
            stmt.setInt(2, e.getMediaId());
            stmt.setInt(3, e.getScore());
            stmt.setString(4, e.getComment());
            stmt.setBoolean(5, e.isConfirmed());

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                e.setId(rs.getInt("rating_id"));
            }
            return e;
        } catch (SQLException ex) {
            throw new RuntimeException("Failed to create rating", ex);
        }
    }

    public Optional<RatingEntity> findById(int id) {
        String sql = "SELECT * FROM ratings WHERE rating_id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return Optional.of(mapResultSetToEntity(rs));
            }
            return Optional.empty();
        } catch (SQLException ex) {
            throw new RuntimeException("Failed to find rating by id", ex);
        }
    }

    public RatingEntity update(RatingEntity e) {
        if (e.getId() == null) {
            return null;
        }

        String sql = """
            UPDATE ratings SET rating_value = ?, comment = ?, confirmed = ?,
                             updated_at = CURRENT_TIMESTAMP
            WHERE rating_id = ?
            """;

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, e.getScore());
            stmt.setString(2, e.getComment());
            stmt.setBoolean(3, e.isConfirmed());
            stmt.setInt(4, e.getId());

            int rowsAffected = stmt.executeUpdate();

            // Update likes in database
            if (rowsAffected > 0) {
                updateLikes(e.getId(), e.getLikedByUserIds());
            }

            return rowsAffected > 0 ? e : null;
        } catch (SQLException ex) {
            throw new RuntimeException("Failed to update rating", ex);
        }
    }

    private void updateLikes(int ratingId, Set<Integer> likedByUserIds) {
        // Remove all existing likes
        String deleteSql = "DELETE FROM rating_likes WHERE rating_id = ?";
        // Insert new likes
        String insertSql = "INSERT INTO rating_likes (rating_id, user_id) VALUES (?, ?) ON CONFLICT DO NOTHING";

        try (Connection conn = DatabaseConnection.getInstance().getConnection()) {
            // Delete existing likes
            try (PreparedStatement deleteStmt = conn.prepareStatement(deleteSql)) {
                deleteStmt.setInt(1, ratingId);
                deleteStmt.executeUpdate();
            }

            // Insert new likes
            if (likedByUserIds != null && !likedByUserIds.isEmpty()) {
                try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                    for (Integer userId : likedByUserIds) {
                        insertStmt.setInt(1, ratingId);
                        insertStmt.setInt(2, userId);
                        insertStmt.addBatch();
                    }
                    insertStmt.executeBatch();
                }
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Failed to update likes", ex);
        }
    }

    private Set<Integer> loadLikes(int ratingId) {
        String sql = "SELECT user_id FROM rating_likes WHERE rating_id = ?";
        Set<Integer> likes = new HashSet<>();

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, ratingId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                likes.add(rs.getInt("user_id"));
            }
            return likes;
        } catch (SQLException ex) {
            throw new RuntimeException("Failed to load likes", ex);
        }
    }

    public boolean delete(int id) {
        // Likes will be automatically deleted due to ON DELETE CASCADE in the database
        String sql = "DELETE FROM ratings WHERE rating_id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete rating", e);
        }
    }

    public List<RatingEntity> findByMediaId(int mediaId) {
        String sql = "SELECT * FROM ratings WHERE media_id = ? ORDER BY created_at DESC";
        List<RatingEntity> results = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, mediaId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                results.add(mapResultSetToEntity(rs));
            }
            return results;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find ratings by media id", e);
        }
    }

    public List<RatingEntity> findByUserId(int userId) {
        String sql = "SELECT * FROM ratings WHERE user_id = ? ORDER BY created_at DESC";
        List<RatingEntity> results = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                results.add(mapResultSetToEntity(rs));
            }
            return results;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find ratings by user id", e);
        }
    }

    public Optional<RatingEntity> findByUserIdAndMediaId(int userId, int mediaId) {
        String sql = "SELECT * FROM ratings WHERE user_id = ? AND media_id = ?";
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
            throw new RuntimeException("Failed to find rating by user and media", e);
        }
    }

    private RatingEntity mapResultSetToEntity(ResultSet rs) throws SQLException {
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

        // Load likes from database
        entity.setLikedByUserIds(loadLikes(entity.getId()));

        return entity;
    }
}

