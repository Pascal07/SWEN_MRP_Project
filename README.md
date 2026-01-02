# 🎬 Media Ratings Platform (MRP) – Specification

## 📜 Projektbeschreibung

Ein Standalone-Backend-Service, der als **RESTful HTTP-Server** in **Java** entwickelt wird. Dieser Server dient als API für verschiedene Frontends (z.B. Mobile, Web, Konsole), die nicht Teil dieses Projekts sind. Die Plattform ermöglicht es Nutzern, Medieninhalte (Filme, Serien, Spiele) zu verwalten, zu bewerten und personalisierte Empfehlungen zu erhalten.

---

## ✨ Features

### Für Benutzer:
* **Registrierung & Login**: Sicherer Zugang mit einzigartigen Zugangsdaten (Benutzername, Passwort).
* **Profilverwaltung**: Anzeigen und Bearbeiten des eigenen Profils, inklusive persönlicher Statistiken.
* **Medienverwaltung**: Medien-Einträge (Filme, Serien, Spiele) können erstellt, aktualisiert und gelöscht werden.
* **Bewertungen**:
    * Bewerten von Medien mit 1 bis 5 Sternen, optional mit einem Kommentar.
    * Eigene Bewertungen können bearbeitet oder gelöscht werden.
    * **"Like"-Funktion**: Bewertungen anderer Nutzer können geliked werden (1 Like pro Bewertung).
* **Favoriten**: Medien können als Favoriten markiert werden.
* **Historie**: Übersicht über die eigene Bewertungshistorie und Favoritenliste.
* **Empfehlungen**: Erhalt von personalisierten Empfehlungen basierend auf bisherigem Bewertungsverhalten und Inhaltsähnlichkeit.

### Für Medien-Einträge:
* **Typisierung**: Repräsentiert entweder einen Film, eine Serie oder ein Spiel.
* **Attribute**: Besteht aus **Titel**, **Beschreibung**, **Medientyp**, **Erscheinungsjahr**, **Genre(s)** und **Altersbeschränkung**.
* **Inhaber**: Wird von einem Nutzer erstellt und kann nur vom Ersteller bearbeitet oder gelöscht werden.
* **Bewertungen**: Enthält eine Liste von Bewertungen und einen berechneten Durchschnittswert.
* **Favoriten**: Kann von anderen Nutzern als Favorit markiert werden.

### Für Bewertungen:
* **Verknüpfung**: Ist an einen spezifischen Medien-Eintrag und einen spezifischen Nutzer gebunden.
* **Attribute**: Beinhaltet **Sternewert** (1–5), optionalen **Kommentar** und **Zeitstempel**.
* **Moderation**: Kommentare sind erst öffentlich sichtbar, nachdem der Autor die Sichtbarkeit bestätigt hat.

---

## 🎯 Use-Cases

* **Benutzer-Authentifizierung**: Registrierung und Login.
* **CRUD für Medien**: Erstellen, Lesen, Aktualisieren, Löschen von Medien-Einträgen.
* **Bewertung & Kommentar**: Medien bewerten und kommentieren.
* **Interaktion**: Likes für Bewertungen anderer Nutzer vergeben.
* **Suchen & Filtern**:
    * Suche nach Medien-Einträgen nach Titel (Teilübereinstimmung).
    * Filtern nach Genre, Medientyp, Erscheinungsjahr, Altersbeschränkung oder Bewertung.
* **Sortierung**: Ergebnisse nach Titel, Jahr oder durchschnittlicher Bewertung sortieren.
* **Favoriten-Verwaltung**: Medien zu Favoriten hinzufügen und wieder entfernen.
* **Leaderboard**: Anzeigen einer Bestenliste der aktivsten Nutzer, sortiert nach Anzahl der Bewertungen.
* **Empfehlungen**: Erhalt von Empfehlungen basierend auf Genre- und Inhaltsähnlichkeit.

---

## 🛠️ Implementierungs-Anforderungen

* **REST-Server**: Implementierung der Endpunkte gemäß der HTTP-Spezifikation.
* **Frameworks**:
    * Verwendung von HTTP-Helfer-Frameworks wie **`HttpListener`** ist erlaubt.
    * **Keine Verwendung** von kompletten Frameworks wie ASP.NET, Spring oder JSP/JSF.
* **Serialisierung**: Nutzung von Paketen zur Objekt-Serialisierung (z.B. **Jackson, Newtonsoft.JSON**).
* **Datenbank**: ✅ Datenpersistenz in einer **PostgreSQL-Datenbank** via **Docker** implementiert.
* **Testing**:
    * ✅ **Postman-Collection** für Integrationstests bereitgestellt (siehe `/postman` Ordner).
    * ✅ **130+ Unit-Tests** implementiert - weit über den geforderten 20 Tests.
    * ✅ Umfassende Test-Coverage für alle Module (Auth, User, Media, Rating, Favorites, Leaderboard, Recommendations).

