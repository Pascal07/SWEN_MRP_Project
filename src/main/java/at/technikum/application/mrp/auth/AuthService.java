package at.technikum.application.mrp.auth;
import at.technikum.application.mrp.auth.dto.AuthRequestDto;
import at.technikum.application.mrp.user.entity.UserEntity;

import java.util.Optional;

public class AuthService {
    private static final String TOKEN_SUFFIX = "-mrpToken";

    private final AuthRepository authRepository;

    public AuthService(AuthRepository authRepository) {
        this.authRepository = authRepository;
    }

    public void register(AuthRequestDto dto) {
        UserEntity entity = new UserEntity();
        entity.setUsername(dto.getUsername());
        entity.setEmail(dto.getEmail());
        entity.setPassword(dto.getPassword());
        authRepository.save(entity);
    }

    public String login(AuthRequestDto dto) {
        return authRepository.findByUsername(dto.getUsername())
                .filter(u -> u.getPassword().equals(dto.getPassword()))
                .map(u -> generateToken(u.getUsername()))
                .orElse(null);
    }

    public boolean usernameExists(String username) {
        return authRepository.findByUsername(username).isPresent();
    }

    public Optional<UserEntity> getUserByToken(String token) {
        return extractUsernameFromToken(token)
                .flatMap(authRepository::findByUsername);
    }

    private String generateToken(String username) {
        return username + TOKEN_SUFFIX;
    }

    private Optional<String> extractUsernameFromToken(String token) {
        if (token == null || token.isBlank()) {
            return Optional.empty();
        }
        if (!token.endsWith(TOKEN_SUFFIX)) {
            return Optional.empty();
        }
        String username = token.substring(0, token.length() - TOKEN_SUFFIX.length());
        return username.isBlank() ? Optional.empty() : Optional.of(username);
    }
}
