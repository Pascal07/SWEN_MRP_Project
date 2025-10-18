package at.technikum.application.mrp.auth;
import at.technikum.application.mrp.auth.dto.AuthRequestDto;
import at.technikum.application.mrp.user.entity.UserEntity;

import java.util.Optional;

public class AuthService {
    private final AuthRepository authRepository;

    public AuthService(AuthRepository authRepository) {
        this.authRepository = authRepository;
    }
    public void register(AuthRequestDto dto) {
        UserEntity entity = new UserEntity();
        entity.setUsername(dto.getUsername());
        entity.setPassword(dto.getPassword());
        authRepository.save(entity);
    }

    public String login(AuthRequestDto dto) {
        return authRepository.findByUsername(dto.getUsername())
                .filter(u -> u.getPassword().equals(dto.getPassword()))
                .map(u -> {
                    String token = u.getUsername() + "-mrpToken";
                    AuthTokenStore.store(token, u);
                    return token;
                })
                .orElse(null);
    }

    public boolean usernameExists(String username) {
        return authRepository.findByUsername(username).isPresent();
    }

    public Optional<UserEntity> getUserByToken(String token) {
        return AuthTokenStore.getUser(token);
    }
}
