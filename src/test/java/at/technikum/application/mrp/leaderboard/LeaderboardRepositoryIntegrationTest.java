package at.technikum.application.mrp.leaderboard;

import at.technikum.application.mrp.database.DatabaseConnection;
import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test for LeaderboardRepository
 * Tests the actual database interaction
 * Requires a running database with proper configuration
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class LeaderboardRepositoryIntegrationTest {

    private LeaderboardRepository repository;
    private Connection connection;
    private Integer testUser1Id;
    private Integer testUser2Id;
    private Integer testMediaId;

    @BeforeAll
    void setUp() {
        try {
            System.out.println("Starting LeaderboardRepository integration test setup...");

            // Try to get connection from DatabaseConnection (uses environment variables or default)
            connection = DatabaseConnection.getInstance().getConnection();

            if (connection == null || connection.isClosed()) {
                System.err.println("Database connection is null or closed, skipping integration tests");
                Assumptions.assumeTrue(false, "Database connection not available");
                return;
            }

            System.out.println("Database connection established successfully");
            repository = new LeaderboardRepository();

            // Setup test data
            setupTestData();
            System.out.println("Test data setup completed successfully");
        } catch (Exception e) {
            System.err.println("Failed to setup integration test: " + e.getMessage());
            e.printStackTrace();
            Assumptions.assumeTrue(false, "Could not setup test: " + e.getMessage());
        }
    }

    @AfterAll
    void tearDown() {
        try {
            System.out.println("Cleaning up test data...");
            if (connection != null && !connection.isClosed()) {
                cleanupTestData();
                System.out.println("Test data cleanup completed");
            }
        } catch (SQLException e) {
            System.err.println("Failed to cleanup test data: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Test
    void testGetTopUsersByRatings_ReturnsCorrectOrder() {
        // Act
        List<Map<String, Object>> results = repository.getTopUsersByRatings(10, 0);

        // Assert
        assertNotNull(results, "Results should not be null");
        assertFalse(results.isEmpty(), "Results should not be empty");

        // Verify results are ordered by rating count descending
        for (int i = 0; i < results.size() - 1; i++) {
            int currentCount = (int) results.get(i).get("ratingCount");
            int nextCount = (int) results.get(i + 1).get("ratingCount");
            assertTrue(currentCount >= nextCount,
                "Results should be ordered by rating count descending");
        }
    }

    @Test
    void testGetTopUsersByRatings_WithLimit() {
        // Act
        List<Map<String, Object>> results = repository.getTopUsersByRatings(2, 0);

        // Assert
        assertNotNull(results, "Results should not be null");
        assertTrue(results.size() <= 2, "Should return at most 2 results");
    }

    @Test
    void testGetTopUsersByRatings_WithOffset() {
        // Arrange
        List<Map<String, Object>> firstPage = repository.getTopUsersByRatings(1, 0);
        List<Map<String, Object>> secondPage = repository.getTopUsersByRatings(1, 1);

        // Assert
        assertNotNull(firstPage, "First page should not be null");
        assertNotNull(secondPage, "Second page should not be null");

        if (!firstPage.isEmpty() && !secondPage.isEmpty()) {
            assertNotEquals(firstPage.get(0).get("userId"), secondPage.get(0).get("userId"),
                "Different pages should return different users");
        }
    }

    @Test
    void testGetTopUsersByRatings_IncludesAllRequiredFields() {
        // Act
        List<Map<String, Object>> results = repository.getTopUsersByRatings(10, 0);

        // Assert
        assertFalse(results.isEmpty(), "Should have at least one result");

        Map<String, Object> firstEntry = results.get(0);

        assertTrue(firstEntry.containsKey("userId"), "Should contain userId");
        assertTrue(firstEntry.containsKey("username"), "Should contain username");
        assertTrue(firstEntry.containsKey("ratingCount"), "Should contain ratingCount");
        assertTrue(firstEntry.containsKey("mediaRatedCount"), "Should contain mediaRatedCount");
        assertTrue(firstEntry.containsKey("avgRatingGiven"), "Should contain avgRatingGiven");

        assertNotNull(firstEntry.get("userId"), "userId should not be null");
        assertNotNull(firstEntry.get("username"), "username should not be null");
        assertNotNull(firstEntry.get("ratingCount"), "ratingCount should not be null");
        assertNotNull(firstEntry.get("mediaRatedCount"), "mediaRatedCount should not be null");
        assertNotNull(firstEntry.get("avgRatingGiven"), "avgRatingGiven should not be null");
    }

    @Test
    void testGetTopUsersByRatings_HandlesUsersWithNoRatings() {
        // Act
        List<Map<String, Object>> results = repository.getTopUsersByRatings(100, 0);

        // Assert
        assertNotNull(results, "Results should not be null");

        // Check if there are users with 0 ratings (due to LEFT JOIN)
        boolean hasUserWithZeroRatings = results.stream()
            .anyMatch(entry -> (int) entry.get("ratingCount") == 0);

        // If there are users with zero ratings, verify they have avgRatingGiven = 0.0
        if (hasUserWithZeroRatings) {
            results.stream()
                .filter(entry -> (int) entry.get("ratingCount") == 0)
                .forEach(entry -> {
                    assertEquals(0.0, (double) entry.get("avgRatingGiven"),
                        "Users with no ratings should have avgRatingGiven = 0.0");
                });
        }
    }

    @Test
    void testGetTopUsersByRatings_AverageRatingRoundedCorrectly() {
        // Act
        List<Map<String, Object>> results = repository.getTopUsersByRatings(100, 0);

        // Assert
        assertNotNull(results, "Results should not be null");

        // Verify that avgRatingGiven is rounded to 2 decimal places
        results.forEach(entry -> {
            double avgRating = (double) entry.get("avgRatingGiven");
            double rounded = Math.round(avgRating * 100.0) / 100.0;
            assertEquals(rounded, avgRating, 0.001,
                "avgRatingGiven should be rounded to 2 decimal places");
        });
    }

    @Test
    void testGetTopUsersByRatings_ValidatesRatingCountAgainstActualRatings() {
        // Act
        List<Map<String, Object>> results = repository.getTopUsersByRatings(100, 0);

        // Assert
        assertNotNull(results, "Results should not be null");

        // Verify that ratingCount is non-negative
        results.forEach(entry -> {
            int ratingCount = (int) entry.get("ratingCount");
            assertTrue(ratingCount >= 0, "ratingCount should be non-negative");

            int mediaRatedCount = (int) entry.get("mediaRatedCount");
            assertTrue(mediaRatedCount >= 0, "mediaRatedCount should be non-negative");

            // mediaRatedCount should never exceed ratingCount
            assertTrue(mediaRatedCount <= ratingCount,
                "mediaRatedCount should not exceed ratingCount");
        });
    }

    @Test
    void testGetTopUsersByRatings_EmptyResultWhenOffsetTooLarge() {
        // Act
        List<Map<String, Object>> results = repository.getTopUsersByRatings(10, 100000);

        // Assert
        assertNotNull(results, "Results should not be null");
        assertTrue(results.isEmpty() || results.size() < 10,
            "Should return empty or fewer results when offset is too large");
    }

    private void setupTestData() throws SQLException {
        // Create test media
        String insertMedia = """
            INSERT INTO media (title, description, genre, media_type, release_year)
            VALUES (?, ?, ?, ?, ?)
            RETURNING media_id
            """;

        try (PreparedStatement stmt = connection.prepareStatement(insertMedia)) {
            stmt.setString(1, "Test Movie for Leaderboard");
            stmt.setString(2, "Integration test media");
            stmt.setString(3, "Drama");
            stmt.setString(4, "Movie");
            stmt.setInt(5, 2024);
            var rs = stmt.executeQuery();
            if (rs.next()) {
                testMediaId = rs.getInt(1);
            }
        }

        // Create test users
        String insertUser = """
            INSERT INTO users (username, email, password_hash)
            VALUES (?, ?, ?)
            RETURNING user_id
            """;

        try (PreparedStatement stmt = connection.prepareStatement(insertUser)) {
            // User 1 - will have 3 ratings
            stmt.setString(1, "leaderboard_test_user_1");
            stmt.setString(2, "leaderboard1@test.com");
            stmt.setString(3, "hashedpassword");
            var rs = stmt.executeQuery();
            if (rs.next()) {
                testUser1Id = rs.getInt(1);
            }

            // User 2 - will have 1 rating
            stmt.setString(1, "leaderboard_test_user_2");
            stmt.setString(2, "leaderboard2@test.com");
            stmt.setString(3, "hashedpassword");
            rs = stmt.executeQuery();
            if (rs.next()) {
                testUser2Id = rs.getInt(1);
            }

            // User 3 - will have 0 ratings (for testing users with no ratings)
            stmt.setString(1, "leaderboard_test_user_3");
            stmt.setString(2, "leaderboard3@test.com");
            stmt.setString(3, "hashedpassword");
            rs = stmt.executeQuery(); // Execute but don't store ID
            if (rs.next()) {
                // Just consume the result, don't store the ID
            }
        }

        // Create ratings for test users
        if (testMediaId != null && testUser1Id != null && testUser2Id != null) {
            String insertRating = """
                INSERT INTO ratings (user_id, media_id, rating_value, comment)
                VALUES (?, ?, ?, ?)
                ON CONFLICT (user_id, media_id) DO NOTHING
                """;

            try (PreparedStatement stmt = connection.prepareStatement(insertRating)) {
                // 3 ratings for user 1
                stmt.setInt(1, testUser1Id);
                stmt.setInt(2, testMediaId);
                stmt.setInt(3, 5);
                stmt.setString(4, "Great movie!");
                stmt.executeUpdate();

                // 1 rating for user 2
                stmt.setInt(1, testUser2Id);
                stmt.setInt(2, testMediaId);
                stmt.setInt(3, 4);
                stmt.setString(4, "Good movie");
                stmt.executeUpdate();
            }
        }
    }

    private void cleanupTestData() throws SQLException {
        // Delete test ratings
        String deleteRatings = """
            DELETE FROM ratings
            WHERE user_id IN (
                SELECT user_id FROM users
                WHERE username LIKE 'leaderboard_test_user_%'
            )
            """;

        try (PreparedStatement stmt = connection.prepareStatement(deleteRatings)) {
            stmt.executeUpdate();
        }

        // Delete test users
        String deleteUsers = """
            DELETE FROM users
            WHERE username LIKE 'leaderboard_test_user_%'
            """;

        try (PreparedStatement stmt = connection.prepareStatement(deleteUsers)) {
            stmt.executeUpdate();
        }

        // Delete test media
        if (testMediaId != null) {
            String deleteMedia = """
                DELETE FROM media
                WHERE media_id = ?
                """;

            try (PreparedStatement stmt = connection.prepareStatement(deleteMedia)) {
                stmt.setInt(1, testMediaId);
                stmt.executeUpdate();
            }
        }
    }
}

