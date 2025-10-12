package at.technikum.application.mrp.user;

import java.util.List;
import java.util.Optional;

public class UserService {

    private final UserRepository userRepository = new UserRepository();

    // Public API: Controller bekommt direkt DTOs und sieht keine Entities
    public Optional<UserProfileDto> getProfile(String authorizationHeader) {
        return authenticate(authorizationHeader)
                .map(this::buildProfile);
    }

    public Optional<UserRatingsDto> getRatings(String authorizationHeader) {
        return authenticate(authorizationHeader)
                .map(user -> {
                    List<Object> ratings = userRepository.findRatingsByUserId(user.getId());
                    return new UserRatingsDto(user.getId(), ratings);
                });
    }

    public Optional<UserFavoritesDto> getFavorites(String authorizationHeader) {
        return authenticate(authorizationHeader)
                .map(user -> {
                    List<Object> favorites = userRepository.findFavoritesByUserId(user.getId());
                    return new UserFavoritesDto(user.getId(), favorites);
                });
    }

    // Intern: Authentifizierung und Hilfslogik
    private Optional<UserEntity> authenticate(String authorizationHeader) {
        String token = extractBearerToken(authorizationHeader);
        if (token == null) return Optional.empty();
        return userRepository.findByToken(token);
    }

    private UserProfileDto buildProfile(UserEntity user) {
        return new UserProfileDto(user.getId(), user.getUsername());
    }

    private String extractBearerToken(String authorizationHeader) {
        if (authorizationHeader == null) return null;
        String prefix = "Bearer ";
        if (!authorizationHeader.startsWith(prefix)) return null;
        String token = authorizationHeader.substring(prefix.length()).trim();
        return token.isEmpty() ? null : token;
    }
}
