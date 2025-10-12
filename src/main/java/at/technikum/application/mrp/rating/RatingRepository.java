package at.technikum.application.mrp.rating;

import at.technikum.application.mrp.rating.entity.RatingEntity;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class RatingRepository {

    private static final Map<Integer, RatingEntity> store = new ConcurrentHashMap<>();
    private static int idCounter = 1;

    public RatingEntity create(RatingEntity e) {
        e.setId(idCounter++);
        store.put(e.getId(), e);
        return e;
    }

    public Optional<RatingEntity> findById(int id) {
        return Optional.ofNullable(store.get(id));
    }

    public RatingEntity update(RatingEntity e) {
        if (e.getId() == null || !store.containsKey(e.getId())) return null;
        store.put(e.getId(), e);
        return e;
    }

    public boolean delete(int id) {
        return store.remove(id) != null;
    }

    public List<RatingEntity> findByMediaId(int mediaId) {
        return store.values().stream()
                .filter(r -> r.getMediaId() != null && r.getMediaId() == mediaId)
                .sorted(Comparator.comparingLong(RatingEntity::getTimestamp).reversed())
                .collect(Collectors.toList());
    }
}
