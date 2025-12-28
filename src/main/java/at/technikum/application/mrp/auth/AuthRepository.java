package at.technikum.application.mrp.auth;

import at.technikum.application.mrp.database.DatabaseConnection;
import at.technikum.application.mrp.user.entity.UserEntity;

import java.sql.*;
import java.util.Optional;

public class AuthRepository {

    public void save(UserEntity user) {
        if (user.getId() == null) {
            // INSERT new user
            String sql = "INSERT INTO users (username, email, password_hash) VALUES (?, ?, ?) RETURNING user_id";
            try (Connection conn = DatabaseConnection.getInstance().getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setString(1, user.getUsername());
                stmt.setString(2, user.getEmail());
                stmt.setString(3, user.getPassword());

                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    user.setId(rs.getInt("user_id"));
                }
            } catch (SQLException e) {
                throw new RuntimeException("Failed to save user", e);
            }
        } else {
            // UPDATE existing user
            String sql = "UPDATE users SET username = ?, email = ?, password_hash = ?, updated_at = CURRENT_TIMESTAMP WHERE user_id = ?";
            try (Connection conn = DatabaseConnection.getInstance().getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setString(1, user.getUsername());
                stmt.setString(2, user.getEmail());
                stmt.setString(3, user.getPassword());
                stmt.setInt(4, user.getId());
                stmt.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException("Failed to update user", e);
            }
        }
    }

    public Optional<UserEntity> findByUsername(String username) {
        String sql = "SELECT user_id, username, email, password_hash, created_at, updated_at FROM users WHERE username = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                UserEntity user = new UserEntity();
                user.setId(rs.getInt("user_id"));
                user.setUsername(rs.getString("username"));
                user.setEmail(rs.getString("email"));
                user.setPassword(rs.getString("password_hash"));
                user.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                user.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
                return Optional.of(user);
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find user by username", e);
        }
    }

    public Optional<UserEntity> findById(int userId) {
        String sql = "SELECT user_id, username, email, password_hash, created_at, updated_at FROM users WHERE user_id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                UserEntity user = new UserEntity();
                user.setId(rs.getInt("user_id"));
                user.setUsername(rs.getString("username"));
                user.setEmail(rs.getString("email"));
                user.setPassword(rs.getString("password_hash"));
                user.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                user.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
                return Optional.of(user);
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find user by id", e);
        }
    }
}


