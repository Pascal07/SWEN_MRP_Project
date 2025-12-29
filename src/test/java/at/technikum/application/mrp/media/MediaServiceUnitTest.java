package at.technikum.application.mrp.media;

import at.technikum.application.mrp.media.dto.MediaDetailDto;
import at.technikum.application.mrp.media.dto.MediaUpsertDto;
import at.technikum.application.mrp.media.entity.MediaEntryEntity;
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
class MediaServiceUnitTest {

    @Mock
    private MediaRepository mediaRepository;

    @Mock
    private UserRepository userRepository;

    private MediaService mediaService;

    @BeforeEach
    void setUp() {
        mediaService = new MediaService(mediaRepository, userRepository);
    }

    // ==================== Authorization Tests ====================

    @Test
    void testGetAuthorizedUserId_ValidBearerToken_ReturnsUserId() {
        // Arrange
        String authHeader = "Bearer valid-token";
        UserEntity mockUser = new UserEntity();
        mockUser.setId(42);
        when(userRepository.findByToken("valid-token")).thenReturn(Optional.of(mockUser));

        // Act
        Optional<Integer> result = mediaService.getAuthorizedUserId(authHeader);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(42, result.get());
        verify(userRepository).findByToken("valid-token");
    }

    @Test
    void testGetAuthorizedUserId_NullAuthHeader_ReturnsEmpty() {
        // Act
        Optional<Integer> result = mediaService.getAuthorizedUserId(null);

        // Assert
        assertFalse(result.isPresent());
        verifyNoInteractions(userRepository);
    }

    @Test
    void testGetAuthorizedUserId_MissingBearerPrefix_ReturnsEmpty() {
        // Act
        Optional<Integer> result = mediaService.getAuthorizedUserId("invalid-token");

        // Assert
        assertFalse(result.isPresent());
        verifyNoInteractions(userRepository);
    }

    @Test
    void testGetAuthorizedUserId_EmptyToken_ReturnsEmpty() {
        // Act
        Optional<Integer> result = mediaService.getAuthorizedUserId("Bearer ");

        // Assert
        assertFalse(result.isPresent());
        verifyNoInteractions(userRepository);
    }

    @Test
    void testGetAuthorizedUserId_InvalidToken_ReturnsEmpty() {
        // Arrange
        when(userRepository.findByToken("invalid-token")).thenReturn(Optional.empty());

        // Act
        Optional<Integer> result = mediaService.getAuthorizedUserId("Bearer invalid-token");

        // Assert
        assertFalse(result.isPresent());
        verify(userRepository).findByToken("invalid-token");
    }

    // ==================== Create Tests ====================

    @Test
    void testCreate_ValidDto_ReturnsMediaDetailDto() {
        // Arrange
        int userId = 1;
        MediaUpsertDto dto = createValidUpsertDto("The Matrix", "movie");
        MediaEntryEntity savedEntity = createMediaEntity(100, userId, "The Matrix", "movie");

        when(mediaRepository.create(any(MediaEntryEntity.class))).thenReturn(savedEntity);

        // Act
        MediaDetailDto result = mediaService.create(userId, dto);

        // Assert
        assertNotNull(result);
        assertEquals(100, result.getId());
        assertEquals("The Matrix", result.getTitle());
        assertEquals("movie", result.getMediaType());
        verify(mediaRepository).create(any(MediaEntryEntity.class));
    }

    @Test
    void testCreate_WithGenres_SavesGenresCorrectly() {
        // Arrange
        int userId = 1;
        MediaUpsertDto dto = createValidUpsertDto("Inception", "movie");
        dto.setGenres(Arrays.asList("Action", "Sci-Fi", "Thriller"));

        MediaEntryEntity savedEntity = createMediaEntity(101, userId, "Inception", "movie");
        savedEntity.setGenres(Arrays.asList("Action", "Sci-Fi", "Thriller"));

        when(mediaRepository.create(any(MediaEntryEntity.class))).thenReturn(savedEntity);

        // Act
        MediaDetailDto result = mediaService.create(userId, dto);

        // Assert
        assertNotNull(result);
        assertEquals(3, result.getGenres().size());
        assertTrue(result.getGenres().contains("Action"));
        assertTrue(result.getGenres().contains("Sci-Fi"));
    }

