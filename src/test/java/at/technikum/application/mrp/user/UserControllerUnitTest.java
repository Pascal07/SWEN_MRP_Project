package at.technikum.application.mrp.user;

import at.technikum.application.mrp.favorites.entity.FavoriteEntity;
import at.technikum.application.mrp.rating.entity.RatingEntity;
import at.technikum.application.mrp.user.dto.UserFavoritesDto;
import at.technikum.application.mrp.user.dto.UserProfileDto;
import at.technikum.application.mrp.user.dto.UserRatingsDto;
import at.technikum.server.http.Request;
import at.technikum.server.http.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserControllerUnitTest {

    @Mock
    private UserService userService;

    @Mock
    private Request request;

    private UserController controller;

    @BeforeEach
    void setUp() {
        controller = new UserController(userService);
    }

    // ==================== GET /users/profile Tests ====================

    @Test
    void testHandle_GetProfile_ValidAuth_ReturnsOk() {
        // Arrange
        when(request.getPath()).thenReturn("/users/profile");
        when(request.getMethod()).thenReturn("GET");
        when(request.getAuthorization()).thenReturn("Bearer valid-token");

        UserProfileDto mockProfile = new UserProfileDto(1, "john_doe", "john@example.com");
        when(userService.getProfile("Bearer valid-token")).thenReturn(Optional.of(mockProfile));

        // Act
        Response response = controller.handle(request);

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
        assertTrue(response.getBody().contains("john_doe"));
        assertTrue(response.getBody().contains("john@example.com"));
        verify(userService).getProfile("Bearer valid-token");
    }

    @Test
    void testHandle_GetProfile_InvalidAuth_ReturnsUnauthorized() {
        // Arrange
        when(request.getPath()).thenReturn("/users/profile");
        when(request.getMethod()).thenReturn("GET");
        when(request.getAuthorization()).thenReturn("Bearer invalid-token");

        when(userService.getProfile("Bearer invalid-token")).thenReturn(Optional.empty());

        // Act
        Response response = controller.handle(request);

        // Assert
        assertNotNull(response);
        assertEquals(401, response.getStatusCode());
        assertTrue(response.getBody().contains("error"));
        assertTrue(response.getBody().contains("Authorization"));
        verify(userService).getProfile("Bearer invalid-token");
    }

    @Test
    void testHandle_GetProfile_MissingAuth_ReturnsUnauthorized() {
        // Arrange
        when(request.getPath()).thenReturn("/users/profile");
        when(request.getMethod()).thenReturn("GET");
        when(request.getAuthorization()).thenReturn(null);

        when(userService.getProfile(null)).thenReturn(Optional.empty());

        // Act
        Response response = controller.handle(request);

        // Assert
        assertNotNull(response);
        assertEquals(401, response.getStatusCode());
        assertTrue(response.getBody().contains("error"));
        verify(userService).getProfile(null);
    }

    // ==================== PUT /users/profile Tests ====================

    @Test
    void testHandle_PutProfile_ValidData_ReturnsOk() {
        // Arrange
        when(request.getPath()).thenReturn("/users/profile");
        when(request.getMethod()).thenReturn("PUT");
        when(request.getAuthorization()).thenReturn("Bearer valid-token");

        String requestBody = "{\"email\":\"newemail@example.com\"}";
        when(request.getBody()).thenReturn(requestBody);

        UserProfileDto updatedProfile = new UserProfileDto(1, "john_doe", "newemail@example.com");
        when(userService.updateProfile(eq("Bearer valid-token"), any())).thenReturn(Optional.of(updatedProfile));

        // Act
        Response response = controller.handle(request);

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
        assertTrue(response.getBody().contains("newemail@example.com"));
        verify(userService).updateProfile(eq("Bearer valid-token"), any());
    }

    @Test
    void testHandle_PutProfile_InvalidAuth_ReturnsBadRequest() {
        // Arrange
        when(request.getPath()).thenReturn("/users/profile");
        when(request.getMethod()).thenReturn("PUT");
        when(request.getAuthorization()).thenReturn("Bearer invalid-token");

        String requestBody = "{\"email\":\"newemail@example.com\"}";
        when(request.getBody()).thenReturn(requestBody);

        when(userService.updateProfile(eq("Bearer invalid-token"), any())).thenReturn(Optional.empty());

        // Act
        Response response = controller.handle(request);

        // Assert
        assertNotNull(response);
        assertEquals(400, response.getStatusCode());
        assertTrue(response.getBody().contains("error"));
        verify(userService).updateProfile(eq("Bearer invalid-token"), any());
    }

    @Test
    void testHandle_PutProfile_InvalidJson_ReturnsBadRequest() {
        // Arrange
        when(request.getPath()).thenReturn("/users/profile");
        when(request.getMethod()).thenReturn("PUT");

        String invalidJson = "{invalid json}";
        when(request.getBody()).thenReturn(invalidJson);

        // Act
        Response response = controller.handle(request);

        // Assert
        assertNotNull(response);
        assertEquals(400, response.getStatusCode());
        assertTrue(response.getBody().contains("error"));
        assertTrue(response.getBody().contains("JSON"));
        verify(userService, never()).updateProfile(any(), any());
    }

    @Test
    void testHandle_PutProfile_EmptyBody_ReturnsBadRequest() {
        // Arrange
        when(request.getPath()).thenReturn("/users/profile");
        when(request.getMethod()).thenReturn("PUT");

        when(request.getBody()).thenReturn("");

        // Act
        Response response = controller.handle(request);

        // Assert
        assertNotNull(response);
        assertEquals(400, response.getStatusCode());
        assertTrue(response.getBody().contains("error"));
    }

    @Test
    void testHandle_PutProfile_NullBody_ReturnsBadRequest() {
        // Arrange
        when(request.getPath()).thenReturn("/users/profile");
        when(request.getMethod()).thenReturn("PUT");

        when(request.getBody()).thenReturn(null);

        // Act
        Response response = controller.handle(request);

        // Assert
        assertNotNull(response);
        assertEquals(400, response.getStatusCode());
        assertTrue(response.getBody().contains("error"));
    }

    // ==================== GET /users/ratings Tests ====================

    @Test
    void testHandle_GetRatings_ValidAuth_ReturnsOk() {
        // Arrange
        when(request.getPath()).thenReturn("/users/ratings");
        when(request.getMethod()).thenReturn("GET");
        when(request.getAuthorization()).thenReturn("Bearer valid-token");

        UserRatingsDto mockRatings = new UserRatingsDto(1, createMockRatings());
        when(userService.getRatings("Bearer valid-token")).thenReturn(Optional.of(mockRatings));

        // Act
        Response response = controller.handle(request);

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
        assertTrue(response.getBody().contains("userId"));
        verify(userService).getRatings("Bearer valid-token");
    }

    @Test
    void testHandle_GetRatings_InvalidAuth_ReturnsUnauthorized() {
        // Arrange
        when(request.getPath()).thenReturn("/users/ratings");
        when(request.getMethod()).thenReturn("GET");
        when(request.getAuthorization()).thenReturn("Bearer invalid-token");

        when(userService.getRatings("Bearer invalid-token")).thenReturn(Optional.empty());

        // Act
        Response response = controller.handle(request);

        // Assert
        assertNotNull(response);
        assertEquals(401, response.getStatusCode());
        assertTrue(response.getBody().contains("error"));
        verify(userService).getRatings("Bearer invalid-token");
    }

    @Test
    void testHandle_GetRatings_PostMethod_ReturnsMethodNotAllowed() {
        // Arrange
        when(request.getPath()).thenReturn("/users/ratings");
        when(request.getMethod()).thenReturn("POST");

        // Act
        Response response = controller.handle(request);

        // Assert
        assertNotNull(response);
        assertEquals(405, response.getStatusCode());
        assertTrue(response.getBody().contains("error"));
        assertTrue(response.getBody().contains("Method not allowed"));
        verify(userService, never()).getRatings(any());
    }

    // ==================== GET /users/favorites Tests ====================

    @Test
    void testHandle_GetFavorites_ValidAuth_ReturnsOk() {
        // Arrange
        when(request.getPath()).thenReturn("/users/favorites");
        when(request.getMethod()).thenReturn("GET");
        when(request.getAuthorization()).thenReturn("Bearer valid-token");

        UserFavoritesDto mockFavorites = new UserFavoritesDto(1, createMockFavorites());
        when(userService.getFavorites("Bearer valid-token")).thenReturn(Optional.of(mockFavorites));

        // Act
        Response response = controller.handle(request);

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
        assertTrue(response.getBody().contains("userId"));
        verify(userService).getFavorites("Bearer valid-token");
    }

    @Test
    void testHandle_GetFavorites_InvalidAuth_ReturnsUnauthorized() {
        // Arrange
        when(request.getPath()).thenReturn("/users/favorites");
        when(request.getMethod()).thenReturn("GET");
        when(request.getAuthorization()).thenReturn("Bearer invalid-token");

        when(userService.getFavorites("Bearer invalid-token")).thenReturn(Optional.empty());

        // Act
        Response response = controller.handle(request);

        // Assert
        assertNotNull(response);
        assertEquals(401, response.getStatusCode());
        assertTrue(response.getBody().contains("error"));
        verify(userService).getFavorites("Bearer invalid-token");
    }

    @Test
    void testHandle_GetFavorites_DeleteMethod_ReturnsMethodNotAllowed() {
        // Arrange
        when(request.getPath()).thenReturn("/users/favorites");
        when(request.getMethod()).thenReturn("DELETE");

        // Act
        Response response = controller.handle(request);

        // Assert
        assertNotNull(response);
        assertEquals(405, response.getStatusCode());
        assertTrue(response.getBody().contains("error"));
        verify(userService, never()).getFavorites(any());
    }

    // ==================== Invalid Path Tests ====================

    @Test
    void testHandle_InvalidPath_ReturnsNotFound() {
        // Arrange
        when(request.getPath()).thenReturn("/users/invalid");
        when(request.getMethod()).thenReturn("GET");

        // Act
        Response response = controller.handle(request);

        // Assert
        assertNotNull(response);
        assertEquals(404, response.getStatusCode());
        assertTrue(response.getBody().contains("error"));
        assertTrue(response.getBody().contains("not found"));
    }

    @Test
    void testHandle_RootUserPath_ReturnsNotFound() {
        // Arrange
        when(request.getPath()).thenReturn("/users");
        when(request.getMethod()).thenReturn("GET");

        // Act
        Response response = controller.handle(request);

        // Assert
        assertNotNull(response);
        assertEquals(404, response.getStatusCode());
        assertTrue(response.getBody().contains("error"));
    }

    // ==================== Method Not Allowed Tests ====================

    @Test
    void testHandle_ProfileDeleteMethod_ReturnsMethodNotAllowed() {
        // Arrange
        when(request.getPath()).thenReturn("/users/profile");
        when(request.getMethod()).thenReturn("DELETE");

        // Act
        Response response = controller.handle(request);

        // Assert
        assertNotNull(response);
        assertEquals(405, response.getStatusCode());
        assertTrue(response.getBody().contains("error"));
        assertTrue(response.getBody().contains("Method not allowed"));
    }

    @Test
    void testHandle_ProfilePostMethod_ReturnsMethodNotAllowed() {
        // Arrange
        when(request.getPath()).thenReturn("/users/profile");
        when(request.getMethod()).thenReturn("POST");

        // Act
        Response response = controller.handle(request);

        // Assert
        assertNotNull(response);
        assertEquals(405, response.getStatusCode());
        assertTrue(response.getBody().contains("error"));
    }

    @Test
    void testHandle_RatingsPutMethod_ReturnsMethodNotAllowed() {
        // Arrange
        when(request.getPath()).thenReturn("/users/ratings");
        when(request.getMethod()).thenReturn("PUT");

        // Act
        Response response = controller.handle(request);

        // Assert
        assertNotNull(response);
        assertEquals(405, response.getStatusCode());
        assertTrue(response.getBody().contains("error"));
    }

    @Test
    void testHandle_FavoritesPutMethod_ReturnsMethodNotAllowed() {
        // Arrange
        when(request.getPath()).thenReturn("/users/favorites");
        when(request.getMethod()).thenReturn("PUT");

        // Act
        Response response = controller.handle(request);

        // Assert
        assertNotNull(response);
        assertEquals(405, response.getStatusCode());
        assertTrue(response.getBody().contains("error"));
    }

    // ==================== Helper Methods ====================

    private List<RatingEntity> createMockRatings() {
        List<RatingEntity> ratings = new ArrayList<>();

        RatingEntity rating = new RatingEntity();
        rating.setId(1);
        rating.setUserId(1);
        rating.setMediaId(100);
        rating.setScore(5);
        rating.setComment("Great!");
        rating.setConfirmed(true);
        ratings.add(rating);

        return ratings;
    }

    private List<FavoriteEntity> createMockFavorites() {
        List<FavoriteEntity> favorites = new ArrayList<>();

        FavoriteEntity favorite = new FavoriteEntity();
        favorite.setId(1);
        favorite.setUserId(1);
        favorite.setMediaId(100);
        favorite.setCreatedAt(LocalDateTime.now());
        favorites.add(favorite);

        return favorites;
    }
}

