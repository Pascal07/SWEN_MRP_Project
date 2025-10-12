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
* **Datenbank**: Datenpersistenz in einer **PostgreSQL-Datenbank**, die in **Docker** laufen kann.
* **Testing**:
    * Bereitstellung einer **Postman-Collection** oder eines **cURL-Skripts** für Integrationstests.
    * Erstellung von mindestens **20 Unit-Tests** zur Validierung der Kern-Geschäftslogik.

---

## 🌐 HTTP-Spezifikation

* **2XX**: Erfolg (z.B. erfolgreiche Anfrage, Erstellung).
* **4XX**: Client-seitiger Fehler (z.B. ungültige Eingabe, fehlende Authentifizierung).
* **5XX**: Server-seitiger Fehler (z.B. Datenbank nicht verfügbar).
