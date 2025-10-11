package at.technikum.server.http;

import com.sun.net.httpserver.HttpExchange;

public class Request {

    private String method;

    private String path;

    private String body; // roher Request-Body (z. B. JSON)

    public String getMethod() {
        return method;
    }
    
    public void setMethod(String method) {
        this.method = method;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    @Override
    public String toString() {
        return "Request {\n" +
                "  Methode: " + method + "\n" +
                "  Pfad: " + path + "\n" +
                "  Body: " + (body != null ? body : "<leer>") + "\n" +
                "}";
    }
}
