package at.technikum.application.mrp.leaderboard;

import at.technikum.server.http.Request;
import at.technikum.server.http.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LeaderboardControllerUnitTest {

    @Mock
    private LeaderboardService leaderboardService;

    @Mock
    private Request request;

    private LeaderboardController controller;

    @BeforeEach
    void setUp() {
        controller = new LeaderboardController(leaderboardService);
    }

    @Test
    void testHandle_GetLeaderboard_ReturnsOkWithData() {
        // Arrange
        when(request.getPath()).thenReturn("/leaderboard");
        when(request.getMethod()).thenReturn("GET");

        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("limit", "10");
        when(request.getQueryParams()).thenReturn(queryParams);

        List<Map<String, Object>> mockLeaderboard = createMockLeaderboard();
        when(leaderboardService.getLeaderboard(queryParams)).thenReturn(mockLeaderboard);

        // Act
        Response response = controller.handle(request);

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(leaderboardService).getLeaderboard(queryParams);
    }

    @Test
    void testHandle_GetLeaderboard_WithoutQueryParams() {
        // Arrange
        when(request.getPath()).thenReturn("/leaderboard");
        when(request.getMethod()).thenReturn("GET");

        Map<String, String> emptyParams = new HashMap<>();
        when(request.getQueryParams()).thenReturn(emptyParams);

        List<Map<String, Object>> mockLeaderboard = createMockLeaderboard();
        when(leaderboardService.getLeaderboard(emptyParams)).thenReturn(mockLeaderboard);

        // Act
        Response response = controller.handle(request);

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
        verify(leaderboardService).getLeaderboard(emptyParams);
    }

    @Test
    void testHandle_PostLeaderboard_ThrowsUnsupportedOperationException() {
        // Arrange
        when(request.getPath()).thenReturn("/leaderboard");
        when(request.getMethod()).thenReturn("POST");

        // Act & Assert
        assertThrows(UnsupportedOperationException.class, () -> controller.handle(request));
    }

    @Test
    void testHandle_PutLeaderboard_ThrowsUnsupportedOperationException() {
        // Arrange
        when(request.getPath()).thenReturn("/leaderboard");
        when(request.getMethod()).thenReturn("PUT");

        // Act & Assert
        assertThrows(UnsupportedOperationException.class, () -> controller.handle(request));
    }

    @Test
    void testHandle_DeleteLeaderboard_ThrowsUnsupportedOperationException() {
        // Arrange
        when(request.getPath()).thenReturn("/leaderboard");
        when(request.getMethod()).thenReturn("DELETE");

        // Act & Assert
        assertThrows(UnsupportedOperationException.class, () -> controller.handle(request));
    }

    @Test
    void testHandle_InvalidPath_ThrowsNoSuchElementException() {
        // Arrange
        when(request.getPath()).thenReturn("/invalid");
        when(request.getMethod()).thenReturn("GET");

        // Act & Assert
        assertThrows(NoSuchElementException.class, () -> controller.handle(request));
    }

    @Test
    void testHandle_GetLeaderboard_EmptyResult() {
        // Arrange
        when(request.getPath()).thenReturn("/leaderboard");
        when(request.getMethod()).thenReturn("GET");

        Map<String, String> queryParams = new HashMap<>();
        when(request.getQueryParams()).thenReturn(queryParams);

        List<Map<String, Object>> emptyList = new ArrayList<>();
        when(leaderboardService.getLeaderboard(queryParams)).thenReturn(emptyList);

        // Act
        Response response = controller.handle(request);

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
        verify(leaderboardService).getLeaderboard(queryParams);
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

