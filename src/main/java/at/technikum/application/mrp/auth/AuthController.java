package at.technikum.application.mrp.auth;

import at.technikum.application.common.Controller;
import at.technikum.server.http.ContentType;
import at.technikum.server.http.Request;
import at.technikum.server.http.Response;
import at.technikum.server.http.Status;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.regex.Pattern;

public class AuthController extends Controller {

    private static final Pattern AUTH_PATH =
            Pattern.compile("^/auth/(register|login)$");

    private final AuthService authService = new AuthService();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public AuthController() {
    }

    @Override
    public Response handle(Request request) {
        Response response = new Response();

        String path = request.getPath();
        String method = request.getMethod();

        if (!AUTH_PATH.matcher(path).matches()) {
            response.setStatus(Status.NOT_FOUND);
            response.setContentType(ContentType.APPLICATION_JSON);
            response.setBody("{\"error\":\"Route not found\"}");
            return response;
        }

        if (path.equals("/auth/register") && method.equals("POST")) {
            String raw = request.getBody();
            response.setContentType(ContentType.APPLICATION_JSON);
            if (raw == null || raw.isBlank()) {
                response.setStatus(Status.BAD_REQUEST);
                response.setBody("{\"error\":\"Request body is empty\"}");
                return response;
            }
            try {
                AuthRequestDto dto = objectMapper.readValue(raw, AuthRequestDto.class);
                boolean exists = authService.usernameExists(dto.getUsername());
                if (exists) {
                    response.setStatus(Status.CONFLICT);
                    response.setBody("{\"error\":\"Username already exists\"}");
                } else {
                    authService.register(dto);
                    response.setStatus(Status.OK);
                    response.setBody("{\"message\":\"User registered\"}");
                }
            } catch (IOException e) {
                response.setStatus(Status.BAD_REQUEST);
                response.setBody("{\"error\":\"Invalid JSON\"}");
            }
        }

        if (path.equals("/auth/login") && method.equals("POST")) {
            String raw = request.getBody();
            response.setContentType(ContentType.APPLICATION_JSON);
            if (raw == null || raw.isBlank()) {
                response.setStatus(Status.BAD_REQUEST);
                response.setBody("{\"error\":\"Request body is empty\"}");
                return response;
            }
            try {
                AuthRequestDto dto = objectMapper.readValue(raw, AuthRequestDto.class);
                String token = authService.login(dto);
                if (token != null) {
                    response.setStatus(Status.OK);
                    response.setBody("{\"token\":\"" + token + "\"}");
                } else {
                    response.setStatus(Status.UNAUTHORIZED);
                    response.setBody("{\"error\":\"Invalid credentials\"}");
                }
            } catch (IOException e) {
                response.setStatus(Status.BAD_REQUEST);
                response.setBody("{\"error\":\"Invalid JSON\"}");
            }
        }

        return response;
    }
}
