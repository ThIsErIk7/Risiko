# Risiko
a Risiko game but a "Light" version 

---

# 📘 Projektdokumentation

## **„Risiko light – Ein strategisches Brettspiel in Java mit grafischer Benutzeroberfläche“**

---

## 🏁 1. Einleitung

Im Rahmen des ALN-Projekts soll ein anspruchsvolles Spiel in Java entwickelt werden, das mindestens zwei Spieler unterstützt.
Ich habe mich für ein strategisches Brettspiel entschieden, das auf dem bekannten Gesellschaftsspiel *Risiko* basiert.
Das Ziel ist die Entwicklung einer **eigenständigen, reduzierten, aber technisch vollständigen Version** des Spiels mit dem Namen **„Risiko light“**.

Das Projekt bietet eine gute Balance zwischen **logischer Komplexität**, **grafischer Gestaltung** und **spielerischem Anspruch**.
Es ermöglicht, sowohl **objektorientierte Programmierung** als auch **GUI-Entwicklung** und (optional) **Netzwerk- bzw. KI-Programmierung** zu kombinieren.

Das fertige Spiel soll eine **grafische Benutzeroberfläche (JavaFX)** besitzen und vollständig spielbar sein.
Zwei Spieler treten dabei gegeneinander an, platzieren und bewegen Armeen, führen Angriffe durch und versuchen, die Kontrolle über die gesamte Karte zu erlangen.

---

## 🧩 2. Zielsetzung des Projekts

Das Projekt verfolgt mehrere Ziele, die sowohl technischer als auch konzeptioneller Natur sind.

### Hauptziele

1. **Entwicklung eines rundenbasierten Zwei-Spieler-Spiels in Java**
   – inklusive Spielregeln, Siegbedingungen und Spiellogik.
2. **Erstellung einer grafischen Benutzeroberfläche** mit **JavaFX**, die die Karte, Gebiete und Spielaktionen visualisiert.
3. **Saubere Trennung von Spiellogik und Darstellung** (Model-View-Controller-Prinzip).
4. **Benutzerfreundliches, stabiles Spiel** mit verständlicher Steuerung und klarer Rückmeldung.

### Erweiterungsziele (optional)

* Implementierung einer einfachen **KI**, um das Spiel auch allein spielen zu können.
* **Speicher- und Ladefunktion** für Spielstände.
* **Online-Mehrspielermodus** über Sockets.
* **Bonusmechaniken**, z. B. Kontinent-Boni oder Ereigniskarten.

Damit soll das Projekt zeigen, dass ein komplexes Brettspiel mit moderner Architektur, strukturierter Logik und GUI in Java realisiert werden kann.

---

## ⚙️ 3. Spielbeschreibung

### 3.1 Grundprinzip

Das Spiel „Risiko light“ basiert auf einem rundenbasierten System.
Es gibt zwei Spieler, die um die Kontrolle einer Karte mit mehreren Gebieten kämpfen.
Jedes Gebiet kann einem Spieler gehören und enthält eine Anzahl von Armeen.

Ziel des Spiels ist es, alle Gebiete auf der Karte zu erobern oder den Gegner so weit zu schwächen, dass dieser keine Armeen mehr kontrolliert.

Die Spieler wechseln sich rundenweise ab.
Jede Runde besteht aus drei klar definierten Phasen:

1. **Verstärkungsphase (Reinforcement Phase)**

   * Der Spieler erhält neue Armeen, abhängig von der Anzahl seiner kontrollierten Gebiete.
   * Diese Armeen kann er beliebig auf seine eigenen Gebiete verteilen.

2. **Angriffsphase (Attack Phase)**

   * Der Spieler kann von einem seiner Gebiete aus ein benachbartes feindliches Gebiet angreifen.
   * Kämpfe werden über ein Würfelsystem entschieden:

     * Der Angreifer darf bis zu 3 Würfel werfen (abhängig von seiner Armeeanzahl).
     * Der Verteidiger darf bis zu 2 Würfel werfen.
     * Die höchsten Würfelwerte werden paarweise verglichen.
     * Bei Gleichstand gewinnt der Verteidiger.
     * Jede verlorene Runde kostet eine Armee.
   * Wenn der Verteidiger keine Armeen mehr hat, wird das Gebiet übernommen.

