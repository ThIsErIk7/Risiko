package com.risiko.view;

import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.scene.text.Font;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.List;
// no Map import needed

public class GameView extends Pane {

    public GameView() {
        // Setze Größe der Pane
        setPrefSize(1200, 800);

        // Karte-Hintergrund
        Rectangle mapPlaceholder = new Rectangle(1000, 700);
        mapPlaceholder.setFill(Color.LIGHTGREEN);
        mapPlaceholder.setStroke(Color.BLACK);
        mapPlaceholder.setStrokeWidth(2);
        mapPlaceholder.setLayoutX(100);
        mapPlaceholder.setLayoutY(50);
        getChildren().add(mapPlaceholder);

        // Titel
        Text title = new Text(120, 30, "Risiko - Testkarte");
        title.setFont(Font.font(20));
        getChildren().add(title);

        // Legend
        Rectangle legendBg = new Rectangle(1120, 60, 60, 120);
        legendBg.setFill(Color.WHITESMOKE);
        legendBg.setStroke(Color.GRAY);
        getChildren().add(legendBg);
        Text legendText = new Text(1130, 80, "Legende:\n- Territorium\n- Nachbar" );
        legendText.setFont(Font.font(12));
        getChildren().add(legendText);

        // Versuche, die Testkarte aus resources zu lesen und einfache Knoten zu platzieren
        try (InputStream in = getClass().getResourceAsStream("/maps/defaultMap.json")) {
            if (in != null) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) sb.append(line).append('\n');
                // Very small, dependency-free parsing for the test map JSON
                String jsonText = sb.toString();
                List<String> territoryNames = new ArrayList<>();
                // crude parsing: find occurrences of "name": "..."
                int idx = 0;
                while (true) {
                    int namePos = jsonText.indexOf("\"name\"", idx);
                    if (namePos == -1) break;
                    int colon = jsonText.indexOf(':', namePos);
                    if (colon == -1) break;
                    int quote1 = jsonText.indexOf('"', colon);
                    if (quote1 == -1) break;
                    int quote2 = jsonText.indexOf('"', quote1 + 1);
                    if (quote2 == -1) break;
                    String name = jsonText.substring(quote1 + 1, quote2);
                    territoryNames.add(name);
                    idx = quote2 + 1;
                }
                if (!territoryNames.isEmpty()) {
                    // build simple territory list from parsed names
                    // Platzierungen für Demo (gleichmäßig verteilt)
                    double startX = 150;
                    double startY = 100;
                    double dx = 350;
                    double dy = 200;
                    int i = 0;
                    for (String name : territoryNames) {
                        double x = startX + (i % 3) * dx;
                        double y = startY + (i / 3) * dy;
                        Circle c = new Circle(x, y, 40, Color.SANDYBROWN);
                        c.setStroke(Color.DARKSLATEGRAY);
                        getChildren().add(c);
                        Text txt = new Text(x - 30, y + 60, name);
                        txt.setFont(Font.font(14));
                        getChildren().add(txt);
                        i++;
                    }
                }
            } else {
                // Fallback: einfache Demo-Knoten
                addDemoTerritories();
            }
        } catch (Exception e) {
            // Bei Fehlern Fallback verwenden
            addDemoTerritories();
        }
    }

    private void addDemoTerritories() {
        Circle c1 = new Circle(300, 200, 40, Color.SANDYBROWN);
        c1.setStroke(Color.DARKSLATEGRAY);
        Text t1 = new Text(270, 260, "Nordland");
        t1.setFont(Font.font(14));
        Circle c2 = new Circle(650, 250, 40, Color.SANDYBROWN);
        c2.setStroke(Color.DARKSLATEGRAY);
        Text t2 = new Text(620, 310, "Ostland");
        t2.setFont(Font.font(14));
        Circle c3 = new Circle(400, 450, 40, Color.SANDYBROWN);
        c3.setStroke(Color.DARKSLATEGRAY);
        Text t3 = new Text(370, 510, "Südland");
        t3.setFont(Font.font(14));
        getChildren().addAll(c1, t1, c2, t2, c3, t3);
    }
}
