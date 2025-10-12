package at.technikum.application.mrp.rating;

import at.technikum.application.mrp.media.MediaRepository;
import at.technikum.application.mrp.media.entity.MediaEntryEntity;
import at.technikum.application.mrp.rating.dto.RatingDetailDto;
import at.technikum.application.mrp.rating.dto.RatingUpsertDto;
import at.technikum.application.mrp.rating.entity.RatingEntity;
import at.technikum.application.mrp.user.UserRepository;
import at.technikum.application.mrp.user.entity.UserEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class RatingService {

    private final RatingRepository ratingRepository = new RatingRepository();
    private final MediaRepository mediaRepository = new MediaRepository();
    private final UserRepository userRepository = new UserRepository();

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
    public RatingDetailDto create(int userId, RatingUpsertDto dto) {
        validateUpsert(dto, true);
        MediaEntryEntity media = mediaRepository.findById(dto.getMediaId())
                .orElseThrow(() -> new IllegalArgumentException("Media not found"));

        RatingEntity e = new RatingEntity();
        e.setUserId(userId);
        e.setMediaId(media.getId());
        e.setScore(dto.getScore());
        e.setComment(dto.getComment());
        e.setConfirmed(false); // moderation required for comments
        e.setTimestamp(System.currentTimeMillis());

        RatingEntity saved = ratingRepository.create(e);
        syncMediaRatings(saved.getMediaId());
        return toDetailDto(saved, userId);
    }

    public Optional<RatingDetailDto> getById(int requesterUserId, int id) {
        return ratingRepository.findById(id).map(r -> toDetailDto(r, requesterUserId));
    }

    public List<RatingDetailDto> listByMedia(int requesterUserId, int mediaId) {
        // verify media exists
        mediaRepository.findById(mediaId).orElseThrow(() -> new IllegalArgumentException("Media not found"));
        return ratingRepository.findByMediaId(mediaId).stream()
                .map(r -> toDetailDto(r, requesterUserId))
                .collect(Collectors.toList());
    }

    // Update only by owner
    public Optional<RatingDetailDto> update(int userId, int id, RatingUpsertDto dto) {
        validateUpsert(dto, false);
        Optional<RatingEntity> existingOpt = ratingRepository.findById(id);
        if (existingOpt.isEmpty()) return Optional.empty();
        RatingEntity existing = existingOpt.get();
        if (!existing.getUserId().equals(userId)) {
            throw new SecurityException("Only creator can update this rating");
        }
        boolean commentChanged = (dto.getComment() != null && !dto.getComment().equals(existing.getComment()))
                || (dto.getComment() == null && existing.getComment() != null);
        existing.setScore(dto.getScore());
        existing.setComment(dto.getComment());
        if (commentChanged) {
            existing.setConfirmed(false);
        }
        existing.setTimestamp(System.currentTimeMillis());
        RatingEntity updated = ratingRepository.update(existing);
        syncMediaRatings(existing.getMediaId());
        return Optional.ofNullable(updated).map(r -> toDetailDto(r, userId));
    }

    // Delete only by owner
    public boolean delete(int userId, int id) {
        Optional<RatingEntity> existingOpt = ratingRepository.findById(id);
        if (existingOpt.isEmpty()) return false;
        RatingEntity existing = existingOpt.get();
        if (!existing.getUserId().equals(userId)) {
            throw new SecurityException("Only creator can delete this rating");
        }
        boolean ok = ratingRepository.delete(id);
        if (ok) syncMediaRatings(existing.getMediaId());
        return ok;
    }

    public Optional<RatingDetailDto> like(int userId, int id) {
        Optional<RatingEntity> existingOpt = ratingRepository.findById(id);
        if (existingOpt.isEmpty()) return Optional.empty();
        RatingEntity existing = existingOpt.get();
        if (existing.getUserId().equals(userId)) {
            throw new IllegalArgumentException("You cannot like your own rating");
        }
        existing.getLikedByUserIds().add(userId);
        existing.setTimestamp(System.currentTimeMillis());
        ratingRepository.update(existing);
        return Optional.of(toDetailDto(existing, userId));
    }

    public Optional<RatingDetailDto> unlike(int userId, int id) {
        Optional<RatingEntity> existingOpt = ratingRepository.findById(id);
        if (existingOpt.isEmpty()) return Optional.empty();
        RatingEntity existing = existingOpt.get();
        existing.getLikedByUserIds().remove(userId);

        ratingRepository.update(existing);
        return Optional.of(toDetailDto(existing, userId));
    }

    public Optional<RatingDetailDto> confirm(int userId, int id) {
        Optional<RatingEntity> existingOpt = ratingRepository.findById(id);
        if (existingOpt.isEmpty()) return Optional.empty();
        RatingEntity existing = existingOpt.get();
        if (!existing.getUserId().equals(userId)) {
            throw new SecurityException("Only creator can confirm this rating");
        }
        existing.setConfirmed(true);
                // Do not update timestamp when confirming
        ratingRepository.update(existing);
        return Optional.of(toDetailDto(existing, userId));
    }

    private void validateUpsert(RatingUpsertDto dto, boolean requireMediaId) {
        if (dto == null) throw new IllegalArgumentException("Body is required");
        if (requireMediaId && (dto.getMediaId() == null || dto.getMediaId() <= 0)) {
            throw new IllegalArgumentException("mediaId is required");
        }
        int score = dto.getScore();
        if (score < 1 || score > 5) throw new IllegalArgumentException("score must be between 1 and 5");
        if (dto.getComment() != null && dto.getComment().length() > 2000) {
            throw new IllegalArgumentException("comment too long");
        }
    }

    private RatingDetailDto toDetailDto(RatingEntity e, int requesterUserId) {
        RatingDetailDto dto = new RatingDetailDto();
        dto.setId(e.getId());
        dto.setMediaId(e.getMediaId());
        dto.setUserId(e.getUserId());
        dto.setScore(e.getScore());
        // moderation: comment visible only when confirmed or requester is author
        String c = getVisibleComment(e, requesterUserId);
        dto.setComment(c);
        dto.setConfirmed(e.isConfirmed());
        dto.setTimestamp(e.getTimestamp());
        dto.setLikes(e.getLikedByUserIds() == null ? 0 : e.getLikedByUserIds().size());
        dto.setLikedByMe(e.getLikedByUserIds() != null && e.getLikedByUserIds().contains(requesterUserId));
        return dto;
    }

    private String getVisibleComment(RatingEntity e, int requesterUserId) {
        if (e.isConfirmed()) {
            return e.getComment();
        }
        if (e.getUserId() != null && e.getUserId().equals(requesterUserId)) {
            return e.getComment();
        }
        return null;
    }

    private void syncMediaRatings(int mediaId) {
        mediaRepository.findById(mediaId).ifPresent(media -> {
            List<RatingEntity> ratings = ratingRepository.findByMediaId(mediaId);
            // keep only userId + score in media to compute average
            List<RatingEntity> compact = new ArrayList<>();
            for (RatingEntity r : ratings) {
                RatingEntity x = new RatingEntity(r.getUserId(), r.getScore());
                compact.add(x);
            }
            media.setRatings(compact);
            mediaRepository.update(media);
        });
    }
}