    @Test
    void testCreate_NullDto_ThrowsIllegalArgumentException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> mediaService.create(1, null)
        );
        assertEquals("Body is required", exception.getMessage());
        verifyNoInteractions(mediaRepository);
    }

    @Test
    void testCreate_MissingTitle_ThrowsIllegalArgumentException() {
        // Arrange
        MediaUpsertDto dto = new MediaUpsertDto();
        dto.setMediaType("movie");

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> mediaService.create(1, dto)
        );
        assertEquals("title is required", exception.getMessage());
        verifyNoInteractions(mediaRepository);
    }

    @Test
    void testCreate_BlankTitle_ThrowsIllegalArgumentException() {
        // Arrange
        MediaUpsertDto dto = new MediaUpsertDto();
        dto.setTitle("   ");
        dto.setMediaType("movie");

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> mediaService.create(1, dto)
        );
        assertEquals("title is required", exception.getMessage());
    }

    @Test
    void testCreate_MissingMediaType_ThrowsIllegalArgumentException() {
        // Arrange
        MediaUpsertDto dto = new MediaUpsertDto();
        dto.setTitle("Test Movie");

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> mediaService.create(1, dto)
        );
        assertEquals("mediaType is required", exception.getMessage());
    }

    @Test
    void testCreate_InvalidMediaType_ThrowsIllegalArgumentException() {
        // Arrange
        MediaUpsertDto dto = new MediaUpsertDto();
        dto.setTitle("Test Media");
        dto.setMediaType("book");

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> mediaService.create(1, dto)
        );
        assertEquals("mediaType must be movie, series, or game", exception.getMessage());
    }

    @Test
    void testCreate_MediaTypeGame_Success() {
        // Arrange
        MediaUpsertDto dto = createValidUpsertDto("Elden Ring", "game");
        MediaEntryEntity savedEntity = createMediaEntity(102, 1, "Elden Ring", "game");
        when(mediaRepository.create(any(MediaEntryEntity.class))).thenReturn(savedEntity);

        // Act
        MediaDetailDto result = mediaService.create(1, dto);

        // Assert
        assertNotNull(result);
        assertEquals("game", result.getMediaType());
    }

    @Test
    void testCreate_MediaTypeSeries_Success() {
        // Arrange
        MediaUpsertDto dto = createValidUpsertDto("Breaking Bad", "series");
        MediaEntryEntity savedEntity = createMediaEntity(103, 1, "Breaking Bad", "series");
        when(mediaRepository.create(any(MediaEntryEntity.class))).thenReturn(savedEntity);

        // Act
        MediaDetailDto result = mediaService.create(1, dto);

        // Assert
        assertNotNull(result);
        assertEquals("series", result.getMediaType());
    }

    // ==================== GetById Tests ====================

    @Test
    void testGetById_ExistingMedia_ReturnsMediaDetailDto() {
        // Arrange
        MediaEntryEntity entity = createMediaEntity(1, 10, "Test Movie", "movie");
        when(mediaRepository.findById(1)).thenReturn(Optional.of(entity));

        // Act
        Optional<MediaDetailDto> result = mediaService.getById(1);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(1, result.get().getId());
        assertEquals("Test Movie", result.get().getTitle());
        verify(mediaRepository).findById(1);
    }

    @Test
    void testGetById_NonExistingMedia_ReturnsEmpty() {
        // Arrange
        when(mediaRepository.findById(999)).thenReturn(Optional.empty());

        // Act
        Optional<MediaDetailDto> result = mediaService.getById(999);

        // Assert
        assertFalse(result.isPresent());
        verify(mediaRepository).findById(999);
    }

    @Test
    void testGetById_MediaWithRatings_CalculatesAverageScore() {
        // Arrange
        MediaEntryEntity entity = createMediaEntity(1, 10, "Rated Movie", "movie");
        List<RatingEntity> ratings = Arrays.asList(
            new RatingEntity(1, 5),
            new RatingEntity(2, 4),
            new RatingEntity(3, 3)
        );
        entity.setRatings(ratings);
        when(mediaRepository.findById(1)).thenReturn(Optional.of(entity));

        // Act
        Optional<MediaDetailDto> result = mediaService.getById(1);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(4.0, result.get().getAverageScore(), 0.01);
        assertEquals(3, result.get().getRatings().size());
    }

    @Test
    void testGetById_MediaWithoutRatings_ReturnsZeroAverageScore() {
        // Arrange
        MediaEntryEntity entity = createMediaEntity(1, 10, "Unrated Movie", "movie");
        entity.setRatings(new ArrayList<>());
        when(mediaRepository.findById(1)).thenReturn(Optional.of(entity));

        // Act
        Optional<MediaDetailDto> result = mediaService.getById(1);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(0.0, result.get().getAverageScore(), 0.01);
        assertTrue(result.get().getRatings().isEmpty());
    }

    // ==================== Update Tests ====================

    @Test
    void testUpdate_ValidUpdate_ByOwner_ReturnsUpdatedDto() {
        // Arrange
        int userId = 1;
        int mediaId = 10;
        MediaUpsertDto dto = createValidUpsertDto("Updated Title", "movie");
        MediaEntryEntity existingEntity = createMediaEntity(mediaId, userId, "Old Title", "movie");
        MediaEntryEntity updatedEntity = createMediaEntity(mediaId, userId, "Updated Title", "movie");

        when(mediaRepository.findById(mediaId)).thenReturn(Optional.of(existingEntity));
        when(mediaRepository.update(any(MediaEntryEntity.class))).thenReturn(updatedEntity);

        // Act
        Optional<MediaDetailDto> result = mediaService.update(userId, mediaId, dto);

        // Assert
        assertTrue(result.isPresent());
        assertEquals("Updated Title", result.get().getTitle());
        verify(mediaRepository).findById(mediaId);
        verify(mediaRepository).update(any(MediaEntryEntity.class));
    }

    @Test
    void testUpdate_NonExistingMedia_ReturnsEmpty() {
        // Arrange
        MediaUpsertDto dto = createValidUpsertDto("Title", "movie");
        when(mediaRepository.findById(999)).thenReturn(Optional.empty());

        // Act
        Optional<MediaDetailDto> result = mediaService.update(1, 999, dto);

        // Assert
        assertFalse(result.isPresent());
        verify(mediaRepository).findById(999);
        verify(mediaRepository, never()).update(any());
    }

    @Test
    void testUpdate_ByNonOwner_ThrowsSecurityException() {
        // Arrange
        int ownerId = 1;
        int otherUserId = 2;
        int mediaId = 10;
        MediaUpsertDto dto = createValidUpsertDto("Updated Title", "movie");
        MediaEntryEntity existingEntity = createMediaEntity(mediaId, ownerId, "Old Title", "movie");

        when(mediaRepository.findById(mediaId)).thenReturn(Optional.of(existingEntity));

        // Act & Assert
        SecurityException exception = assertThrows(
            SecurityException.class,
            () -> mediaService.update(otherUserId, mediaId, dto)
        );
        assertEquals("Only creator can update this entry", exception.getMessage());
        verify(mediaRepository).findById(mediaId);
        verify(mediaRepository, never()).update(any());
    }

    @Test
    void testUpdate_InvalidDto_ThrowsIllegalArgumentException() {
        // Arrange
        MediaUpsertDto dto = new MediaUpsertDto();
        dto.setTitle("Valid Title");
        dto.setMediaType("invalid");

        // Act & Assert
        assertThrows(
            IllegalArgumentException.class,
            () -> mediaService.update(1, 10, dto)
        );
    }

    // ==================== Delete Tests ====================

    @Test
    void testDelete_ExistingMedia_ByOwner_ReturnsTrue() {
        // Arrange
        int userId = 1;
        int mediaId = 10;
        MediaEntryEntity entity = createMediaEntity(mediaId, userId, "To Delete", "movie");

        when(mediaRepository.findById(mediaId)).thenReturn(Optional.of(entity));
        when(mediaRepository.delete(mediaId)).thenReturn(true);

        // Act
        boolean result = mediaService.delete(userId, mediaId);

        // Assert
        assertTrue(result);
        verify(mediaRepository).findById(mediaId);
        verify(mediaRepository).delete(mediaId);
    }

    @Test
    void testDelete_NonExistingMedia_ReturnsFalse() {
        // Arrange
        when(mediaRepository.findById(999)).thenReturn(Optional.empty());

        // Act
        boolean result = mediaService.delete(1, 999);

        // Assert
        assertFalse(result);
        verify(mediaRepository).findById(999);
        verify(mediaRepository, never()).delete(anyInt());
    }

    @Test
    void testDelete_ByNonOwner_ThrowsSecurityException() {
        // Arrange
        int ownerId = 1;
        int otherUserId = 2;
        int mediaId = 10;
        MediaEntryEntity entity = createMediaEntity(mediaId, ownerId, "Protected", "movie");

        when(mediaRepository.findById(mediaId)).thenReturn(Optional.of(entity));

        // Act & Assert
        SecurityException exception = assertThrows(
            SecurityException.class,
            () -> mediaService.delete(otherUserId, mediaId)
        );
        assertEquals("Only creator can delete this entry", exception.getMessage());
        verify(mediaRepository).findById(mediaId);
        verify(mediaRepository, never()).delete(anyInt());
    }

    // ==================== Search Tests ====================

    @Test
    void testSearch_NoFilters_ReturnsAllMedia() {
        // Arrange
        Map<String, String> query = new HashMap<>();
        List<MediaEntryEntity> entities = Arrays.asList(
            createMediaEntity(1, 1, "Movie 1", "movie"),
            createMediaEntity(2, 1, "Movie 2", "movie")
        );

        when(mediaRepository.search(null, null, null, null, null, null, null))
            .thenReturn(entities);

        // Act
        List<MediaDetailDto> result = mediaService.search(query);

        // Assert
        assertEquals(2, result.size());
        verify(mediaRepository).search(null, null, null, null, null, null, null);
    }

    @Test
    void testSearch_WithTitleFilter_PassesNormalizedTitle() {
        // Arrange
        Map<String, String> query = new HashMap<>();
        query.put("title", "  Matrix  ");

        when(mediaRepository.search("Matrix", null, null, null, null, null, null))
            .thenReturn(new ArrayList<>());

        // Act
        mediaService.search(query);

        // Assert
        verify(mediaRepository).search("Matrix", null, null, null, null, null, null);
    }

    @Test
    void testSearch_WithGenreFilter_PassesNormalizedGenre() {
        // Arrange
        Map<String, String> query = new HashMap<>();
        query.put("genre", "  Action  ");

        when(mediaRepository.search(null, "Action", null, null, null, null, null))
            .thenReturn(new ArrayList<>());

        // Act
        mediaService.search(query);

        // Assert
        verify(mediaRepository).search(null, "Action", null, null, null, null, null);
    }

    @Test
    void testSearch_WithMediaTypeFilter_PassesNormalizedType() {
        // Arrange
        Map<String, String> query = new HashMap<>();
        query.put("mediaType", "  movie  ");

        when(mediaRepository.search(null, null, "movie", null, null, null, null))
            .thenReturn(new ArrayList<>());

        // Act
        mediaService.search(query);

        // Assert
        verify(mediaRepository).search(null, null, "movie", null, null, null, null);
    }

    @Test
    void testSearch_WithReleaseYear_PassesInteger() {
        // Arrange
        Map<String, String> query = new HashMap<>();
        query.put("releaseYear", "2020");

        when(mediaRepository.search(null, null, null, 2020, null, null, null))
            .thenReturn(new ArrayList<>());

        // Act
        mediaService.search(query);

        // Assert
        verify(mediaRepository).search(null, null, null, 2020, null, null, null);
    }

    @Test
    void testSearch_WithAgeRestriction_PassesInteger() {
        // Arrange
        Map<String, String> query = new HashMap<>();
        query.put("ageRestriction", "18");

        when(mediaRepository.search(null, null, null, null, 18, null, null))
            .thenReturn(new ArrayList<>());

        // Act
        mediaService.search(query);

        // Assert
        verify(mediaRepository).search(null, null, null, null, 18, null, null);
    }

    @Test
    void testSearch_WithMinRating_PassesInteger() {
        // Arrange
        Map<String, String> query = new HashMap<>();
        query.put("rating", "4");

        when(mediaRepository.search(null, null, null, null, null, 4, null))
            .thenReturn(new ArrayList<>());

        // Act
        mediaService.search(query);

        // Assert
        verify(mediaRepository).search(null, null, null, null, null, 4, null);
    }

    @Test
    void testSearch_WithSortBy_PassesNormalizedValue() {
        // Arrange
        Map<String, String> query = new HashMap<>();
        query.put("sortBy", "  rating  ");

        when(mediaRepository.search(null, null, null, null, null, null, "rating"))
            .thenReturn(new ArrayList<>());

        // Act
        mediaService.search(query);

        // Assert
        verify(mediaRepository).search(null, null, null, null, null, null, "rating");
    }

    @Test
    void testSearch_WithAllFilters_PassesAllParameters() {
        // Arrange
        Map<String, String> query = new HashMap<>();
        query.put("title", "Matrix");
        query.put("genre", "Sci-Fi");
        query.put("mediaType", "movie");
        query.put("releaseYear", "1999");
        query.put("ageRestriction", "16");
        query.put("rating", "4");
        query.put("sortBy", "rating");

        when(mediaRepository.search("Matrix", "Sci-Fi", "movie", 1999, 16, 4, "rating"))
            .thenReturn(new ArrayList<>());

        // Act
        mediaService.search(query);

        // Assert
        verify(mediaRepository).search("Matrix", "Sci-Fi", "movie", 1999, 16, 4, "rating");
    }

    @Test
    void testSearch_WithInvalidInteger_IgnoresParameter() {
        // Arrange
        Map<String, String> query = new HashMap<>();
        query.put("releaseYear", "invalid");

        when(mediaRepository.search(null, null, null, null, null, null, null))
            .thenReturn(new ArrayList<>());

        // Act
        mediaService.search(query);

        // Assert
        verify(mediaRepository).search(null, null, null, null, null, null, null);
    }

    @Test
    void testSearch_WithEmptyStringValue_TreatsAsNull() {
        // Arrange
        Map<String, String> query = new HashMap<>();
        query.put("title", "   ");

        when(mediaRepository.search(null, null, null, null, null, null, null))
            .thenReturn(new ArrayList<>());

        // Act
        mediaService.search(query);

        // Assert
        verify(mediaRepository).search(null, null, null, null, null, null, null);
    }

    // ==================== Helper Methods ====================

    private MediaUpsertDto createValidUpsertDto(String title, String mediaType) {
        MediaUpsertDto dto = new MediaUpsertDto();
        dto.setTitle(title);
        dto.setMediaType(mediaType);
        dto.setDescription("A great " + mediaType);
        dto.setReleaseYear(2020);
        dto.setAgeRestriction(12);
        return dto;
    }

    private MediaEntryEntity createMediaEntity(Integer id, Integer creatorUserId, String title, String mediaType) {
        MediaEntryEntity entity = new MediaEntryEntity();
        entity.setId(id);
        entity.setCreatorUserId(creatorUserId);
        entity.setTitle(title);
        entity.setMediaType(mediaType);
        entity.setDescription("Description of " + title);
        entity.setReleaseYear(2020);
        entity.setAgeRestriction(12);
        entity.setGenres(new ArrayList<>());
        entity.setRatings(new ArrayList<>());
        return entity;
    }
}

