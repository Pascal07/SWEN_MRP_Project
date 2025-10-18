package at.technikum.application.mrp.media;

import at.technikum.application.mrp.media.dto.MediaDetailDto;
import at.technikum.application.mrp.media.dto.MediaUpsertDto;
import at.technikum.application.mrp.rating.dto.RatingDto;
import at.technikum.application.mrp.media.entity.MediaEntryEntity;
import at.technikum.application.mrp.user.UserRepository;
import at.technikum.application.mrp.user.entity.UserEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class MediaService {

    private final MediaRepository mediaRepository;
    private final UserRepository userRepository;

    public MediaService(MediaRepository mediaRepository, UserRepository userRepository) {
        this.mediaRepository = mediaRepository;
        this.userRepository = userRepository;
    }

    // Auth helpers
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

    // Create
    public MediaDetailDto create(int userId, MediaUpsertDto dto) {
        validateUpsert(dto);
        MediaEntryEntity e = new MediaEntryEntity();
        e.setCreatorUserId(userId);
        applyUpsert(e, dto);
        MediaEntryEntity saved = mediaRepository.create(e);
        return toDetailDto(saved);
    }

    // Get by id (auth already checked in controller)
    public Optional<MediaDetailDto> getById(int id) {
        return mediaRepository.findById(id).map(this::toDetailDto);
    }

    // Update (only by owner)
    public Optional<MediaDetailDto> update(int userId, int id, MediaUpsertDto dto) {
        validateUpsert(dto);
        Optional<MediaEntryEntity> existingOpt = mediaRepository.findById(id);
        if (existingOpt.isEmpty()) return Optional.empty();
        MediaEntryEntity existing = existingOpt.get();
        if (!existing.getCreatorUserId().equals(userId)) {
            throw new SecurityException("Only creator can update this entry");
        }
        applyUpsert(existing, dto);
        MediaEntryEntity updated = mediaRepository.update(existing);
        return Optional.ofNullable(updated).map(this::toDetailDto);
    }

    // Delete (only by owner)
    public boolean delete(int userId, int id) {
        Optional<MediaEntryEntity> existingOpt = mediaRepository.findById(id);
        if (existingOpt.isEmpty()) return false;
        MediaEntryEntity existing = existingOpt.get();
        if (!existing.getCreatorUserId().equals(userId)) {
            throw new SecurityException("Only creator can delete this entry");
        }
        return mediaRepository.delete(id);
    }

    // Search & filter
    public List<MediaDetailDto> search(Map<String, String> query) {
        String title = normalize(query.get("title"));
        String genre = normalize(query.get("genre"));
        String mediaType = normalize(query.get("mediaType"));
        Integer releaseYear = parseInt(query.get("releaseYear"));
        Integer ageRestriction = parseInt(query.get("ageRestriction"));
        Integer minRating = parseInt(query.get("rating"));
        String sortBy = normalize(query.get("sortBy"));

        return mediaRepository.search(title, genre, mediaType, releaseYear, ageRestriction, minRating, sortBy)
                .stream()
                .map(this::toDetailDto)
                .collect(Collectors.toList());
    }

    private String normalize(String v) {
        if (v == null) return null;
        v = v.trim();
        return v.isEmpty() ? null : v;
    }

    private Integer parseInt(String s) {
        try {
            if (s == null || s.isBlank()) return null;
            return Integer.parseInt(s.trim());
        } catch (Exception e) {
            return null;
        }
    }

    private void validateUpsert(MediaUpsertDto dto) {
        if (dto == null) throw new IllegalArgumentException("Body is required");
        if (dto.getTitle() == null || dto.getTitle().isBlank()) {
            throw new IllegalArgumentException("title is required");
        }
        if (dto.getMediaType() == null || dto.getMediaType().isBlank()) {
            throw new IllegalArgumentException("mediaType is required");
        }
        String mt = dto.getMediaType().toLowerCase();
        if (!mt.equals("movie") && !mt.equals("series") && !mt.equals("game")) {
            throw new IllegalArgumentException("mediaType must be movie, series, or game");
        }
        // Optional: further field validations can be added here
    }

    private void applyUpsert(MediaEntryEntity e, MediaUpsertDto dto) {
        e.setTitle(dto.getTitle());
        e.setDescription(dto.getDescription());
        e.setMediaType(dto.getMediaType());
        e.setReleaseYear(dto.getReleaseYear());
        e.setGenres(dto.getGenres() == null ? new ArrayList<>() : new ArrayList<>(dto.getGenres()));
        e.setAgeRestriction(dto.getAgeRestriction());
    }

    private MediaDetailDto toDetailDto(MediaEntryEntity e) {
        List<RatingDto> ratings = e.getRatings() == null ? List.of() : e.getRatings().stream()
                .map(r -> new RatingDto(r.getUserId(), r.getScore()))
                .collect(Collectors.toList());
        return new MediaDetailDto(
                e.getId(),
                e.getTitle(),
                e.getDescription(),
                e.getMediaType(),
                e.getReleaseYear(),
                e.getGenres(),
                e.getAgeRestriction(),
                ratings,
                e.getAverageScore()
        );
    }
}
