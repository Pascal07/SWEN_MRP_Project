package at.technikum.application.mrp.rating.entity;

public class RatingEntity {
    private Integer userId;
    private int score; // e.g. 1..5

    public RatingEntity() {}
    public RatingEntity(Integer userId, int score) {
        this.userId = userId;
        this.score = score;
    }

    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }

    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }
}

