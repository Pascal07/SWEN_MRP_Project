# GitHub Issues to Create

This file contains templates for GitHub issues identified in the code review. Each issue should be created manually in GitHub.

---

## Issue #1: [CRITICAL] Implement Password Hashing

**Priority:** Critical  
**Labels:** security, bug, critical

**Description:**

Passwords are currently stored in plain text in the database, which is a critical security vulnerability. If the database is compromised, all user passwords would be exposed.

**Location:**
- `AuthService.java:16,22`
- `AuthRepository.java:20,37,59,83`

**Current Code:**
```java
// AuthService.java line 16
entity.setPassword(dto.getPassword()); // Stored as plain text!

// AuthService.java line 22
.filter(u -> u.getPassword().equals(dto.getPassword())) // Plain text comparison
```

**Required Changes:**
1. Add BCrypt dependency to `pom.xml`
2. Update `AuthService` to hash passwords on registration
3. Update `AuthService` to verify passwords using hash comparison on login
4. Migrate existing user passwords (if any production data exists)

**Suggested Implementation:**
```java
// Add to pom.xml
<dependency>
    <groupId>org.springframework.security</groupId>
    <artifactId>spring-security-crypto</artifactId>
    <version>6.2.0</version>
</dependency>

// In AuthService.java
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

// Registration
entity.setPassword(passwordEncoder.encode(dto.getPassword()));

// Login
.filter(u -> passwordEncoder.matches(dto.getPassword(), u.getPassword()))
```

**Acceptance Criteria:**
- [ ] Passwords are hashed using BCrypt before storage
- [ ] Login verifies passwords using hash comparison
- [ ] Existing passwords are migrated (if applicable)
- [ ] Unit tests verify password hashing works correctly

---

## Issue #2: [CRITICAL] Fix Weak Token Generation

**Priority:** Critical  
**Labels:** security, bug, critical

**Description:**

Authentication tokens are generated using a predictable pattern (`username + "-mrpToken"`), which allows attackers to easily impersonate users by guessing their tokens if they know the username.

**Location:**
- `AuthService.java:24`

**Current Code:**
```java
String token = u.getUsername() + "-mrpToken"; // Predictable token!
```

**Impact:**
- Session hijacking vulnerability
- No randomness in token generation
- Tokens can be guessed by knowing username

**Required Changes:**
1. Use `SecureRandom` to generate cryptographically secure tokens
2. Make tokens long enough (at least 32 bytes)
3. Use URL-safe Base64 encoding

**Suggested Implementation:**
```java
import java.security.SecureRandom;
import java.util.Base64;

private final SecureRandom secureRandom = new SecureRandom();

private String generateSecureToken() {
    byte[] tokenBytes = new byte[32];
    secureRandom.nextBytes(tokenBytes);
    return Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
}

// In login method:
String token = generateSecureToken();
```

**Acceptance Criteria:**
- [ ] Tokens are generated using SecureRandom
- [ ] Tokens are at least 32 bytes (256 bits) of randomness
- [ ] Tokens use URL-safe Base64 encoding
- [ ] Tokens cannot be predicted or guessed
- [ ] Unit tests verify token uniqueness and unpredictability

---

## Issue #3: [HIGH] Fix Database Connection Leak for Rating Likes

**Priority:** High  
**Labels:** bug, database, feature

**Description:**

The rating "like" feature stores data in memory (`RatingEntity.likedByUserIds`) but the database schema doesn't support it. This means:
- Like data is lost on server restart
- Likes are not persisted to the database
- Data inconsistency between memory and database

**Location:**
- `RatingEntity.java:14`
- `RatingService.java:116,126`
- Database schema missing `rating_likes` table

**Current Code:**
```java
// RatingEntity.java
private Set<Integer> likedByUserIds = new HashSet<>(); // Not in DB!

// RatingService.java:116-119
public Optional<RatingDetailDto> like(int userId, int id) {
    existing.getLikedByUserIds().add(userId); // Not persisted!
    ratingRepository.update(existing); // Update won't save likes
}
```

