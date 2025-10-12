package at.technikum.application.mrp;
import at.technikum.application.common.Application;
import at.technikum.application.common.Router;
import at.technikum.application.mrp.auth.AuthController;
import at.technikum.application.mrp.user.UserController;
import at.technikum.application.ping.PingController;
import at.technikum.server.http.ContentType;
import at.technikum.server.http.Request;
import at.technikum.server.http.Response;
import at.technikum.server.http.Status;

public class MrpApplication implements Application {

    private final Router router;

    public MrpApplication() {
        this.router = new Router();
        this.router.addRoute("/users", new UserController());
        this.router.addRoute("/auth", new AuthController());
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