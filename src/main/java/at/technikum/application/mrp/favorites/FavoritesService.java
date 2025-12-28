package at.technikum.application.mrp.favorites;

import at.technikum.application.mrp.favorites.entity.FavoriteEntity;
import at.technikum.application.mrp.media.MediaRepository;
import at.technikum.application.mrp.user.UserRepository;
import at.technikum.application.mrp.user.entity.UserEntity;

import java.util.List;
import java.util.stream.Collectors;

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
        return favoritesRepository.findByUserId(userId).stream()
                .map(FavoriteEntity::getMediaId)
                .collect(Collectors.toList());
    }

    public void addFavorite(String authorizationHeader, int mediaId) {
        int userId = requireAuthorizedUserId(authorizationHeader);
        validateMediaId(mediaId);

        // Prüfen ob Media existiert
        if (mediaRepository.findById(mediaId).isEmpty()) {
            throw new IllegalArgumentException("Media not found");
        }

        // Prüfen ob bereits Favorit
        if (favoritesRepository.exists(userId, mediaId)) {
            throw new IllegalStateException("Already in favorites");
        }

        FavoriteEntity favorite = new FavoriteEntity(userId, mediaId);
        favoritesRepository.create(favorite);
    }

    public void removeFavorite(String authorizationHeader, int mediaId) {
        int userId = requireAuthorizedUserId(authorizationHeader);
        validateMediaId(mediaId);

        if (!favoritesRepository.delete(userId, mediaId)) {
            throw new IllegalStateException("Favorite not found");
        }
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


