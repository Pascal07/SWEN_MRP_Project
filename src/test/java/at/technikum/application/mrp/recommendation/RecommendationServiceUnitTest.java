package at.technikum.application.mrp.recommendation;

import at.technikum.application.mrp.media.MediaRepository;
import at.technikum.application.mrp.media.entity.MediaEntryEntity;
import at.technikum.application.mrp.rating.RatingRepository;
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
class RecommendationServiceUnitTest {

    @Mock
    private RecommendationRepository recommendationRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private MediaRepository mediaRepository;

    @Mock
    private RatingRepository ratingRepository;

    private RecommendationService recommendationService;

    @BeforeEach
    void setUp() {
        recommendationService = new RecommendationService(
            recommendationRepository, userRepository, mediaRepository, ratingRepository
        );
    }

    // ==================== recommendationsByGenre Tests ====================

    @Test
    void testRecommendationsByGenre_ValidGenre_ReturnsMediaList() {
        // Arrange
        String authHeader = "Bearer valid-token";
        String genre = "Action";

        UserEntity mockUser = createMockUser(1, "john_doe");
        when(userRepository.findByToken("valid-token")).thenReturn(Optional.of(mockUser));

        MediaEntryEntity media1 = createMockMedia(1, "Action Movie 1", "Action");
        MediaEntryEntity media2 = createMockMedia(2, "Action Movie 2", "Action");

        when(mediaRepository.search(null, genre, null, null, null, null, "score"))
            .thenReturn(Arrays.asList(media1, media2));

        // Act
        List<Map<String, Object>> result = recommendationService.recommendationsByGenre(authHeader, genre);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Action Movie 1", result.get(0).get("title"));
        assertEquals("Action Movie 2", result.get(1).get("title"));
        verify(mediaRepository).search(null, genre, null, null, null, null, "score");
    }

