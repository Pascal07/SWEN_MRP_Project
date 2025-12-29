package at.technikum.application.mrp.leaderboard;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LeaderboardServiceUnitTest {

    @Mock
    private LeaderboardRepository repository;

    private LeaderboardService service;

    @BeforeEach
    void setUp() {
        service = new LeaderboardService(repository);
    }

    @Test
    void testGetLeaderboard_WithDefaultParameters() {
        // Arrange
        Map<String, String> queryParams = new HashMap<>();
        List<Map<String, Object>> expectedResult = createMockLeaderboard();
        when(repository.getTopUsersByRatings(10, 0)).thenReturn(expectedResult);

        // Act
        List<Map<String, Object>> result = service.getLeaderboard(queryParams);

        // Assert
        assertNotNull(result);
        assertEquals(expectedResult, result);
        verify(repository).getTopUsersByRatings(10, 0);
    }

    @Test
    void testGetLeaderboard_WithCustomLimitAndOffset() {
        // Arrange
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("limit", "20");
        queryParams.put("offset", "5");
        List<Map<String, Object>> expectedResult = createMockLeaderboard();
        when(repository.getTopUsersByRatings(20, 5)).thenReturn(expectedResult);

        // Act
        List<Map<String, Object>> result = service.getLeaderboard(queryParams);

        // Assert
        assertNotNull(result);
        assertEquals(expectedResult, result);
        verify(repository).getTopUsersByRatings(20, 5);
    }

    @Test
    void testGetLeaderboard_LimitTooLow_UsesDefault() {
        // Arrange
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("limit", "0");
        List<Map<String, Object>> expectedResult = createMockLeaderboard();
        when(repository.getTopUsersByRatings(10, 0)).thenReturn(expectedResult);

        // Act
        List<Map<String, Object>> result = service.getLeaderboard(queryParams);

        // Assert
        assertNotNull(result);
        verify(repository).getTopUsersByRatings(10, 0);
    }

    @Test
    void testGetLeaderboard_LimitTooHigh_UsesDefault() {
        // Arrange
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("limit", "150");
        List<Map<String, Object>> expectedResult = createMockLeaderboard();
        when(repository.getTopUsersByRatings(10, 0)).thenReturn(expectedResult);

        // Act
        List<Map<String, Object>> result = service.getLeaderboard(queryParams);

        // Assert
        assertNotNull(result);
        verify(repository).getTopUsersByRatings(10, 0);
    }

    @Test
    void testGetLeaderboard_NegativeOffset_UsesZero() {
        // Arrange
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("offset", "-5");
        List<Map<String, Object>> expectedResult = createMockLeaderboard();
        when(repository.getTopUsersByRatings(10, 0)).thenReturn(expectedResult);

        // Act
        List<Map<String, Object>> result = service.getLeaderboard(queryParams);

        // Assert
        assertNotNull(result);
        verify(repository).getTopUsersByRatings(10, 0);
    }

    @Test
    void testGetLeaderboard_InvalidLimitFormat_UsesDefault() {
        // Arrange
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("limit", "invalid");
        List<Map<String, Object>> expectedResult = createMockLeaderboard();
        when(repository.getTopUsersByRatings(10, 0)).thenReturn(expectedResult);

        // Act
        List<Map<String, Object>> result = service.getLeaderboard(queryParams);

        // Assert
        assertNotNull(result);
        verify(repository).getTopUsersByRatings(10, 0);
    }

    @Test
    void testGetLeaderboard_InvalidOffsetFormat_UsesDefault() {
        // Arrange
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("offset", "abc");
        List<Map<String, Object>> expectedResult = createMockLeaderboard();
        when(repository.getTopUsersByRatings(10, 0)).thenReturn(expectedResult);

        // Act
        List<Map<String, Object>> result = service.getLeaderboard(queryParams);

        // Assert
        assertNotNull(result);
        verify(repository).getTopUsersByRatings(10, 0);
    }

    @Test
    void testGetLeaderboard_MaximumValidLimit() {
        // Arrange
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("limit", "100");
        List<Map<String, Object>> expectedResult = createMockLeaderboard();
        when(repository.getTopUsersByRatings(100, 0)).thenReturn(expectedResult);

        // Act
        List<Map<String, Object>> result = service.getLeaderboard(queryParams);

        // Assert
        assertNotNull(result);
        verify(repository).getTopUsersByRatings(100, 0);
    }

    @Test
    void testGetLeaderboard_EmptyResult() {
        // Arrange
        Map<String, String> queryParams = new HashMap<>();
        List<Map<String, Object>> emptyResult = new ArrayList<>();
        when(repository.getTopUsersByRatings(10, 0)).thenReturn(emptyResult);

        // Act
        List<Map<String, Object>> result = service.getLeaderboard(queryParams);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(repository).getTopUsersByRatings(10, 0);
    }

    // New tests validating null queryParams and edge cases
    @Test
    void testGetLeaderboard_NullQueryParams_UsesDefaults() {
        // Arrange
        Map<String, String> queryParams = null;
        List<Map<String, Object>> expectedResult = createMockLeaderboard();
        when(repository.getTopUsersByRatings(10, 0)).thenReturn(expectedResult);

        // Act
        List<Map<String, Object>> result = service.getLeaderboard(queryParams);

        // Assert
        assertNotNull(result);
        assertEquals(expectedResult, result);
        verify(repository).getTopUsersByRatings(10, 0);
    }

    @Test
    void testGetLeaderboard_MinimumValidLimit() {
        // Arrange
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("limit", "1");
        List<Map<String, Object>> expectedResult = createMockLeaderboard();
        when(repository.getTopUsersByRatings(1, 0)).thenReturn(expectedResult);

        // Act
        List<Map<String, Object>> result = service.getLeaderboard(queryParams);

        // Assert
        assertNotNull(result);
        verify(repository).getTopUsersByRatings(1, 0);
    }

    @Test
    void testGetLeaderboard_NegativeLimitString_UsesDefault() {
        // Arrange
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("limit", "-10");
        List<Map<String, Object>> expectedResult = createMockLeaderboard();
        when(repository.getTopUsersByRatings(10, 0)).thenReturn(expectedResult);

        // Act
        List<Map<String, Object>> result = service.getLeaderboard(queryParams);

        // Assert
        assertNotNull(result);
        verify(repository).getTopUsersByRatings(10, 0);
    }

    private List<Map<String, Object>> createMockLeaderboard() {
        List<Map<String, Object>> leaderboard = new ArrayList<>();

        Map<String, Object> user1 = new HashMap<>();
        user1.put("userId", 1);
        user1.put("username", "user1");
        user1.put("ratingCount", 50);
        user1.put("mediaRatedCount", 30);
        user1.put("avgRatingGiven", 4.5);

        Map<String, Object> user2 = new HashMap<>();
        user2.put("userId", 2);
        user2.put("username", "user2");
        user2.put("ratingCount", 40);
        user2.put("mediaRatedCount", 25);
        user2.put("avgRatingGiven", 4.2);

        leaderboard.add(user1);
        leaderboard.add(user2);

        return leaderboard;
    }
}

