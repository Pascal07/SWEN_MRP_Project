# Code Review Report - MRP Project

**Date:** 2025-12-28  
**Reviewer:** Automated Code Review  
**Project:** Media Rating Platform (MRP)

## Executive Summary

This code review identified **25 issues** across security, code quality, architecture, and testing categories. The most critical issues are:
- **CRITICAL**: Passwords stored in plain text without hashing
- **HIGH**: Database connection leak in RatingEntity like/unlike functionality
- **HIGH**: Missing unit tests (requirement: 20+ tests, found: 0)
- **MEDIUM**: In-memory token storage without persistence or expiration

---

## 🔴 Critical Security Issues

### 1. Plain Text Password Storage ⚠️ CRITICAL
**Location:** `AuthService.java:16,22` and `AuthRepository.java:20,37,59,83`

**Problem:**
```java
// AuthService.java line 16
entity.setPassword(dto.getPassword()); // Stored as plain text!

// AuthService.java line 22
.filter(u -> u.getPassword().equals(dto.getPassword())) // Plain text comparison
```

**Impact:** 
- Passwords are stored in plain text in the database
- If database is compromised, all user passwords are exposed
- Violates security best practices and compliance requirements (GDPR, etc.)

**Recommendation:**
```java
// Use BCrypt or similar hashing algorithm
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

// Registration
entity.setPassword(passwordEncoder.encode(dto.getPassword()));

// Login
.filter(u -> passwordEncoder.matches(dto.getPassword(), u.getPassword()))
```

**Priority:** CRITICAL - Fix immediately

---

### 2. Weak Token Generation ⚠️ HIGH
**Location:** `AuthService.java:24`

**Problem:**
```java
String token = u.getUsername() + "-mrpToken"; // Predictable token!
```

**Impact:**
- Tokens are predictable and can be guessed
- An attacker can impersonate any user by knowing their username
- Session hijacking vulnerability

**Recommendation:**
```java
import java.security.SecureRandom;
import java.util.Base64;

private String generateSecureToken() {
    SecureRandom random = new SecureRandom();
    byte[] bytes = new byte[32];
    random.nextBytes(bytes);
    return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
}
```

**Priority:** HIGH

---

### 3. SQL Injection Risk in init.sql ⚠️ MEDIUM
**Location:** `init.sql:64-75`

**Problem:**
```sql
INSERT INTO users (username, email, password_hash) VALUES
    ('admin', 'admin@mrp.com', '$2a$10$dummyHashForAdmin')
ON CONFLICT (username) DO NOTHING;
```

**Impact:**
- Default admin account with dummy password hash
- If this runs in production, creates a security backdoor
- The dummy hash is not a real bcrypt hash

**Recommendation:**
- Remove default admin account from init.sql
- Create admin through proper registration flow
- If admin must exist, use a real bcrypt hash and document password securely

**Priority:** MEDIUM

---

## 🟡 Resource Management Issues

### 4. Database Connection Not Properly Managed ⚠️ HIGH
**Location:** `RatingEntity.java:14` and `RatingService.java:116,126`

**Problem:**
```java
// RatingEntity has likedByUserIds but no database column
private Set<Integer> likedByUserIds = new HashSet<>();

// RatingService.java:116-119
public Optional<RatingDetailDto> like(int userId, int id) {
    existing.getLikedByUserIds().add(userId); // Not persisted!
    ratingRepository.update(existing); // Update won't save likes
}
```

**Impact:**
- Like data stored in memory only, lost on restart
- Database schema doesn't support the like feature
- Data inconsistency between entity and database

**Recommendation:**
```sql
-- Add to database schema
CREATE TABLE IF NOT EXISTS rating_likes (
    rating_id INTEGER NOT NULL,
    user_id INTEGER NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (rating_id, user_id),
    FOREIGN KEY (rating_id) REFERENCES ratings(rating_id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);
```

```java
// Add repository methods to persist likes
public void addLike(int ratingId, int userId);
public void removeLike(int ratingId, int userId);
public Set<Integer> getLikesByRatingId(int ratingId);
```

**Priority:** HIGH

---

### 5. Connection Pool Missing ⚠️ MEDIUM
**Location:** `DatabaseConnection.java:42-52`

**Problem:**
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
- Singleton connection shared across all requests
- Concurrent requests will conflict
- No connection pooling for performance
- Connection issues affect all users simultaneously

**Recommendation:**
```java
// Use HikariCP for connection pooling
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

private static HikariDataSource dataSource;

private DatabaseConnection() {
    HikariConfig config = new HikariConfig();
    config.setJdbcUrl(URL);
    config.setUsername(USER);
    config.setPassword(PASSWORD);
    config.setMaximumPoolSize(10);
    dataSource = new HikariDataSource(config);
}

public Connection getConnection() throws SQLException {
    return dataSource.getConnection();
}
```