**Required Changes:**
1. Add `rating_likes` table to database schema
2. Create repository methods to persist likes
3. Update `RatingService` to use database for likes
4. Update `RatingRepository` to load likes when fetching ratings

**Suggested Implementation:**

**Database Schema:**
```sql
CREATE TABLE IF NOT EXISTS rating_likes (
    rating_id INTEGER NOT NULL,
    user_id INTEGER NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (rating_id, user_id),
    FOREIGN KEY (rating_id) REFERENCES ratings(rating_id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_rating_likes_rating_id ON rating_likes(rating_id);
```

**Repository Methods:**
```java
// In RatingRepository
public void addLike(int ratingId, int userId) {
    String sql = "INSERT INTO rating_likes (rating_id, user_id) VALUES (?, ?) ON CONFLICT DO NOTHING";
    // ... implementation
}

public void removeLike(int ratingId, int userId) {
    String sql = "DELETE FROM rating_likes WHERE rating_id = ? AND user_id = ?";
    // ... implementation
}

public Set<Integer> getLikesByRatingId(int ratingId) {
    String sql = "SELECT user_id FROM rating_likes WHERE rating_id = ?";
    // ... implementation
}
```

**Acceptance Criteria:**
- [ ] `rating_likes` table created in database
- [ ] Repository methods implemented for like operations
- [ ] `RatingService.like()` persists to database
- [ ] `RatingService.unlike()` removes from database
- [ ] Likes are loaded when fetching ratings
- [ ] Unit tests verify like persistence
- [ ] Integration tests verify data survives restart

---

## Issue #4: [HIGH] Add Token Expiration and Cleanup

**Priority:** High  
**Labels:** security, bug, memory-leak

**Description:**

The `AuthTokenStore` keeps tokens in memory forever with no expiration or cleanup mechanism. This causes:
- Memory leak (tokens accumulate indefinitely)
- Security risk (old tokens never expire)
- No session timeout for users
- Logout doesn't effectively revoke access

**Location:**
- `AuthTokenStore.java:14`

**Current Code:**
```java
private static final Map<String, UserEntity> TOKENS = new ConcurrentHashMap<>();
// No expiration, no cleanup!
```

**Required Changes:**
1. Add expiration timestamp to token storage
2. Check expiration when validating tokens
3. Add scheduled cleanup task to remove expired tokens
4. Set reasonable token validity period (e.g., 24 hours)

**Suggested Implementation:**
```java
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public final class AuthTokenStore {
    
    private static class TokenEntry {
        final UserEntity user;
        final Instant expiry;
        
        TokenEntry(UserEntity user, Instant expiry) {
            this.user = user;
            this.expiry = expiry;
        }
        
        boolean isExpired() {
            return Instant.now().isAfter(expiry);
        }
    }
    
    private static final Map<String, TokenEntry> TOKENS = new ConcurrentHashMap<>();
    private static final long TOKEN_VALIDITY_HOURS = 24;
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    
    static {
        // Clean up expired tokens every hour
        scheduler.scheduleAtFixedRate(
            AuthTokenStore::cleanupExpiredTokens,
            1, 1, TimeUnit.HOURS
        );
    }
    
    public static void store(String token, UserEntity user) {
        if (token == null || token.isBlank() || user == null) return;
        Instant expiry = Instant.now().plusSeconds(TOKEN_VALIDITY_HOURS * 3600);
        TOKENS.put(token, new TokenEntry(user, expiry));
    }
    
    public static Optional<UserEntity> getUser(String token) {
        if (token == null || token.isBlank()) return Optional.empty();
        TokenEntry entry = TOKENS.get(token);
        if (entry == null) return Optional.empty();
        
        if (entry.isExpired()) {
            TOKENS.remove(token);
            return Optional.empty();
        }
        
        return Optional.of(entry.user);
    }
    
    public static void cleanupExpiredTokens() {
        Instant now = Instant.now();
        TOKENS.entrySet().removeIf(e -> e.getValue().expiry.isBefore(now));
    }
}
```

