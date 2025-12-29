package at.technikum.application.mrp.user.dto;

public class UserProfileDto {
    private Integer userId;
    private String username;
    private String email;

    public UserProfileDto() {}

    public UserProfileDto(Integer userId, String username, String email) {
        this.userId = userId;
        this.username = username;
        this.email = email;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() { return email;}

    public void setEmail(String email) { this.email = email;}
}