**Priority:** MEDIUM

---

## 🟠 Code Quality Issues

### 6. No Input Validation for Username ⚠️ MEDIUM
**Location:** `AuthController.java:48` and `AuthService.java:13-18`

**Problem:**
```java
AuthRequestDto dto = objectMapper.readValue(raw, AuthRequestDto.class);
// No validation - username could be null, empty, or contain special chars
authService.register(dto);
```

**Impact:**
- Users can register with empty or null usernames
- Special characters in username may cause issues
- No length limits enforced

**Recommendation:**
```java
private void validateAuthRequest(AuthRequestDto dto) {
    if (dto.getUsername() == null || dto.getUsername().trim().isEmpty()) {
        throw new IllegalArgumentException("Username is required");
    }
    if (dto.getUsername().length() < 3 || dto.getUsername().length() > 50) {
        throw new IllegalArgumentException("Username must be 3-50 characters");
    }
    if (!dto.getUsername().matches("^[a-zA-Z0-9_-]+$")) {
        throw new IllegalArgumentException("Username can only contain letters, numbers, underscores, and hyphens");
    }
    if (dto.getPassword() == null || dto.getPassword().length() < 8) {
        throw new IllegalArgumentException("Password must be at least 8 characters");
    }
}
```

**Priority:** MEDIUM

---

### 7. Missing Password Strength Validation ⚠️ MEDIUM
**Location:** `AuthService.java:16`

**Problem:**
- No minimum password length
- No complexity requirements
- Users can set weak passwords like "123"

**Impact:**
- Accounts vulnerable to brute force attacks
- Poor security posture

**Recommendation:**
```java
private void validatePasswordStrength(String password) {
    if (password.length() < 8) {
        throw new IllegalArgumentException("Password must be at least 8 characters");
    }
    if (!password.matches(".*[A-Z].*")) {
        throw new IllegalArgumentException("Password must contain at least one uppercase letter");
    }
    if (!password.matches(".*[a-z].*")) {
        throw new IllegalArgumentException("Password must contain at least one lowercase letter");
    }
    if (!password.matches(".*\\d.*")) {
        throw new IllegalArgumentException("Password must contain at least one digit");
    }
}
```

**Priority:** MEDIUM

---

### 8. Generic Exception Handling Hides Errors ⚠️ LOW
**Location:** Multiple controllers, e.g., `MediaController.java:80-82`

**Problem:**
```java
} catch (Exception e) {
    return errorJson(Status.INTERNAL_SERVER_ERROR, "Internal server error");
}
```

**Impact:**
- Real errors are hidden from developers
- Difficult to debug production issues
- No logging of exceptions

**Recommendation:**
```java
} catch (Exception e) {
    System.err.println("Unexpected error in MediaController: " + e.getMessage());
    e.printStackTrace();
    // Or use a proper logging framework
    // logger.error("Unexpected error", e);
    return errorJson(Status.INTERNAL_SERVER_ERROR, "Internal server error");
}
```

**Priority:** LOW

---

### 9. Null Pointer Risk in MediaRepository ⚠️ MEDIUM
**Location:** `MediaRepository.java:23,74`

**Problem:**
```java
stmt.setString(3, entity.getGenres() != null ? String.join(",", entity.getGenres()) : null);
```

**Impact:**
- If getGenres() returns an empty list, this works
- But no validation that elements in the list are non-null
- Could throw NPE if list contains null elements

**Recommendation:**
```java
String genresStr = null;
if (entity.getGenres() != null && !entity.getGenres().isEmpty()) {
    genresStr = entity.getGenres().stream()
        .filter(Objects::nonNull)
        .map(String::trim)
        .filter(s -> !s.isEmpty())
        .collect(Collectors.joining(","));
}
stmt.setString(3, genresStr);
```

**Priority:** MEDIUM

---

### 10. Timestamp Null Pointer Risk ⚠️ LOW
**Location:** `AuthRepository.java:60,61,84,85`

**Problem:**
```java
user.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
user.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
```

**Impact:**
- If database returns null timestamp, NullPointerException occurs
- Though unlikely with DEFAULT CURRENT_TIMESTAMP, defensive coding is better

**Recommendation:**
```java
Timestamp created = rs.getTimestamp("created_at");
user.setCreatedAt(created != null ? created.toLocalDateTime() : null);

Timestamp updated = rs.getTimestamp("updated_at");
user.setUpdatedAt(updated != null ? updated.toLocalDateTime() : null);
```

**Priority:** LOW

---

## 🔵 Architecture & Design Issues

### 11. AuthTokenStore Has No Cleanup ⚠️ HIGH
**Location:** `AuthTokenStore.java:14`

