package at.technikum.application.mrp.rating;

import at.technikum.application.common.Controller;
import at.technikum.application.mrp.rating.dto.RatingUpsertDto;
import at.technikum.server.http.Request;
import at.technikum.server.http.Response;
import at.technikum.server.http.Status;

import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RatingController extends Controller {

    private final RatingService service = new RatingService();

    private static final Pattern MEDIA_PATTERN = Pattern.compile("^/rating/media/(\\d+)$");
    private static final Pattern ID_PATTERN = Pattern.compile("^/rating/(\\d+)$");
    private static final Pattern LIKE_PATTERN = Pattern.compile("^/rating/(\\d+)/like$");
    private static final Pattern CONFIRM_PATTERN = Pattern.compile("^/rating/(\\d+)/confirm$");

    @Override
    public Response handle(Request request) {
        String path = request.getPath();
        String method = request.getMethod();

        try {
            Optional<Integer> userIdOpt = service.getAuthorizedUserId(request.getAuthorization());
            if (userIdOpt.isEmpty()) {
                return errorJson(Status.UNAUTHORIZED, "Missing or invalid Authorization header");
            }
            int userId = userIdOpt.get();

            // Create via: POST /rating/media/{mediaId}
            Matcher mMedia = MEDIA_PATTERN.matcher(path);
            if (mMedia.matches()) {
                if (!"POST".equals(method)) return errorJson(Status.METHOD_NOT_ALLOWED, "Method not allowed");
                Integer mediaIdFromPath = parseId(mMedia);
                if (mediaIdFromPath == null) return errorJson(Status.BAD_REQUEST, "Invalid media id");
                RatingUpsertDto createDto = readBodyAsUpsert(request.getBody());
                if (createDto.getMediaId() == null) {
                    createDto.setMediaId(mediaIdFromPath);
                }
                return okJson(service.create(userId, createDto));
            }

            // List or create via body on base path "/rating" (optional convenience)
            if ("/rating".equals(path)) {
                switch (method) {
                    case "GET":
                        Map<String, String> qp = request.getQueryParams();
                        Integer mediaId = parseInt(qp.get("mediaId"));
                        if (mediaId == null) {
                            return errorJson(Status.BAD_REQUEST, "mediaId query parameter is required");
                        }
                        return okJson(service.listByMedia(userId, mediaId));
                    case "POST":
                        RatingUpsertDto createDto = readBodyAsUpsert(request.getBody());
                        return okJson(service.create(userId, createDto));
                    default:
                        return errorJson(Status.METHOD_NOT_ALLOWED, "Method not allowed");
                }
            }

            Matcher mLike = LIKE_PATTERN.matcher(path);
            if (mLike.matches()) {
                Integer id = parseId(mLike);
                if (id == null) return errorJson(Status.BAD_REQUEST, "Invalid id");
                switch (method) {
                    case "POST":
                        return service.like(userId, id)
                                .map(this::okJson)
                                .orElseGet(() -> errorJson(Status.NOT_FOUND, "Rating not found"));
                    case "DELETE":
                        return service.unlike(userId, id)
                                .map(this::okJson)
                                .orElseGet(() -> errorJson(Status.NOT_FOUND, "Rating not found"));
                    default:
                        return errorJson(Status.METHOD_NOT_ALLOWED, "Method not allowed");
                }
            }

            Matcher mConfirm = CONFIRM_PATTERN.matcher(path);
            if (mConfirm.matches()) {
                Integer id = parseId(mConfirm);
                if (id == null) return errorJson(Status.BAD_REQUEST, "Invalid id");
                if (!"POST".equals(method)) return errorJson(Status.METHOD_NOT_ALLOWED, "Method not allowed");
                return service.confirm(userId, id)
                        .map(this::okJson)
                        .orElseGet(() -> errorJson(Status.NOT_FOUND, "Rating not found"));
            }

            Matcher mId = ID_PATTERN.matcher(path);
            if (mId.matches()) {
                Integer id = parseId(mId);
                if (id == null) return errorJson(Status.BAD_REQUEST, "Invalid id");
                switch (method) {
                    case "GET":
                        return service.getById(userId, id)
                                .map(this::okJson)
                                .orElseGet(() -> errorJson(Status.NOT_FOUND, "Rating not found"));
                    case "PUT":
                        RatingUpsertDto updateDto = readBodyAsUpsert(request.getBody());
                        return service.update(userId, id, updateDto)
                                .map(this::okJson)
                                .orElseGet(() -> errorJson(Status.NOT_FOUND, "Rating not found"));
                    case "DELETE":
                        boolean deleted = service.delete(userId, id);
                        if (deleted) return okJson(java.util.Map.of("message", "Rating deleted"));
                        return errorJson(Status.NOT_FOUND, "Rating not found");
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

    private RatingUpsertDto readBodyAsUpsert(String body) throws Exception {
        if (body == null || body.isBlank()) {
            throw new IllegalArgumentException("Request body is empty");
        }
        return objectMapper.readValue(body, RatingUpsertDto.class);
    }

    private Integer parseId(Matcher matcher) {
        try {
            return Integer.parseInt(matcher.group(1));
        } catch (Exception e) {
            return null;
        }
    }

    private Integer parseInt(String s) {
        try {
            if (s == null || s.isBlank()) return null;
            return Integer.parseInt(s.trim());
        } catch (Exception e) {
            return null;
        }
    }
}