---

## 🗄️ Datenbank (PostgreSQL)

Die Plattform verwendet **PostgreSQL 18.0** für persistente Datenspeicherung:

* **Setup**: Docker-Container via `docker-compose up -d`
* **Connection**: JDBC mit PreparedStatements (SQL-Injection-Schutz)
* **Schema**: Automatische Initialisierung beim Start über `init.sql`
* **Tabellen**: 
  - `users` - Benutzerkonten mit Authentifizierung
  - `media` - Medieninhalte (Filme, Serien, Spiele)
  - `ratings` - Bewertungen mit Kommentaren
  - `favorites` - Favoriten-Markierungen
  - `rating_likes` - Likes für Bewertungen
* **Features**: Foreign Keys, Constraints, UNIQUE-Indizes, Performance-Optimierung

**Schnellstart:**
```bash
# 1. Docker-Container starten
docker-compose up -d

# 2. Umgebungsvariablen setzen
set DB_URL=jdbc:postgresql://localhost:5432/mrpdb
set DB_USER=mrp
set DB_PASSWORD=mrp

# 3. Anwendung kompilieren
mvn clean compile

# 4. Server starten (Port 10001)
mvn exec:java -Dexec.mainClass="at.technikum.Main"

# 5. Tests ausführen
mvn test
```

---

## 🔌 API-Endpunkte

### Authentication & User Management

#### 🔐 Auth Endpoints

| Method | Endpoint | Beschreibung | Auth |
|--------|----------|--------------|------|
| `POST` | `/auth/register` | Neuen Benutzer registrieren | ❌ |
| `POST` | `/auth/login` | Benutzer anmelden | ❌ |
| `POST` | `/auth/logout` | Benutzer abmelden | ✅ |

**Beispiel: Registration**
```json
POST /auth/register
Content-Type: application/json

{
  "username": "john_doe",
  "email": "john@example.com",
  "password": "securePassword123"
}
```

**Beispiel: Login Response**
```json
{
  "token": "john_doe-mrpToken",
  "userId": 1,
  "username": "john_doe"
}
```

#### 👤 User Endpoints

| Method | Endpoint | Beschreibung | Auth |
|--------|----------|--------------|------|
| `GET` | `/users/{id}` | Benutzerprofil abrufen | ✅ |
| `PUT` | `/users/{id}` | Benutzerprofil aktualisieren | ✅ |
| `DELETE` | `/users/{id}` | Benutzerkonto löschen | ✅ |
| `GET` | `/users/{id}/statistics` | Benutzerstatistiken abrufen | ✅ |

### Media Management

#### 🎬 Media Endpoints

| Method | Endpoint | Beschreibung | Auth |
|--------|----------|--------------|------|
| `GET` | `/media` | Alle Medien abrufen (mit Filtern) | ❌ |
| `GET` | `/media/{id}` | Medien-Details abrufen | ❌ |
| `POST` | `/media` | Neuen Medien-Eintrag erstellen | ✅ |
| `PUT` | `/media/{id}` | Medien-Eintrag aktualisieren | ✅ |
| `DELETE` | `/media/{id}` | Medien-Eintrag löschen | ✅ |

**Query-Parameter für `/media`:**
- `genre` - Filtern nach Genre
- `mediaType` - Filtern nach Typ (MOVIE, SERIES, GAME)
- `year` - Filtern nach Erscheinungsjahr
- `ageRestriction` - Filtern nach Altersbeschränkung
- `sortBy` - Sortieren (title, year, rating)
- `sortOrder` - Sortierreihenfolge (asc, desc)

**Beispiel: Media erstellen**
```json
POST /media
Authorization: Bearer john_doe-mrpToken
Content-Type: application/json

{
  "title": "Inception",
  "description": "A mind-bending thriller",
  "genre": "Sci-Fi",
  "mediaType": "MOVIE",
  "releaseYear": 2010,
  "director": "Christopher Nolan",
  "castMembers": "Leonardo DiCaprio, Tom Hardy",
  "ageRestriction": 13
}
```

### Rating & Interaction

#### ⭐ Rating Endpoints