**Acceptance Criteria:**
- [ ] Tokens have expiration timestamp (24 hours)
- [ ] Expired tokens are rejected on validation
- [ ] Cleanup task runs periodically (every hour)
- [ ] Memory usage doesn't grow indefinitely
- [ ] Unit tests verify expiration works
- [ ] Unit tests verify cleanup removes old tokens

---

## Issue #5: [HIGH] Implement Connection Pooling

**Priority:** High  
**Labels:** performance, bug, database

**Description:**

The current database connection management uses a singleton pattern with a single shared connection. This causes:
- Concurrent request conflicts
- Poor performance under load
- No connection pooling
- Single point of failure

**Location:**
- `DatabaseConnection.java:42-52`

**Current Code:**
```java
public Connection getConnection() {
    try {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
        }
    } catch (SQLException e) {
        throw new RuntimeException("Database connection failed", e);
    }
    return connection;
}
```

**Impact:**
- Thread safety issues with concurrent requests
- Poor scalability
- Connection timeout issues

**Required Changes:**
1. Add HikariCP dependency to `pom.xml`
2. Replace singleton connection with connection pool
3. Configure pool size and timeout settings
4. Update all callers to properly close connections

**Suggested Implementation:**

**Add Dependency:**
```xml
<dependency>
    <groupId>com.zaxxer</groupId>
    <artifactId>HikariCP</artifactId>
    <version>5.1.0</version>
</dependency>
```

**Update DatabaseConnection:**
```java
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public class DatabaseConnection {
    private static DatabaseConnection instance;
    private HikariDataSource dataSource;

    private DatabaseConnection() {
        if (URL == null || USER == null || PASSWORD == null) {
            throw new RuntimeException(
                "Database configuration is missing. Please set DB_URL, DB_USER, and DB_PASSWORD environment variables.");
        }
        
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(URL);
        config.setUsername(USER);
        config.setPassword(PASSWORD);
        config.setMaximumPoolSize(10);
        config.setMinimumIdle(2);
        config.setConnectionTimeout(30000);
        config.setIdleTimeout(600000);
        config.setMaxLifetime(1800000);
        
        this.dataSource = new HikariDataSource(config);
        System.out.println("Database connection pool initialized successfully");
    }

    public static synchronized DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }

    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public void closePool() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            System.out.println("Database connection pool closed");
        }
    }
}
```

**Acceptance Criteria:**
- [ ] HikariCP dependency added to pom.xml
- [ ] Connection pool initialized on startup
- [ ] Pool size configured appropriately (10 max, 2 min)
- [ ] All database operations use pooled connections
- [ ] Connections are properly closed after use
- [ ] Performance improves under concurrent load
- [ ] No connection leaks

---

## Issue #6: [CRITICAL] Create Unit Tests (20+ required)

**Priority:** Critical  
**Labels:** testing, requirement

**Description:**

The project specification requires "mindestens 20 Unit-Tests" but currently no test files exist. This is a critical requirement gap.

**Location:**
- `src/test/` directory is empty

**Impact:**
- Project fails stated requirements
- No automated verification of business logic
- High risk of bugs in refactoring
- No regression testing

**Required Tests:**

**AuthService Tests (5 tests):**
1. Test successful user registration
2. Test duplicate username registration fails
3. Test successful login with valid credentials
4. Test login fails with invalid credentials
5. Test token is generated on successful login

**MediaService Tests (5 tests):**
1. Test create media entry
2. Test update media (only by owner)
3. Test delete media (only by owner)
4. Test search media by title
5. Test filter media by genre

**RatingService Tests (5 tests):**
1. Test create rating
2. Test update rating (only by owner)
3. Test delete rating (only by owner)
4. Test like rating (not your own)
5. Test confirm rating (only by owner)

**Repository Tests (5 tests):**
1. Test AuthRepository save and find user
2. Test MediaRepository CRUD operations
3. Test RatingRepository CRUD operations
4. Test FavoritesRepository CRUD operations
5. Test UserRepository find ratings and favorites

**Validation Tests (3+ tests):**
1. Test media validation (title, type required)
2. Test rating validation (score 1-5)
3. Test input sanitization