3. **Bewegungsphase (Fortify Phase)**

   * Der Spieler darf einmalig in seiner Runde Armeen zwischen zwei eigenen Gebieten verschieben, sofern diese über eine eigene Verbindungskette erreichbar sind.

Die Runde endet, sobald der Spieler seine Aktionen abgeschlossen hat. Danach wechselt der Zug zum zweiten Spieler.

### 3.2 Siegbedingungen

Ein Spieler gewinnt, wenn:

* er alle Gebiete der Karte kontrolliert **oder**
* der Gegner keine Armeen mehr besitzt.

---

## 🧱 4. Technische Umsetzung

### 4.1 Verwendete Technologien

| Komponente             | Beschreibung                   |
| ---------------------- | ------------------------------ |
| **Programmiersprache** | Java 21                        |
| **GUI-Framework**      | JavaFX                         |
| **Build-System**       | Gradle oder Maven              |
| **Datenspeicherung**   | JSON (Karten- und Spielstände) |
| **IDE**                | IntelliJ IDEA oder Eclipse     |

---

### 4.2 Architektur (MVC)

Das Projekt wird nach dem **Model-View-Controller (MVC)**-Prinzip aufgebaut, um eine saubere Trennung zwischen Spiellogik, Darstellung und Steuerung zu gewährleisten.

| Ebene          | Beschreibung                                                                     |
| -------------- | -------------------------------------------------------------------------------- |
| **Model**      | Enthält die Spiellogik, Datenstrukturen (Spieler, Gebiete, Armeen) und Zustände. |
| **View**       | Visualisiert die Karte, Armeen und Benutzerinteraktionen in JavaFX.              |
| **Controller** | Verbindet Model und View, verarbeitet Eingaben und steuert die Spielphasen.      |

---

### 4.3 Datenmodell

**Wichtige Klassen:**

| Klasse                  | Aufgabe                                                               |
| ----------------------- | --------------------------------------------------------------------- |
| `Game`                  | Steuert den gesamten Spielablauf, Phasen und Spielerwechsel.          |
| `GameState`             | Enthält alle aktuellen Spielinformationen (Gebiete, Spieler, Phase).  |
| `Territory`             | Repräsentiert ein einzelnes Gebiet mit Armeen, Besitzer und Nachbarn. |
| `Player`                | Enthält Name, Farbe, Armeen und Besitzübersicht.                      |
| `CombatResolver`        | Führt Kämpfe basierend auf Würfelergebnissen aus.                     |
| `MapLoader`             | Liest Karteninformationen aus JSON-Dateien ein.                       |
| `MapView`               | Zeichnet die Karte grafisch mit JavaFX.                               |
| `ControlPanel`          | Zeigt Buttons, Status und Aktionen in der GUI.                        |
| `AIPlayer` *(optional)* | Führt einfache Angriffe und Verstärkungen selbstständig aus.          |

---

### 4.4 Logik der Kampfberechnung (Beispiel)

Kämpfe zwischen zwei Gebieten werden mit Würfeln simuliert:

1. Angreifer wählt bis zu 3 Würfel (je nach Armeegröße).
2. Verteidiger wählt bis zu 2 Würfel.
3. Beide würfeln, sortieren ihre Ergebnisse.
4. Die höchsten Ergebnisse werden paarweise verglichen.
5. Bei Gleichstand verliert der Angreifer.
6. Jede Niederlage reduziert die Armeeanzahl des betroffenen Gebiets.

Diese Logik ist in der Klasse `CombatResolver` implementiert und kann separat getestet werden (Unit-Tests).

---

### 4.5 Darstellung (GUI)

Die Benutzeroberfläche besteht aus mehreren Bereichen:

1. **Kartenansicht (MapView)**

   * Visualisiert alle Gebiete als farbige Flächen oder Kreise.
   * Gebiete eines Spielers sind farblich markiert.
   * Klicks auf Gebiete ermöglichen Aktionen wie Angreifen oder Verstärken.

