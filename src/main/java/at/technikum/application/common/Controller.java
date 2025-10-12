package at.technikum.application.common;

import at.technikum.server.http.ContentType;
import at.technikum.server.http.Request;
import at.technikum.server.http.Response;
import at.technikum.server.http.Status;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.DeserializationFeature;

public abstract class Controller {

    protected final ObjectMapper objectMapper = new ObjectMapper();

    // Configure object mapper to be lenient with unknown fields to prevent 500 on extra JSON fields
    protected Controller() {
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public abstract Response handle(Request request);

    protected Response okJson(Object body) {
        Response response = new Response();
        response.setStatus(Status.OK);
        response.setContentType(ContentType.APPLICATION_JSON);
        try {
            response.setBody(objectMapper.writeValueAsString(body));
        } catch (Exception e) {
            return errorJson(Status.INTERNAL_SERVER_ERROR, "Failed to render JSON");
        }
        return response;
    }

    protected Response errorJson(Status status, String message) {
        Response response = new Response();
        response.setStatus(status);
        response.setContentType(ContentType.APPLICATION_JSON);
        try {
            java.util.Map<String, String> errorMap = java.util.Map.of("error", message);
            response.setBody(objectMapper.writeValueAsString(errorMap));
        } catch (Exception e) {
            response.setBody("{\"error\":\"Internal server error\"}");
        }
        return response;
    }
}