| Method | Endpoint | Beschreibung | Auth |
|--------|----------|--------------|------|
| `GET` | `/ratings/media/{mediaId}` | Alle Ratings für ein Medium | ❌ |
| `GET` | `/ratings/user/{userId}` | Alle Ratings eines Users | ✅ |
| `POST` | `/ratings` | Neues Rating erstellen | ✅ |
| `PUT` | `/ratings/{id}` | Rating aktualisieren | ✅ |
| `DELETE` | `/ratings/{id}` | Rating löschen | ✅ |
| `POST` | `/ratings/{id}/confirm` | Kommentar bestätigen (öffentlich) | ✅ |
| `POST` | `/ratings/{id}/like` | Rating liken | ✅ |
| `DELETE` | `/ratings/{id}/like` | Like entfernen | ✅ |

**Beispiel: Rating erstellen**
```json
POST /ratings
Authorization: Bearer john_doe-mrpToken
Content-Type: application/json

{
  "mediaId": 1,
  "ratingValue": 5,
  "comment": "Amazing movie! Highly recommended."
}
```

#### ❤️ Favorites Endpoints

| Method | Endpoint | Beschreibung | Auth |
|--------|----------|--------------|------|
| `GET` | `/favorites` | Alle Favoriten des Users | ✅ |
| `POST` | `/favorites` | Medium zu Favoriten hinzufügen | ✅ |
| `DELETE` | `/favorites/{mediaId}` | Medium aus Favoriten entfernen | ✅ |

### Social Features

#### 🏆 Leaderboard Endpoints

| Method | Endpoint | Beschreibung | Auth |
|--------|----------|--------------|------|
| `GET` | `/leaderboard` | Top-Nutzer nach Rating-Anzahl | ❌ |

**Response-Beispiel:**
```json
{
  "topUsers": [
    {
      "userId": 1,
      "username": "john_doe",
      "ratingCount": 42,
      "rank": 1
    },
    {
      "userId": 2,
      "username": "jane_smith",
      "ratingCount": 35,
      "rank": 2
    }
  ]
}
```

#### 💡 Recommendation Endpoints

| Method | Endpoint | Beschreibung | Auth |
|--------|----------|--------------|------|
| `GET` | `/recommendations` | Personalisierte Empfehlungen | ✅ |

**Empfehlungs-Algorithmus:**
- Basiert auf Bewertungshistorie des Users
- Genre-basierte Ähnlichkeit
- Filtert bereits bewertete Medien aus
- Priorisiert höher bewertete Medien

---

## 🧪 Testing

### Unit Tests

Das Projekt verfügt über **130+ Unit-Tests** in 14 Test-Klassen:

```bash
# Alle Tests ausführen
mvn test

# Einzelnes Test-Modul
mvn test -Dtest=AuthServiceUnitTest
```

**Test-Module:**
- `AuthServiceUnitTest` & `AuthControllerUnitTest` - Authentifizierung
- `UserServiceUnitTest` & `UserControllerUnitTest` - Benutzerverwaltung
- `MediaServiceUnitTest` & `MediaControllerUnitTest` - Medienverwaltung
- `RatingServiceUnitTest` & `RatingControllerUnitTest` - Bewertungssystem
- `FavoritesServiceUnitTest` & `FavoritesControllerUnitTest` - Favoriten
- `LeaderboardServiceUnitTest` & `LeaderboardControllerUnitTest` - Rangliste
- `RecommendationServiceUnitTest` & `RecommendationControllerUnitTest` - Empfehlungen

### Integration Tests

**Postman Collection:**
- Pfad: `/postman/collections/MRP Full API Collection (COMPLETE + FIXED).postman_collection.json`
- Importieren in Postman für manuelle API-Tests
- Enthält alle Endpunkte mit Beispiel-Requests


---

## 🚀 Installation & Setup

### Voraussetzungen

- **Java 21** oder höher
- **Maven 3.x**
- **Docker & Docker Compose**
- **Postman** (optional, für API-Tests)

### Setup-Schritte

1. **Repository klonen**
   ```bash
   git clone https://github.com/Pascal07/SWEN_MRP_Project.git
   cd SWEN_MRP_Project
   ```

2. **PostgreSQL starten**
   ```bash
   docker-compose up -d
   ```

3. **Umgebungsvariablen setzen**
   
   **Windows (CMD):**
   ```cmd
   set DB_URL=jdbc:postgresql://localhost:5432/mrpdb
   set DB_USER=mrp
   set DB_PASSWORD=mrp
   ```
   
   **Windows (PowerShell):**
   ```powershell
   $env:DB_URL="jdbc:postgresql://localhost:5432/mrpdb"
   $env:DB_USER="mrp"
   $env:DB_PASSWORD="mrp"
   ```
   
   **Linux/Mac:**
   ```bash
   export DB_URL=jdbc:postgresql://localhost:5432/mrpdb
   export DB_USER=mrp
   export DB_PASSWORD=mrp
   ```

