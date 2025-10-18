package at.technikum.application.mrp.favorites;

import at.technikum.application.mrp.media.MediaRepository;
import at.technikum.application.mrp.user.UserRepository;
import at.technikum.application.mrp.user.entity.UserEntity;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class FavoritesService {
    private final FavoritesRepository favoritesRepository;
    private final UserRepository userRepository;
    private final MediaRepository mediaRepository;

    public FavoritesService(FavoritesRepository favoritesRepository, UserRepository userRepository, MediaRepository mediaRepository) {
        this.favoritesRepository = favoritesRepository;
        this.userRepository = userRepository;
        this.mediaRepository = mediaRepository;
    }

    // Auth helper (gleich wie in anderen Services)
    public Optional<Integer> getAuthorizedUserId(String authorizationHeader) {
        String token = extractBearerToken(authorizationHeader);
        if (token == null) return Optional.empty();
        return userRepository.findByToken(token).map(UserEntity::getId);
    }

    private String extractBearerToken(String authorizationHeader) {
        if (authorizationHeader == null) return null;
        String prefix = "Bearer ";
        if (!authorizationHeader.startsWith(prefix)) return null;
        String token = authorizationHeader.substring(prefix.length()).trim();
        return token.isEmpty() ? null : token;
    }

    // Logikfreie Skeleton-Methoden
    public List<Integer> listFavorites(int userId) {
        return Collections.emptyList();
    }

    public boolean addFavorite(int userId, int mediaId) {
        return false;
    }

    public boolean removeFavorite(int userId, int mediaId) {
        return false;
    }
}
