package at.technikum.application.mrp;
import at.technikum.application.common.Application;
import at.technikum.application.common.Router;
import at.technikum.application.mrp.auth.AuthController;
import at.technikum.application.mrp.auth.AuthRepository;
import at.technikum.application.mrp.auth.AuthService;
import at.technikum.application.mrp.favorites.FavoritesController;
import at.technikum.application.mrp.leaderboard.LeaderboardController;
import at.technikum.application.mrp.media.MediaRepository;
import at.technikum.application.mrp.media.MediaService;
import at.technikum.application.mrp.rating.RatingRepository;
import at.technikum.application.mrp.rating.RatingService;
import at.technikum.application.mrp.recommendation.RecommendationController;
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
    private final UserService userService = new UserService(userRepository);
    private final AuthService authService = new AuthService(authRepository);
    private final MediaService mediaService = new MediaService(mediaRepository, userRepository);
    private final RatingService ratingService = new RatingService(ratingRepository, mediaRepository, userRepository);

    public MrpApplication() {
        this.router = new Router();
        this.router.addRoute("/users", new UserController(userService));
        this.router.addRoute("/auth", new AuthController(authService));
        this.router.addRoute("/media", new MediaController(mediaService));
        this.router.addRoute("/rating", new RatingController(ratingService));
        this.router.addRoute("/favorite", new FavoritesController());
        this.router.addRoute("/leaderboard", new LeaderboardController());
        this.router.addRoute("rec?type=genre", new RecommendationController());
        this.router.addRoute("/ping", new PingController());
    }

    @Override
    public Response handle(Request request) {
        final String path = request.getPath();
        return router.findController(path)
                .map(controller -> controller.handle(request))
                .orElseGet(this::notFound);
    }

    private Response notFound() {
        Response response = new Response();
        response.setStatus(Status.NOT_FOUND);
        response.setContentType(ContentType.TEXT_PLAIN);
        response.setBody("Route not found");
        return response;
    }
}