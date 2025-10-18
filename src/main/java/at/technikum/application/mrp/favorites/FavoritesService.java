package at.technikum.application.mrp.favorites;

import at.technikum.application.mrp.media.MediaRepository;
import at.technikum.application.mrp.user.UserRepository;
import at.technikum.application.mrp.user.entity.UserEntity;

import java.util.Collections;
import java.util.List;

public class FavoritesService {
    private final FavoritesRepository favoritesRepository;
    private final UserRepository userRepository;
    private final MediaRepository mediaRepository;

    public FavoritesService(FavoritesRepository favoritesRepository, UserRepository userRepository, MediaRepository mediaRepository) {
        this.favoritesRepository = favoritesRepository;
        this.userRepository = userRepository;
        this.mediaRepository = mediaRepository;
    }

    // Öffentliche API: wirft Exceptions, die zentral gemappt werden
    public List<Integer> listFavorites(String authorizationHeader) {
        int userId = requireAuthorizedUserId(authorizationHeader);
        // Logikfrei: Platzhalter
        return Collections.emptyList();
    }

    public void addFavorite(String authorizationHeader, int mediaId) {
        int userId = requireAuthorizedUserId(authorizationHeader);
        validateMediaId(mediaId);
        // Logikfrei: Platzhalter (kein Persist)
    }

    public void removeFavorite(String authorizationHeader, int mediaId) {
        int userId = requireAuthorizedUserId(authorizationHeader);
        validateMediaId(mediaId);
        // Logikfrei: Platzhalter (kein Persist)
    }

    // Helpers
    private int requireAuthorizedUserId(String authorizationHeader) {
        String token = extractBearerToken(authorizationHeader);
        if (token == null) {
            throw new SecurityException("Missing or invalid Authorization header");
        }
        return userRepository.findByToken(token)
                .map(UserEntity::getId)
                .orElseThrow(() -> new SecurityException("Invalid token"));
    }

    private void validateMediaId(int mediaId) {
        if (mediaId <= 0) {
            throw new IllegalArgumentException("Invalid media id");
        }
    }

    private String extractBearerToken(String authorizationHeader) {
        if (authorizationHeader == null) return null;
        String prefix = "Bearer ";
        if (!authorizationHeader.startsWith(prefix)) return null;
        String token = authorizationHeader.substring(prefix.length()).trim();
        return token.isEmpty() ? null : token;
    }
}
