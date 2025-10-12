package at.technikum.application.mrp.auth;

import at.technikum.application.mrp.user.entity.UserEntity;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Einfacher In-Memory Token Store. Speichert Mapping von Token -> User.
 * Nicht persistent, nur für Demo/Übung.
 */
public final class AuthTokenStore {
    private static final Map<String, UserEntity> TOKENS = new ConcurrentHashMap<>();

    private AuthTokenStore() {}

    public static void store(String token, UserEntity user) {
        if (token == null || token.isBlank() || user == null) return;
        TOKENS.put(token, user);
    }

    public static Optional<UserEntity> getUser(String token) {
        if (token == null || token.isBlank()) return Optional.empty();
        return Optional.ofNullable(TOKENS.get(token));
    }

    public static boolean isValid(String token) {
        return getUser(token).isPresent();
    }

    public static void revoke(String token) {
        if (token == null) return;
        TOKENS.remove(token);
    }
}

