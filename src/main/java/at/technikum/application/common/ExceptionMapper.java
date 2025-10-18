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
        } else if (e instanceof UnsupportedOperationException) {
            r.setStatus(Status.METHOD_NOT_ALLOWED);
            r.setBody("{\"error\":\"" + sanitize(messageOrDefault(e, "Method not allowed")) + "\"}");
        } else if (e instanceof java.util.NoSuchElementException) {
            r.setStatus(Status.NOT_FOUND);
            r.setBody("{\"error\":\"" + sanitize(messageOrDefault(e, "Not found")) + "\"}");
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

    private static String messageOrDefault(Exception e, String def) {
        String m = e.getMessage();
        return (m == null || m.isBlank()) ? def : m;
    }
}
