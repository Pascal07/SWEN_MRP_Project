package at.technikum.application.mrp.database;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseInitializer {

    public static void initializeDatabase() {
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement()) {

            System.out.println("Checking database schema...");

            // Check if tables already exist
            boolean tablesExist = checkIfTablesExist(conn);

            if (tablesExist) {
                System.out.println("✓ Database tables already exist - no changes needed");
                System.out.println("✓ Existing data will be preserved");
            } else {
                System.out.println("→ Creating database tables for the first time...");
            }

            // Create USERS table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS users (
                    user_id SERIAL PRIMARY KEY,
                    username VARCHAR(255) UNIQUE NOT NULL,
                    email VARCHAR(255),
                    password_hash VARCHAR(255) NOT NULL,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
                """);

            // Create MEDIA table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS media (
                    media_id SERIAL PRIMARY KEY,
                    title VARCHAR(255) NOT NULL,
                    description TEXT,
                    genre TEXT,
                    media_type VARCHAR(50),
                    release_year INTEGER,
                    director VARCHAR(255),
                    cast_members TEXT,
                    creator_user_id INTEGER,
                    age_restriction INTEGER,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
                """);

            // Create RATINGS table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS ratings (
                    rating_id SERIAL PRIMARY KEY,
                    user_id INTEGER NOT NULL,
                    media_id INTEGER NOT NULL,
                    rating_value INTEGER NOT NULL CHECK (rating_value >= 1 AND rating_value <= 5),
                    comment TEXT,
                    confirmed BOOLEAN DEFAULT FALSE,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
                    FOREIGN KEY (media_id) REFERENCES media(media_id) ON DELETE CASCADE,
                    UNIQUE (user_id, media_id)
                )
                """);

            // Create FAVORITES table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS favorites (
                    favorite_id SERIAL PRIMARY KEY,
                    user_id INTEGER NOT NULL,
                    media_id INTEGER NOT NULL,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
                    FOREIGN KEY (media_id) REFERENCES media(media_id) ON DELETE CASCADE,
                    UNIQUE (user_id, media_id)
                )
                """);

            // Create RATING_LIKES table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS rating_likes (
                    rating_like_id SERIAL PRIMARY KEY,
                    rating_id INTEGER NOT NULL,
                    user_id INTEGER NOT NULL,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY (rating_id) REFERENCES ratings(rating_id) ON DELETE CASCADE,
                    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
                    UNIQUE (rating_id, user_id)
                )
                """);

            // Create index for faster queries
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_ratings_media_id ON ratings(media_id)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_ratings_user_id ON ratings(user_id)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_favorites_user_id ON favorites(user_id)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_favorites_media_id ON favorites(media_id)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_rating_likes_rating_id ON rating_likes(rating_id)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_rating_likes_user_id ON rating_likes(user_id)");

            if (tablesExist) {
                System.out.println("✓ Database schema verified - all tables present");
            } else {
                System.out.println("✓ Database schema created successfully - tables and indexes ready");
            }

        } catch (SQLException e) {
            System.err.println("Failed to initialize database schema: " + e.getMessage());
            throw new RuntimeException("Database initialization failed", e);
        }
    }

    private static boolean checkIfTablesExist(Connection conn) throws SQLException {
        String query = """
            SELECT EXISTS (
                SELECT FROM information_schema.tables
                WHERE table_schema = 'public'
                AND table_name = 'users'
            )
            """;

        try (var stmt = conn.createStatement();
             var rs = stmt.executeQuery(query)) {
            if (rs.next()) {
                return rs.getBoolean(1);
            }
            return false;
        }
    }
}