**Example Test Structure:**
```java
package at.technikum.application.mrp.auth;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthServiceTest {
    
    @Mock
    private AuthRepository mockRepository;
    
    private AuthService authService;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        authService = new AuthService(mockRepository);
    }
    
    @Test
    void testRegisterWithValidUser() {
        AuthRequestDto dto = new AuthRequestDto();
        dto.setUsername("testuser");
        dto.setPassword("Test123!");
        
        when(mockRepository.findByUsername("testuser"))
            .thenReturn(Optional.empty());
        
        assertDoesNotThrow(() -> authService.register(dto));
        verify(mockRepository, times(1)).save(any());
    }
    
    @Test
    void testLoginWithInvalidCredentials() {
        AuthRequestDto dto = new AuthRequestDto();
        dto.setUsername("nonexistent");
        dto.setPassword("wrong");
        
        when(mockRepository.findByUsername("nonexistent"))
            .thenReturn(Optional.empty());
        
        String token = authService.login(dto);
        assertNull(token);
    }
    
    // Add 3+ more tests...
}
```

**Acceptance Criteria:**
- [ ] At least 20 unit tests created
- [ ] All tests pass
- [ ] Tests cover AuthService
- [ ] Tests cover MediaService
- [ ] Tests cover RatingService
- [ ] Tests cover validation logic
- [ ] Tests use mocking where appropriate
- [ ] Test coverage report shows >70% coverage

---

## Issue #7: [MEDIUM] Add Input Validation for Username and Password

**Priority:** Medium  
**Labels:** security, validation, enhancement

**Description:**

Currently, there's no validation for usernames and passwords during registration. This allows:
- Empty or null usernames
- Very short usernames (single character)
- Special characters that may cause issues
- Weak passwords (no minimum length or complexity)

**Location:**
- `AuthController.java:48`
- `AuthService.java:13-18`

**Current Code:**
```java
AuthRequestDto dto = objectMapper.readValue(raw, AuthRequestDto.class);
// No validation - username could be null, empty, or contain special chars
authService.register(dto);
```

**Required Changes:**
1. Add username validation (length, allowed characters)
2. Add password strength validation
3. Return clear error messages for validation failures
4. Add unit tests for validation

**Suggested Implementation:**
```java
public class AuthValidator {
    
    public static void validateRegistration(AuthRequestDto dto) {
        validateUsername(dto.getUsername());
        validatePassword(dto.getPassword());
    }
    
    private static void validateUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username is required");
        }
        
        String trimmed = username.trim();
        if (trimmed.length() < 3) {
            throw new IllegalArgumentException("Username must be at least 3 characters");
        }
        if (trimmed.length() > 50) {
            throw new IllegalArgumentException("Username must not exceed 50 characters");
        }
        
        if (!trimmed.matches("^[a-zA-Z0-9_-]+$")) {
            throw new IllegalArgumentException(
                "Username can only contain letters, numbers, underscores, and hyphens");
        }
    }
    
    private static void validatePassword(String password) {
        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("Password is required");
        }
        
        if (password.length() < 8) {
            throw new IllegalArgumentException("Password must be at least 8 characters");
        }
        
        if (password.length() > 100) {
            throw new IllegalArgumentException("Password must not exceed 100 characters");
        }
        
        if (!password.matches(".*[A-Z].*")) {
            throw new IllegalArgumentException(
                "Password must contain at least one uppercase letter");
        }
        
        if (!password.matches(".*[a-z].*")) {
            throw new IllegalArgumentException(
                "Password must contain at least one lowercase letter");
        }
        
        if (!password.matches(".*\\d.*")) {
            throw new IllegalArgumentException(
                "Password must contain at least one digit");
        }
        
        if (!password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*")) {
            throw new IllegalArgumentException(
                "Password must contain at least one special character");
        }
    }
}

// In AuthService.java
public void register(AuthRequestDto dto) {
    AuthValidator.validateRegistration(dto);
    // ... rest of registration logic
}
```

**Acceptance Criteria:**
- [ ] Username validation enforces 3-50 character length
- [ ] Username only allows alphanumeric, underscore, hyphen
- [ ] Password validation enforces minimum 8 characters
- [ ] Password requires uppercase, lowercase, digit, special char
- [ ] Clear error messages for each validation failure
- [ ] Unit tests verify all validation rules
- [ ] Validation occurs before database operations