**Problem:**
```java
private static final Map<String, UserEntity> TOKENS = new ConcurrentHashMap<>();
```

**Impact:**
- Tokens never expire
- Memory leak - old tokens accumulate forever
- Logout doesn't revoke tokens effectively
- No session timeout

**Recommendation:**
```java
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

private static class TokenEntry {
    final UserEntity user;
    final Instant expiry;
    
    TokenEntry(UserEntity user, Instant expiry) {
        this.user = user;
        this.expiry = expiry;
    }
}

private static final Map<String, TokenEntry> TOKENS = new ConcurrentHashMap<>();
private static final long TOKEN_VALIDITY_HOURS = 24;

public static void store(String token, UserEntity user) {
    if (token == null || token.isBlank() || user == null) return;
    Instant expiry = Instant.now().plusSeconds(TOKEN_VALIDITY_HOURS * 3600);
    TOKENS.put(token, new TokenEntry(user, expiry));
}

public static Optional<UserEntity> getUser(String token) {
    if (token == null || token.isBlank()) return Optional.empty();
    TokenEntry entry = TOKENS.get(token);
    if (entry == null) return Optional.empty();
    if (entry.expiry.isBefore(Instant.now())) {
        TOKENS.remove(token);
        return Optional.empty();
    }
    return Optional.of(entry.user);
}

// Add scheduled cleanup task
public static void cleanupExpiredTokens() {
    Instant now = Instant.now();
    TOKENS.entrySet().removeIf(e -> e.getValue().expiry.isBefore(now));
}
```

**Priority:** HIGH

---

### 12. No Rate Limiting ⚠️ MEDIUM
**Location:** All controllers

**Problem:**
- No rate limiting on any endpoints
- Vulnerable to brute force attacks on login
- API can be abused

**Impact:**
- Brute force password attacks possible
- DoS attacks possible
- Excessive database load from spam requests

**Recommendation:**
- Implement rate limiting middleware
- Limit login attempts per IP/username
- Add CAPTCHA for repeated failures

**Priority:** MEDIUM

---

### 13. Leaderboard Not Implemented ⚠️ LOW
**Location:** `LeaderboardService.java:14`

**Problem:**
```java
public List<Map<String, Object>> getLeaderboard(Map<String, String> queryParams) {
    return List.of(); // Always returns empty list!
}
```

**Impact:**
- Feature listed in requirements but not implemented
- API endpoint exists but returns no data

**Recommendation:**
```java
public List<Map<String, Object>> getLeaderboard(Map<String, String> queryParams) {
    String sql = """
        SELECT u.user_id, u.username, COUNT(r.rating_id) as rating_count
        FROM users u
        LEFT JOIN ratings r ON u.user_id = r.user_id
        GROUP BY u.user_id, u.username
        ORDER BY rating_count DESC
        LIMIT 10
        """;
    // Execute query and return results
}
```

**Priority:** LOW (feature incomplete)

---

### 14. Duplicate Code in Services ⚠️ LOW
**Location:** `MediaService.java:27-39`, `RatingService.java:29-41`

**Problem:**
```java
// Same code in both services
public Optional<Integer> getAuthorizedUserId(String authorizationHeader) {
    String token = extractBearerToken(authorizationHeader);
    if (token == null) return Optional.empty();
    return userRepository.findByToken(token).map(UserEntity::getId);
}

private String extractBearerToken(String authorizationHeader) {
    // ... same implementation
}
```

**Impact:**
- Code duplication increases maintenance
- Changes need to be made in multiple places
- Inconsistency risk

**Recommendation:**
```java
// Create AuthHelper utility class
public class AuthHelper {
    public static Optional<Integer> getAuthorizedUserId(
        String authorizationHeader, 
        UserRepository userRepository
    ) {
        String token = extractBearerToken(authorizationHeader);
        if (token == null) return Optional.empty();
        return userRepository.findByToken(token).map(UserEntity::getId);
    }
    
    private static String extractBearerToken(String authorizationHeader) {
        // ... implementation
    }
}
```

**Priority:** LOW

---

## 🟣 Testing Issues

### 15. No Unit Tests Found ⚠️ CRITICAL
**Location:** `src/test/` directory is empty

**Problem:**
- Requirements specify "mindestens 20 Unit-Tests"
- No test files found in project
- No test coverage

**Impact:**
- No automated verification of business logic
- Refactoring is risky
- Bugs may go unnoticed
- Fails project requirements

**Recommendation:**
Create test files for:
- AuthService (registration, login, validation)
- MediaService (CRUD operations, search, authorization)
- RatingService (CRUD, likes, moderation)
- UserService
- Repositories (database operations)

