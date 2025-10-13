# MRP Project - Projektprotokoll

**Git-Repository:** https://github.com/Pascal07/SWEN_MRP_Project

## Projektübersicht

Das MRP (Media Rating Platform) Projekt ist eine Java-basierte HTTP-Server-Anwendung, die als Plattform für Medien-Bewertungen fungiert. Das System bietet Authentifizierung, Benutzerverwaltung, Medienverwaltung und ein Bewertungssystem.

## Software-Architektur

### Gesamtarchitektur

```mermaid
graph TB
    subgraph "Client Layer"
        C[HTTP Clients]
    end
    
    subgraph "Server Layer"
        S[Java HTTP Server]
        H[Handler]
    end
    
    subgraph "Application Layer"
        MA[MrpApplication]
        EA[EchoApplication]
        R[Router]

        subgraph "Controller Layer"
            AC[AuthController]
            UC[UserController]
            MC[MediaController]
            RC[RatingController]
            PC[PingController]
        end

        subgraph "Service Layer"
            AS[AuthService]
            US[UserService]
            MS[MediaService]
            RS[RatingService]
        end

        subgraph "Repository Layer"
            AR[AuthRepository]
            UR[UserRepository]
            MR[MediaRepository]
            RR[RatingRepository]
        end

        subgraph "Data Layer"
            TS[AuthTokenStore]
            DB[(In-Memory Storage)]
        end
    end
    
    
    
    C --> S
    S --> H
    H --> MA
    H --> EA
    MA --> R
    R --> AC
    R --> UC
    R --> MC
    R --> RC
    R --> PC
    
    AC --> AS
    UC --> US
    MC --> MS
    RC --> RS
    
    AS --> AR
    US --> UR
    MS --> MR
    RS --> RR
    
    AS --> TS
    AR --> DB
    UR --> DB
    MR --> DB
    RR --> DB
```

### Schichtarchitektur

```mermaid
graph TD
    subgraph "Presentation Layer"
        HTTP[HTTP Interface]
        REQ[Request/Response]
    end
    
    subgraph "Application Layer"
        APP[Applications]
        ROUTE[Routing Logic]
    end
    
    subgraph "Business Layer"
        CTRL[Controllers]
        SVC[Services]
        DTO[DTOs]
    end
    
    subgraph "Data Access Layer"
        REPO[Repositories]
        ENT[Entities]
    end
    
    subgraph "Infrastructure Layer"
        STORE[Token Store]
        MEM[In-Memory Storage]
    end
    
    HTTP --> APP
    REQ --> ROUTE
    ROUTE --> CTRL
    CTRL --> SVC
    SVC --> REPO
    REPO --> ENT
    SVC --> STORE
    REPO --> MEM
```

## Klassendiagramm

### Core Framework Classes

```mermaid
classDiagram
    class Application {
        <<interface>>
        +handle(Request) Response
    }
    
    class Server {
        -HttpServer httpServer
        -int port
        -Application application
        +Server(int, Application)
        +start() void
    }
    
    class Handler {
        -Application application
        -RequestMapper requestMapper
        +handle(HttpExchange) void
    }
    
    class Router {
        -List~Route~ routes
        +findController(String) Optional~Controller~
        +addRoute(String, Controller) void
    }
    
    class Route {
        -String path
        -Controller controller
        +getPath() String
        +getController() Controller
    }
    
    class Controller {
        <<abstract>>
        +handle(Request) Response
    }
    
    Server --> Application
    Server --> Handler
    Application --> Router
    Router --> Route
    Route --> Controller
```

### MRP Application Classes

```mermaid
classDiagram
    class MrpApplication {
        -Router router
        +handle(Request) Response
        -notFound() Response
    }
    
    class AuthController {
        -AuthService authService
        -ObjectMapper objectMapper
        +handle(Request) Response
    }
    
    class AuthService {
        -AuthRepository authRepository
        +register(AuthRequestDto) void
        +login(AuthRequestDto) String
        +usernameExists(String) boolean
    }
    
    class AuthTokenStore {
        -Map~String,UserEntity~ TOKENS
        +store(String, UserEntity) void
        +getUser(String) Optional~UserEntity~
        +isValid(String) boolean
        +revoke(String) void
    }
    
    class UserController {
        -UserService userService
        +handle(Request) Response
    }
    
    class MediaController {
        -MediaService mediaService
        +handle(Request) Response
    }
    
    class RatingController {
        -RatingService ratingService
        +handle(Request) Response
    }
    
    MrpApplication --> AuthController
    MrpApplication --> UserController
    MrpApplication --> MediaController
    MrpApplication --> RatingController
    AuthController --> AuthService
    AuthService --> AuthTokenStore
```

### HTTP Classes

```mermaid
classDiagram
    class Request {
        -String method
        -String path
        -Map~String,String~ headers
        -String body
        +getMethod() String
        +getPath() String
        +getHeaders() Map
        +getBody() String
    }
    
    class Response {
        -Status status
        -ContentType contentType
        -String body
        +setStatus(Status) void
        +setContentType(ContentType) void
        +setBody(String) void
    }
    
    class Status {
        <<enumeration>>
        OK
        BAD_REQUEST
        UNAUTHORIZED
        NOT_FOUND
        CONFLICT
        INTERNAL_SERVER_ERROR
    }
    
    class ContentType {
        <<enumeration>>
        APPLICATION_JSON
        TEXT_PLAIN
        TEXT_HTML
    }
    
    Response --> Status
    Response --> ContentType
```

