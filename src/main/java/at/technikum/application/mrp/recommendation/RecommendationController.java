package at.technikum.application.mrp.recommendation;

import at.technikum.application.common.Controller;
import at.technikum.server.http.Request;
import at.technikum.server.http.Response;
import at.technikum.server.http.Status;

import java.util.Map;
import java.util.Optional;

public class RecommendationController extends Controller {

    private final RecommendationService recommendationService;

    public RecommendationController(RecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    @Override
    public Response handle(Request request) {
        String path = request.getPath();
        String method = request.getMethod();

        try {
            if ("/rec".equals(path)) {
                if (!"GET".equals(method)) {
                    return errorJson(Status.METHOD_NOT_ALLOWED, "Method not allowed");
                }

                Optional<Integer> userIdOpt = recommendationService.getAuthorizedUserId(request.getAuthorization());
                if (userIdOpt.isEmpty()) {
                    return errorJson(Status.UNAUTHORIZED, "Missing or invalid Authorization header");
                }
                int userId = userIdOpt.get();

                Map<String, String> qp = request.getQueryParams();
                String type = normalize(qp.get("type"));
                if ("genre".equalsIgnoreCase(type)) {
                    String genre = normalize(qp.get("genre"));
                    if (genre == null) {
                        return errorJson(Status.BAD_REQUEST, "genre query parameter is required for type=genre");
                    }
                    return okJson(recommendationService.byGenre(userId, genre));
                }

                // Default: Empfehlungen für Nutzer
                return okJson(recommendationService.forUser(userId));
            }

            return errorJson(Status.NOT_FOUND, "Route not found");
        } catch (IllegalArgumentException iae) {
            return errorJson(Status.BAD_REQUEST, iae.getMessage());
        } catch (Exception e) {
            return errorJson(Status.INTERNAL_SERVER_ERROR, "Internal server error");
        }
    }

    private String normalize(String v) {
        if (v == null) return null;
        v = v.trim();
        return v.isEmpty() ? null : v;
    }
}
