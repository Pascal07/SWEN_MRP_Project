package at.technikum.application.mrp.user.dto;

import at.technikum.application.mrp.favorites.entity.FavoriteEntity;

import java.util.List;

public class UserFavoritesDto {
    private Integer userId;
    private List<FavoriteEntity> favorites;

    public UserFavoritesDto() {}

    public UserFavoritesDto(Integer userId, List<FavoriteEntity> favorites) {
        this.userId = userId;
        this.favorites = favorites;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public List<FavoriteEntity> getFavorites() {
        return favorites;
    }

    public void setFavorites(List<FavoriteEntity> favorites) {
        this.favorites = favorites;
    }
}

