package at.technikum.application.mrp.leaderboard;

import at.technikum.application.common.Controller;
import at.technikum.server.http.Request;
import at.technikum.server.http.Response;
import at.technikum.server.http.Status;

public class LeaderboardController extends Controller {
    private final LeaderboardService leaderboardService;

    public LeaderboardController(LeaderboardService leaderboardService) {
        this.leaderboardService = leaderboardService;
    }

    @Override
    public Response handle(Request request) {
        String path = request.getPath();
        String method = request.getMethod();

        try {
            if ("/leaderboard".equals(path)) {
                if ("GET".equals(method)) {
                    return okJson(leaderboardService.getLeaderboard(request.getQueryParams()));
                }
                return errorJson(Status.METHOD_NOT_ALLOWED, "Method not allowed");
            }
            return errorJson(Status.NOT_FOUND, "Route not found");
        } catch (IllegalArgumentException iae) {
            return errorJson(Status.BAD_REQUEST, iae.getMessage());
        } catch (Exception e) {
            return errorJson(Status.INTERNAL_SERVER_ERROR, "Internal server error");
        }
    }
}
