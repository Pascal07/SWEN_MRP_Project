package at.technikum.application.mrp.rating.dto;

import com.fasterxml.jackson.annotation.JsonAlias;

public class RatingUpsertDto {
    private int score; // 1..5
    private String comment; // optional
        private Integer mediaId; // for POST

    public RatingUpsertDto() {}

    public int getScore() { return score; }
    @JsonAlias({"stars"})
    public void setScore(int score) { this.score = score; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public Integer getMediaId() { return mediaId; }
    public void setMediaId(Integer mediaId) { this.mediaId = mediaId; }
}
