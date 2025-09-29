package at.technikum.application.mrp.user;

import at.technikum.application.common.Controller;
import at.technikum.server.http.ContentType;
import at.technikum.server.http.Request;
import at.technikum.server.http.Response;
import at.technikum.server.http.Status;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UserController extends Controller {


    private static final Pattern USER_PATH =
            Pattern.compile("^/users/([^/]+)/(profile|ratings|favorites)$");


    public UserController() {

    }

    @Override
    public Response handle(Request request) {
        Response response = new Response();

        String path = request.getPath();
        String method = request.getMethod();

        Matcher matcher = USER_PATH.matcher(path);
        if (!matcher.matches()) {
            response.setStatus(Status.NOT_FOUND);
            response.setContentType(ContentType.TEXT_PLAIN);
            response.setBody("Route not found");
            return response;
        }

        String userId = matcher.group(1);
        String resource = matcher.group(2);

        switch (resource) {
            case "profile" -> {
                if ("GET".equals(method)) {
                    response.setStatus(Status.OK);
                    response.setContentType(ContentType.TEXT_PLAIN);
                    response.setBody("GET-profile");
                } else if ("PUT".equals(method)) {
                    response.setStatus(Status.OK);
                    response.setContentType(ContentType.TEXT_PLAIN);
                    response.setBody("PUT-profile");
                } else {
                    response.setStatus(Status.METHOD_NOT_ALLOWED);
                }
            }
            case "ratings" -> {
                if ("POST".equals(method)) {
                    response.setStatus(Status.METHOD_NOT_ALLOWED);
                } else {
                    response.setStatus(Status.OK);
                    response.setContentType(ContentType.TEXT_PLAIN);
                    response.setBody("GET-ratings");
                }
            }
            case "favorites" -> {
                if ("POST".equals(method)) {
                    response.setStatus(Status.METHOD_NOT_ALLOWED);
                } else {
                    response.setStatus(Status.OK);
                    response.setContentType(ContentType.TEXT_PLAIN);
                    response.setBody("GET-favorites");
                }
            }
            default -> {
                response.setStatus(Status.NOT_FOUND);
                response.setContentType(ContentType.TEXT_PLAIN);
                response.setBody("Route not found");
            }
        }
        return response;
    }
}
