package at.technikum.application.mrp.auth;

import at.technikum.application.mrp.auth.dto.AuthRequestDto;
import at.technikum.application.mrp.user.entity.UserEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceUnitTest {

    @Mock
    private AuthRepository authRepository;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(authRepository);
    }

    // ==================== register Tests ====================

    @Test
    void testRegister_ValidDto_SavesUser() {
        // Arrange
        AuthRequestDto dto = new AuthRequestDto();
        dto.setUsername("john_doe");
        dto.setEmail("john@example.com");
        dto.setPassword("password123");

        // Act
        authService.register(dto);

        // Assert
        ArgumentCaptor<UserEntity> captor = ArgumentCaptor.forClass(UserEntity.class);
        verify(authRepository).save(captor.capture());

        UserEntity savedUser = captor.getValue();
        assertEquals("john_doe", savedUser.getUsername());
        assertEquals("john@example.com", savedUser.getEmail());
        assertEquals("password123", savedUser.getPassword());
    }

    @Test
    void testRegister_MultipleUsers_SavesAll() {
        // Arrange
        AuthRequestDto dto1 = new AuthRequestDto();
        dto1.setUsername("user1");
        dto1.setEmail("user1@example.com");
        dto1.setPassword("pass1");

        AuthRequestDto dto2 = new AuthRequestDto();
        dto2.setUsername("user2");
        dto2.setEmail("user2@example.com");
        dto2.setPassword("pass2");

        // Act
        authService.register(dto1);
        authService.register(dto2);

        // Assert
        verify(authRepository, times(2)).save(any(UserEntity.class));
    }

    // ==================== login Tests ====================

    @Test
    void testLogin_ValidCredentials_ReturnsToken() {
        // Arrange
        AuthRequestDto dto = new AuthRequestDto();
        dto.setUsername("john_doe");
        dto.setPassword("password123");

        UserEntity mockUser = new UserEntity();
        mockUser.setUsername("john_doe");
        mockUser.setPassword("password123");

        when(authRepository.findByUsername("john_doe")).thenReturn(Optional.of(mockUser));

        // Act
        String token = authService.login(dto);

        // Assert
        assertNotNull(token);
        assertEquals("john_doe-mrpToken", token);
        verify(authRepository).findByUsername("john_doe");
    }

    @Test
    void testLogin_InvalidPassword_ReturnsNull() {
        // Arrange
        AuthRequestDto dto = new AuthRequestDto();
        dto.setUsername("john_doe");
        dto.setPassword("wrongpassword");

        UserEntity mockUser = new UserEntity();
        mockUser.setUsername("john_doe");
        mockUser.setPassword("password123");

        when(authRepository.findByUsername("john_doe")).thenReturn(Optional.of(mockUser));

        // Act
        String token = authService.login(dto);

        // Assert
        assertNull(token);
        verify(authRepository).findByUsername("john_doe");
    }

    @Test
    void testLogin_UserNotFound_ReturnsNull() {
        // Arrange
        AuthRequestDto dto = new AuthRequestDto();
        dto.setUsername("nonexistent");
        dto.setPassword("password123");

        when(authRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        // Act
        String token = authService.login(dto);

        // Assert
        assertNull(token);
        verify(authRepository).findByUsername("nonexistent");
    }

    // ==================== usernameExists Tests ====================

    @Test
    void testUsernameExists_ExistingUser_ReturnsTrue() {
        // Arrange
        UserEntity mockUser = new UserEntity();
        mockUser.setUsername("john_doe");
        when(authRepository.findByUsername("john_doe")).thenReturn(Optional.of(mockUser));

        // Act
        boolean exists = authService.usernameExists("john_doe");

        // Assert
        assertTrue(exists);
        verify(authRepository).findByUsername("john_doe");
    }

    @Test
    void testUsernameExists_NonExistentUser_ReturnsFalse() {
        // Arrange
        when(authRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        // Act
        boolean exists = authService.usernameExists("nonexistent");

        // Assert
        assertFalse(exists);
        verify(authRepository).findByUsername("nonexistent");
    }

    // ==================== getUserByToken Tests ====================

    @Test
    void testGetUserByToken_ValidToken_ReturnsUser() {
        // Arrange
        String token = "john_doe-mrpToken";
        UserEntity mockUser = new UserEntity();
        mockUser.setUsername("john_doe");

        when(authRepository.findByUsername("john_doe")).thenReturn(Optional.of(mockUser));

        // Act
        Optional<UserEntity> result = authService.getUserByToken(token);

        // Assert
        assertTrue(result.isPresent());
        assertEquals("john_doe", result.get().getUsername());
        verify(authRepository).findByUsername("john_doe");
    }

    @Test
    void testGetUserByToken_InvalidTokenFormat_ReturnsEmpty() {
        // Arrange
        String token = "invalidtoken";

        // Act
        Optional<UserEntity> result = authService.getUserByToken(token);

        // Assert
        assertFalse(result.isPresent());
        verify(authRepository, never()).findByUsername(any());
    }

    @Test
    void testGetUserByToken_NullToken_ReturnsEmpty() {
        // Arrange
        String token = null;

        // Act
        Optional<UserEntity> result = authService.getUserByToken(token);

        // Assert
        assertFalse(result.isPresent());
        verify(authRepository, never()).findByUsername(any());
    }

    @Test
    void testGetUserByToken_BlankToken_ReturnsEmpty() {
        // Arrange
        String token = "  ";

        // Act
        Optional<UserEntity> result = authService.getUserByToken(token);

        // Assert
        assertFalse(result.isPresent());
        verify(authRepository, never()).findByUsername(any());
    }

    @Test
    void testGetUserByToken_TokenWithoutSuffix_ReturnsEmpty() {
        // Arrange
        String token = "john_doe-wrongSuffix";

        // Act
        Optional<UserEntity> result = authService.getUserByToken(token);

        // Assert
        assertFalse(result.isPresent());
        verify(authRepository, never()).findByUsername(any());
    }

    @Test
    void testGetUserByToken_OnlySuffix_ReturnsEmpty() {
        // Arrange
        String token = "-mrpToken";

        // Act
        Optional<UserEntity> result = authService.getUserByToken(token);

        // Assert
        assertFalse(result.isPresent());
        verify(authRepository, never()).findByUsername(any());
    }

    @Test
    void testGetUserByToken_UserNotFound_ReturnsEmpty() {
        // Arrange
        String token = "nonexistent-mrpToken";
        when(authRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        // Act
        Optional<UserEntity> result = authService.getUserByToken(token);

        // Assert
        assertFalse(result.isPresent());
        verify(authRepository).findByUsername("nonexistent");
    }
}

