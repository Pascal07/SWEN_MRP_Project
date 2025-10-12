package at.technikum.application.mrp.auth;

import at.technikum.application.mrp.user.UserEntity;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/*  Später für DB implementierung mit JPA
    public interface AuthRepository extends JpaRepository<UserEntity, Long> {
        Optional<UserEntity> findByUsername(String username);
    }
*/

public class AuthRepository {

    private final Map<String, UserEntity> users = new ConcurrentHashMap<>();
    private int idCounter = 1;

    public void save(UserEntity user) {
        if (user.getId() == null) {
            user.setId(idCounter++);
        }
        users.put(user.getUsername(), user);
    }

    public Optional<UserEntity> findByUsername(String username) {
        return Optional.ofNullable(users.get(username));
    }
}
