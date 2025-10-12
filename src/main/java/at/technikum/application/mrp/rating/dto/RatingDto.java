package at.technikum.application.mrp.rating.dto;

public class RatingDto {
    private Integer userId;
    private int score;

    public RatingDto() {}

    public RatingDto(Integer userId, int score) {
        this.userId = userId;
        this.score = score;
    }

    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }

    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }
}

