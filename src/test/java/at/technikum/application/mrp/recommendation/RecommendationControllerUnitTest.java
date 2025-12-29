package at.technikum.application.mrp.recommendation;

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
class RecommendationControllerUnitTest {

    @Mock
    private RecommendationService recommendationService;

    @Mock
    private Request request;

    private RecommendationController controller;

    @BeforeEach
    void setUp() {
        controller = new RecommendationController(recommendationService);
    }

    // ==================== GET /rec?type=genre Tests ====================

    @Test
    void testHandle_GenreRecommendations_ValidRequest_ReturnsOk() {
        // Arrange
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("type", "genre");
        queryParams.put("genre", "Action");

        when(request.getPath()).thenReturn("/rec");
        when(request.getMethod()).thenReturn("GET");
        when(request.getQueryParams()).thenReturn(queryParams);
        when(request.getAuthorization()).thenReturn("Bearer valid-token");

        List<Map<String, Object>> mockRecommendations = Arrays.asList(
            createMockMediaMap(1, "Action Movie 1"),
            createMockMediaMap(2, "Action Movie 2")
        );
        when(recommendationService.recommendationsByGenre("Bearer valid-token", "Action"))
            .thenReturn(mockRecommendations);

        // Act
        Response response = controller.handle(request);

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
        assertTrue(response.getBody().contains("Action Movie 1"));
        assertTrue(response.getBody().contains("Action Movie 2"));
        verify(recommendationService).recommendationsByGenre("Bearer valid-token", "Action");
    }

    @Test
    void testHandle_GenreRecommendations_TrimmedGenre_ReturnsOk() {
        // Arrange
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("type", "genre");
        queryParams.put("genre", "  Action  ");

        when(request.getPath()).thenReturn("/rec");
        when(request.getMethod()).thenReturn("GET");
        when(request.getQueryParams()).thenReturn(queryParams);
        when(request.getAuthorization()).thenReturn("Bearer valid-token");

        List<Map<String, Object>> mockRecommendations = Collections.singletonList(
            createMockMediaMap(1, "Action Movie")
        );
        when(recommendationService.recommendationsByGenre("Bearer valid-token", "Action"))
            .thenReturn(mockRecommendations);

        // Act
        Response response = controller.handle(request);

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
        verify(recommendationService).recommendationsByGenre("Bearer valid-token", "Action");
    }

    @Test
    void testHandle_GenreRecommendations_EmptyResults_ReturnsEmptyArray() {
        // Arrange
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("type", "genre");
        queryParams.put("genre", "NonExistent");

        when(request.getPath()).thenReturn("/rec");
        when(request.getMethod()).thenReturn("GET");
        when(request.getQueryParams()).thenReturn(queryParams);
        when(request.getAuthorization()).thenReturn("Bearer valid-token");
        when(recommendationService.recommendationsByGenre("Bearer valid-token", "NonExistent"))
            .thenReturn(Collections.emptyList());

        // Act
        Response response = controller.handle(request);

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
        assertTrue(response.getBody().contains("[]"));
    }

    // ==================== GET /rec?type=movie Tests ====================

    @Test
    void testHandle_MovieRecommendations_ReturnsOk() {
        // Arrange
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("type", "movie");

        when(request.getPath()).thenReturn("/rec");
        when(request.getMethod()).thenReturn("GET");
        when(request.getQueryParams()).thenReturn(queryParams);
        when(request.getAuthorization()).thenReturn("Bearer valid-token");

        List<Map<String, Object>> mockRecommendations = Arrays.asList(
            createMockMediaMap(1, "Movie 1"),
            createMockMediaMap(2, "Movie 2")
        );
        when(recommendationService.recommendationsByMediaType("Bearer valid-token", "movie"))
            .thenReturn(mockRecommendations);

        // Act
        Response response = controller.handle(request);

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
        assertTrue(response.getBody().contains("Movie 1"));
        assertTrue(response.getBody().contains("Movie 2"));
        verify(recommendationService).recommendationsByMediaType("Bearer valid-token", "movie");
    }

