package at.technikum.server.http;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Request {

    private String method;

    private String path;

    private String body; // roher Request-Body (z. B. JSON)

    // Neu: Header (normalisiert auf lower-case Keys, erster Wert)
    private Map<String, String> headers = new HashMap<>();

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

    // Header-API
    public void setHeaders(Map<String, String> headers) {
        this.headers.clear();
        if (headers != null) {
            this.headers.putAll(headers);
        }
    }

    public Map<String, String> getHeaders() {
        return Collections.unmodifiableMap(headers);
    }

    public String getHeader(String name) {
        if (name == null) return null;
        return headers.get(name.toLowerCase());
    }

    public String getAuthorization() {
        return getHeader("authorization");
    }

    @Override
    public String toString() {
        return "Request {\n" +
                "  Methode: " + method + "\n" +
                "  Pfad: " + path + "\n" +
                "  Headers: " + headers + "\n" +
                "  Body: " + (body != null ? body : "<leer>") + "\n" +
                "}";
    }
}
