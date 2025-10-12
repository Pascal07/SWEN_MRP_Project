package at.technikum.application.mrp.media;

import at.technikum.application.common.Controller;
import at.technikum.server.http.Request;
import at.technikum.server.http.Response;
import at.technikum.server.http.Status;
import at.technikum.application.mrp.media.dto.MediaUpsertDto;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MediaController extends Controller {

    private final MediaService service = new MediaService();

    // Regex Pattern für /media/{id}
    private static final Pattern MEDIA_ID_PATTERN = Pattern.compile("^/media/(\\d+)$");

    public MediaController() {}

    @Override
    public Response handle(Request request){
        String path = request.getPath();
        String method = request.getMethod();

        try {
            // require auth for all media endpoints
            Optional<Integer> userIdOpt = service.getAuthorizedUserId(request.getAuthorization());
            if (userIdOpt.isEmpty()) {
                return errorJson(Status.UNAUTHORIZED, "Missing or invalid Authorization header");
            }
            int userId = userIdOpt.get();

            if ("/media".equals(path)) {
                switch (method) {
                    case "GET":
                        return okJson(service.search(request.getQueryParams()));
                    case "POST":
                        MediaUpsertDto createDto = readBodyAsUpsert(request.getBody());
                        return okJson(service.create(userId, createDto));
                    default:
                        return errorJson(Status.METHOD_NOT_ALLOWED, "Method not allowed");
                }
            }

            Matcher matcher = MEDIA_ID_PATTERN.matcher(path);
            if (matcher.matches()) {
                Integer id = parseIdWithRegex(matcher);
                switch (method) {
                    case "GET":
                        return service.getById(id)
                                .map(this::okJson)
                                .orElseGet(() -> errorJson(Status.NOT_FOUND, "Media not found"));
                    case "PUT":
                        MediaUpsertDto updateDto = readBodyAsUpsert(request.getBody());
                        return service.update(userId, id, updateDto)
                                .map(this::okJson)
                                .orElseGet(() -> errorJson(Status.NOT_FOUND, "Media not found"));
                    case "DELETE":
                        boolean deleted = service.delete(userId, id);
                        if (deleted) {
                            return okJson(java.util.Map.of("message", "Media deleted"));
                        } else {
                            return errorJson(Status.NOT_FOUND, "Media not found");
                        }
                    default:
                        return errorJson(Status.METHOD_NOT_ALLOWED, "Method not allowed");
                }
            }

            return errorJson(Status.NOT_FOUND, "Route not found");
        } catch (SecurityException se) {
            return errorJson(Status.UNAUTHORIZED, se.getMessage());
        } catch (IllegalArgumentException iae) {
            return errorJson(Status.BAD_REQUEST, iae.getMessage());
        } catch (Exception e) {
            return errorJson(Status.INTERNAL_SERVER_ERROR, "Internal server error");
        }
    }

    private MediaUpsertDto readBodyAsUpsert(String body) throws Exception {
        if (body == null || body.isBlank()) {
            throw new IllegalArgumentException("Request body is empty");
        }
        return objectMapper.readValue(body, MediaUpsertDto.class);
    }

    // Neue Methode für Regex-Parsing
    private Integer parseIdWithRegex(Matcher matcher) {
        try {
            return Integer.parseInt(matcher.group(1));
        } catch (Exception e) {
            return null;
        }
    }
}