    @Test
    void testRecommendationsByGenre_NullGenre_ThrowsException() {
        // Arrange
        String authHeader = "Bearer valid-token";
        UserEntity mockUser = createMockUser(1, "john_doe");
        when(userRepository.findByToken("valid-token")).thenReturn(Optional.of(mockUser));

        // Act & Assert
        assertThrows(IllegalArgumentException.class,
            () -> recommendationService.recommendationsByGenre(authHeader, null));
        verify(mediaRepository, never()).search(any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void testRecommendationsByGenre_BlankGenre_ThrowsException() {
        // Arrange
        String authHeader = "Bearer valid-token";
        UserEntity mockUser = createMockUser(1, "john_doe");
        when(userRepository.findByToken("valid-token")).thenReturn(Optional.of(mockUser));

        // Act & Assert
        assertThrows(IllegalArgumentException.class,
            () -> recommendationService.recommendationsByGenre(authHeader, "   "));
        verify(mediaRepository, never()).search(any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void testRecommendationsByGenre_NoResults_ReturnsEmptyList() {
        // Arrange
        String authHeader = "Bearer valid-token";
        String genre = "NonExistent";

        UserEntity mockUser = createMockUser(1, "john_doe");
        when(userRepository.findByToken("valid-token")).thenReturn(Optional.of(mockUser));
        when(mediaRepository.search(null, genre, null, null, null, null, "score"))
            .thenReturn(Collections.emptyList());

        // Act
        List<Map<String, Object>> result = recommendationService.recommendationsByGenre(authHeader, genre);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testRecommendationsByGenre_InvalidAuth_ThrowsSecurityException() {
        // Arrange
        String authHeader = "Bearer invalid-token";
        when(userRepository.findByToken("invalid-token")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(SecurityException.class,
            () -> recommendationService.recommendationsByGenre(authHeader, "Action"));
        verify(mediaRepository, never()).search(any(), any(), any(), any(), any(), any(), any());
    }

    // ==================== recommendationsByMediaType Tests ====================

    @Test
    void testRecommendationsByMediaType_Movie_ReturnsMovieList() {
        // Arrange
        String authHeader = "Bearer valid-token";
        String mediaType = "movie";

        UserEntity mockUser = createMockUser(1, "john_doe");
        when(userRepository.findByToken("valid-token")).thenReturn(Optional.of(mockUser));

        MediaEntryEntity movie1 = createMockMedia(1, "Movie 1", "Action");
        movie1.setMediaType("movie");
        MediaEntryEntity movie2 = createMockMedia(2, "Movie 2", "Comedy");
        movie2.setMediaType("movie");

        when(mediaRepository.search(null, null, mediaType, null, null, null, "score"))
            .thenReturn(Arrays.asList(movie1, movie2));

        // Act
        List<Map<String, Object>> result = recommendationService.recommendationsByMediaType(authHeader, mediaType);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Movie 1", result.get(0).get("title"));
        assertEquals("Movie 2", result.get(1).get("title"));
        verify(mediaRepository).search(null, null, mediaType, null, null, null, "score");
    }

    @Test
    void testRecommendationsByMediaType_Series_ReturnsSeriesList() {
        // Arrange
        String authHeader = "Bearer valid-token";
        String mediaType = "series";

        UserEntity mockUser = createMockUser(1, "john_doe");
        when(userRepository.findByToken("valid-token")).thenReturn(Optional.of(mockUser));

        MediaEntryEntity series1 = createMockMedia(1, "Series 1", "Drama");
        series1.setMediaType("series");

        when(mediaRepository.search(null, null, mediaType, null, null, null, "score"))
            .thenReturn(Collections.singletonList(series1));

        // Act
        List<Map<String, Object>> result = recommendationService.recommendationsByMediaType(authHeader, mediaType);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Series 1", result.get(0).get("title"));
    }

    @Test
    void testRecommendationsByMediaType_InvalidAuth_ThrowsSecurityException() {
        // Arrange
        String authHeader = "Bearer invalid-token";
        when(userRepository.findByToken("invalid-token")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(SecurityException.class,
            () -> recommendationService.recommendationsByMediaType(authHeader, "movie"));
    }

    // ==================== recommendationsForUser Tests ====================

    @Test
    void testRecommendationsForUser_ReturnsTop10() {
        // Arrange
        String authHeader = "Bearer valid-token";

        UserEntity mockUser = createMockUser(1, "john_doe");
        when(userRepository.findByToken("valid-token")).thenReturn(Optional.of(mockUser));

        List<MediaEntryEntity> allMedia = new ArrayList<>();
        for (int i = 1; i <= 15; i++) {
            allMedia.add(createMockMedia(i, "Media " + i, "Genre"));
        }

        when(mediaRepository.search(null, null, null, null, null, null, "score"))
            .thenReturn(allMedia);

        // Act
        List<Map<String, Object>> result = recommendationService.recommendationsForUser(authHeader);

        // Assert
        assertNotNull(result);
        assertEquals(10, result.size());
        assertEquals("Media 1", result.get(0).get("title"));
        assertEquals("Media 10", result.get(9).get("title"));
        verify(mediaRepository).search(null, null, null, null, null, null, "score");
    }

    @Test
    void testRecommendationsForUser_LessThan10_ReturnsAll() {
        // Arrange
        String authHeader = "Bearer valid-token";

        UserEntity mockUser = createMockUser(1, "john_doe");
        when(userRepository.findByToken("valid-token")).thenReturn(Optional.of(mockUser));

        MediaEntryEntity media1 = createMockMedia(1, "Media 1", "Genre");
        MediaEntryEntity media2 = createMockMedia(2, "Media 2", "Genre");

        when(mediaRepository.search(null, null, null, null, null, null, "score"))
            .thenReturn(Arrays.asList(media1, media2));

        // Act
        List<Map<String, Object>> result = recommendationService.recommendationsForUser(authHeader);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    void testRecommendationsForUser_NoMedia_ReturnsEmptyList() {
        // Arrange
        String authHeader = "Bearer valid-token";

        UserEntity mockUser = createMockUser(1, "john_doe");
        when(userRepository.findByToken("valid-token")).thenReturn(Optional.of(mockUser));
        when(mediaRepository.search(null, null, null, null, null, null, "score"))
            .thenReturn(Collections.emptyList());

        // Act
        List<Map<String, Object>> result = recommendationService.recommendationsForUser(authHeader);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testRecommendationsForUser_InvalidAuth_ThrowsSecurityException() {
        // Arrange
        String authHeader = "Bearer invalid-token";
        when(userRepository.findByToken("invalid-token")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(SecurityException.class,
            () -> recommendationService.recommendationsForUser(authHeader));
    }

    // ==================== Media to Map Conversion Tests ====================

    @Test
    void testMediaToMap_WithRatings_CalculatesAverage() {
        // Arrange
        String authHeader = "Bearer valid-token";
        UserEntity mockUser = createMockUser(1, "john_doe");
        when(userRepository.findByToken("valid-token")).thenReturn(Optional.of(mockUser));

        MediaEntryEntity media = createMockMedia(1, "Test Movie", "Action");

        RatingEntity rating1 = new RatingEntity(1, 5);
        RatingEntity rating2 = new RatingEntity(2, 4);
        RatingEntity rating3 = new RatingEntity(3, 3);

        media.setRatings(Arrays.asList(rating1, rating2, rating3));

        when(mediaRepository.search(null, null, null, null, null, null, "score"))
            .thenReturn(Collections.singletonList(media));

        // Act
        List<Map<String, Object>> result = recommendationService.recommendationsForUser(authHeader);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        Map<String, Object> mediaMap = result.get(0);
        assertEquals(3, mediaMap.get("ratingCount"));
        assertEquals(4.0, mediaMap.get("averageRating"));
    }

    @Test
    void testMediaToMap_WithoutRatings_NullAverage() {
        // Arrange
        String authHeader = "Bearer valid-token";
        UserEntity mockUser = createMockUser(1, "john_doe");
        when(userRepository.findByToken("valid-token")).thenReturn(Optional.of(mockUser));

        MediaEntryEntity media = createMockMedia(1, "Test Movie", "Action");
        media.setRatings(new ArrayList<>());

        when(mediaRepository.search(null, null, null, null, null, null, "score"))
            .thenReturn(Collections.singletonList(media));

        // Act
        List<Map<String, Object>> result = recommendationService.recommendationsForUser(authHeader);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        Map<String, Object> mediaMap = result.get(0);
        assertNull(mediaMap.get("averageRating"));
        assertEquals(0, mediaMap.get("ratingCount"));
    }

    // ==================== Authorization Helper Tests ====================

    @Test
    void testRequireAuthorizedUserId_NullAuth_ThrowsSecurityException() {
        // Act & Assert
        assertThrows(SecurityException.class,
            () -> recommendationService.recommendationsForUser(null));
        verify(userRepository, never()).findByToken(any());
    }

    @Test
    void testRequireAuthorizedUserId_MissingBearerPrefix_ThrowsSecurityException() {
        // Act & Assert
        assertThrows(SecurityException.class,
            () -> recommendationService.recommendationsForUser("valid-token"));
        verify(userRepository, never()).findByToken(any());
    }

    // ==================== Helper Methods ====================

    private UserEntity createMockUser(int id, String username) {
        UserEntity user = new UserEntity();
        user.setId(id);
        user.setUsername(username);
        return user;
    }

    private MediaEntryEntity createMockMedia(int id, String title, String genre) {
        MediaEntryEntity media = new MediaEntryEntity();
        media.setId(id);
        media.setTitle(title);
        media.setDescription("Description for " + title);
        media.setGenres(Collections.singletonList(genre));
        media.setMediaType("movie");
        media.setReleaseYear(2020);
        media.setAgeRestriction(12);
        media.setRatings(new ArrayList<>());
        return media;
    }
}

