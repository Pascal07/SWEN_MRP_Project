package at.technikum.application.mrp.favorites;

import at.technikum.application.common.Controller;
import at.technikum.server.http.Request;
import at.technikum.server.http.Response;
import at.technikum.server.http.Status;

import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FavoritesController extends Controller {
    private final FavoritesService favoritesService;

    private static final Pattern MEDIA_PATTERN = Pattern.compile("^/favorite/media/(\\d+)$");

    public FavoritesController(FavoritesService favoritesService) {
        this.favoritesService = favoritesService;
    }

    @Override
    public Response handle(Request request) {
        String path = request.getPath();
        String method = request.getMethod();

        try {
            Optional<Integer> userIdOpt = favoritesService.getAuthorizedUserId(request.getAuthorization());
            if (userIdOpt.isEmpty()) {
                return errorJson(Status.UNAUTHORIZED, "Missing or invalid Authorization header");
            }
            int userId = userIdOpt.get();

            if ("/favorite".equals(path)) {
                if ("GET".equals(method)) {
                    return okJson(favoritesService.listFavorites(userId));
                }
                return errorJson(Status.METHOD_NOT_ALLOWED, "Method not allowed");
            }

            Matcher mMedia = MEDIA_PATTERN.matcher(path);
            if (mMedia.matches()) {
                Integer mediaId = parseId(mMedia);
                if (mediaId == null) return errorJson(Status.BAD_REQUEST, "Invalid media id");
                switch (method) {
                    case "POST":
                        boolean added = favoritesService.addFavorite(userId, mediaId);
                        if (added) return okJson(Map.of("message", "Favorite added"));
                        return errorJson(Status.BAD_REQUEST, "Could not add favorite");
                    case "DELETE":
                        boolean removed = favoritesService.removeFavorite(userId, mediaId);
                        if (removed) return okJson(Map.of("message", "Favorite removed"));
                        return errorJson(Status.NOT_FOUND, "Favorite not found");
                    default:
                        return errorJson(Status.METHOD_NOT_ALLOWED, "Method not allowed");
                }
            }

            return errorJson(Status.NOT_FOUND, "Route not found");
        } catch (IllegalArgumentException iae) {
            return errorJson(Status.BAD_REQUEST, iae.getMessage());
        } catch (Exception e) {
            return errorJson(Status.INTERNAL_SERVER_ERROR, "Internal server error");
        }
    }

    private Integer parseId(Matcher matcher) {
        try {
            return Integer.parseInt(matcher.group(1));
        } catch (Exception e) {
            return null;
        }
    }
}
