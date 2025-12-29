package at.technikum.application.mrp.user;

import at.technikum.application.mrp.favorites.entity.FavoriteEntity;
import at.technikum.application.mrp.rating.entity.RatingEntity;
import at.technikum.application.mrp.user.dto.UpdateProfileDto;
import at.technikum.application.mrp.user.dto.UserFavoritesDto;
import at.technikum.application.mrp.user.dto.UserProfileDto;
import at.technikum.application.mrp.user.dto.UserRatingsDto;
import at.technikum.application.mrp.user.entity.UserEntity;
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
class UserServiceUnitTest {

    @Mock
    private UserRepository userRepository;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository);
    }

    // ==================== getProfile Tests ====================

    @Test
    void testGetProfile_ValidToken_ReturnsProfile() {
        // Arrange
        String authHeader = "Bearer valid-token";
        UserEntity mockUser = createMockUser(1, "john_doe", "john@example.com");
        when(userRepository.findByToken("valid-token")).thenReturn(Optional.of(mockUser));

        // Act
        Optional<UserProfileDto> result = userService.getProfile(authHeader);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(1, result.get().getUserId());
        assertEquals("john_doe", result.get().getUsername());
        assertEquals("john@example.com", result.get().getEmail());
        verify(userRepository).findByToken("valid-token");
    }

    @Test
    void testGetProfile_InvalidToken_ReturnsEmpty() {
        // Arrange
        String authHeader = "Bearer invalid-token";
        when(userRepository.findByToken("invalid-token")).thenReturn(Optional.empty());

        // Act
        Optional<UserProfileDto> result = userService.getProfile(authHeader);

        // Assert
        assertFalse(result.isPresent());
        verify(userRepository).findByToken("invalid-token");
    }

    @Test
    void testGetProfile_NullAuthHeader_ReturnsEmpty() {
        // Arrange
        String authHeader = null;

        // Act
        Optional<UserProfileDto> result = userService.getProfile(authHeader);

        // Assert
        assertFalse(result.isPresent());
        verify(userRepository, never()).findByToken(any());
    }

    @Test
    void testGetProfile_EmptyAuthHeader_ReturnsEmpty() {
        // Arrange
        String authHeader = "";

        // Act
        Optional<UserProfileDto> result = userService.getProfile(authHeader);

        // Assert
        assertFalse(result.isPresent());
        verify(userRepository, never()).findByToken(any());
    }

    @Test
    void testGetProfile_MissingBearerPrefix_ReturnsEmpty() {
        // Arrange
        String authHeader = "valid-token";

        // Act
        Optional<UserProfileDto> result = userService.getProfile(authHeader);

        // Assert
        assertFalse(result.isPresent());
        verify(userRepository, never()).findByToken(any());
    }

    @Test
    void testGetProfile_BearerWithEmptyToken_ReturnsEmpty() {
        // Arrange
        String authHeader = "Bearer ";

        // Act
        Optional<UserProfileDto> result = userService.getProfile(authHeader);

        // Assert
        assertFalse(result.isPresent());
        verify(userRepository, never()).findByToken(any());
    }

    @Test
    void testGetProfile_BearerWithWhitespaceToken_ReturnsEmpty() {
        // Arrange
        String authHeader = "Bearer    ";

        // Act
        Optional<UserProfileDto> result = userService.getProfile(authHeader);

        // Assert
        assertFalse(result.isPresent());
        verify(userRepository, never()).findByToken(any());
    }

    // ==================== getRatings Tests ====================

    @Test
    void testGetRatings_ValidToken_ReturnsRatings() {
        // Arrange
        String authHeader = "Bearer valid-token";
        UserEntity mockUser = createMockUser(1, "john_doe", "john@example.com");
        List<RatingEntity> mockRatings = createMockRatings();

        when(userRepository.findByToken("valid-token")).thenReturn(Optional.of(mockUser));
        when(userRepository.findRatingsByUserId(1)).thenReturn(mockRatings);

        // Act
        Optional<UserRatingsDto> result = userService.getRatings(authHeader);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(1, result.get().getUserId());
        assertEquals(2, result.get().getRatings().size());
        verify(userRepository).findByToken("valid-token");
        verify(userRepository).findRatingsByUserId(1);
    }

    @Test
    void testGetRatings_InvalidToken_ReturnsEmpty() {
        // Arrange
        String authHeader = "Bearer invalid-token";
        when(userRepository.findByToken("invalid-token")).thenReturn(Optional.empty());

        // Act
        Optional<UserRatingsDto> result = userService.getRatings(authHeader);

        // Assert
        assertFalse(result.isPresent());
        verify(userRepository).findByToken("invalid-token");
        verify(userRepository, never()).findRatingsByUserId(any());
    }

    @Test
    void testGetRatings_EmptyRatingsList_ReturnsEmptyList() {
        // Arrange
        String authHeader = "Bearer valid-token";
        UserEntity mockUser = createMockUser(1, "john_doe", "john@example.com");

        when(userRepository.findByToken("valid-token")).thenReturn(Optional.of(mockUser));
        when(userRepository.findRatingsByUserId(1)).thenReturn(new ArrayList<>());

        // Act
        Optional<UserRatingsDto> result = userService.getRatings(authHeader);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(0, result.get().getRatings().size());
        verify(userRepository).findRatingsByUserId(1);
    }

    // ==================== getFavorites Tests ====================

    @Test
    void testGetFavorites_ValidToken_ReturnsFavorites() {
        // Arrange
        String authHeader = "Bearer valid-token";
        UserEntity mockUser = createMockUser(1, "john_doe", "john@example.com");
        List<FavoriteEntity> mockFavorites = createMockFavorites();

        when(userRepository.findByToken("valid-token")).thenReturn(Optional.of(mockUser));
        when(userRepository.findFavoritesByUserId(1)).thenReturn(mockFavorites);

        // Act
        Optional<UserFavoritesDto> result = userService.getFavorites(authHeader);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(1, result.get().getUserId());
        assertEquals(2, result.get().getFavorites().size());
        verify(userRepository).findByToken("valid-token");
        verify(userRepository).findFavoritesByUserId(1);
    }

    @Test
    void testGetFavorites_InvalidToken_ReturnsEmpty() {
        // Arrange
        String authHeader = "Bearer invalid-token";
        when(userRepository.findByToken("invalid-token")).thenReturn(Optional.empty());

        // Act
        Optional<UserFavoritesDto> result = userService.getFavorites(authHeader);

        // Assert
        assertFalse(result.isPresent());
        verify(userRepository).findByToken("invalid-token");
        verify(userRepository, never()).findFavoritesByUserId(any());
    }

    @Test
    void testGetFavorites_EmptyFavoritesList_ReturnsEmptyList() {
        // Arrange
        String authHeader = "Bearer valid-token";
        UserEntity mockUser = createMockUser(1, "john_doe", "john@example.com");

        when(userRepository.findByToken("valid-token")).thenReturn(Optional.of(mockUser));
        when(userRepository.findFavoritesByUserId(1)).thenReturn(new ArrayList<>());

        // Act
        Optional<UserFavoritesDto> result = userService.getFavorites(authHeader);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(0, result.get().getFavorites().size());
        verify(userRepository).findFavoritesByUserId(1);
    }

    // ==================== updateProfile Tests ====================

    @Test
    void testUpdateProfile_ValidTokenAndEmail_ReturnsUpdatedProfile() {
        // Arrange
        String authHeader = "Bearer valid-token";
        UserEntity mockUser = createMockUser(1, "john_doe", "john@example.com");
        UpdateProfileDto updateDto = new UpdateProfileDto();
        updateDto.setEmail("newemail@example.com");

        when(userRepository.findByToken("valid-token")).thenReturn(Optional.of(mockUser));
        when(userRepository.updateProfile(1, "newemail@example.com")).thenReturn(true);

        // Act
        Optional<UserProfileDto> result = userService.updateProfile(authHeader, updateDto);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(1, result.get().getUserId());
        assertEquals("john_doe", result.get().getUsername());
        assertEquals("newemail@example.com", result.get().getEmail());
        verify(userRepository).findByToken("valid-token");
        verify(userRepository).updateProfile(1, "newemail@example.com");
    }

    @Test
    void testUpdateProfile_InvalidToken_ReturnsEmpty() {
        // Arrange
        String authHeader = "Bearer invalid-token";
        UpdateProfileDto updateDto = new UpdateProfileDto();
        updateDto.setEmail("newemail@example.com");

        when(userRepository.findByToken("invalid-token")).thenReturn(Optional.empty());

        // Act
        Optional<UserProfileDto> result = userService.updateProfile(authHeader, updateDto);

        // Assert
        assertFalse(result.isPresent());
        verify(userRepository).findByToken("invalid-token");
        verify(userRepository, never()).updateProfile(any(), any());
    }

    @Test
    void testUpdateProfile_NullEmail_ReturnsEmpty() {
        // Arrange
        String authHeader = "Bearer valid-token";
        UserEntity mockUser = createMockUser(1, "john_doe", "john@example.com");
        UpdateProfileDto updateDto = new UpdateProfileDto();
        updateDto.setEmail(null);

        when(userRepository.findByToken("valid-token")).thenReturn(Optional.of(mockUser));

        // Act
        Optional<UserProfileDto> result = userService.updateProfile(authHeader, updateDto);

        // Assert
        assertFalse(result.isPresent());
        verify(userRepository).findByToken("valid-token");
        verify(userRepository, never()).updateProfile(any(), any());
    }

    @Test
    void testUpdateProfile_EmptyEmail_ReturnsEmpty() {
        // Arrange
        String authHeader = "Bearer valid-token";
        UserEntity mockUser = createMockUser(1, "john_doe", "john@example.com");
        UpdateProfileDto updateDto = new UpdateProfileDto();
        updateDto.setEmail("");

        when(userRepository.findByToken("valid-token")).thenReturn(Optional.of(mockUser));

        // Act
        Optional<UserProfileDto> result = userService.updateProfile(authHeader, updateDto);

        // Assert
        assertFalse(result.isPresent());
        verify(userRepository).findByToken("valid-token");
        verify(userRepository, never()).updateProfile(any(), any());
    }

    @Test
    void testUpdateProfile_WhitespaceEmail_ReturnsEmpty() {
        // Arrange
        String authHeader = "Bearer valid-token";
        UserEntity mockUser = createMockUser(1, "john_doe", "john@example.com");
        UpdateProfileDto updateDto = new UpdateProfileDto();
        updateDto.setEmail("   ");

        when(userRepository.findByToken("valid-token")).thenReturn(Optional.of(mockUser));

        // Act
        Optional<UserProfileDto> result = userService.updateProfile(authHeader, updateDto);

        // Assert
        assertFalse(result.isPresent());
        verify(userRepository).findByToken("valid-token");
        verify(userRepository, never()).updateProfile(any(), any());
    }

    @Test
    void testUpdateProfile_UpdateFails_ReturnsEmpty() {
        // Arrange
        String authHeader = "Bearer valid-token";
        UserEntity mockUser = createMockUser(1, "john_doe", "john@example.com");
        UpdateProfileDto updateDto = new UpdateProfileDto();
        updateDto.setEmail("newemail@example.com");

        when(userRepository.findByToken("valid-token")).thenReturn(Optional.of(mockUser));
        when(userRepository.updateProfile(1, "newemail@example.com")).thenReturn(false);

        // Act
        Optional<UserProfileDto> result = userService.updateProfile(authHeader, updateDto);

        // Assert
        assertFalse(result.isPresent());
        verify(userRepository).findByToken("valid-token");
        verify(userRepository).updateProfile(1, "newemail@example.com");
    }

    // ==================== Helper Methods ====================

    private UserEntity createMockUser(Integer id, String username, String email) {
        UserEntity user = new UserEntity();
        user.setId(id);
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword("hashedPassword");
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        return user;
    }

    private List<RatingEntity> createMockRatings() {
        List<RatingEntity> ratings = new ArrayList<>();

        RatingEntity rating1 = new RatingEntity();
        rating1.setId(1);
        rating1.setUserId(1);
        rating1.setMediaId(100);
        rating1.setScore(5);
        rating1.setComment("Great movie!");
        rating1.setConfirmed(true);
        rating1.setTimestamp(System.currentTimeMillis());
        ratings.add(rating1);

        RatingEntity rating2 = new RatingEntity();
        rating2.setId(2);
        rating2.setUserId(1);
        rating2.setMediaId(101);
        rating2.setScore(4);
        rating2.setComment("Good series");
        rating2.setConfirmed(true);
        rating2.setTimestamp(System.currentTimeMillis());
        ratings.add(rating2);

        return ratings;
    }

    private List<FavoriteEntity> createMockFavorites() {
        List<FavoriteEntity> favorites = new ArrayList<>();

        FavoriteEntity favorite1 = new FavoriteEntity();
        favorite1.setId(1);
        favorite1.setUserId(1);
        favorite1.setMediaId(100);
        favorite1.setCreatedAt(LocalDateTime.now());
        favorites.add(favorite1);

        FavoriteEntity favorite2 = new FavoriteEntity();
        favorite2.setId(2);
        favorite2.setUserId(1);
        favorite2.setMediaId(101);
        favorite2.setCreatedAt(LocalDateTime.now());
        favorites.add(favorite2);

        return favorites;
    }
}