    @Test
    void testHandle_MovieRecommendations_CaseInsensitive_ReturnsOk() {
        // Arrange
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("type", "MOVIE");

        when(request.getPath()).thenReturn("/rec");
        when(request.getMethod()).thenReturn("GET");
        when(request.getQueryParams()).thenReturn(queryParams);
        when(request.getAuthorization()).thenReturn("Bearer valid-token");

        List<Map<String, Object>> mockRecommendations = Collections.singletonList(
            createMockMediaMap(1, "Movie 1")
        );
        when(recommendationService.recommendationsByMediaType("Bearer valid-token", "MOVIE"))
            .thenReturn(mockRecommendations);

        // Act
        Response response = controller.handle(request);

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
        verify(recommendationService).recommendationsByMediaType("Bearer valid-token", "MOVIE");
    }

    // ==================== GET /rec?type=series Tests ====================

    @Test
    void testHandle_SeriesRecommendations_ReturnsOk() {
        // Arrange
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("type", "series");

        when(request.getPath()).thenReturn("/rec");
        when(request.getMethod()).thenReturn("GET");
        when(request.getQueryParams()).thenReturn(queryParams);
        when(request.getAuthorization()).thenReturn("Bearer valid-token");

        List<Map<String, Object>> mockRecommendations = Collections.singletonList(
            createMockMediaMap(1, "Series 1")
        );
        when(recommendationService.recommendationsByMediaType("Bearer valid-token", "series"))
            .thenReturn(mockRecommendations);

        // Act
        Response response = controller.handle(request);

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
        assertTrue(response.getBody().contains("Series 1"));
        verify(recommendationService).recommendationsByMediaType("Bearer valid-token", "series");
    }

    // ==================== GET /rec (no type) Tests ====================

    @Test
    void testHandle_DefaultRecommendations_NoTypeParam_ReturnsOk() {
        // Arrange
        Map<String, String> queryParams = new HashMap<>();

        when(request.getPath()).thenReturn("/rec");
        when(request.getMethod()).thenReturn("GET");
        when(request.getQueryParams()).thenReturn(queryParams);
        when(request.getAuthorization()).thenReturn("Bearer valid-token");

        List<Map<String, Object>> mockRecommendations = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            mockRecommendations.add(createMockMediaMap(i, "Media " + i));
        }
        when(recommendationService.recommendationsForUser("Bearer valid-token"))
            .thenReturn(mockRecommendations);

