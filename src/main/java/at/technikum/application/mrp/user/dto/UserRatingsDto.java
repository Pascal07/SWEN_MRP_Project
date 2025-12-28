package at.technikum.application.mrp.user.dto;

import at.technikum.application.mrp.rating.entity.RatingEntity;

import java.util.List;

public class UserRatingsDto {
    private Integer userId;
    private List<RatingEntity> ratings;

    public UserRatingsDto() {}

    public UserRatingsDto(Integer userId, List<RatingEntity> ratings) {
        this.userId = userId;
        this.ratings = ratings;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public List<RatingEntity> getRatings() {
        return ratings;
    }

    public void setRatings(List<RatingEntity> ratings) {
        this.ratings = ratings;
    }
}

