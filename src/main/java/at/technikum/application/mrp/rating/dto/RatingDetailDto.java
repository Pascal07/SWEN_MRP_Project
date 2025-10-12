package at.technikum.application.mrp.rating.dto;

public class RatingDetailDto {
    private Integer id;
    private Integer mediaId;
    private Integer userId;
    private int score;
        private String comment; // null if not visible
    private boolean confirmed;
    private long timestamp;
    private int likes;
    private boolean likedByMe;

    public RatingDetailDto() {}

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Integer getMediaId() { return mediaId; }
    public void setMediaId(Integer mediaId) { this.mediaId = mediaId; }
    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }
    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }
    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
    public boolean isConfirmed() { return confirmed; }
    public void setConfirmed(boolean confirmed) { this.confirmed = confirmed; }
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    public int getLikes() { return likes; }
    public void setLikes(int likes) { this.likes = likes; }
    public boolean isLikedByMe() { return likedByMe; }
    public void setLikedByMe(boolean likedByMe) { this.likedByMe = likedByMe; }
}

