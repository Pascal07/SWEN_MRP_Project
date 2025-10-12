package at.technikum.application.mrp.media;

import at.technikum.application.mrp.media.entity.MediaEntryEntity;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class MediaRepository {

    private static final Map<Integer, MediaEntryEntity> store = new ConcurrentHashMap<>();
    private static int idCounter = 1;

    public MediaEntryEntity create(MediaEntryEntity entity) {
        entity.setId(idCounter++);
        store.put(entity.getId(), entity);
        return entity;
    }

    public Optional<MediaEntryEntity> findById(int id) {
        return Optional.ofNullable(store.get(id));
    }

    public MediaEntryEntity update(MediaEntryEntity entity) {
        if (entity.getId() == null || !store.containsKey(entity.getId())) {
            return null;
        }
        store.put(entity.getId(), entity);
        return entity;
    }

    public boolean delete(int id) {
        return store.remove(id) != null;
    }

    public Collection<MediaEntryEntity> findAll() {
        return store.values();
    }

    public List<MediaEntryEntity> search(String title, String genre, String mediaType,
                                         Integer releaseYear, Integer ageRestriction,
                                         Integer minAverageRating, String sortBy) {
        List<MediaEntryEntity> all = new ArrayList<>(store.values());

        return all.stream()
                .filter(e -> title == null || e.getTitle() != null && e.getTitle().toLowerCase().contains(title.toLowerCase()))
                .filter(e -> genre == null || e.getGenres() != null && e.getGenres().stream().anyMatch(g -> g != null && g.equalsIgnoreCase(genre)))
                .filter(e -> mediaType == null || (e.getMediaType() != null && e.getMediaType().equalsIgnoreCase(mediaType)))
                .filter(e -> releaseYear == null || Objects.equals(e.getReleaseYear(), releaseYear))
                .filter(e -> ageRestriction == null || Objects.equals(e.getAgeRestriction(), ageRestriction))
                .filter(e -> {
                    if (minAverageRating == null) return true;
                    double avg = e.getAverageScore();
                    return avg >= minAverageRating;
                })
                .sorted((a, b) -> {
                    if (sortBy == null || sortBy.isBlank()) return Integer.compare(a.getId(), b.getId());
                    switch (sortBy.toLowerCase()) {
                        case "title":
                            String at = a.getTitle() == null ? "" : a.getTitle();
                            String bt = b.getTitle() == null ? "" : b.getTitle();
                            return at.compareToIgnoreCase(bt);
                        case "score":
                            return Double.compare(b.getAverageScore(), a.getAverageScore()); // desc
                        case "year":
                            int ay = a.getReleaseYear() == null ? 0 : a.getReleaseYear();
                            int by = b.getReleaseYear() == null ? 0 : b.getReleaseYear();
                            return Integer.compare(ay, by);
                        default:
                            return Integer.compare(a.getId(), b.getId());
                    }
                })
                .collect(Collectors.toList());
    }
}
