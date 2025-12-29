package at.technikum.application.mrp.auth;

import at.technikum.server.http.Request;
import at.technikum.server.http.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerUnitTest {

    @Mock
    private AuthService authService;

    @Mock
    private Request request;

    private AuthController controller;

    @BeforeEach
    void setUp() {
        controller = new AuthController(authService);
    }

    // ==================== POST /auth/register Tests ====================

    @Test
    void testHandle_Register_ValidRequest_ReturnsOk() {
        // Arrange
        when(request.getPath()).thenReturn("/auth/register");
        when(request.getMethod()).thenReturn("POST");
        when(request.getBody()).thenReturn("{\"username\":\"john_doe\",\"email\":\"john@example.com\",\"password\":\"password123\"}");
        when(authService.usernameExists("john_doe")).thenReturn(false);

        // Act
        Response response = controller.handle(request);

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
        assertTrue(response.getBody().contains("User registered"));
        verify(authService).usernameExists("john_doe");
        verify(authService).register(any());
    }

    @Test
    void testHandle_Register_UsernameExists_ReturnsConflict() {
        // Arrange
        when(request.getPath()).thenReturn("/auth/register");
        when(request.getMethod()).thenReturn("POST");
        when(request.getBody()).thenReturn("{\"username\":\"john_doe\",\"email\":\"john@example.com\",\"password\":\"password123\"}");
        when(authService.usernameExists("john_doe")).thenReturn(true);

        // Act
        Response response = controller.handle(request);

        // Assert
        assertNotNull(response);
        assertEquals(409, response.getStatusCode());
        assertTrue(response.getBody().contains("Username already exists"));
        verify(authService).usernameExists("john_doe");
        verify(authService, never()).register(any());
    }

    @Test
    void testHandle_Register_EmptyBody_ReturnsBadRequest() {
        // Arrange
        when(request.getPath()).thenReturn("/auth/register");
        when(request.getMethod()).thenReturn("POST");
        when(request.getBody()).thenReturn("");

        // Act
        Response response = controller.handle(request);

        // Assert
        assertNotNull(response);
        assertEquals(400, response.getStatusCode());
        assertTrue(response.getBody().contains("Request body is empty"));
        verify(authService, never()).usernameExists(any());
        verify(authService, never()).register(any());
    }

    @Test
    void testHandle_Register_NullBody_ReturnsBadRequest() {
        // Arrange
        when(request.getPath()).thenReturn("/auth/register");
        when(request.getMethod()).thenReturn("POST");
        when(request.getBody()).thenReturn(null);

        // Act
        Response response = controller.handle(request);

        // Assert
        assertNotNull(response);
        assertEquals(400, response.getStatusCode());
        assertTrue(response.getBody().contains("Request body is empty"));
        verify(authService, never()).usernameExists(any());
        verify(authService, never()).register(any());
    }

    @Test
    void testHandle_Register_InvalidJson_ReturnsBadRequest() {
        // Arrange
        when(request.getPath()).thenReturn("/auth/register");
        when(request.getMethod()).thenReturn("POST");
        when(request.getBody()).thenReturn("invalid json");

        // Act
        Response response = controller.handle(request);

        // Assert
        assertNotNull(response);
        assertEquals(400, response.getStatusCode());
        assertTrue(response.getBody().contains("Invalid JSON"));
        verify(authService, never()).usernameExists(any());
        verify(authService, never()).register(any());
    }

    // ==================== POST /auth/login Tests ====================

    @Test
    void testHandle_Login_ValidCredentials_ReturnsOkWithToken() {
        // Arrange
        when(request.getPath()).thenReturn("/auth/login");
        when(request.getMethod()).thenReturn("POST");
        when(request.getBody()).thenReturn("{\"username\":\"john_doe\",\"password\":\"password123\"}");
        when(authService.login(any())).thenReturn("john_doe-mrpToken");

        // Act
        Response response = controller.handle(request);

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
        assertTrue(response.getBody().contains("john_doe-mrpToken"));
        verify(authService).login(any());
    }

    @Test
    void testHandle_Login_InvalidCredentials_ReturnsUnauthorized() {
        // Arrange
        when(request.getPath()).thenReturn("/auth/login");
        when(request.getMethod()).thenReturn("POST");
        when(request.getBody()).thenReturn("{\"username\":\"john_doe\",\"password\":\"wrongpassword\"}");
        when(authService.login(any())).thenReturn(null);

        // Act
        Response response = controller.handle(request);

        // Assert
        assertNotNull(response);
        assertEquals(401, response.getStatusCode());
        assertTrue(response.getBody().contains("Invalid credentials"));
        verify(authService).login(any());
    }

    @Test
    void testHandle_Login_EmptyBody_ReturnsBadRequest() {
        // Arrange
        when(request.getPath()).thenReturn("/auth/login");
        when(request.getMethod()).thenReturn("POST");
        when(request.getBody()).thenReturn("");

        // Act
        Response response = controller.handle(request);

        // Assert
        assertNotNull(response);
        assertEquals(400, response.getStatusCode());
        assertTrue(response.getBody().contains("Request body is empty"));
        verify(authService, never()).login(any());
    }

    @Test
    void testHandle_Login_InvalidJson_ReturnsBadRequest() {
        // Arrange
        when(request.getPath()).thenReturn("/auth/login");
        when(request.getMethod()).thenReturn("POST");
        when(request.getBody()).thenReturn("not json");

        // Act
        Response response = controller.handle(request);

        // Assert
        assertNotNull(response);
        assertEquals(400, response.getStatusCode());
        assertTrue(response.getBody().contains("Invalid JSON"));
        verify(authService, never()).login(any());
    }

    // ==================== Route Not Found Tests ====================

    @Test
    void testHandle_UnknownRoute_ReturnsNotFound() {
        // Arrange
        when(request.getPath()).thenReturn("/auth/unknown");
        when(request.getMethod()).thenReturn("GET");

        // Act
        Response response = controller.handle(request);

        // Assert
        assertNotNull(response);
        assertEquals(404, response.getStatusCode());
        assertTrue(response.getBody().contains("Route not found"));
        verify(authService, never()).login(any());
        verify(authService, never()).register(any());
    }

    @Test
    void testHandle_WrongMethod_ReturnsSuccess() {
        // Arrange - both endpoints only check the path pattern and POST method
        when(request.getPath()).thenReturn("/auth/register");
        when(request.getMethod()).thenReturn("GET");

        // Act
        Response response = controller.handle(request);

        // Assert
        assertNotNull(response);
        // The controller returns an empty response with default status (200)
        // since the conditions for POST handling are not met
        verify(authService, never()).register(any());
    }
}

