package at.technikum.application.common;

import at.technikum.server.http.ContentType;
import at.technikum.server.http.Response;
import at.technikum.server.http.Status;

public class ExceptionMapper {

    public static Response toResponse(Exception e) {
        Response r = new Response();
        r.setContentType(ContentType.APPLICATION_JSON);
        // Map some common exceptions to client errors
        if (e instanceof IllegalArgumentException) {
            r.setStatus(Status.BAD_REQUEST);
            r.setBody("{\"error\":\"" + sanitize(e.getMessage()) + "\"}");
        } else if (e instanceof SecurityException) {
            r.setStatus(Status.UNAUTHORIZED);
            r.setBody("{\"error\":\"" + sanitize(e.getMessage()) + "\"}");
        } else {
            r.setStatus(Status.INTERNAL_SERVER_ERROR);
            String msg = e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage();
            r.setBody("{\"error\":\"" + sanitize(msg) + "\"}");
        }
        return r;
    }

    private static String sanitize(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}

