package at.technikum.application.mrp;
import at.technikum.application.common.Application;
import at.technikum.application.common.Router;
import at.technikum.application.common.ExceptionMapper;
import at.technikum.application.mrp.auth.AuthController;
import at.technikum.application.mrp.auth.AuthRepository;
import at.technikum.application.mrp.auth.AuthService;
import at.technikum.application.mrp.favorites.FavoritesController;
import at.technikum.application.mrp.favorites.FavoritesRepository;
import at.technikum.application.mrp.favorites.FavoritesService;
import at.technikum.application.mrp.leaderboard.LeaderboardController;
import at.technikum.application.mrp.leaderboard.LeaderboardRepository;
import at.technikum.application.mrp.leaderboard.LeaderboardService;
import at.technikum.application.mrp.media.MediaRepository;
import at.technikum.application.mrp.media.MediaService;
import at.technikum.application.mrp.rating.RatingRepository;
import at.technikum.application.mrp.rating.RatingService;
import at.technikum.application.mrp.recommendation.RecommendationController;
import at.technikum.application.mrp.recommendation.RecommendationRepository;
import at.technikum.application.mrp.recommendation.RecommendationService;
import at.technikum.application.mrp.user.UserController;
import at.technikum.application.mrp.user.UserRepository;
import at.technikum.application.mrp.user.UserService;
import at.technikum.application.ping.PingController;
import at.technikum.application.mrp.media.MediaController;
import at.technikum.application.mrp.rating.RatingController;
import at.technikum.server.http.ContentType;
import at.technikum.server.http.Request;
import at.technikum.server.http.Response;
import at.technikum.server.http.Status;

public class MrpApplication implements Application {

    private final Router router;
    private final UserRepository userRepository = new UserRepository();
    private final MediaRepository mediaRepository = new MediaRepository();
    private final AuthRepository authRepository = new AuthRepository();
    private final RatingRepository ratingRepository = new RatingRepository();
    private final LeaderboardRepository leaderboardRepository = new LeaderboardRepository();
    private final FavoritesRepository favoritesRepository = new FavoritesRepository();
    private final RecommendationRepository recommendationRepository = new RecommendationRepository();

    private final UserService userService = new UserService(userRepository);
    private final AuthService authService = new AuthService(authRepository);
    private final MediaService mediaService = new MediaService(mediaRepository, userRepository);
    private final RatingService ratingService = new RatingService(ratingRepository, mediaRepository, userRepository);
    private final LeaderboardService leaderboardService = new LeaderboardService(leaderboardRepository);
    private final FavoritesService favoritesService = new FavoritesService(favoritesRepository, userRepository, mediaRepository);
    private final RecommendationService recommendationService = new RecommendationService(recommendationRepository, userRepository, mediaRepository, ratingRepository);

    public MrpApplication() {
        this.router = new Router();
        this.router.addRoute("/users", new UserController(userService));
        this.router.addRoute("/auth", new AuthController(authService));
        this.router.addRoute("/media", new MediaController(mediaService));
        this.router.addRoute("/rating", new RatingController(ratingService));
        this.router.addRoute("/favorite", new FavoritesController(favoritesService));
        this.router.addRoute("/leaderboard", new LeaderboardController(leaderboardService));
        this.router.addRoute("/rec", new RecommendationController(recommendationService));
        this.router.addRoute("/ping", new PingController());
    }

    @Override
    public Response handle(Request request) {
        final String path = request.getPath();
        return router.findController(path).map(controller -> {
                    try {
                        return controller.handle(request);
                    } catch (Exception e) {
                        return ExceptionMapper.toResponse(e);
                    }
                })
                .orElseGet(() -> ExceptionMapper.toResponse(new java.util.NoSuchElementException("Route not found")));
    }
}