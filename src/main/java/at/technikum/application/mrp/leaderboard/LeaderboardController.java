package at.technikum.application.mrp.leaderboard;

import at.technikum.application.common.Controller;
import at.technikum.server.http.Request;
import at.technikum.server.http.Response;

public class LeaderboardController extends Controller {
    private final LeaderboardService leaderboardService;

    public LeaderboardController(LeaderboardService leaderboardService) {
        this.leaderboardService = leaderboardService;
    }

    @Override
    public Response handle(Request request) {
        String path = request.getPath();
        String method = request.getMethod();

        if ("/leaderboard".equals(path)) {
            if ("GET".equals(method)) {
                return okJson(leaderboardService.getLeaderboard(request.getQueryParams()));
            }
            throw new UnsupportedOperationException("Method not allowed");
        }
        throw new java.util.NoSuchElementException("Route not found");
    }
}
