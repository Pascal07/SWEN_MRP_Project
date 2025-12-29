package at.technikum.application.mrp.recommendation;

import at.technikum.application.mrp.media.MediaRepository;
import at.technikum.application.mrp.media.entity.MediaEntryEntity;
import at.technikum.application.mrp.rating.RatingRepository;
import at.technikum.application.mrp.rating.entity.RatingEntity;
import at.technikum.application.mrp.user.UserRepository;
import at.technikum.application.mrp.user.entity.UserEntity;
import java.util.*;
import java.util.stream.Collectors;

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

    // Empfehlungen basierend auf Genre
    public List<Map<String, Object>> recommendationsByGenre(String authorizationHeader, String genre) {
        int userId = requireAuthorizedUserId(authorizationHeader);
        if (genre == null || genre.isBlank()) {
            throw new IllegalArgumentException("genre query parameter is required for type=genre");
        }

        // Suche nach Media mit dem angegebenen Genre
        List<MediaEntryEntity> mediaList = mediaRepository.search(null, genre, null, null, null, null, "score");

        // In Map-Format konvertieren und Bewertungen einschließen
        return mediaList.stream()
                .map(this::mediaToMap)
                .collect(Collectors.toList());
    }

    // Empfehlungen basierend auf Media-Type (movie oder series)
    public List<Map<String, Object>> recommendationsByMediaType(String authorizationHeader, String mediaType) {
        int userId = requireAuthorizedUserId(authorizationHeader);

        // Suche nach Media mit dem angegebenen Type
        List<MediaEntryEntity> mediaList = mediaRepository.search(null, null, mediaType, null, null, null, "score");

        return mediaList.stream()
                .map(this::mediaToMap)
                .collect(Collectors.toList());
    }

    // Allgemeine Empfehlungen für User (basierend auf höchsten Bewertungen)
    public List<Map<String, Object>> recommendationsForUser(String authorizationHeader) {
        int userId = requireAuthorizedUserId(authorizationHeader);

        // Hole alle Media sortiert nach Score
        List<MediaEntryEntity> mediaList = mediaRepository.search(null, null, null, null, null, null, "score");

        // Limitiere auf Top 10 Empfehlungen
        return mediaList.stream()
                .limit(10)
                .map(this::mediaToMap)
                .collect(Collectors.toList());
    }

    // Helper: MediaEntryEntity zu Map konvertieren
    private Map<String, Object> mediaToMap(MediaEntryEntity media) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", media.getId());
        map.put("title", media.getTitle());
        map.put("description", media.getDescription());
        map.put("genres", media.getGenres());
        map.put("mediaType", media.getMediaType());
        map.put("releaseYear", media.getReleaseYear());
        map.put("ageRestriction", media.getAgeRestriction());

        // Durchschnittliche Bewertung berechnen
        if (media.getRatings() != null && !media.getRatings().isEmpty()) {
            double avgRating = media.getRatings().stream()
                    .mapToInt(RatingEntity::getScore)
                    .average()
                    .orElse(0.0);
            map.put("averageRating", Math.round(avgRating * 100.0) / 100.0);
            map.put("ratingCount", media.getRatings().size());
        } else {
            map.put("averageRating", null);
            map.put("ratingCount", 0);
        }

        return map;
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
