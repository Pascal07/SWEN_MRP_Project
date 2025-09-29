package at.technikum.application.common;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Router {

    private List<Route> routes;

    public Router() {
        this.routes = new ArrayList<>();
    }

    public Optional<Controller> findController(String path) {
        for (Route route: this.routes) {
            if (path.startsWith(route.getPath())) {
                return Optional.of(route.getController());
            }
        }
        return Optional.empty();
    }

    public void addRoute(String path, Controller controller) {
        routes.add(
                new Route(path, controller)
        );
    }
}
