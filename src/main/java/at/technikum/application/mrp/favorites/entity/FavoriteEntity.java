package at.technikum.application.mrp.favorites.entity;

import java.time.LocalDateTime;

public class FavoriteEntity {
    private Integer id;
    private Integer userId;
    private Integer mediaId;
    private LocalDateTime createdAt;

    public FavoriteEntity() {}

    public FavoriteEntity(Integer userId, Integer mediaId) {
        this.userId = userId;
        this.mediaId = mediaId;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Integer getMediaId() {
        return mediaId;
    }

    public void setMediaId(Integer mediaId) {
        this.mediaId = mediaId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}

