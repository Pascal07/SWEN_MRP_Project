package at.technikum.application.mrp.rating;

import at.technikum.application.mrp.rating.dto.RatingDetailDto;
import at.technikum.application.mrp.rating.dto.RatingUpsertDto;
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
class RatingControllerUnitTest {

    @Mock
    private RatingService ratingService;

    @Mock
    private Request request;

    private RatingController controller;

    @BeforeEach
    void setUp() {
        controller = new RatingController(ratingService);
    }

    // ==================== POST /rating/media/{mediaId} Tests ====================

    @Test
    void testHandle_CreateRating_ValidRequest_ReturnsOk() {
        // Arrange
        when(request.getPath()).thenReturn("/rating/media/100");
        when(request.getMethod()).thenReturn("POST");
        when(request.getAuthorization()).thenReturn("Bearer valid-token");
        when(request.getBody()).thenReturn("{\"score\":5,\"comment\":\"Great!\"}");
        when(ratingService.getAuthorizedUserId("Bearer valid-token")).thenReturn(Optional.of(1));

        RatingDetailDto mockRating = createMockRatingDto(1, 1, 100, 5, "Great!");
        when(ratingService.create(eq(1), any(RatingUpsertDto.class))).thenReturn(mockRating);

        // Act
        Response response = controller.handle(request);

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
        assertTrue(response.getBody().contains("\"id\":1"));
        verify(ratingService).create(eq(1), any(RatingUpsertDto.class));
    }

    @Test
    void testHandle_CreateRating_Unauthorized_ReturnsUnauthorized() {
        // Arrange
        when(request.getPath()).thenReturn("/rating/media/100");
        when(request.getMethod()).thenReturn("POST");
        when(request.getAuthorization()).thenReturn("Bearer invalid-token");
        when(ratingService.getAuthorizedUserId("Bearer invalid-token")).thenReturn(Optional.empty());

        // Act
        Response response = controller.handle(request);

        // Assert
        assertNotNull(response);
        assertEquals(401, response.getStatusCode());
        assertTrue(response.getBody().contains("error"));
        verify(ratingService, never()).create(anyInt(), any());
    }

    @Test
    void testHandle_CreateRating_InvalidMediaId_ReturnsNotFound() {
        // Arrange
        when(request.getPath()).thenReturn("/rating/media/invalid");
        when(request.getMethod()).thenReturn("POST");
        when(request.getAuthorization()).thenReturn("Bearer valid-token");
        when(ratingService.getAuthorizedUserId("Bearer valid-token")).thenReturn(Optional.of(1));

        // Act
        Response response = controller.handle(request);

        // Assert
        assertNotNull(response);
        assertEquals(404, response.getStatusCode());
        assertTrue(response.getBody().contains("Route not found"));
        verify(ratingService, never()).create(anyInt(), any());
    }

    // ==================== GET /rating Tests ====================

    @Test
    void testHandle_ListRatings_ValidMediaId_ReturnsOk() {
        // Arrange
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("mediaId", "100");

        when(request.getPath()).thenReturn("/rating");
        when(request.getMethod()).thenReturn("GET");
        when(request.getAuthorization()).thenReturn("Bearer valid-token");
        when(request.getQueryParams()).thenReturn(queryParams);
        when(ratingService.getAuthorizedUserId("Bearer valid-token")).thenReturn(Optional.of(1));

        List<RatingDetailDto> mockRatings = Arrays.asList(
            createMockRatingDto(1, 1, 100, 5, "Great!"),
            createMockRatingDto(2, 2, 100, 4, "Good")
        );
        when(ratingService.listByMedia(1, 100)).thenReturn(mockRatings);

        // Act
        Response response = controller.handle(request);

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
        assertTrue(response.getBody().contains("\"id\":1"));
        assertTrue(response.getBody().contains("\"id\":2"));
        verify(ratingService).listByMedia(1, 100);
    }

    @Test
    void testHandle_ListRatings_MissingMediaId_ReturnsBadRequest() {
        // Arrange
        Map<String, String> queryParams = new HashMap<>();

        when(request.getPath()).thenReturn("/rating");
        when(request.getMethod()).thenReturn("GET");
        when(request.getAuthorization()).thenReturn("Bearer valid-token");
        when(request.getQueryParams()).thenReturn(queryParams);
        when(ratingService.getAuthorizedUserId("Bearer valid-token")).thenReturn(Optional.of(1));

        // Act
        Response response = controller.handle(request);

        // Assert
        assertNotNull(response);
        assertEquals(400, response.getStatusCode());
        assertTrue(response.getBody().contains("mediaId query parameter is required"));
        verify(ratingService, never()).listByMedia(anyInt(), anyInt());
    }

    // ==================== GET /rating/{id} Tests ====================

