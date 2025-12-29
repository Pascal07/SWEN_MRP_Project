package at.technikum.application.mrp.favorites;

import at.technikum.server.http.Request;
import at.technikum.server.http.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FavoritesControllerUnitTest {

    @Mock
    private FavoritesService favoritesService;

    @Mock
    private Request request;

    private FavoritesController controller;

    @BeforeEach
    void setUp() {
        controller = new FavoritesController(favoritesService);
    }

    // ==================== GET /favorite Tests ====================

    @Test
    void testHandle_ListFavorites_ReturnsOk() {
        // Arrange
        when(request.getPath()).thenReturn("/favorite");
        when(request.getMethod()).thenReturn("GET");
        when(request.getAuthorization()).thenReturn("Bearer valid-token");

        List<Integer> mockFavorites = Arrays.asList(100, 200, 300);
        when(favoritesService.listFavorites("Bearer valid-token")).thenReturn(mockFavorites);

        // Act
        Response response = controller.handle(request);

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
        assertTrue(response.getBody().contains("100"));
        assertTrue(response.getBody().contains("200"));
        assertTrue(response.getBody().contains("300"));
        verify(favoritesService).listFavorites("Bearer valid-token");
    }

    @Test
    void testHandle_ListFavorites_EmptyList_ReturnsOk() {
        // Arrange
        when(request.getPath()).thenReturn("/favorite");
        when(request.getMethod()).thenReturn("GET");
        when(request.getAuthorization()).thenReturn("Bearer valid-token");
        when(favoritesService.listFavorites("Bearer valid-token")).thenReturn(List.of());

        // Act
        Response response = controller.handle(request);

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
        assertTrue(response.getBody().contains("[]"));
        verify(favoritesService).listFavorites("Bearer valid-token");
    }

    @Test
    void testHandle_ListFavorites_WrongMethod_ThrowsException() {
        // Arrange
        when(request.getPath()).thenReturn("/favorite");
        when(request.getMethod()).thenReturn("POST");

        // Act & Assert
        assertThrows(UnsupportedOperationException.class, () -> controller.handle(request));
        verify(favoritesService, never()).listFavorites(any());
    }

    // ==================== POST /favorite/media/{mediaId} Tests ====================

    @Test
    void testHandle_AddFavorite_ValidRequest_ReturnsOk() {
        // Arrange
        when(request.getPath()).thenReturn("/favorite/media/100");
        when(request.getMethod()).thenReturn("POST");
        when(request.getAuthorization()).thenReturn("Bearer valid-token");

        doNothing().when(favoritesService).addFavorite("Bearer valid-token", 100);

        // Act
        Response response = controller.handle(request);

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
        assertTrue(response.getBody().contains("Favorite added"));
        verify(favoritesService).addFavorite("Bearer valid-token", 100);
    }

    @Test
    void testHandle_AddFavorite_InvalidMediaId_ThrowsException() {
        // Arrange
        when(request.getPath()).thenReturn("/favorite/media/invalid");
        when(request.getMethod()).thenReturn("POST");

        // Act & Assert
        assertThrows(NoSuchElementException.class, () -> controller.handle(request));
        verify(favoritesService, never()).addFavorite(any(), anyInt());
    }

    @Test
    void testHandle_AddFavorite_ServiceThrowsException_Propagates() {
        // Arrange
        when(request.getPath()).thenReturn("/favorite/media/100");
        when(request.getMethod()).thenReturn("POST");
        when(request.getAuthorization()).thenReturn("Bearer valid-token");

        doThrow(new IllegalStateException("Already in favorites"))
            .when(favoritesService).addFavorite("Bearer valid-token", 100);

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> controller.handle(request));
        verify(favoritesService).addFavorite("Bearer valid-token", 100);
    }

    // ==================== DELETE /favorite/media/{mediaId} Tests ====================

    @Test
    void testHandle_RemoveFavorite_ValidRequest_ReturnsOk() {
        // Arrange
        when(request.getPath()).thenReturn("/favorite/media/100");
        when(request.getMethod()).thenReturn("DELETE");
        when(request.getAuthorization()).thenReturn("Bearer valid-token");

        doNothing().when(favoritesService).removeFavorite("Bearer valid-token", 100);

        // Act
        Response response = controller.handle(request);

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
        assertTrue(response.getBody().contains("Favorite removed"));
        verify(favoritesService).removeFavorite("Bearer valid-token", 100);
    }

    @Test
    void testHandle_RemoveFavorite_InvalidMediaId_ThrowsException() {
        // Arrange
        when(request.getPath()).thenReturn("/favorite/media/abc");
        when(request.getMethod()).thenReturn("DELETE");

        // Act & Assert
        assertThrows(NoSuchElementException.class, () -> controller.handle(request));
        verify(favoritesService, never()).removeFavorite(any(), anyInt());
    }

    @Test
    void testHandle_RemoveFavorite_ServiceThrowsException_Propagates() {
        // Arrange
        when(request.getPath()).thenReturn("/favorite/media/100");
        when(request.getMethod()).thenReturn("DELETE");
        when(request.getAuthorization()).thenReturn("Bearer valid-token");

        doThrow(new IllegalStateException("Favorite not found"))
            .when(favoritesService).removeFavorite("Bearer valid-token", 100);

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> controller.handle(request));
        verify(favoritesService).removeFavorite("Bearer valid-token", 100);
    }

    @Test
    void testHandle_MediaPath_UnsupportedMethod_ThrowsException() {
        // Arrange
        when(request.getPath()).thenReturn("/favorite/media/100");
        when(request.getMethod()).thenReturn("GET");

        // Act & Assert
        assertThrows(UnsupportedOperationException.class, () -> controller.handle(request));
        verify(favoritesService, never()).addFavorite(any(), anyInt());
        verify(favoritesService, never()).removeFavorite(any(), anyInt());
    }

    // ==================== Route Not Found Tests ====================

    @Test
    void testHandle_UnknownRoute_ThrowsException() {
        // Arrange
        when(request.getPath()).thenReturn("/favorite/unknown");
        when(request.getMethod()).thenReturn("GET");

        // Act & Assert
        assertThrows(NoSuchElementException.class, () -> controller.handle(request));
        verify(favoritesService, never()).listFavorites(any());
    }

    @Test
    void testHandle_InvalidPath_ThrowsException() {
        // Arrange
        when(request.getPath()).thenReturn("/invalid");
        when(request.getMethod()).thenReturn("GET");

        // Act & Assert
        assertThrows(NoSuchElementException.class, () -> controller.handle(request));
    }

    // ==================== Security Tests ====================

    @Test
    void testHandle_AddFavorite_SecurityException_Propagates() {
        // Arrange
        when(request.getPath()).thenReturn("/favorite/media/100");
        when(request.getMethod()).thenReturn("POST");
        when(request.getAuthorization()).thenReturn("Bearer invalid-token");

        doThrow(new SecurityException("Invalid token"))
            .when(favoritesService).addFavorite("Bearer invalid-token", 100);

        // Act & Assert
        assertThrows(SecurityException.class, () -> controller.handle(request));
    }

    @Test
    void testHandle_ListFavorites_SecurityException_Propagates() {
        // Arrange
        when(request.getPath()).thenReturn("/favorite");
        when(request.getMethod()).thenReturn("GET");
        when(request.getAuthorization()).thenReturn("Bearer invalid-token");

        doThrow(new SecurityException("Invalid token"))
            .when(favoritesService).listFavorites("Bearer invalid-token");

        // Act & Assert
        assertThrows(SecurityException.class, () -> controller.handle(request));
    }
}

