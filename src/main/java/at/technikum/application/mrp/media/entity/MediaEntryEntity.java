package at.technikum.application.mrp.media.entity;

import at.technikum.application.mrp.rating.entity.RatingEntity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MediaEntryEntity {
    private Integer id;
    private Integer creatorUserId;
    private String title;
    private String description;
    private String mediaType; // movie | series | game
    private Integer releaseYear;
    private List<String> genres = new ArrayList<>();
    private Integer ageRestriction;

    // Social fields
    private List<RatingEntity> ratings = new ArrayList<>();
    private Set<Integer> favoriteUserIds = new HashSet<>();

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getCreatorUserId() { return creatorUserId; }
    public void setCreatorUserId(Integer creatorUserId) { this.creatorUserId = creatorUserId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getMediaType() { return mediaType; }
    public void setMediaType(String mediaType) { this.mediaType = mediaType; }

    public Integer getReleaseYear() { return releaseYear; }
    public void setReleaseYear(Integer releaseYear) { this.releaseYear = releaseYear; }

    public List<String> getGenres() { return genres; }
    public void setGenres(List<String> genres) { this.genres = genres != null ? new ArrayList<>(genres) : new ArrayList<>(); }

    public Integer getAgeRestriction() { return ageRestriction; }
    public void setAgeRestriction(Integer ageRestriction) { this.ageRestriction = ageRestriction; }

    public List<RatingEntity> getRatings() { return ratings; }
    public void setRatings(List<RatingEntity> ratings) { this.ratings = ratings != null ? new ArrayList<>(ratings) : new ArrayList<>(); }

    public Set<Integer> getFavoriteUserIds() { return favoriteUserIds; }
    public void setFavoriteUserIds(Set<Integer> favoriteUserIds) { this.favoriteUserIds = favoriteUserIds != null ? new HashSet<>(favoriteUserIds) : new HashSet<>(); }

    public double getAverageScore() {
        if (ratings == null || ratings.isEmpty()) return 0.0;
        int sum = 0;
        for (RatingEntity r : ratings) {
            sum += r.getScore();
        }
        return sum / (double) ratings.size();
    }
}

