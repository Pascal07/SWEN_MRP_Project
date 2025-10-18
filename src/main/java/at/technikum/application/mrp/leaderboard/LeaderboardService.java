package at.technikum.application.mrp.leaderboard;

import java.util.List;
import java.util.Map;

public class LeaderboardService {
    private final LeaderboardRepository repository;

    public LeaderboardService(LeaderboardRepository repository) {
        this.repository = repository;
    }

    // Liefert aktuell eine leere Liste; Logik wird später implementiert
    public List<Map<String, Object>> getLeaderboard(Map<String, String> queryParams) {
        return List.of();
    }
}
