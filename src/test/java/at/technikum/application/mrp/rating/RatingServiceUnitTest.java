package at.technikum.application.mrp.rating;

import at.technikum.application.mrp.media.MediaRepository;
import at.technikum.application.mrp.media.entity.MediaEntryEntity;
import at.technikum.application.mrp.rating.dto.RatingDetailDto;
import at.technikum.application.mrp.rating.dto.RatingUpsertDto;
import at.technikum.application.mrp.rating.entity.RatingEntity;
import at.technikum.application.mrp.user.UserRepository;
import at.technikum.application.mrp.user.entity.UserEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RatingServiceUnitTest {

    @Mock
    private RatingRepository ratingRepository;

    @Mock
    private MediaRepository mediaRepository;

    @Mock
    private UserRepository userRepository;

    private RatingService ratingService;

    @BeforeEach
    void setUp() {
        ratingService = new RatingService(ratingRepository, mediaRepository, userRepository);
    }

    // ==================== getAuthorizedUserId Tests ====================

    @Test
    void testGetAuthorizedUserId_ValidToken_ReturnsUserId() {
        // Arrange
        String authHeader = "Bearer valid-token";
        UserEntity mockUser = createMockUser(1, "john_doe");
        when(userRepository.findByToken("valid-token")).thenReturn(Optional.of(mockUser));

        // Act
        Optional<Integer> result = ratingService.getAuthorizedUserId(authHeader);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(1, result.get());
        verify(userRepository).findByToken("valid-token");
    }

    @Test
    void testGetAuthorizedUserId_InvalidToken_ReturnsEmpty() {
        // Arrange
        String authHeader = "Bearer invalid-token";
        when(userRepository.findByToken("invalid-token")).thenReturn(Optional.empty());

        // Act
        Optional<Integer> result = ratingService.getAuthorizedUserId(authHeader);

        // Assert
        assertFalse(result.isPresent());
        verify(userRepository).findByToken("invalid-token");
    }

    @Test
    void testGetAuthorizedUserId_NullAuthHeader_ReturnsEmpty() {
        // Act
        Optional<Integer> result = ratingService.getAuthorizedUserId(null);

        // Assert
        assertFalse(result.isPresent());
        verify(userRepository, never()).findByToken(any());
    }

    @Test
    void testGetAuthorizedUserId_MissingBearerPrefix_ReturnsEmpty() {
        // Arrange
        String authHeader = "valid-token";

        // Act
        Optional<Integer> result = ratingService.getAuthorizedUserId(authHeader);

        // Assert
        assertFalse(result.isPresent());
        verify(userRepository, never()).findByToken(any());
    }

    // ==================== create Tests ====================

    @Test
    void testCreate_ValidDto_CreatesRating() {
        // Arrange
        int userId = 1;
        RatingUpsertDto dto = new RatingUpsertDto();
        dto.setMediaId(100);
        dto.setScore(5);
        dto.setComment("Great movie!");

        MediaEntryEntity mockMedia = new MediaEntryEntity();
        mockMedia.setId(100);
        mockMedia.setTitle("Test Movie");

        RatingEntity savedRating = new RatingEntity();
        savedRating.setId(1);
        savedRating.setUserId(userId);
        savedRating.setMediaId(100);
        savedRating.setScore(5);
        savedRating.setComment("Great movie!");
        savedRating.setConfirmed(false);

        when(mediaRepository.findById(100)).thenReturn(Optional.of(mockMedia));
        when(ratingRepository.create(any(RatingEntity.class))).thenReturn(savedRating);
        when(ratingRepository.findByMediaId(100)).thenReturn(Collections.singletonList(savedRating));
        when(mediaRepository.findById(100)).thenReturn(Optional.of(mockMedia));

        // Act
        RatingDetailDto result = ratingService.create(userId, dto);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals(100, result.getMediaId());
        assertEquals(userId, result.getUserId());
        assertEquals(5, result.getScore());
        assertFalse(result.isConfirmed());
        verify(ratingRepository).create(any(RatingEntity.class));
        verify(mediaRepository).update(mockMedia);
    }

    @Test
    void testCreate_MediaNotFound_ThrowsException() {
        // Arrange
        int userId = 1;
        RatingUpsertDto dto = new RatingUpsertDto();
        dto.setMediaId(999);
        dto.setScore(5);

        when(mediaRepository.findById(999)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> ratingService.create(userId, dto));
        verify(ratingRepository, never()).create(any());
    }

    @Test
    void testCreate_InvalidScore_ThrowsException() {
        // Arrange
        int userId = 1;
        RatingUpsertDto dto = new RatingUpsertDto();
        dto.setMediaId(100);
        dto.setScore(6); // Invalid score > 5

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> ratingService.create(userId, dto));
        verify(mediaRepository, never()).findById(anyInt());
    }

    @Test
    void testCreate_ScoreTooLow_ThrowsException() {
        // Arrange
        int userId = 1;
        RatingUpsertDto dto = new RatingUpsertDto();
        dto.setMediaId(100);
        dto.setScore(0); // Invalid score < 1

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> ratingService.create(userId, dto));
        verify(mediaRepository, never()).findById(anyInt());
    }

    // ==================== getById Tests ====================

    @Test
    void testGetById_ExistingRating_ReturnsRating() {
        // Arrange
        int userId = 1;
        RatingEntity mockRating = createMockRating(1, userId, 100, 5, "Great!");

        when(ratingRepository.findById(1)).thenReturn(Optional.of(mockRating));

        // Act
        Optional<RatingDetailDto> result = ratingService.getById(userId, 1);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(1, result.get().getId());
        verify(ratingRepository).findById(1);
    }

    @Test
    void testGetById_NonExistingRating_ReturnsEmpty() {
        // Arrange
        when(ratingRepository.findById(999)).thenReturn(Optional.empty());

        // Act
        Optional<RatingDetailDto> result = ratingService.getById(1, 999);

        // Assert
        assertFalse(result.isPresent());
        verify(ratingRepository).findById(999);
    }

    // ==================== listByMedia Tests ====================

    @Test
    void testListByMedia_ValidMedia_ReturnsList() {
        // Arrange
        int userId = 1;
        int mediaId = 100;

        MediaEntryEntity mockMedia = new MediaEntryEntity();
        mockMedia.setId(mediaId);

        RatingEntity rating1 = createMockRating(1, userId, mediaId, 5, "Great!");
        RatingEntity rating2 = createMockRating(2, 2, mediaId, 4, "Good");

        when(mediaRepository.findById(mediaId)).thenReturn(Optional.of(mockMedia));
        when(ratingRepository.findByMediaId(mediaId)).thenReturn(Arrays.asList(rating1, rating2));

        // Act
        List<RatingDetailDto> result = ratingService.listByMedia(userId, mediaId);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(ratingRepository).findByMediaId(mediaId);
    }

    @Test
    void testListByMedia_MediaNotFound_ThrowsException() {
        // Arrange
        when(mediaRepository.findById(999)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> ratingService.listByMedia(1, 999));
        verify(ratingRepository, never()).findByMediaId(anyInt());
    }

    // ==================== update Tests ====================

    @Test
    void testUpdate_ValidUpdate_UpdatesRating() {
        // Arrange
        int userId = 1;
        int ratingId = 1;

        RatingEntity existing = createMockRating(ratingId, userId, 100, 4, "Good");

        RatingUpsertDto updateDto = new RatingUpsertDto();
        updateDto.setScore(5);
        updateDto.setComment("Excellent!");

        when(ratingRepository.findById(ratingId)).thenReturn(Optional.of(existing));
        when(ratingRepository.update(any(RatingEntity.class))).thenReturn(existing);
        when(ratingRepository.findByMediaId(100)).thenReturn(Collections.singletonList(existing));

        MediaEntryEntity mockMedia = new MediaEntryEntity();
        mockMedia.setId(100);
        when(mediaRepository.findById(100)).thenReturn(Optional.of(mockMedia));

        // Act
        Optional<RatingDetailDto> result = ratingService.update(userId, ratingId, updateDto);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(5, result.get().getScore());
        verify(ratingRepository).update(any(RatingEntity.class));
    }

    @Test
    void testUpdate_NotOwner_ThrowsSecurityException() {
        // Arrange
        int userId = 1;
        int otherUserId = 2;
        int ratingId = 1;

        RatingEntity existing = createMockRating(ratingId, otherUserId, 100, 4, "Good");

        RatingUpsertDto updateDto = new RatingUpsertDto();
        updateDto.setScore(5);

        when(ratingRepository.findById(ratingId)).thenReturn(Optional.of(existing));

        // Act & Assert
        assertThrows(SecurityException.class, () -> ratingService.update(userId, ratingId, updateDto));
        verify(ratingRepository, never()).update(any());
    }

    @Test
    void testUpdate_NonExisting_ReturnsEmpty() {
        // Arrange
        RatingUpsertDto updateDto = new RatingUpsertDto();
        updateDto.setScore(5);

        when(ratingRepository.findById(999)).thenReturn(Optional.empty());

        // Act
        Optional<RatingDetailDto> result = ratingService.update(1, 999, updateDto);

        // Assert
        assertFalse(result.isPresent());
        verify(ratingRepository, never()).update(any());
    }

    // ==================== delete Tests ====================

    @Test
    void testDelete_ValidDelete_ReturnsTrue() {
        // Arrange
        int userId = 1;
        int ratingId = 1;

        RatingEntity existing = createMockRating(ratingId, userId, 100, 5, "Great!");

        when(ratingRepository.findById(ratingId)).thenReturn(Optional.of(existing));
        when(ratingRepository.delete(ratingId)).thenReturn(true);
        when(ratingRepository.findByMediaId(100)).thenReturn(Collections.emptyList());

        MediaEntryEntity mockMedia = new MediaEntryEntity();
        mockMedia.setId(100);
        when(mediaRepository.findById(100)).thenReturn(Optional.of(mockMedia));

        // Act
        boolean result = ratingService.delete(userId, ratingId);

        // Assert
        assertTrue(result);
        verify(ratingRepository).delete(ratingId);
        verify(mediaRepository).update(mockMedia);
    }

    @Test
    void testDelete_NotOwner_ThrowsSecurityException() {
        // Arrange
        int userId = 1;
        int otherUserId = 2;
        int ratingId = 1;

        RatingEntity existing = createMockRating(ratingId, otherUserId, 100, 5, "Great!");

        when(ratingRepository.findById(ratingId)).thenReturn(Optional.of(existing));

        // Act & Assert
        assertThrows(SecurityException.class, () -> ratingService.delete(userId, ratingId));
        verify(ratingRepository, never()).delete(anyInt());
    }

    @Test
    void testDelete_NonExisting_ReturnsFalse() {
        // Arrange
        when(ratingRepository.findById(999)).thenReturn(Optional.empty());

        // Act
        boolean result = ratingService.delete(1, 999);

        // Assert
        assertFalse(result);
        verify(ratingRepository, never()).delete(anyInt());
    }

    // ==================== like/unlike Tests ====================

    @Test
    void testLike_ValidRating_AddsLike() {
        // Arrange
        int userId = 1;
        int ratingId = 1;

        RatingEntity existing = createMockRating(ratingId, 2, 100, 5, "Great!");

        when(ratingRepository.findById(ratingId)).thenReturn(Optional.of(existing));
        when(ratingRepository.update(any())).thenReturn(existing);

        // Act
        Optional<RatingDetailDto> result = ratingService.like(userId, ratingId);

        // Assert
        assertTrue(result.isPresent());
        assertTrue(result.get().isLikedByMe());
        verify(ratingRepository).update(any());
    }

    @Test
    void testUnlike_ValidRating_RemovesLike() {
        // Arrange
        int userId = 1;
        int ratingId = 1;

        RatingEntity existing = createMockRating(ratingId, 2, 100, 5, "Great!");
        existing.getLikedByUserIds().add(userId);

        when(ratingRepository.findById(ratingId)).thenReturn(Optional.of(existing));
        when(ratingRepository.update(any())).thenReturn(existing);

        // Act
        Optional<RatingDetailDto> result = ratingService.unlike(userId, ratingId);

        // Assert
        assertTrue(result.isPresent());
        assertFalse(result.get().isLikedByMe());
        verify(ratingRepository).update(any());
    }

    // ==================== confirm Tests ====================

    @Test
    void testConfirm_ValidConfirm_SetsConfirmed() {
        // Arrange
        int userId = 1;
        int ratingId = 1;

        RatingEntity existing = createMockRating(ratingId, userId, 100, 5, "Great!");
        existing.setConfirmed(false);

        when(ratingRepository.findById(ratingId)).thenReturn(Optional.of(existing));
        when(ratingRepository.update(any())).thenReturn(existing);

        // Act
        Optional<RatingDetailDto> result = ratingService.confirm(userId, ratingId);

        // Assert
        assertTrue(result.isPresent());
        assertTrue(result.get().isConfirmed());
        verify(ratingRepository).update(any());
    }

    @Test
    void testConfirm_NotOwner_ThrowsSecurityException() {
        // Arrange
        int userId = 1;
        int otherUserId = 2;
        int ratingId = 1;

        RatingEntity existing = createMockRating(ratingId, otherUserId, 100, 5, "Great!");

        when(ratingRepository.findById(ratingId)).thenReturn(Optional.of(existing));

        // Act & Assert
        assertThrows(SecurityException.class, () -> ratingService.confirm(userId, ratingId));
        verify(ratingRepository, never()).update(any());
    }

    // ==================== Helper Methods ====================

    private UserEntity createMockUser(int id, String username) {
        UserEntity user = new UserEntity();
        user.setId(id);
        user.setUsername(username);
        return user;
    }

    private RatingEntity createMockRating(int id, int userId, int mediaId, int score, String comment) {
        RatingEntity rating = new RatingEntity();
        rating.setId(id);
        rating.setUserId(userId);
        rating.setMediaId(mediaId);
        rating.setScore(score);
        rating.setComment(comment);
        rating.setConfirmed(true);
        rating.setTimestamp(System.currentTimeMillis());
        rating.setLikedByUserIds(new HashSet<>());
        return rating;
    }
}

