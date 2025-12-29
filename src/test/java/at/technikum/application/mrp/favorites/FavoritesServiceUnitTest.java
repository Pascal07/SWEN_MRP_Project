package at.technikum.application.mrp.favorites;

import at.technikum.application.mrp.favorites.entity.FavoriteEntity;
import at.technikum.application.mrp.media.MediaRepository;
import at.technikum.application.mrp.media.entity.MediaEntryEntity;
import at.technikum.application.mrp.user.UserRepository;
import at.technikum.application.mrp.user.entity.UserEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FavoritesServiceUnitTest {

    @Mock
    private FavoritesRepository favoritesRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private MediaRepository mediaRepository;

    private FavoritesService favoritesService;

    @BeforeEach
    void setUp() {
        favoritesService = new FavoritesService(favoritesRepository, userRepository, mediaRepository);
    }

    // ==================== listFavorites Tests ====================

    @Test
    void testListFavorites_ValidAuth_ReturnsList() {
        // Arrange
        String authHeader = "Bearer valid-token";
        UserEntity mockUser = createMockUser(1, "john_doe");
        when(userRepository.findByToken("valid-token")).thenReturn(Optional.of(mockUser));

        FavoriteEntity fav1 = new FavoriteEntity(1, 100);
        FavoriteEntity fav2 = new FavoriteEntity(1, 200);
        when(favoritesRepository.findByUserId(1)).thenReturn(Arrays.asList(fav1, fav2));

        // Act
        List<Integer> result = favoritesService.listFavorites(authHeader);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.contains(100));
        assertTrue(result.contains(200));
        verify(favoritesRepository).findByUserId(1);
    }

    @Test
    void testListFavorites_NoFavorites_ReturnsEmptyList() {
        // Arrange
        String authHeader = "Bearer valid-token";
        UserEntity mockUser = createMockUser(1, "john_doe");
        when(userRepository.findByToken("valid-token")).thenReturn(Optional.of(mockUser));
        when(favoritesRepository.findByUserId(1)).thenReturn(List.of());

        // Act
        List<Integer> result = favoritesService.listFavorites(authHeader);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(favoritesRepository).findByUserId(1);
    }

    @Test
    void testListFavorites_InvalidToken_ThrowsSecurityException() {
        // Arrange
        String authHeader = "Bearer invalid-token";
        when(userRepository.findByToken("invalid-token")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(SecurityException.class, () -> favoritesService.listFavorites(authHeader));
        verify(favoritesRepository, never()).findByUserId(anyInt());
    }

    @Test
    void testListFavorites_NullAuth_ThrowsSecurityException() {
        // Act & Assert
        assertThrows(SecurityException.class, () -> favoritesService.listFavorites(null));
        verify(userRepository, never()).findByToken(any());
    }

    @Test
    void testListFavorites_MissingBearerPrefix_ThrowsSecurityException() {
        // Arrange
        String authHeader = "valid-token";

        // Act & Assert
        assertThrows(SecurityException.class, () -> favoritesService.listFavorites(authHeader));
        verify(userRepository, never()).findByToken(any());
    }

    // ==================== addFavorite Tests ====================

    @Test
    void testAddFavorite_ValidRequest_AddsFavorite() {
        // Arrange
        String authHeader = "Bearer valid-token";
        int mediaId = 100;

        UserEntity mockUser = createMockUser(1, "john_doe");
        MediaEntryEntity mockMedia = new MediaEntryEntity();
        mockMedia.setId(mediaId);

        when(userRepository.findByToken("valid-token")).thenReturn(Optional.of(mockUser));
        when(mediaRepository.findById(mediaId)).thenReturn(Optional.of(mockMedia));
        when(favoritesRepository.exists(1, mediaId)).thenReturn(false);

        // Act
        favoritesService.addFavorite(authHeader, mediaId);

        // Assert
        ArgumentCaptor<FavoriteEntity> captor = ArgumentCaptor.forClass(FavoriteEntity.class);
        verify(favoritesRepository).create(captor.capture());

        FavoriteEntity saved = captor.getValue();
        assertEquals(1, saved.getUserId());
        assertEquals(mediaId, saved.getMediaId());
    }

    @Test
    void testAddFavorite_MediaNotFound_ThrowsException() {
        // Arrange
        String authHeader = "Bearer valid-token";
        int mediaId = 999;

        UserEntity mockUser = createMockUser(1, "john_doe");
        when(userRepository.findByToken("valid-token")).thenReturn(Optional.of(mockUser));
        when(mediaRepository.findById(mediaId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> favoritesService.addFavorite(authHeader, mediaId));
        verify(favoritesRepository, never()).create(any());
    }

    @Test
    void testAddFavorite_AlreadyExists_ThrowsException() {
        // Arrange
        String authHeader = "Bearer valid-token";
        int mediaId = 100;

        UserEntity mockUser = createMockUser(1, "john_doe");
        MediaEntryEntity mockMedia = new MediaEntryEntity();
        mockMedia.setId(mediaId);

        when(userRepository.findByToken("valid-token")).thenReturn(Optional.of(mockUser));
        when(mediaRepository.findById(mediaId)).thenReturn(Optional.of(mockMedia));
        when(favoritesRepository.exists(1, mediaId)).thenReturn(true);

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> favoritesService.addFavorite(authHeader, mediaId));
        verify(favoritesRepository, never()).create(any());
    }

    @Test
    void testAddFavorite_InvalidMediaId_ThrowsException() {
        // Arrange
        String authHeader = "Bearer valid-token";
        int invalidMediaId = 0;

        UserEntity mockUser = createMockUser(1, "john_doe");
        when(userRepository.findByToken("valid-token")).thenReturn(Optional.of(mockUser));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> favoritesService.addFavorite(authHeader, invalidMediaId));
        verify(mediaRepository, never()).findById(anyInt());
        verify(favoritesRepository, never()).create(any());
    }

    @Test
    void testAddFavorite_NegativeMediaId_ThrowsException() {
        // Arrange
        String authHeader = "Bearer valid-token";
        int invalidMediaId = -1;

        UserEntity mockUser = createMockUser(1, "john_doe");
        when(userRepository.findByToken("valid-token")).thenReturn(Optional.of(mockUser));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> favoritesService.addFavorite(authHeader, invalidMediaId));
        verify(mediaRepository, never()).findById(anyInt());
        verify(favoritesRepository, never()).create(any());
    }

    // ==================== removeFavorite Tests ====================

    @Test
    void testRemoveFavorite_ValidRequest_RemovesFavorite() {
        // Arrange
        String authHeader = "Bearer valid-token";
        int mediaId = 100;

        UserEntity mockUser = createMockUser(1, "john_doe");
        when(userRepository.findByToken("valid-token")).thenReturn(Optional.of(mockUser));
        when(favoritesRepository.delete(1, mediaId)).thenReturn(true);

        // Act
        favoritesService.removeFavorite(authHeader, mediaId);

        // Assert
        verify(favoritesRepository).delete(1, mediaId);
    }

    @Test
    void testRemoveFavorite_NotFound_ThrowsException() {
        // Arrange
        String authHeader = "Bearer valid-token";
        int mediaId = 100;

        UserEntity mockUser = createMockUser(1, "john_doe");
        when(userRepository.findByToken("valid-token")).thenReturn(Optional.of(mockUser));
        when(favoritesRepository.delete(1, mediaId)).thenReturn(false);

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> favoritesService.removeFavorite(authHeader, mediaId));
        verify(favoritesRepository).delete(1, mediaId);
    }

    @Test
    void testRemoveFavorite_InvalidMediaId_ThrowsException() {
        // Arrange
        String authHeader = "Bearer valid-token";
        int invalidMediaId = 0;

        UserEntity mockUser = createMockUser(1, "john_doe");
        when(userRepository.findByToken("valid-token")).thenReturn(Optional.of(mockUser));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> favoritesService.removeFavorite(authHeader, invalidMediaId));
        verify(favoritesRepository, never()).delete(anyInt(), anyInt());
    }

    @Test
    void testRemoveFavorite_InvalidAuth_ThrowsSecurityException() {
        // Arrange
        String authHeader = "Bearer invalid-token";
        when(userRepository.findByToken("invalid-token")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(SecurityException.class, () -> favoritesService.removeFavorite(authHeader, 100));
        verify(favoritesRepository, never()).delete(anyInt(), anyInt());
    }

    // ==================== Helper Methods ====================

    private UserEntity createMockUser(int id, String username) {
        UserEntity user = new UserEntity();
        user.setId(id);
        user.setUsername(username);
        return user;
    }
}

