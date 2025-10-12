package at.technikum.application.mrp.rating.entity;

import java.util.HashSet;
import java.util.Set;

public class RatingEntity {
    private Integer id;
    private Integer mediaId;
    private Integer userId;
    private int score; // 1..5
    private String comment; // optional
    private long timestamp; // epoch millis (created/last updated)
    private boolean confirmed; // comment visible when true
    private Set<Integer> likedByUserIds = new HashSet<>();

    public RatingEntity() {}

    public RatingEntity(Integer userId, int score) {
        this.userId = userId;
        this.score = score;
    }

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

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public boolean isConfirmed() { return confirmed; }
    public void setConfirmed(boolean confirmed) { this.confirmed = confirmed; }

    public Set<Integer> getLikedByUserIds() { return likedByUserIds; }
    public void setLikedByUserIds(Set<Integer> likedByUserIds) { this.likedByUserIds = likedByUserIds != null ? new HashSet<>(likedByUserIds) : new HashSet<>(); }
}
