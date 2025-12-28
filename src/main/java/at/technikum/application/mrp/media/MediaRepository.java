package at.technikum.application.mrp.media;

import at.technikum.application.mrp.database.DatabaseConnection;
import at.technikum.application.mrp.media.entity.MediaEntryEntity;

import java.sql.*;
import java.util.*;

public class MediaRepository {

    public MediaEntryEntity create(MediaEntryEntity entity) {
        String sql = """
            INSERT INTO media (title, description, genre, media_type, release_year, 
                               director, cast_members, creator_user_id, age_restriction)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?) RETURNING media_id
            """;

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, entity.getTitle());
            stmt.setString(2, entity.getDescription());
            stmt.setString(3, entity.getGenres() != null ? String.join(",", entity.getGenres()) : null);
            stmt.setString(4, entity.getMediaType());
            stmt.setObject(5, entity.getReleaseYear(), Types.INTEGER);
            stmt.setString(6, null); // director - not in current entity
            stmt.setString(7, null); // cast_members - not in current entity
            stmt.setObject(8, entity.getCreatorUserId(), Types.INTEGER);
            stmt.setObject(9, entity.getAgeRestriction(), Types.INTEGER);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                entity.setId(rs.getInt("media_id"));
            }
            return entity;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to create media entry", e);
        }
    }

    public Optional<MediaEntryEntity> findById(int id) {
        String sql = "SELECT * FROM media WHERE media_id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return Optional.of(mapResultSetToEntity(rs));
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find media by id", e);
        }
    }

    public MediaEntryEntity update(MediaEntryEntity entity) {
        if (entity.getId() == null) {
            return null;
        }

        String sql = """
            UPDATE media SET title = ?, description = ?, genre = ?, media_type = ?,
                           release_year = ?, age_restriction = ?, updated_at = CURRENT_TIMESTAMP
            WHERE media_id = ?
            """;

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, entity.getTitle());
            stmt.setString(2, entity.getDescription());
            stmt.setString(3, entity.getGenres() != null ? String.join(",", entity.getGenres()) : null);
            stmt.setString(4, entity.getMediaType());
            stmt.setObject(5, entity.getReleaseYear(), Types.INTEGER);
            stmt.setObject(6, entity.getAgeRestriction(), Types.INTEGER);
            stmt.setInt(7, entity.getId());

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0 ? entity : null;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update media entry", e);
        }
    }

    public boolean delete(int id) {
        String sql = "DELETE FROM media WHERE media_id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete media entry", e);
        }
    }

    public Collection<MediaEntryEntity> findAll() {
        String sql = "SELECT * FROM media ORDER BY media_id";
        List<MediaEntryEntity> results = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                results.add(mapResultSetToEntity(rs));
            }
            return results;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find all media", e);
        }
    }

    public List<MediaEntryEntity> search(String title, String genre, String mediaType,
                                         Integer releaseYear, Integer ageRestriction,
                                         Integer minAverageRating, String sortBy) {
        StringBuilder sql = new StringBuilder("SELECT m.* FROM media m");

        if (minAverageRating != null) {
            sql.append(" LEFT JOIN (SELECT media_id, AVG(rating_value) as avg_rating FROM ratings GROUP BY media_id) r ON m.media_id = r.media_id");
        }

        List<String> conditions = new ArrayList<>();
        List<Object> params = new ArrayList<>();

        if (title != null && !title.isBlank()) {
            conditions.add("LOWER(m.title) LIKE ?");
            params.add("%" + title.toLowerCase() + "%");
        }
        if (genre != null && !genre.isBlank()) {
            conditions.add("LOWER(m.genre) LIKE ?");
            params.add("%" + genre.toLowerCase() + "%");
        }
        if (mediaType != null && !mediaType.isBlank()) {
            conditions.add("LOWER(m.media_type) = ?");
            params.add(mediaType.toLowerCase());
        }
        if (releaseYear != null) {
            conditions.add("m.release_year = ?");
            params.add(releaseYear);
        }
        if (ageRestriction != null) {
            conditions.add("m.age_restriction = ?");
            params.add(ageRestriction);
        }
        if (minAverageRating != null) {
            conditions.add("r.avg_rating >= ?");
            params.add(minAverageRating);
        }

        if (!conditions.isEmpty()) {
            sql.append(" WHERE ").append(String.join(" AND ", conditions));
        }

        // Sorting
        if (sortBy != null && !sortBy.isBlank()) {
            switch (sortBy.toLowerCase()) {
                case "title" -> sql.append(" ORDER BY m.title");
                case "score" -> sql.append(" ORDER BY r.avg_rating DESC NULLS LAST");
                case "year" -> sql.append(" ORDER BY m.release_year");
                default -> sql.append(" ORDER BY m.media_id");
            }
        } else {
            sql.append(" ORDER BY m.media_id");
        }

        List<MediaEntryEntity> results = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                results.add(mapResultSetToEntity(rs));
            }
            return results;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to search media", e);
        }
    }

    private MediaEntryEntity mapResultSetToEntity(ResultSet rs) throws SQLException {
        MediaEntryEntity entity = new MediaEntryEntity();
        entity.setId(rs.getInt("media_id"));
        entity.setTitle(rs.getString("title"));
        entity.setDescription(rs.getString("description"));

        String genreStr = rs.getString("genre");
        if (genreStr != null && !genreStr.isBlank()) {
            entity.setGenres(Arrays.asList(genreStr.split(",")));
        }

        entity.setMediaType(rs.getString("media_type"));
        entity.setReleaseYear((Integer) rs.getObject("release_year"));
        entity.setCreatorUserId((Integer) rs.getObject("creator_user_id"));
        entity.setAgeRestriction((Integer) rs.getObject("age_restriction"));

        return entity;
    }
}

