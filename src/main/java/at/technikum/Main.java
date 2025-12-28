package at.technikum;

import at.technikum.application.mrp.database.DatabaseInitializer;
import at.technikum.server.Server;
import at.technikum.application.mrp.MrpApplication;

public class Main {
    public static void main(String[] args) {

        // Initialize database schema
        System.out.println("Initializing database...");
        DatabaseInitializer.initializeDatabase();
        System.out.println("Database initialized successfully!");

        Server server = new Server(8080, new MrpApplication());
        server.start();

    }
}


