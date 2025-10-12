package at.technikum.application.mrp.user.dto;

import java.util.List;

public class UserRatingsDto {
    private Integer userId;
    private List<Object> ratings;

    public UserRatingsDto() {}

    public UserRatingsDto(Integer userId, List<Object> ratings) {
        this.userId = userId;
        this.ratings = ratings;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public List<Object> getRatings() {
        return ratings;
    }

    public void setRatings(List<Object> ratings) {
        this.ratings = ratings;
    }
}