    @Test
    void testHandle_GetRating_Exists_ReturnsOk() {
        // Arrange
        when(request.getPath()).thenReturn("/rating/1");
        when(request.getMethod()).thenReturn("GET");
        when(request.getAuthorization()).thenReturn("Bearer valid-token");
        when(ratingService.getAuthorizedUserId("Bearer valid-token")).thenReturn(Optional.of(1));

        RatingDetailDto mockRating = createMockRatingDto(1, 1, 100, 5, "Great!");
        when(ratingService.getById(1, 1)).thenReturn(Optional.of(mockRating));

        // Act
        Response response = controller.handle(request);

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
        assertTrue(response.getBody().contains("\"id\":1"));
        verify(ratingService).getById(1, 1);
    }

    @Test
    void testHandle_GetRating_NotFound_ReturnsNotFound() {
        // Arrange
        when(request.getPath()).thenReturn("/rating/999");
        when(request.getMethod()).thenReturn("GET");
        when(request.getAuthorization()).thenReturn("Bearer valid-token");
        when(ratingService.getAuthorizedUserId("Bearer valid-token")).thenReturn(Optional.of(1));
        when(ratingService.getById(1, 999)).thenReturn(Optional.empty());

        // Act
        Response response = controller.handle(request);

        // Assert
        assertNotNull(response);
        assertEquals(404, response.getStatusCode());
        assertTrue(response.getBody().contains("Rating not found"));
        verify(ratingService).getById(1, 999);
    }

    // ==================== PUT /rating/{id} Tests ====================

    @Test
    void testHandle_UpdateRating_ValidRequest_ReturnsOk() {
        // Arrange
        when(request.getPath()).thenReturn("/rating/1");
        when(request.getMethod()).thenReturn("PUT");
        when(request.getAuthorization()).thenReturn("Bearer valid-token");
        when(request.getBody()).thenReturn("{\"score\":4,\"comment\":\"Updated\"}");
        when(ratingService.getAuthorizedUserId("Bearer valid-token")).thenReturn(Optional.of(1));

        RatingDetailDto mockRating = createMockRatingDto(1, 1, 100, 4, "Updated");
        when(ratingService.update(eq(1), eq(1), any(RatingUpsertDto.class))).thenReturn(Optional.of(mockRating));

        // Act
        Response response = controller.handle(request);

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
        assertTrue(response.getBody().contains("\"score\":4"));
        verify(ratingService).update(eq(1), eq(1), any(RatingUpsertDto.class));
    }

    @Test
    void testHandle_UpdateRating_NotFound_ReturnsNotFound() {
        // Arrange
        when(request.getPath()).thenReturn("/rating/999");
        when(request.getMethod()).thenReturn("PUT");
        when(request.getAuthorization()).thenReturn("Bearer valid-token");
        when(request.getBody()).thenReturn("{\"score\":4}");
        when(ratingService.getAuthorizedUserId("Bearer valid-token")).thenReturn(Optional.of(1));
        when(ratingService.update(eq(1), eq(999), any(RatingUpsertDto.class))).thenReturn(Optional.empty());

        // Act
        Response response = controller.handle(request);

        // Assert
        assertNotNull(response);
        assertEquals(404, response.getStatusCode());
        assertTrue(response.getBody().contains("Rating not found"));
    }

    // ==================== DELETE /rating/{id} Tests ====================

    @Test
    void testHandle_DeleteRating_Success_ReturnsOk() {
        // Arrange
        when(request.getPath()).thenReturn("/rating/1");
        when(request.getMethod()).thenReturn("DELETE");
        when(request.getAuthorization()).thenReturn("Bearer valid-token");
        when(ratingService.getAuthorizedUserId("Bearer valid-token")).thenReturn(Optional.of(1));
        when(ratingService.delete(1, 1)).thenReturn(true);

        // Act
        Response response = controller.handle(request);

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
        assertTrue(response.getBody().contains("Rating deleted"));
        verify(ratingService).delete(1, 1);
    }

    @Test
    void testHandle_DeleteRating_NotFound_ReturnsNotFound() {
        // Arrange
        when(request.getPath()).thenReturn("/rating/999");
        when(request.getMethod()).thenReturn("DELETE");
        when(request.getAuthorization()).thenReturn("Bearer valid-token");
        when(ratingService.getAuthorizedUserId("Bearer valid-token")).thenReturn(Optional.of(1));
        when(ratingService.delete(1, 999)).thenReturn(false);

        // Act
        Response response = controller.handle(request);

        // Assert
        assertNotNull(response);
        assertEquals(404, response.getStatusCode());
        assertTrue(response.getBody().contains("Rating not found"));
    }

    // ==================== POST /rating/{id}/like Tests ====================

    @Test
    void testHandle_LikeRating_Success_ReturnsOk() {
        // Arrange
        when(request.getPath()).thenReturn("/rating/1/like");
        when(request.getMethod()).thenReturn("POST");
        when(request.getAuthorization()).thenReturn("Bearer valid-token");
        when(ratingService.getAuthorizedUserId("Bearer valid-token")).thenReturn(Optional.of(1));

        RatingDetailDto mockRating = createMockRatingDto(1, 2, 100, 5, "Great!");
        when(ratingService.like(1, 1)).thenReturn(Optional.of(mockRating));

        // Act
        Response response = controller.handle(request);

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
        verify(ratingService).like(1, 1);
    }

