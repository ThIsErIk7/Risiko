

## 🧭 **Gesamtprojektplan – „Risiko light“**

---

### **Woche 1 – Planung & Setup**

**Ziel:** Struktur, Organisation, Git-Setup und Projektgrundlage schaffen.

| Tag            | Aufgaben                                                                                                                                                 |
| -------------- | -------------------------------------------------------------------------------------------------------------------------------------------------------- |
| **Montag**     | Projektordner erstellen, Packages anlegen (`model`, `view`, `controller`, `util`), Git-Repo klonen und `.gitignore` hinzufügen.                          |
| **Dienstag**   | Klassendiagramme und erste Architekturentwürfe (MVC). Besprechung: Welche Klassen und Datenmodelle werden benötigt (`Player`, `Territory`, `Game` etc.). |
| **Mittwoch**   | Aufgabenverteilung mit deinem Freund festlegen, gemeinsame Arbeitsweise über Git klären, Zeitplan (diese Tabelle) finalisieren.                          |
| **Donnerstag** | Branch-Struktur anlegen (`main`, `dev`, `feature/*`), Git-Workflow testen (Branch, Commit, Pull Request, Merge).                                         |
| **Freitag**    | README erweitern mit Projektbeschreibung, Zielsetzung, Anforderungen, technische Umsetzung.                                                              |

---

### **Woche 2 – Grundlogik & Datenmodell**

**Ziel:** Fundament der Spielmechanik legen (Klassen, Spielzustand, Beziehungen).

| Tag            | Aufgaben                                                                      |
| -------------- | ----------------------------------------------------------------------------- |
| **Montag**     | Klasse `Player`: Name, Farbe, Armeen, Besitzliste.                            |
| **Dienstag**   | Klasse `Territory`: ID, Name, Nachbarn, Besitzer, Armeen.                     |
| **Mittwoch**   | Klasse `GameState`: enthält Spieler, Gebiete, Phase, Methoden für Spielstart. |
| **Donnerstag** | Klasse `Phase` (Enum) + `Game` (zentrale Spielsteuerung) erstellen.           |
| **Freitag**    | Erste Tests über die Konsole: Spieler, Gebiete, Start-Armeen.                 |

---

### **Woche 3 – Kartenlogik & Initialisierung**

**Ziel:** Spielkarte laden und initialisieren.

| Tag            | Aufgaben                                                                            |
| -------------- | ----------------------------------------------------------------------------------- |
| **Montag**     | `MapLoader` implementieren, JSON-Karten laden und in `Territory`-Objekte umwandeln. |
| **Dienstag**   | Testkarte (`defaultMap.json`) mit 4–6 Gebieten erstellen.                           |
| **Mittwoch**   | Karte in `GameState` einbinden, Spielstart mit 2 Spielern ermöglichen.              |
| **Donnerstag** | Validierung: Nachbarschaften und Gebietsverbindungen prüfen.                        |
| **Freitag**    | Konsolentest: Karte, Spieler und Armeen initialisiert.                              |

---

### **Woche 4 – Verstärkungsphase**

**Ziel:** Armeen erhalten und platzieren können.

| Tag            | Aufgaben                                                          |
| -------------- | ----------------------------------------------------------------- |
| **Montag**     | Verstärkungslogik in `Game` integrieren (Armeen = Gebiete/3).     |
| **Dienstag**   | Methode `addArmies(Territory t, int count)` implementieren.       |
| **Mittwoch**   | Verstärkungsphase über Konsole testbar machen.                    |
| **Donnerstag** | Limitierung der Platzierung prüfen (keine negativen Armeen etc.). |
| **Freitag**    | Code aufräumen, Zwischenstand committen, Konsolentest.            |

---

### **Woche 5 – Angriffsphase & Würfelsystem**

**Ziel:** Kämpfe zwischen Gebieten ermöglichen.

| Tag            | Aufgaben                                                   |
| -------------- | ---------------------------------------------------------- |
| **Montag**     | Klasse `CombatResolver`: Angriff, Würfel, Vergleichslogik. |
| **Dienstag**   | Angriffsmethode in `Game` integrieren.                     |
| **Mittwoch**   | Armeenverlust und Gebietsübernahme implementieren.         |
| **Donnerstag** | Testfälle: 1v1, 3v2, Gleichstand etc.                      |
| **Freitag**    | Konsolen-Testspiele mit Angriffen durchführen.             |