---

## Issue #8: [MEDIUM] Add Error Logging to Exception Handlers

**Priority:** Medium  
**Labels:** logging, debugging, enhancement

**Description:**

Generic exception handlers in controllers catch all exceptions but don't log them, making debugging production issues very difficult.

**Location:**
- `MediaController.java:80-82`
- `RatingController.java:126-128`
- Similar patterns in other controllers

**Current Code:**
```java
} catch (Exception e) {
    return errorJson(Status.INTERNAL_SERVER_ERROR, "Internal server error");
    // Error details are lost!
}
```

**Impact:**
- Debugging production issues is difficult
- No visibility into actual errors
- Cannot track error patterns

**Required Changes:**
1. Add logging framework (SLF4J + Logback)
2. Log exceptions with full stack traces
3. Log request context (path, method, user)
4. Consider adding correlation IDs for request tracking

**Suggested Implementation:**

**Add Dependencies:**
```xml
<dependency>
    <groupId>org.slf4j</groupId>
    <artifactId>slf4j-api</artifactId>
    <version>2.0.9</version>
</dependency>
<dependency>
    <groupId>ch.qos.logback</groupId>
    <artifactId>logback-classic</artifactId>
    <version>1.4.11</version>
</dependency>
```

**Update Controllers:**
```java
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MediaController extends Controller {
    private static final Logger logger = LoggerFactory.getLogger(MediaController.class);
    
    @Override
    public Response handle(Request request) {
        String path = request.getPath();
        String method = request.getMethod();
        
        try {
            // ... handling logic
        } catch (SecurityException se) {
            logger.warn("Security exception for {} {}: {}", method, path, se.getMessage());
            return errorJson(Status.UNAUTHORIZED, se.getMessage());
        } catch (IllegalArgumentException iae) {
            logger.warn("Validation error for {} {}: {}", method, path, iae.getMessage());
            return errorJson(Status.BAD_REQUEST, iae.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error handling {} {}", method, path, e);
            return errorJson(Status.INTERNAL_SERVER_ERROR, "Internal server error");
        }
    }
}
```

**Acceptance Criteria:**
- [ ] SLF4J and Logback dependencies added
- [ ] All controllers log exceptions with context
- [ ] Stack traces logged for unexpected errors
- [ ] Log levels used appropriately (ERROR, WARN, INFO)
- [ ] Request context included in logs (method, path)
- [ ] Logs don't include sensitive data (passwords, tokens)

---

## Issue #9: [MEDIUM] Fix Null Pointer Risks in Repository Mapping

**Priority:** Medium  
**Labels:** bug, null-safety, defensive-programming

**Description:**

Several repository methods have potential null pointer exceptions when mapping database results to entities.

**Locations:**
- `MediaRepository.java:23,74` - genres list may contain nulls
- `AuthRepository.java:60,61,84,85` - timestamp may be null

**Current Code:**
```java
// MediaRepository.java
stmt.setString(3, entity.getGenres() != null ? String.join(",", entity.getGenres()) : null);

// AuthRepository.java
user.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
user.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
```

**Impact:**
- Runtime NullPointerException if data is unexpected
- Application crashes instead of graceful error handling

**Required Changes:**
1. Add null checks for timestamps
2. Filter out null/empty genres
3. Add defensive checks throughout mapping code

**Suggested Implementation:**
```java
// MediaRepository.java - safe genre handling
String genresStr = null;
if (entity.getGenres() != null && !entity.getGenres().isEmpty()) {
    genresStr = entity.getGenres().stream()
        .filter(Objects::nonNull)
        .map(String::trim)
        .filter(s -> !s.isEmpty())
        .collect(Collectors.joining(","));
}
stmt.setString(3, genresStr);

// AuthRepository.java - safe timestamp handling
Timestamp createdTimestamp = rs.getTimestamp("created_at");
if (createdTimestamp != null) {
    user.setCreatedAt(createdTimestamp.toLocalDateTime());
}

Timestamp updatedTimestamp = rs.getTimestamp("updated_at");
if (updatedTimestamp != null) {
    user.setUpdatedAt(updatedTimestamp.toLocalDateTime());
}
```

