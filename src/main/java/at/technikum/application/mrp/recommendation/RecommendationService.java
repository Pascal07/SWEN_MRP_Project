package at.technikum.application.mrp.recommendation;

import at.technikum.application.mrp.media.MediaRepository;
import at.technikum.application.mrp.rating.RatingRepository;
import at.technikum.application.mrp.user.UserRepository;
import at.technikum.application.mrp.user.entity.UserEntity;
import java.util.List;
import java.util.Map;

public class RecommendationService {
    private final RecommendationRepository recommendationRepository;
    private final UserRepository userRepository;
    private final MediaRepository mediaRepository;
    private final RatingRepository ratingRepository;

    public RecommendationService(RecommendationRepository recommendationRepository, UserRepository userRepository,
                                 MediaRepository mediaRepository,
                                 RatingRepository ratingRepository) {
        this.recommendationRepository = recommendationRepository;
        this.userRepository = userRepository;
        this.mediaRepository = mediaRepository;
        this.ratingRepository = ratingRepository;
    }

    // wirft Exceptions, die vom ExceptionMapper verarbeitet werden
    public List<Map<String, Object>> recommendationsByGenre(String authorizationHeader, String genre) {
        int userId = requireAuthorizedUserId(authorizationHeader);
        if (genre == null || genre.isBlank()) {
            throw new IllegalArgumentException("genre query parameter is required for type=genre");
        }
        // Logikfrei: Platzhalter
        return List.of();
    }

    public List<Map<String, Object>> recommendationsForUser(String authorizationHeader) {
        int userId = requireAuthorizedUserId(authorizationHeader);
        // Logikfrei: Platzhalter
        return List.of();
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

    private String extractBearerToken(String authorizationHeader) {
        if (authorizationHeader == null) return null;
        String prefix = "Bearer ";
        if (!authorizationHeader.startsWith(prefix)) return null;
        String token = authorizationHeader.substring(prefix.length()).trim();
        return token.isEmpty() ? null : token;
    }
}
