package at.technikum.application.mrp.user;

import at.technikum.application.common.Controller;
import at.technikum.server.http.ContentType;
import at.technikum.server.http.Request;
import at.technikum.server.http.Response;
import at.technikum.server.http.Status;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Optional;

public class UserController extends Controller {

    private final UserService userService = new UserService();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public UserController() {}

    @Override
    public Response handle(Request request) {
        String path = request.getPath();
        String method = request.getMethod();

        switch (path) {
            case "/users/profile" -> {
                if (!"GET".equals(method)) {
                    return errorJson(Status.METHOD_NOT_ALLOWED, "Method not allowed");
                }
                Optional<UserProfileDto> profileOpt = userService.getProfile(request.getAuthorization());
                if (profileOpt.isEmpty()) {
                    return errorJson(Status.UNAUTHORIZED, "Missing or invalid Authorization header");
                }
                return okJson(profileOpt.get());
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
                Response response = new Response();
                response.setStatus(Status.NOT_FOUND);
                response.setContentType(ContentType.TEXT_PLAIN);
                response.setBody("Route not found");
                return response;
            }
        }
    }

    private Response okJson(Object body) {
        Response response = new Response();
        response.setStatus(Status.OK);
        response.setContentType(ContentType.APPLICATION_JSON);
        try {
            response.setBody(objectMapper.writeValueAsString(body));
        } catch (Exception e) {
            return errorJson(Status.INTERNAL_SERVER_ERROR, "Failed to render JSON");
        }
        return response;
    }

    private Response errorJson(Status status, String message) {
        Response response = new Response();
        response.setStatus(status);
        response.setContentType(ContentType.APPLICATION_JSON);
                try {
            java.util.Map<String, String> errorMap = java.util.Map.of("error", message);
            response.setBody(objectMapper.writeValueAsString(errorMap));
        } catch (Exception e) {
            response.setBody("{\"error\":\"Internal server error\"}");
        }
        return response;
    }
}