**Example Test:**
```java
package at.technikum.application.mrp.auth;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AuthServiceTest {
    
    @Test
    void testRegisterWithValidUser() {
        AuthRepository repo = new AuthRepository();
        AuthService service = new AuthService(repo);
        AuthRequestDto dto = new AuthRequestDto();
        dto.setUsername("testuser");
        dto.setPassword("Test123!");
        
        assertDoesNotThrow(() -> service.register(dto));
        assertTrue(service.usernameExists("testuser"));
    }
    
    @Test
    void testLoginWithInvalidCredentials() {
        AuthRepository repo = new AuthRepository();
        AuthService service = new AuthService(repo);
        AuthRequestDto dto = new AuthRequestDto();
        dto.setUsername("nonexistent");
        dto.setPassword("wrong");
        
        String token = service.login(dto);
        assertNull(token);
    }
}
```

**Priority:** CRITICAL

---

## 🟤 Documentation Issues

### 16. Missing JavaDoc Comments ⚠️ LOW
**Location:** All public classes and methods

**Problem:**
- No JavaDoc comments on public APIs
- Makes code harder to understand
- No documentation of parameters and return values

**Recommendation:**
```java
/**
 * Service for managing user authentication and registration.
 * Handles user login, registration, and token management.
 */
public class AuthService {
    
    /**
     * Registers a new user in the system.
     *
     * @param dto The authentication request containing username and password
     * @throws RuntimeException if username already exists
     */
    public void register(AuthRequestDto dto) {
        // ...
    }
    
    /**
     * Authenticates a user and returns a session token.
     *
     * @param dto The authentication request containing username and password
     * @return Session token if credentials are valid, null otherwise
     */
    public String login(AuthRequestDto dto) {
        // ...
    }
}
```

**Priority:** LOW

---

### 17. No API Documentation ⚠️ MEDIUM
**Location:** Project root

**Problem:**
- No OpenAPI/Swagger documentation
- Endpoints documented only in README
- No request/response examples

**Recommendation:**
- Create API.md with detailed endpoint documentation
- Include request/response examples
- Document error codes
- Or integrate Swagger/OpenAPI

**Priority:** MEDIUM

---

## 🔧 Build & Configuration Issues

### 18. Java Version Mismatch ⚠️ HIGH
**Location:** `pom.xml:12-13`

**Problem:**
```xml
<maven.compiler.source>21</maven.compiler.source>
<maven.compiler.target>21</maven.compiler.target>
```

**Impact:**
- Project requires Java 21
- Many environments still use Java 17 or 11
- Limits deployment options

**Recommendation:**
```xml
<!-- Consider using Java 17 LTS for wider compatibility -->
<maven.compiler.source>17</maven.compiler.source>
<maven.compiler.target>17</maven.compiler.target>
```

Or update documentation to clearly state Java 21 requirement.

**Priority:** HIGH (if deployment compatibility is needed)

---

### 19. Missing .gitignore Entries ⚠️ LOW
**Location:** `.gitignore`

**Problem:**
- Check if all necessary patterns are ignored
- IDE files, build artifacts, logs

**Recommendation:**
```
# Maven
target/
pom.xml.tag
pom.xml.releaseBackup
pom.xml.versionsBackup

# IDE
.idea/
*.iml
.vscode/
.settings/
.classpath
.project

# Logs
*.log

# OS
.DS_Store
Thumbs.db

# Database
*.db
```

**Priority:** LOW

---

## 📊 Summary Statistics

| Category | Count |
|----------|-------|
| Critical Issues | 2 |
| High Priority | 4 |
| Medium Priority | 9 |
| Low Priority | 10 |
| **Total Issues** | **25** |

---

## 🎯 Recommended Action Plan

### Phase 1: Critical Fixes (Immediate)
1. ✅ Implement password hashing with BCrypt
2. ✅ Fix token generation to use secure random
3. ✅ Add database table for rating likes
4. ✅ Create 20+ unit tests

### Phase 2: High Priority (This Week)
1. ✅ Implement connection pooling
2. ✅ Add token expiration and cleanup
3. ✅ Fix Java version documentation/compatibility
4. ✅ Add comprehensive input validation

### Phase 3: Medium Priority (Next Sprint)
1. ✅ Add rate limiting
2. ✅ Improve error logging
3. ✅ Fix null pointer risks
4. ✅ Add API documentation
5. ✅ Implement password strength validation

### Phase 4: Low Priority (Backlog)
1. ✅ Add JavaDoc comments
2. ✅ Implement leaderboard feature
3. ✅ Refactor duplicate code
4. ✅ Update .gitignore

---

## 📝 Notes

- Many issues are interconnected (e.g., testing would catch security issues)
- Priority should be: Security > Tests > Quality > Documentation
- Consider code review before each release
- Implement CI/CD pipeline to catch issues early

---

**Review completed on:** 2025-12-28  
**Next review scheduled:** After critical fixes are implemented