2. **Kontrollpanel (ControlPanel)**

   * Anzeige der aktuellen Spielphase und Spielerfarbe.
   * Buttons für „Angreifen“, „Bewegen“, „Runde beenden“.
   * Anzeige der verbleibenden Armeen in der Verstärkungsphase.

3. **Spielprotokoll (LogPanel)**

   * Textfeld, das die letzten Aktionen und Ergebnisse anzeigt (z. B. „Spieler A greift Gebiet 5 an – 3:1 Sieg“).

4. **Würfelfenster (Popup)**

   * Kleine visuelle Darstellung der Würfelergebnisse.

Die gesamte GUI wird mit **JavaFX** umgesetzt und nutzt CSS zur Formatierung.

---

## 🧮 5. Kartenaufbau

Die Spielkarte wird als **Graphenstruktur** modelliert.
Jedes Gebiet ist ein **Knoten**, und Nachbarschaften sind **Kanten**.
Dies ermöglicht eine flexible Darstellung beliebiger Karten.

Ein Beispiel für eine JSON-basierte Kartenbeschreibung:

```json
{
  "territories": [
    {"id": 1, "name": "Nordland", "neighbors": [2,3]},
    {"id": 2, "name": "Ostland", "neighbors": [1,4]},
    {"id": 3, "name": "Südland", "neighbors": [1,4]},
    {"id": 4, "name": "Westland", "neighbors": [2,3]}
  ]
}
```

Dadurch lässt sich das Spiel später leicht erweitern oder mit einem Map-Editor kombinieren.

---

## 🎯 6. Geplanter Funktionsumfang

### Pflichtfunktionen

* Zwei spielbare Spieler (lokal)
* Verstärkungs-, Angriffs- und Bewegungsphase
* Würfelbasierte Kampfmechanik
* Sieg- und Niederlagenbedingung
* Grafische Darstellung der Karte und Armeen

### Erweiterte Funktionen (optional)

* Künstliche Intelligenz (AI-Spieler)
* Online-Multiplayer über Socket-Verbindung
* Speichern und Laden des Spielstands
* Kontinent-Boni
* Animierte Würfelwürfe
* Soundeffekte

---

## 🧭 7. Projektzeitplan (10 Wochen)

| Woche  | Ziel / Arbeitspaket                                                   |
| ------ | --------------------------------------------------------------------- |
| **1**  | Projektplanung, Klassendiagramm, Einrichtung der Entwicklungsumgebung |
| **2**  | Implementierung der Grundklassen (Player, Territory, GameState)       |
| **3**  | MapLoader & erste Testkarte (JSON-basierend)                          |
| **4**  | Verstärkungsphase implementieren                                      |
| **5**  | Angriffsphase mit CombatResolver (Würfelsystem)                       |
| **6**  | Bewegungsphase & Rundenwechsel                                        |
| **7**  | Aufbau der JavaFX-GUI (MapView, Buttons, Statusanzeige)               |
| **8**  | Animationen, Soundeffekte und Polishing                               |
| **9**  | Optionale KI / Netzwerkverbindung                                     |
| **10** | Testen, Dokumentation, Präsentation, Feinschliff                      |

---

## 🧠 8. Lernziele & Kompetenzen

Durch die Arbeit an „Risiko light“ sollen folgende Kompetenzen erworben bzw. vertieft werden:

### Fachliche Kompetenzen

* Anwendung objektorientierter Prinzipien (Kapselung, Vererbung, Polymorphie)
* Strukturierte Software-Architektur (Model-View-Controller)
* GUI-Entwicklung mit JavaFX
* Arbeiten mit Datenstrukturen (Graphen, Listen, Maps)
* Implementierung von Spielregeln, Zufallsfunktionen und Logik

### Methodische Kompetenzen

* Planung und Strukturierung eines Softwareprojekts
* Anwendung von Versionskontrolle (Git)
* Testen, Debugging und Fehlersuche
* Dokumentation von Entwicklungsprozessen

### Soziale Kompetenzen (bei Teamarbeit)

* Kommunikation über Aufgabenverteilung und Designentscheidungen
* Präsentation eines Projektergebnisses

---





