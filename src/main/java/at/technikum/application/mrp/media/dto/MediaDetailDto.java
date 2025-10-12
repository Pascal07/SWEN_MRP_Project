package at.technikum.application.mrp.media.dto;

import at.technikum.application.mrp.rating.dto.RatingDto;

import java.util.List;

public class MediaDetailDto {
    private Integer id;
    private String title;
    private String description;
    private String mediaType;
    private Integer releaseYear;
    private List<String> genres;
    private Integer ageRestriction;
    private List<RatingDto> ratings;
    private double averageScore;

    public MediaDetailDto() {}

    public MediaDetailDto(Integer id, String title, String description, String mediaType, Integer releaseYear,
                          List<String> genres, Integer ageRestriction, List<RatingDto> ratings, double averageScore) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.mediaType = mediaType;
        this.releaseYear = releaseYear;
        this.genres = genres;
        this.ageRestriction = ageRestriction;
        this.ratings = ratings;
        this.averageScore = averageScore;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getMediaType() { return mediaType; }
    public void setMediaType(String mediaType) { this.mediaType = mediaType; }
    public Integer getReleaseYear() { return releaseYear; }
    public void setReleaseYear(Integer releaseYear) { this.releaseYear = releaseYear; }
    public List<String> getGenres() { return genres; }
    public void setGenres(List<String> genres) { this.genres = genres; }
    public Integer getAgeRestriction() { return ageRestriction; }
    public void setAgeRestriction(Integer ageRestriction) { this.ageRestriction = ageRestriction; }
    public List<RatingDto> getRatings() { return ratings; }
    public void setRatings(List<RatingDto> ratings) { this.ratings = ratings; }
    public double getAverageScore() { return averageScore; }
    public void setAverageScore(double averageScore) { this.averageScore = averageScore; }
}

