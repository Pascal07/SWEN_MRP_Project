package at.technikum.application.mrp.user.dto;

import java.util.List;

public class UserFavoritesDto {
    private Integer userId;
    private List<Object> favorites;

    public UserFavoritesDto() {}

    public UserFavoritesDto(Integer userId, List<Object> favorites) {
        this.userId = userId;
        this.favorites = favorites;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public List<Object> getFavorites() {
        return favorites;
    }

    public void setFavorites(List<Object> favorites) {
        this.favorites = favorites;
    }
}

