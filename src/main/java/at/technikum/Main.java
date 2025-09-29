package at.technikum;

import at.technikum.server.Server;
import at.technikum.application.mrp.MrpApplication;

public class Main {
    public static void main(String[] args) {

        Server server = new Server(8080, new MrpApplication());
        server.start();
    }
}
