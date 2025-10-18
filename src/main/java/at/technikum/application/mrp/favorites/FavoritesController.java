package at.technikum.application.mrp.favorites;

import at.technikum.application.common.Controller;
import at.technikum.server.http.Request;
import at.technikum.server.http.Response;

import java.util.Map;
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

        if ("/favorite".equals(path)) {
            if ("GET".equals(method)) {
                return okJson(favoritesService.listFavorites(request.getAuthorization()));
            }
            throw new UnsupportedOperationException("Method not allowed");
        }

        Matcher mMedia = MEDIA_PATTERN.matcher(path);
        if (mMedia.matches()) {
            int mediaId = parseIdOrThrow(mMedia);
            switch (method) {
                case "POST":
                    favoritesService.addFavorite(request.getAuthorization(), mediaId);
                    return okJson(Map.of("message", "Favorite added"));
                case "DELETE":
                    favoritesService.removeFavorite(request.getAuthorization(), mediaId);
                    return okJson(Map.of("message", "Favorite removed"));
                default:
                    throw new UnsupportedOperationException("Method not allowed");
            }
        }

        throw new java.util.NoSuchElementException("Route not found");
    }

    private int parseIdOrThrow(Matcher matcher) {
        try {
            return Integer.parseInt(matcher.group(1));
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid media id");
        }
    }
}
