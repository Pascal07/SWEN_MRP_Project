package at.technikum.application.mrp.recommendation;

import at.technikum.application.mrp.media.MediaRepository;
import at.technikum.application.mrp.rating.RatingRepository;
import at.technikum.application.mrp.user.UserRepository;
import at.technikum.application.mrp.user.entity.UserEntity;

import java.util.List;
import java.util.Map;
import java.util.Optional;

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

    // Auth helper (wie in anderen Services)
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

    // Logikfreie Platzhalter-Methoden
    public List<Map<String, Object>> byGenre(int userId, String genre) {
        return List.of();
    }

    public List<Map<String, Object>> forUser(int userId) {
        return List.of();
    }
}
