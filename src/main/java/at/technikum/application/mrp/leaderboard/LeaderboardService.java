package at.technikum.application.mrp.leaderboard;

import java.util.List;
import java.util.Map;
import java.util.Collections;

public class LeaderboardService {
    private final LeaderboardRepository repository;

    public LeaderboardService(LeaderboardRepository repository) {
        this.repository = repository;
    }

    /**
     * Get leaderboard with pagination support
     * @param queryParams Query parameters (limit, offset)
     * @return List of user statistics ordered by rating count
     */
    public List<Map<String, Object>> getLeaderboard(Map<String, String> queryParams) {
        if (queryParams == null) {
            queryParams = Collections.emptyMap();
        }

        int limit = parseInt(queryParams.get("limit"), 10);
        int offset = parseInt(queryParams.get("offset"), 0);

        // Validate and constrain parameters
        if (limit < 1 || limit > 100) {
            limit = 10;
        }
        if (offset < 0) {
            offset = 0;
        }

        return repository.getTopUsersByRatings(limit, offset);
    }

    /**
     * Parse integer from string with default value
     * @param value String value to parse
     * @param defaultValue Default value if parsing fails
     * @return Parsed integer or default value
     */
    private int parseInt(String value, int defaultValue) {
        try {
            return value != null ? Integer.parseInt(value) : defaultValue;
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}
