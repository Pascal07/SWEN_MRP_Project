package at.technikum.application.mrp.auth;
import at.technikum.application.mrp.user.UserEntity;

public class AuthService {
    private final AuthRepository authRepository = new AuthRepository();

    public void register(AuthRequestDto dto) {
        UserEntity entity = new UserEntity();
        entity.setUsername(dto.getUsername());
        entity.setPassword(dto.getPassword());
        authRepository.save(entity);
    }

    public String login(AuthRequestDto dto) {
        return authRepository.findByUsername(dto.getUsername())
                .filter(u -> u.getPassword().equals(dto.getPassword()))
                .map(u -> u.getUsername() + "-mrpToken")
                .orElse(null);
    }

    public boolean usernameExists(String username) {
        return authRepository.findByUsername(username).isPresent();
    }
}
