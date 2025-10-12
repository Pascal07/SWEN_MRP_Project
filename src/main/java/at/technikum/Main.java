package at.technikum;

import at.technikum.application.echo.EchoApplication;
import at.technikum.server.Server;
import at.technikum.application.mrp.MrpApplication;

public class Main {
    public static void main(String[] args) {

        Server server = new Server(8080, new MrpApplication());
        Server echoServer = new Server(3333, new EchoApplication());
        server.start();
        echoServer.start();
    }
}