**Acceptance Criteria:**
- [ ] All timestamp mappings check for null
- [ ] Genre list filters null/empty values
- [ ] No NullPointerException in normal operations
- [ ] Unit tests verify null handling
- [ ] Defensive programming applied throughout repositories

---

## Issue #10: [MEDIUM] Implement Leaderboard Feature

**Priority:** Medium  
**Labels:** feature, incomplete

**Description:**

The leaderboard feature is mentioned in requirements but not implemented. The service currently returns an empty list.

**Location:**
- `LeaderboardService.java:14`

**Current Code:**
```java
public List<Map<String, Object>> getLeaderboard(Map<String, String> queryParams) {
    return List.of(); // Always returns empty list!
}
```

**Required Changes:**
1. Implement query to get top users by rating count
2. Add support for query parameters (limit, offset)
3. Return user data with statistics
4. Add unit tests

**Suggested Implementation:**
```java
// In LeaderboardRepository
public List<Map<String, Object>> getTopUsersByRatings(int limit, int offset) {
    String sql = """
        SELECT 
            u.user_id,
            u.username,
            COUNT(r.rating_id) as rating_count,
            COUNT(DISTINCT r.media_id) as media_rated_count,
            AVG(r.rating_value) as avg_rating_given
        FROM users u
        LEFT JOIN ratings r ON u.user_id = r.user_id
        GROUP BY u.user_id, u.username
        ORDER BY rating_count DESC, u.username ASC
        LIMIT ? OFFSET ?
        """;
    
    List<Map<String, Object>> results = new ArrayList<>();
    try (Connection conn = DatabaseConnection.getInstance().getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {
        
        stmt.setInt(1, limit);
        stmt.setInt(2, offset);
        
        ResultSet rs = stmt.executeQuery();
        while (rs.next()) {
            Map<String, Object> entry = new HashMap<>();
            entry.put("userId", rs.getInt("user_id"));
            entry.put("username", rs.getString("username"));
            entry.put("ratingCount", rs.getInt("rating_count"));
            entry.put("mediaRatedCount", rs.getInt("media_rated_count"));
            entry.put("avgRatingGiven", rs.getDouble("avg_rating_given"));
            results.add(entry);
        }
        return results;
    } catch (SQLException e) {
        throw new RuntimeException("Failed to get leaderboard", e);
    }
}

// In LeaderboardService
public List<Map<String, Object>> getLeaderboard(Map<String, String> queryParams) {
    int limit = parseInt(queryParams.get("limit"), 10);
    int offset = parseInt(queryParams.get("offset"), 0);
    
    if (limit < 1 || limit > 100) limit = 10;
    if (offset < 0) offset = 0;
    
    return repository.getTopUsersByRatings(limit, offset);
}

private int parseInt(String value, int defaultValue) {
    try {
        return value != null ? Integer.parseInt(value) : defaultValue;
    } catch (NumberFormatException e) {
        return defaultValue;
    }
}
```

**Acceptance Criteria:**
- [ ] Query returns top users by rating count
- [ ] Supports limit parameter (default 10, max 100)
- [ ] Supports offset parameter for pagination
- [ ] Returns user statistics (rating count, media count, avg rating)
- [ ] Results ordered by rating count descending
- [ ] Unit tests verify leaderboard logic
- [ ] Integration tests verify database query

---

## Notes

These issues represent the most critical and high-priority items from the code review. Lower priority items are documented in CODE_REVIEW.md but don't require immediate GitHub issues.

**Recommended Creation Order:**
1. Create all CRITICAL issues first
2. Create HIGH priority issues
3. Create MEDIUM priority issues as capacity allows

**Labels to use:**
- `critical` - Must fix before production
- `security` - Security vulnerability
- `bug` - Something broken
- `feature` - New functionality
- `enhancement` - Improvement to existing functionality
- `testing` - Related to tests
- `documentation` - Documentation improvements
- `performance` - Performance optimization