## Technische Entscheidungen und Architekturbeschreibung

### 1. Architektur: Layered Architecture

**Schichten:**
1. **Presentation Layer:** HTTP Request/Response Handling
2. **Application Layer:** Routing und Application Logic
3. **Business Layer:** Controllers und Services
4. **Data Access Layer:** Repositories und Entities
5. **Infrastructure Layer:** Token Storage und Utilities (In-Memory Storage) Teporarily!

**Vorteile:**
- Klare Trennung der Verantwortlichkeiten
- Testbarkeit durch Schichtentrennung
- Wartbarkeit und Erweiterbarkeit

### 3. Routing-Mechanismus

**Entscheidung:** Custom Router mit Pattern-Matching für URL-Routen.

**Implementation:**
```java
// Router registriert Controller für spezifische Pfade
router.addRoute("/users", new UserController());
router.addRoute("/auth", new AuthController());
router.addRoute("/media", new MediaController());
```

**Vorteile:**
- Einfache und verständliche Routing-Logik
- Flexibilität bei Path-Matching
- Zentrale Verwaltung der Routen

### 4. Authentication & Token-Handling

**Entscheidung:** Token-basierte Authentifizierung mit In-Memory Storage. (Teporarily!)

**Implementation:**
- `AuthTokenStore`: Singleton für Token-Verwaltung
- Einfache String-Token (für Demonstration)
- In-Memory `ConcurrentHashMap` für Thread-Safety

**Sicherheitsüberlegungen:**
```java
// Thread-safe Token Storage
private static final Map<String, UserEntity> TOKENS = new ConcurrentHashMap<>();

// Token-Validierung
public static Optional<UserEntity> getUser(String token) {
    return Optional.ofNullable(TOKENS.get(token));
}
```

**Limitierungen:**
- Tokens sind nicht persistent (verloren bei Server-Restart)
- Keine Token-Expiration implementiert
- Einfache String-Tokens (nicht JWT)

### 5. Data Persistence

**Aktueller Stand:** Die Daten werden derzeit temporär im In-Memory Storage gehalten.

**Zukünftige Lösung:** Später werden alle Daten in einer Datenbank gespeichert.

**Begründung für die aktuelle Lösung:**
\- Schnelle und einfache Implementierung für Prototyping
\- Keine externe Datenbank-Konfiguration notwendig

**Nachteile der aktuellen Lösung:**
\- Daten gehen bei Server-Restart verloren
\- Nicht für den produktiven Einsatz geeignet
### 6. JSON-Handling

**Entscheidung:** Jackson ObjectMapper für JSON-Serialisierung/-Deserialisierung.

**Dependencies:**
```xml
<dependency>
    <groupId>com.fasterxml.jackson.core</groupId>
    <artifactId>jackson-databind</artifactId>
    <version>2.17.1</version>
</dependency>
```

**Vorteile:**
- Weit verbreitete und stabile Library
- Gute Performance
- Einfache Integration

### 7. Error Handling

**Entscheidung:** Manuelle Error-Handling mit HTTP-Status-Codes und JSON-Error-Responses.

**Implementation:**
```java
// Beispiel für Error Response
if (raw == null || raw.isBlank()) {
    response.setStatus(Status.BAD_REQUEST);
    response.setBody("{\"error\":\"Request body is empty\"}");
    return response;
}
```

## Technische Schritte und Implementierungsdetails

### 1. Server-Initialisierung

Der Server wird in der `Main`-Klasse gestartet:
```java
Server server = new Server(8080, new MrpApplication());
Server echoServer = new Server(3333, new EchoApplication());
```

### 2. Request-Processing Flow

1. **HTTP Request** wird vom `HttpServer` empfangen
2. **Handler** konvertiert `HttpExchange` zu `Request`-Objekt
3. **Application** verarbeitet Request über Router
4. **Router** findet entsprechenden Controller
5. **Controller** delegiert an Service-Layer
6. **Service** führt Business-Logic aus und nutzt Repository
7. **Response** wird zurück durch die Schichten gereicht

### 3. Authentication Flow

```mermaid
sequenceDiagram
    participant Client
    participant AuthController
    participant AuthService
    participant AuthTokenStore
    participant AuthRepository
    
    Client->>AuthController: POST /auth/login
    AuthController->>AuthService: login(credentials)
    AuthService->>AuthRepository: validateCredentials()
    AuthRepository-->>AuthService: user valid
    AuthService->>AuthTokenStore: store(token, user)
    AuthService-->>AuthController: return token
    AuthController-->>Client: {"token": "abc123"}
```

### 4. Modularität und Erweiterbarkeit

Das System ist modular aufgebaut:
- **Neue Controller** können einfach hinzugefügt werden
- **Neue Services** folgen dem gleichen Pattern
- **Authentication** ist wiederverwendbar
- **Error-Handling** ist konsistent implementiert

## Fazit

Das MRP-Projekt demonstriert eine solide, schichtweise Architektur mit klaren Verantwortlichkeiten. Die Implementierung zeigt gute Praktiken für:

- **Separation of Concerns**
- **Dependency Management**
- **HTTP-Request-Handling**
- **Authentication**
- **Error-Handling**
- **SOLID-Prinzipien wurden angewendet**


Die gewählte Architektur ermöglicht einfache Erweiterungen und Wartung, während sie gleichzeitig die Grundlagen der Webentwicklung ohne schwere Frameworks vermittelt.
