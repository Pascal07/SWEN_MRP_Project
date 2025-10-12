package at.technikum.application.mrp.user;

import at.technikum.application.mrp.auth.AuthTokenStore;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class UserRepository {

    public Optional<UserEntity> findByToken(String token) {
        return AuthTokenStore.getUser(token);
    }

    public List<Object> findRatingsByUserId(Integer userId) {
        // Platzhalter: hier später DB-Abfrage
        return Collections.emptyList();
    }

    public List<Object> findFavoritesByUserId(Integer userId) {
        // Platzhalter: hier später DB-Abfrage
        return Collections.emptyList();
    }
}
