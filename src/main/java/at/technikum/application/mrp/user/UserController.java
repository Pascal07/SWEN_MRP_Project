package at.technikum.application.mrp.user;

import at.technikum.application.common.Controller;
import at.technikum.application.mrp.user.dto.UpdateProfileDto;
import at.technikum.application.mrp.user.dto.UserFavoritesDto;
import at.technikum.application.mrp.user.dto.UserProfileDto;
import at.technikum.application.mrp.user.dto.UserRatingsDto;
import at.technikum.server.http.ContentType;
import at.technikum.server.http.Request;
import at.technikum.server.http.Response;
import at.technikum.server.http.Status;

import java.util.Optional;
import java.util.regex.Pattern;

public class UserController extends Controller {

    private final UserService userService;
    private static final Pattern USER_PATH =
            Pattern.compile("^/users/(profile|ratings|favorites)$");

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Override
    public Response handle(Request request) {
        String path = request.getPath();
        String method = request.getMethod();

        if (!USER_PATH.matcher(path).matches()) {
            Response response = new Response();
            response.setStatus(Status.NOT_FOUND);
            response.setContentType(ContentType.APPLICATION_JSON);
            response.setBody("{\"error\":\"Route not found\"}");
            return response;
        }

        switch (path) {
            case "/users/profile" -> {
                if ("GET".equals(method)) {
                    Optional<UserProfileDto> profileOpt = userService.getProfile(request.getAuthorization());
                    if (profileOpt.isEmpty()) {
                        return errorJson(Status.UNAUTHORIZED, "Missing or invalid Authorization header");
                    }
                    return okJson(profileOpt.get());
                } else if ("PUT".equals(method)) {
                    try {
                        UpdateProfileDto updateDto = objectMapper.readValue(request.getBody(), UpdateProfileDto.class);
                        Optional<UserProfileDto> updatedProfile = userService.updateProfile(request.getAuthorization(), updateDto);
                        if (updatedProfile.isEmpty()) {
                            return errorJson(Status.BAD_REQUEST, "Failed to update profile or invalid authorization");
                        }
                        return okJson(updatedProfile.get());
                    } catch (Exception e) {
                        return errorJson(Status.BAD_REQUEST, "Invalid JSON format: " + e.getMessage());
                    }
                } else {
                    return errorJson(Status.METHOD_NOT_ALLOWED, "Method not allowed");
                }
            }
            case "/users/ratings" -> {
                if (!"GET".equals(method)) {
                    return errorJson(Status.METHOD_NOT_ALLOWED, "Method not allowed");
                }
                Optional<UserRatingsDto> ratingsOpt = userService.getRatings(request.getAuthorization());
                if (ratingsOpt.isEmpty()) {
                    return errorJson(Status.UNAUTHORIZED, "Missing or invalid Authorization header");
                }
                return okJson(ratingsOpt.get());
            }
            case "/users/favorites" -> {
                if (!"GET".equals(method)) {
                    return errorJson(Status.METHOD_NOT_ALLOWED, "Method not allowed");
                }
                Optional<UserFavoritesDto> favoritesOpt = userService.getFavorites(request.getAuthorization());
                if (favoritesOpt.isEmpty()) {
                    return errorJson(Status.UNAUTHORIZED, "Missing or invalid Authorization header");
                }
                return okJson(favoritesOpt.get());
            }
            default -> {
                // Wird durch das Pattern oben abgefangen, aber zur Sicherheit:
                Response response = new Response();
                response.setStatus(Status.NOT_FOUND);
                response.setContentType(ContentType.APPLICATION_JSON);
                response.setBody("{\"error\":\"Route not found\"}");
                return response;
            }
        }
    }
}