---

### **Woche 6 – Bewegungsphase & Rundenwechsel**

**Ziel:** Spieler können Armeen verschieben, Runden enden korrekt.

| Tag            | Aufgaben                                                  |
| -------------- | --------------------------------------------------------- |
| **Montag**     | Bewegung zwischen zwei eigenen Gebieten implementieren.   |
| **Dienstag**   | Erreichbarkeit prüfen (Pfad über eigene Gebiete).         |
| **Mittwoch**   | Rundenwechsel und Spielerrotation umsetzen.               |
| **Donnerstag** | Phasensteuerung testen: Verstärkung → Angriff → Bewegung. |
| **Freitag**    | Voller Testlauf einer Spielrunde.                         |

---

### **Woche 7 – Siegbedingungen & Spielende**

**Ziel:** Gewinnerkennung und Spielende korrekt umsetzen.

| Tag            | Aufgaben                                         |
| -------------- | ------------------------------------------------ |
| **Montag**     | Siegprüfung: alle Gebiete gehören einem Spieler. |
| **Dienstag**   | Niederlageprüfung: keine Armeen mehr.            |
| **Mittwoch**   | Anzeige im Konsolenmodus, wer gewonnen hat.      |
| **Donnerstag** | Komplettspiel testen (mehrere Runden).           |
| **Freitag**    | Code-Review & Refactoring.                       |

---

### **Woche 8 – Tests & Stabilisierung**

**Ziel:** Alles testen, dokumentieren, vorbereiten für GUI-Integration.

| Tag            | Aufgaben                                                        |
| -------------- | --------------------------------------------------------------- |
| **Montag**     | Unit-Tests für `Game`, `Player`, `Territory`, `CombatResolver`. |
| **Dienstag**   | Fehleranalyse & Bugfixing.                                      |
| **Mittwoch**   | Methoden und Klassen kommentieren (Javadoc).                    |
| **Donnerstag** | GUI-Design planen (Mockups oder Wireframe).                     |
| **Freitag**    | GUI-Struktur vorbereiten (`GameView`, `ControlPanel`).          |

---

### **Woche 9 – GUI mit JavaFX**

**Ziel:** Visuelle Darstellung der Karte und Spielsteuerung.

| Tag            | Aufgaben                                                    |
| -------------- | ----------------------------------------------------------- |
| **Montag**     | JavaFX-Fenster erstellen (`Main.java`, `Stage`, `Scene`).   |
| **Dienstag**   | `MapView` erstellen – Gebiete zeichnen (Kreise/Farben).     |
| **Mittwoch**   | `ControlPanel` mit Buttons: „Angreifen“, „Bewegen“, „Ende“. |
| **Donnerstag** | Klick-Events implementieren, Verbindung zu `Game`.          |
| **Freitag**    | Konsolenlogik über GUI bedienbar machen.                    |

---

### **Woche 10 – Feinschliff, Dokumentation & Präsentation**

**Ziel:** Fertigstellung, Dokumentation und Vorbereitung der Präsentation.

| Tag            | Aufgaben                                           |
| -------------- | -------------------------------------------------- |
| **Montag**     | Speicher- und Ladefunktion (JSON) implementieren.  |
| **Dienstag**   | GUI finalisieren (Farben, Layout, Style.css).      |
| **Mittwoch**   | End-to-End-Test: gesamte Spiellogik über GUI.      |
| **Donnerstag** | Projektdokumentation & README finalisieren.        |
| **Freitag**    | Präsentation vorbereiten, Testlauf, finale Abgabe. |

---

### 🔧 **Optionale Erweiterungen (wenn Zeit bleibt)**

* **KI-Gegner:** automatisierte Verstärkung & Angriffe
* **Online-Spiel:** Verbindung über Sockets
* **Kontinent-Boni:** Gebiete gruppieren und Bonusarmeen vergeben
* **Würfelanimationen:** einfache JavaFX-Animation bei Angriffen
* **Soundeffekte:** Würfeln, Sieg, Verlust etc.


