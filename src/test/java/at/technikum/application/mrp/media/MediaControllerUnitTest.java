package at.technikum.application.mrp.media;

import at.technikum.application.mrp.media.dto.MediaDetailDto;
import at.technikum.application.mrp.media.dto.MediaUpsertDto;
import at.technikum.server.http.Request;
import at.technikum.server.http.Response;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MediaControllerUnitTest {

    @Mock
    private MediaService mediaService;

    @Mock
    private Request request;

    private MediaController controller;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        controller = new MediaController(mediaService);
        objectMapper = new ObjectMapper();
    }

    // ==================== GET /media Tests ====================

    @Test
    void testHandle_GetMediaList_ValidAuth_ReturnsOk() {
        // Arrange
        when(request.getPath()).thenReturn("/media");
        when(request.getMethod()).thenReturn("GET");
        when(request.getAuthorization()).thenReturn("Bearer valid-token");
        when(request.getQueryParams()).thenReturn(new HashMap<>());
        when(mediaService.getAuthorizedUserId("Bearer valid-token")).thenReturn(Optional.of(1));

        List<MediaDetailDto> mediaList = Arrays.asList(
            createMediaDetailDto(1, "Movie 1", "movie"),
            createMediaDetailDto(2, "Movie 2", "movie")
        );
        when(mediaService.search(any())).thenReturn(mediaList);

        // Act
        Response response = controller.handle(request);

        // Assert
        assertEquals(200, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("Movie 1"));
        assertTrue(response.getBody().contains("Movie 2"));
        verify(mediaService).getAuthorizedUserId("Bearer valid-token");
        verify(mediaService).search(any());
    }

    @Test
    void testHandle_GetMediaList_WithQueryParams_PassesThemToService() {
        // Arrange
        when(request.getPath()).thenReturn("/media");
        when(request.getMethod()).thenReturn("GET");
        when(request.getAuthorization()).thenReturn("Bearer valid-token");

        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("title", "Matrix");
        queryParams.put("genre", "Sci-Fi");
        when(request.getQueryParams()).thenReturn(queryParams);

        when(mediaService.getAuthorizedUserId("Bearer valid-token")).thenReturn(Optional.of(1));
        when(mediaService.search(queryParams)).thenReturn(new ArrayList<>());

        // Act
        Response response = controller.handle(request);

        // Assert
        assertEquals(200, response.getStatusCode());
        verify(mediaService).search(queryParams);
    }

    @Test
    void testHandle_GetMediaList_NoAuth_ReturnsUnauthorized() {
        // Arrange
        when(request.getPath()).thenReturn("/media");
        when(request.getMethod()).thenReturn("GET");
        when(request.getAuthorization()).thenReturn(null);
        when(mediaService.getAuthorizedUserId(null)).thenReturn(Optional.empty());

        // Act
        Response response = controller.handle(request);

        // Assert
        assertEquals(401, response.getStatusCode());
        assertTrue(response.getBody().contains("Missing or invalid Authorization header"));
        verify(mediaService, never()).search(any());
    }

    @Test
    void testHandle_GetMediaList_InvalidAuth_ReturnsUnauthorized() {
        // Arrange
        when(request.getPath()).thenReturn("/media");
        when(request.getMethod()).thenReturn("GET");
        when(request.getAuthorization()).thenReturn("Bearer invalid-token");
        when(mediaService.getAuthorizedUserId("Bearer invalid-token")).thenReturn(Optional.empty());

        // Act
        Response response = controller.handle(request);

        // Assert
        assertEquals(401, response.getStatusCode());
        verify(mediaService, never()).search(any());
    }

    // ==================== POST /media Tests ====================

    @Test
    void testHandle_CreateMedia_ValidData_ReturnsCreatedMedia() throws Exception {
        // Arrange
        when(request.getPath()).thenReturn("/media");
        when(request.getMethod()).thenReturn("POST");
        when(request.getAuthorization()).thenReturn("Bearer valid-token");
        when(mediaService.getAuthorizedUserId("Bearer valid-token")).thenReturn(Optional.of(1));

        MediaUpsertDto dto = createValidUpsertDto("New Movie", "movie");
        String requestBody = objectMapper.writeValueAsString(dto);
        when(request.getBody()).thenReturn(requestBody);

        MediaDetailDto createdMedia = createMediaDetailDto(100, "New Movie", "movie");
        when(mediaService.create(eq(1), any(MediaUpsertDto.class))).thenReturn(createdMedia);

        // Act
        Response response = controller.handle(request);

        // Assert
        assertEquals(200, response.getStatusCode());
        assertTrue(response.getBody().contains("New Movie"));
        verify(mediaService).create(eq(1), any(MediaUpsertDto.class));
    }

    @Test
    void testHandle_CreateMedia_EmptyBody_ReturnsBadRequest() {
        // Arrange
        when(request.getPath()).thenReturn("/media");
        when(request.getMethod()).thenReturn("POST");
        when(request.getAuthorization()).thenReturn("Bearer valid-token");
        when(mediaService.getAuthorizedUserId("Bearer valid-token")).thenReturn(Optional.of(1));
        when(request.getBody()).thenReturn("");

        // Act
        Response response = controller.handle(request);

        // Assert
        assertEquals(400, response.getStatusCode());
        assertTrue(response.getBody().contains("Request body is empty"));
        verify(mediaService, never()).create(anyInt(), any());
    }

    @Test
    void testHandle_CreateMedia_NullBody_ReturnsBadRequest() {
        // Arrange
        when(request.getPath()).thenReturn("/media");
        when(request.getMethod()).thenReturn("POST");
        when(request.getAuthorization()).thenReturn("Bearer valid-token");
        when(mediaService.getAuthorizedUserId("Bearer valid-token")).thenReturn(Optional.of(1));
        when(request.getBody()).thenReturn(null);

        // Act
        Response response = controller.handle(request);

        // Assert
        assertEquals(400, response.getStatusCode());
        verify(mediaService, never()).create(anyInt(), any());
    }

    @Test
    void testHandle_CreateMedia_InvalidJson_ReturnsBadRequest() {
        // Arrange
        when(request.getPath()).thenReturn("/media");
        when(request.getMethod()).thenReturn("POST");
        when(request.getAuthorization()).thenReturn("Bearer valid-token");
        when(mediaService.getAuthorizedUserId("Bearer valid-token")).thenReturn(Optional.of(1));
        when(request.getBody()).thenReturn("{invalid json");

        // Act
        Response response = controller.handle(request);

        // Assert
        assertEquals(500, response.getStatusCode());
        verify(mediaService, never()).create(anyInt(), any());
    }

    @Test
    void testHandle_CreateMedia_ServiceThrowsIllegalArgumentException_ReturnsBadRequest() throws Exception {
        // Arrange
        when(request.getPath()).thenReturn("/media");
        when(request.getMethod()).thenReturn("POST");
        when(request.getAuthorization()).thenReturn("Bearer valid-token");
        when(mediaService.getAuthorizedUserId("Bearer valid-token")).thenReturn(Optional.of(1));

        MediaUpsertDto dto = createValidUpsertDto("", "movie");
        String requestBody = objectMapper.writeValueAsString(dto);
        when(request.getBody()).thenReturn(requestBody);

        when(mediaService.create(eq(1), any(MediaUpsertDto.class)))
            .thenThrow(new IllegalArgumentException("title is required"));

        // Act
        Response response = controller.handle(request);

        // Assert
        assertEquals(400, response.getStatusCode());
        assertTrue(response.getBody().contains("title is required"));
    }

    // ==================== GET /media/{id} Tests ====================

    @Test
    void testHandle_GetMediaById_ExistingMedia_ReturnsMedia() {
        // Arrange
        when(request.getPath()).thenReturn("/media/42");
        when(request.getMethod()).thenReturn("GET");
        when(request.getAuthorization()).thenReturn("Bearer valid-token");
        when(mediaService.getAuthorizedUserId("Bearer valid-token")).thenReturn(Optional.of(1));

        MediaDetailDto media = createMediaDetailDto(42, "Test Movie", "movie");
        when(mediaService.getById(42)).thenReturn(Optional.of(media));

        // Act
        Response response = controller.handle(request);

        // Assert
        assertEquals(200, response.getStatusCode());
        assertTrue(response.getBody().contains("Test Movie"));
        verify(mediaService).getById(42);
    }

    @Test
    void testHandle_GetMediaById_NonExistingMedia_ReturnsNotFound() {
        // Arrange
        when(request.getPath()).thenReturn("/media/999");
        when(request.getMethod()).thenReturn("GET");
        when(request.getAuthorization()).thenReturn("Bearer valid-token");
        when(mediaService.getAuthorizedUserId("Bearer valid-token")).thenReturn(Optional.of(1));
        when(mediaService.getById(999)).thenReturn(Optional.empty());

        // Act
        Response response = controller.handle(request);

        // Assert
        assertEquals(404, response.getStatusCode());
        assertTrue(response.getBody().contains("Media not found"));
        verify(mediaService).getById(999);
    }

    @Test
    void testHandle_GetMediaById_InvalidId_ReturnsBadRequest() {
        // Arrange
        when(request.getPath()).thenReturn("/media/invalid");
        when(request.getMethod()).thenReturn("GET");
        when(request.getAuthorization()).thenReturn("Bearer valid-token");
        when(mediaService.getAuthorizedUserId("Bearer valid-token")).thenReturn(Optional.of(1));

        // Act
        Response response = controller.handle(request);

        // Assert
        assertEquals(404, response.getStatusCode());
        verify(mediaService, never()).getById(anyInt());
    }

    // ==================== PUT /media/{id} Tests ====================

    @Test
    void testHandle_UpdateMedia_ValidUpdate_ReturnsUpdatedMedia() throws Exception {
        // Arrange
        when(request.getPath()).thenReturn("/media/10");
        when(request.getMethod()).thenReturn("PUT");
        when(request.getAuthorization()).thenReturn("Bearer valid-token");
        when(mediaService.getAuthorizedUserId("Bearer valid-token")).thenReturn(Optional.of(1));

        MediaUpsertDto dto = createValidUpsertDto("Updated Movie", "movie");
        String requestBody = objectMapper.writeValueAsString(dto);
        when(request.getBody()).thenReturn(requestBody);

        MediaDetailDto updatedMedia = createMediaDetailDto(10, "Updated Movie", "movie");
        when(mediaService.update(eq(1), eq(10), any(MediaUpsertDto.class)))
            .thenReturn(Optional.of(updatedMedia));

        // Act
        Response response = controller.handle(request);

        // Assert
        assertEquals(200, response.getStatusCode());
        assertTrue(response.getBody().contains("Updated Movie"));
        verify(mediaService).update(eq(1), eq(10), any(MediaUpsertDto.class));
    }

    @Test
    void testHandle_UpdateMedia_NonExistingMedia_ReturnsNotFound() throws Exception {
        // Arrange
        when(request.getPath()).thenReturn("/media/999");
        when(request.getMethod()).thenReturn("PUT");
        when(request.getAuthorization()).thenReturn("Bearer valid-token");
        when(mediaService.getAuthorizedUserId("Bearer valid-token")).thenReturn(Optional.of(1));

        MediaUpsertDto dto = createValidUpsertDto("Updated Movie", "movie");
        String requestBody = objectMapper.writeValueAsString(dto);
        when(request.getBody()).thenReturn(requestBody);

        when(mediaService.update(eq(1), eq(999), any(MediaUpsertDto.class)))
            .thenReturn(Optional.empty());

        // Act
        Response response = controller.handle(request);

        // Assert
        assertEquals(404, response.getStatusCode());
        assertTrue(response.getBody().contains("Media not found"));
    }

    @Test
    void testHandle_UpdateMedia_NotOwner_ReturnsUnauthorized() throws Exception {
        // Arrange
        when(request.getPath()).thenReturn("/media/10");
        when(request.getMethod()).thenReturn("PUT");
        when(request.getAuthorization()).thenReturn("Bearer valid-token");
        when(mediaService.getAuthorizedUserId("Bearer valid-token")).thenReturn(Optional.of(2));

        MediaUpsertDto dto = createValidUpsertDto("Updated Movie", "movie");
        String requestBody = objectMapper.writeValueAsString(dto);
        when(request.getBody()).thenReturn(requestBody);

        when(mediaService.update(eq(2), eq(10), any(MediaUpsertDto.class)))
            .thenThrow(new SecurityException("Only creator can update this entry"));

        // Act
        Response response = controller.handle(request);

        // Assert
        assertEquals(401, response.getStatusCode());
        assertTrue(response.getBody().contains("Only creator can update this entry"));
    }

    @Test
    void testHandle_UpdateMedia_EmptyBody_ReturnsBadRequest() {
        // Arrange
        when(request.getPath()).thenReturn("/media/10");
        when(request.getMethod()).thenReturn("PUT");
        when(request.getAuthorization()).thenReturn("Bearer valid-token");
        when(mediaService.getAuthorizedUserId("Bearer valid-token")).thenReturn(Optional.of(1));
        when(request.getBody()).thenReturn("   ");

        // Act
        Response response = controller.handle(request);

        // Assert
        assertEquals(400, response.getStatusCode());
        verify(mediaService, never()).update(anyInt(), anyInt(), any());
    }

    @Test
    void testHandle_UpdateMedia_InvalidId_ReturnsBadRequest() {
        // Arrange
        when(request.getPath()).thenReturn("/media/abc");
        when(request.getMethod()).thenReturn("PUT");
        when(request.getAuthorization()).thenReturn("Bearer valid-token");
        when(mediaService.getAuthorizedUserId("Bearer valid-token")).thenReturn(Optional.of(1));

        // Act
        Response response = controller.handle(request);

        // Assert
        assertEquals(404, response.getStatusCode());
        verify(mediaService, never()).update(anyInt(), anyInt(), any());
    }

    // ==================== DELETE /media/{id} Tests ====================

    @Test
    void testHandle_DeleteMedia_ExistingMedia_ByOwner_ReturnsSuccess() {
        // Arrange
        when(request.getPath()).thenReturn("/media/10");
        when(request.getMethod()).thenReturn("DELETE");
        when(request.getAuthorization()).thenReturn("Bearer valid-token");
        when(mediaService.getAuthorizedUserId("Bearer valid-token")).thenReturn(Optional.of(1));
        when(mediaService.delete(1, 10)).thenReturn(true);

        // Act
        Response response = controller.handle(request);

        // Assert
        assertEquals(200, response.getStatusCode());
        assertTrue(response.getBody().contains("Media deleted"));
        verify(mediaService).delete(1, 10);
    }

    @Test
    void testHandle_DeleteMedia_NonExistingMedia_ReturnsNotFound() {
        // Arrange
        when(request.getPath()).thenReturn("/media/999");
        when(request.getMethod()).thenReturn("DELETE");
        when(request.getAuthorization()).thenReturn("Bearer valid-token");
        when(mediaService.getAuthorizedUserId("Bearer valid-token")).thenReturn(Optional.of(1));
        when(mediaService.delete(1, 999)).thenReturn(false);

        // Act
        Response response = controller.handle(request);

        // Assert
        assertEquals(404, response.getStatusCode());
        assertTrue(response.getBody().contains("Media not found"));
    }

    @Test
    void testHandle_DeleteMedia_NotOwner_ReturnsUnauthorized() {
        // Arrange
        when(request.getPath()).thenReturn("/media/10");
        when(request.getMethod()).thenReturn("DELETE");
        when(request.getAuthorization()).thenReturn("Bearer valid-token");
        when(mediaService.getAuthorizedUserId("Bearer valid-token")).thenReturn(Optional.of(2));
        when(mediaService.delete(2, 10))
            .thenThrow(new SecurityException("Only creator can delete this entry"));

        // Act
        Response response = controller.handle(request);

        // Assert
        assertEquals(401, response.getStatusCode());
        assertTrue(response.getBody().contains("Only creator can delete this entry"));
    }

    @Test
    void testHandle_DeleteMedia_InvalidId_ReturnsBadRequest() {
        // Arrange
        when(request.getPath()).thenReturn("/media/xyz");
        when(request.getMethod()).thenReturn("DELETE");
        when(request.getAuthorization()).thenReturn("Bearer valid-token");
        when(mediaService.getAuthorizedUserId("Bearer valid-token")).thenReturn(Optional.of(1));

        // Act
        Response response = controller.handle(request);

        // Assert
        assertEquals(404, response.getStatusCode());
        verify(mediaService, never()).delete(anyInt(), anyInt());
    }

    // ==================== Method Not Allowed Tests ====================

    @Test
    void testHandle_InvalidMethod_OnMediaEndpoint_ReturnsMethodNotAllowed() {
        // Arrange
        when(request.getPath()).thenReturn("/media");
        when(request.getMethod()).thenReturn("PATCH");
        when(request.getAuthorization()).thenReturn("Bearer valid-token");
        when(mediaService.getAuthorizedUserId("Bearer valid-token")).thenReturn(Optional.of(1));

        // Act
        Response response = controller.handle(request);

        // Assert
        assertEquals(405, response.getStatusCode());
        assertTrue(response.getBody().contains("Method not allowed"));
    }

    @Test
    void testHandle_InvalidMethod_OnMediaIdEndpoint_ReturnsMethodNotAllowed() {
        // Arrange
        when(request.getPath()).thenReturn("/media/10");
        when(request.getMethod()).thenReturn("PATCH");
        when(request.getAuthorization()).thenReturn("Bearer valid-token");
        when(mediaService.getAuthorizedUserId("Bearer valid-token")).thenReturn(Optional.of(1));

        // Act
        Response response = controller.handle(request);

        // Assert
        assertEquals(405, response.getStatusCode());
        assertTrue(response.getBody().contains("Method not allowed"));
    }

    // ==================== Route Not Found Tests ====================

    @Test
    void testHandle_InvalidRoute_ReturnsNotFound() {
        // Arrange
        when(request.getPath()).thenReturn("/media/10/invalid");
        when(request.getMethod()).thenReturn("GET");
        when(request.getAuthorization()).thenReturn("Bearer valid-token");
        when(mediaService.getAuthorizedUserId("Bearer valid-token")).thenReturn(Optional.of(1));

        // Act
        Response response = controller.handle(request);

        // Assert
        assertEquals(404, response.getStatusCode());
        assertTrue(response.getBody().contains("Route not found"));
    }

    @Test
    void testHandle_CompletelyWrongPath_ReturnsNotFound() {
        // Arrange
        when(request.getPath()).thenReturn("/wrong/path");
        when(request.getMethod()).thenReturn("GET");
        when(request.getAuthorization()).thenReturn("Bearer valid-token");
        when(mediaService.getAuthorizedUserId("Bearer valid-token")).thenReturn(Optional.of(1));

        // Act
        Response response = controller.handle(request);

        // Assert
        assertEquals(404, response.getStatusCode());
        assertTrue(response.getBody().contains("Route not found"));
    }

    // ==================== Edge Cases Tests ====================

    @Test
    void testHandle_MediaIdZero_WorksCorrectly() {
        // Arrange
        when(request.getPath()).thenReturn("/media/0");
        when(request.getMethod()).thenReturn("GET");
        when(request.getAuthorization()).thenReturn("Bearer valid-token");
        when(mediaService.getAuthorizedUserId("Bearer valid-token")).thenReturn(Optional.of(1));

        MediaDetailDto media = createMediaDetailDto(0, "Test", "movie");
        when(mediaService.getById(0)).thenReturn(Optional.of(media));

        // Act
        Response response = controller.handle(request);

        // Assert
        assertEquals(200, response.getStatusCode());
        verify(mediaService).getById(0);
    }

    @Test
    void testHandle_LargeMediaId_WorksCorrectly() {
        // Arrange
        when(request.getPath()).thenReturn("/media/999999999");
        when(request.getMethod()).thenReturn("GET");
        when(request.getAuthorization()).thenReturn("Bearer valid-token");
        when(mediaService.getAuthorizedUserId("Bearer valid-token")).thenReturn(Optional.of(1));
        when(mediaService.getById(999999999)).thenReturn(Optional.empty());

        // Act
        Response response = controller.handle(request);

        // Assert
        assertEquals(404, response.getStatusCode());
        verify(mediaService).getById(999999999);
    }

    @Test
    void testHandle_NegativeMediaId_ReturnsNotFound() {
        // Arrange
        when(request.getPath()).thenReturn("/media/-1");
        when(request.getMethod()).thenReturn("GET");
        when(request.getAuthorization()).thenReturn("Bearer valid-token");
        when(mediaService.getAuthorizedUserId("Bearer valid-token")).thenReturn(Optional.of(1));

        // Act
        Response response = controller.handle(request);

        // Assert
        assertEquals(404, response.getStatusCode());
        verify(mediaService, never()).getById(anyInt());
    }

    @Test
    void testHandle_PathWithTrailingSlash_ReturnsNotFound() {
        // Arrange
        when(request.getPath()).thenReturn("/media/10/");
        when(request.getMethod()).thenReturn("GET");
        when(request.getAuthorization()).thenReturn("Bearer valid-token");
        when(mediaService.getAuthorizedUserId("Bearer valid-token")).thenReturn(Optional.of(1));

        // Act
        Response response = controller.handle(request);

        // Assert
        assertEquals(404, response.getStatusCode());
    }

    // ==================== Helper Methods ====================

    private MediaUpsertDto createValidUpsertDto(String title, String mediaType) {
        MediaUpsertDto dto = new MediaUpsertDto();
        dto.setTitle(title);
        dto.setMediaType(mediaType);
        dto.setDescription("A great " + mediaType);
        dto.setReleaseYear(2020);
        dto.setAgeRestriction(12);
        dto.setGenres(Arrays.asList("Action", "Drama"));
        return dto;
    }

    private MediaDetailDto createMediaDetailDto(Integer id, String title, String mediaType) {
        return new MediaDetailDto(
            id,
            title,
            "Description of " + title,
            mediaType,
            2020,
            Arrays.asList("Action", "Drama"),
            12,
            new ArrayList<>(),
            0.0
        );
    }
}

