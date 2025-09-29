package at.technikum.application.common;

public class Route {

    private final String path;

    private final Controller controller;

    public Route(String path, Controller controller) {
        this.path = path;
        this.controller = controller;
    }

    public String getPath() {
        return path;
    }

    public Controller getController() {
        return controller;
    }
}
