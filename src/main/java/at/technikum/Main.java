package at.technikum;

import at.technikum.application.echo.EchoApplication;
import at.technikum.server.Server;

public class Main {
    public static void main(String[] args) {

        Server server = new Server(
            8080,
            new EchoApplication()
        );
        server.start();
    }
}