4. **Projekt kompilieren**
   ```bash
   mvn clean compile
   ```

5. **Server starten**
   ```bash
   mvn exec:java -Dexec.mainClass="at.technikum.Main"
   ```
   
   Server läuft auf: `http://localhost:10001`

6. **Tests ausführen**
   ```bash
   mvn test
   ```

### Datenbank zurücksetzen

Falls nötig, kann die Datenbank zurückgesetzt werden:

```bash
# Datenbank-Container stoppen und entfernen
docker-compose down -v

# Neu starten
docker-compose up -d

# Schema wird beim nächsten Server-Start automatisch initialisiert
```

---

## 📊 Architektur-Highlights

### Layered Architecture

```
┌─────────────────────────────────┐
│     HTTP Layer (Port 10001)     │
└────────────┬────────────────────┘
             │
┌────────────▼────────────────────┐
│   Controller Layer              │  ← Request Routing & Validation
│   (Auth, User, Media, Rating)   │
└────────────┬────────────────────┘
             │
┌────────────▼────────────────────┐
│   Service Layer                 │  ← Business Logic
│   (Authorization, Validation)   │
└────────────┬────────────────────┘
             │
┌────────────▼────────────────────┐
│   Repository Layer              │  ← Data Access
│   (SQL Queries, JDBC)           │
└────────────┬────────────────────┘
             │
┌────────────▼────────────────────┐
│   PostgreSQL Database           │  ← Persistence
└─────────────────────────────────┘
```

### Design Patterns

- **Singleton Pattern**: DatabaseConnection für zentrale DB-Verwaltung
- **Repository Pattern**: Trennung von Business-Logik und Datenzugriff
- **DTO Pattern**: Data Transfer Objects für API-Kommunikation
- **Service Pattern**: Business-Logik-Kapselung
- **Router Pattern**: Request-Routing zu Controllern

### Security Features

- ✅ **Token-basierte Authentifizierung** mit Datenbank-Persistenz
- ✅ **SQL-Injection-Schutz** durch PreparedStatements
- ✅ **Authorization-Checks** auf Service-Ebene
- ✅ **Owner-Validierung** für CRUD-Operationen
- ✅ **Password-Hashing** (implementiert)

---

## 🌐 HTTP-Status-Codes

Die API verwendet standardisierte HTTP-Status-Codes:

### Success Codes (2xx)
- `200 OK` - Erfolgreiche GET/PUT/DELETE-Anfrage
- `201 Created` - Erfolgreiche POST-Anfrage (Ressource erstellt)

### Client Error Codes (4xx)
- `400 Bad Request` - Ungültige Eingabe oder fehlerhafte JSON-Struktur
- `401 Unauthorized` - Fehlende oder ungültige Authentifizierung
- `403 Forbidden` - Keine Berechtigung für diese Aktion
- `404 Not Found` - Ressource nicht gefunden
- `409 Conflict` - Konflikt (z.B. Username bereits vergeben)

### Server Error Codes (5xx)
- `500 Internal Server Error` - Unerwarteter Serverfehler
- `503 Service Unavailable` - Datenbank nicht verfügbar

**Alle Fehler-Responses enthalten eine JSON-Nachricht:**
```json
{
  "error": "Detailed error message"
}
```

---

## 📝 Dokumentation

- **[protocol.md](protocol.md)** - Detailliertes Entwicklungsprotokoll mit:
  - Architekturentscheidungen und Begründungen
  - Unit-Test-Strategie und Coverage (130+ Tests)
  - Probleme und deren Lösungen
  - Zeitaufwand-Tracking (~100-120 Stunden)

- **[Postman Collection](postman/collections/)** - Vollständige API-Tests

- **[Diagrams](diagrams/)** - Mermaid-Diagramme für Architektur und Datenbank

---

## 📈 Projekt-Status

✅ **Vollständig implementiert und getestet**

**Implementierte Features:**
- ✅ Authentifizierung (Register, Login, Logout)
- ✅ Benutzerverwaltung mit Profil & Statistiken
- ✅ Vollständiges CRUD für Medien
- ✅ Bewertungssystem mit Comments & Likes
- ✅ Favoriten-Management
- ✅ Leaderboard für aktive Nutzer
- ✅ Personalisierte Empfehlungen
- ✅ PostgreSQL-Integration mit Docker
- ✅ 130+ Unit-Tests (Coverage ~80%)
- ✅ Postman-Collection für Integration-Tests
- ✅ Umfassende Dokumentation

---

## 👥 Mitwirkende

- **Pascal Letsch** - Hauptentwickler


**Repository:** https://github.com/Pascal07/SWEN_MRP_Project