        // Act
        Response response = controller.handle(request);

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
        assertTrue(response.getBody().contains("Media 1"));
        assertTrue(response.getBody().contains("Media 10"));
        verify(recommendationService).recommendationsForUser("Bearer valid-token");
    }

    @Test
    void testHandle_DefaultRecommendations_UnknownType_ReturnsDefault() {
        // Arrange
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("type", "unknown");

        when(request.getPath()).thenReturn("/rec");
        when(request.getMethod()).thenReturn("GET");
        when(request.getQueryParams()).thenReturn(queryParams);
        when(request.getAuthorization()).thenReturn("Bearer valid-token");

        List<Map<String, Object>> mockRecommendations = Collections.singletonList(
            createMockMediaMap(1, "Default Media")
        );
        when(recommendationService.recommendationsForUser("Bearer valid-token"))
            .thenReturn(mockRecommendations);

        // Act
        Response response = controller.handle(request);

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
        assertTrue(response.getBody().contains("Default Media"));
        verify(recommendationService).recommendationsForUser("Bearer valid-token");
    }

    // ==================== Route and Method Validation Tests ====================

    @Test
    void testHandle_WrongPath_ThrowsException() {
        // Arrange
        when(request.getPath()).thenReturn("/recommendations");
        when(request.getMethod()).thenReturn("GET");

        // Act & Assert
        assertThrows(NoSuchElementException.class, () -> controller.handle(request));
        verify(recommendationService, never()).recommendationsForUser(any());
    }

    @Test
    void testHandle_WrongMethod_ThrowsException() {
        // Arrange
        when(request.getPath()).thenReturn("/rec");
        when(request.getMethod()).thenReturn("POST");

        // Act & Assert
        assertThrows(UnsupportedOperationException.class, () -> controller.handle(request));
        verify(recommendationService, never()).recommendationsForUser(any());
    }

    @Test
    void testHandle_DeleteMethod_ThrowsException() {
        // Arrange
        when(request.getPath()).thenReturn("/rec");
        when(request.getMethod()).thenReturn("DELETE");

        // Act & Assert
        assertThrows(UnsupportedOperationException.class, () -> controller.handle(request));
    }

    // ==================== Exception Propagation Tests ====================

    @Test
    void testHandle_ServiceThrowsSecurityException_Propagates() {
        // Arrange
        Map<String, String> queryParams = new HashMap<>();

        when(request.getPath()).thenReturn("/rec");
        when(request.getMethod()).thenReturn("GET");
        when(request.getQueryParams()).thenReturn(queryParams);
        when(request.getAuthorization()).thenReturn("Bearer invalid-token");

        doThrow(new SecurityException("Invalid token"))
            .when(recommendationService).recommendationsForUser("Bearer invalid-token");

        // Act & Assert
        assertThrows(SecurityException.class, () -> controller.handle(request));
    }

    @Test
    void testHandle_ServiceThrowsIllegalArgumentException_Propagates() {
        // Arrange
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("type", "genre");
        queryParams.put("genre", "");

        when(request.getPath()).thenReturn("/rec");
        when(request.getMethod()).thenReturn("GET");
        when(request.getQueryParams()).thenReturn(queryParams);
        when(request.getAuthorization()).thenReturn("Bearer valid-token");

        doThrow(new IllegalArgumentException("genre required"))
            .when(recommendationService).recommendationsByGenre("Bearer valid-token", null);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> controller.handle(request));
    }

    // ==================== Edge Cases Tests ====================

    @Test
    void testHandle_NullQueryParams_ReturnsDefault() {
        // Arrange
        when(request.getPath()).thenReturn("/rec");
        when(request.getMethod()).thenReturn("GET");
        when(request.getQueryParams()).thenReturn(new HashMap<>());
        when(request.getAuthorization()).thenReturn("Bearer valid-token");

        List<Map<String, Object>> mockRecommendations = Collections.singletonList(
            createMockMediaMap(1, "Default")
        );
        when(recommendationService.recommendationsForUser("Bearer valid-token"))
            .thenReturn(mockRecommendations);

        // Act
        Response response = controller.handle(request);

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
        verify(recommendationService).recommendationsForUser("Bearer valid-token");
    }

    @Test
    void testHandle_EmptyTypeParam_ReturnsDefault() {
        // Arrange
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("type", "");

        when(request.getPath()).thenReturn("/rec");
        when(request.getMethod()).thenReturn("GET");
        when(request.getQueryParams()).thenReturn(queryParams);
        when(request.getAuthorization()).thenReturn("Bearer valid-token");

        List<Map<String, Object>> mockRecommendations = Collections.singletonList(
            createMockMediaMap(1, "Default")
        );
        when(recommendationService.recommendationsForUser("Bearer valid-token"))
            .thenReturn(mockRecommendations);

        // Act
        Response response = controller.handle(request);

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
        verify(recommendationService).recommendationsForUser("Bearer valid-token");
    }

    // ==================== Helper Methods ====================

    private Map<String, Object> createMockMediaMap(int id, String title) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", id);
        map.put("title", title);
        map.put("description", "Description for " + title);
        map.put("genres", Collections.singletonList("Action"));
        map.put("mediaType", "movie");
        map.put("releaseYear", 2020);
        map.put("ageRestriction", 12);
        map.put("averageRating", 4.5);
        map.put("ratingCount", 10);
        return map;
    }
}