    @Test
    void testHandle_LikeRating_NotFound_ReturnsNotFound() {
        // Arrange
        when(request.getPath()).thenReturn("/rating/999/like");
        when(request.getMethod()).thenReturn("POST");
        when(request.getAuthorization()).thenReturn("Bearer valid-token");
        when(ratingService.getAuthorizedUserId("Bearer valid-token")).thenReturn(Optional.of(1));
        when(ratingService.like(1, 999)).thenReturn(Optional.empty());

        // Act
        Response response = controller.handle(request);

        // Assert
        assertNotNull(response);
        assertEquals(404, response.getStatusCode());
        assertTrue(response.getBody().contains("Rating not found"));
    }

    // ==================== DELETE /rating/{id}/like Tests ====================

    @Test
    void testHandle_UnlikeRating_Success_ReturnsOk() {
        // Arrange
        when(request.getPath()).thenReturn("/rating/1/like");
        when(request.getMethod()).thenReturn("DELETE");
        when(request.getAuthorization()).thenReturn("Bearer valid-token");
        when(ratingService.getAuthorizedUserId("Bearer valid-token")).thenReturn(Optional.of(1));

        RatingDetailDto mockRating = createMockRatingDto(1, 2, 100, 5, "Great!");
        when(ratingService.unlike(1, 1)).thenReturn(Optional.of(mockRating));

        // Act
        Response response = controller.handle(request);

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
        verify(ratingService).unlike(1, 1);
    }

    // ==================== POST /rating/{id}/confirm Tests ====================

    @Test
    void testHandle_ConfirmRating_Success_ReturnsOk() {
        // Arrange
        when(request.getPath()).thenReturn("/rating/1/confirm");
        when(request.getMethod()).thenReturn("POST");
        when(request.getAuthorization()).thenReturn("Bearer valid-token");
        when(ratingService.getAuthorizedUserId("Bearer valid-token")).thenReturn(Optional.of(1));

        RatingDetailDto mockRating = createMockRatingDto(1, 1, 100, 5, "Great!");
        when(ratingService.confirm(1, 1)).thenReturn(Optional.of(mockRating));

        // Act
        Response response = controller.handle(request);

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
        verify(ratingService).confirm(1, 1);
    }

    @Test
    void testHandle_ConfirmRating_WrongMethod_ReturnsMethodNotAllowed() {
        // Arrange
        when(request.getPath()).thenReturn("/rating/1/confirm");
        when(request.getMethod()).thenReturn("GET");
        when(request.getAuthorization()).thenReturn("Bearer valid-token");
        when(ratingService.getAuthorizedUserId("Bearer valid-token")).thenReturn(Optional.of(1));

        // Act
        Response response = controller.handle(request);

        // Assert
        assertNotNull(response);
        assertEquals(405, response.getStatusCode());
        assertTrue(response.getBody().contains("Method not allowed"));
        verify(ratingService, never()).confirm(anyInt(), anyInt());
    }

    // ==================== Exception Handling Tests ====================

    @Test
    void testHandle_SecurityException_ReturnsUnauthorized() {
        // Arrange
        when(request.getPath()).thenReturn("/rating/1");
        when(request.getMethod()).thenReturn("PUT");
        when(request.getAuthorization()).thenReturn("Bearer valid-token");
        when(request.getBody()).thenReturn("{\"score\":5}");
        when(ratingService.getAuthorizedUserId("Bearer valid-token")).thenReturn(Optional.of(1));
        when(ratingService.update(eq(1), eq(1), any())).thenThrow(new SecurityException("Not authorized"));

        // Act
        Response response = controller.handle(request);

        // Assert
        assertNotNull(response);
        assertEquals(401, response.getStatusCode());
        assertTrue(response.getBody().contains("Not authorized"));
    }

    @Test
    void testHandle_IllegalArgumentException_ReturnsBadRequest() {
        // Arrange
        when(request.getPath()).thenReturn("/rating/media/100");
        when(request.getMethod()).thenReturn("POST");
        when(request.getAuthorization()).thenReturn("Bearer valid-token");
        when(request.getBody()).thenReturn("{\"score\":10}");
        when(ratingService.getAuthorizedUserId("Bearer valid-token")).thenReturn(Optional.of(1));
        when(ratingService.create(eq(1), any())).thenThrow(new IllegalArgumentException("Invalid score"));

        // Act
        Response response = controller.handle(request);

        // Assert
        assertNotNull(response);
        assertEquals(400, response.getStatusCode());
        assertTrue(response.getBody().contains("Invalid score"));
    }

    // ==================== Helper Methods ====================

    private RatingDetailDto createMockRatingDto(int id, int userId, int mediaId, int score, String comment) {
        RatingDetailDto dto = new RatingDetailDto();
        dto.setId(id);
        dto.setUserId(userId);
        dto.setMediaId(mediaId);
        dto.setScore(score);
        dto.setComment(comment);
        dto.setConfirmed(true);
        dto.setTimestamp(System.currentTimeMillis());
        dto.setLikes(0);
        dto.setLikedByMe(false);
        return dto;
    }
}

