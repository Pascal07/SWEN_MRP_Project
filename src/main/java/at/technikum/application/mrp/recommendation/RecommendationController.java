package at.technikum.application.mrp.recommendation;

import at.technikum.application.common.Controller;
import at.technikum.server.http.Request;
import at.technikum.server.http.Response;
import java.util.Map;
import java.util.NoSuchElementException;

public class RecommendationController extends Controller {

    private final RecommendationService recommendationService;

    public RecommendationController(RecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    @Override
    public Response handle(Request request) {
        final String path = request.getPath();
        final String method = request.getMethod();

        if (!"/rec".equals(path)) {
            throw new NoSuchElementException("Route not found");
        }
        if (!"GET".equals(method)) {
            throw new UnsupportedOperationException("Method not allowed");
        }

        Map<String, String> qp = request.getQueryParams();
        String type = normalize(qp.get("type"));

        if ("genre".equalsIgnoreCase(type)) {
            String genre = normalize(qp.get("genre"));
            return okJson(recommendationService.recommendationsByGenre(request.getAuthorization(), genre));
        }

        if ("movie".equalsIgnoreCase(type) || "series".equalsIgnoreCase(type)) {
            return okJson(recommendationService.recommendationsByMediaType(request.getAuthorization(), type));
        }

        return okJson(recommendationService.recommendationsForUser(request.getAuthorization()));
    }

    private String normalize(String v) {
        if (v == null) return null;
        v = v.trim();
        return v.isEmpty() ? null : v;
    }
}
