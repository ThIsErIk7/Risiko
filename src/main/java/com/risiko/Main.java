package com.risiko;

import javafx.application.Application;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.stage.Stage;
import javafx.scene.Group;

/**
 * Risiko-UI für 2 Spieler mit 3 unregelmäßigen Hex-Inseln.
 * - Nur JavaFX
 * - Inseln bestehen aus vielen kleinen Hex-Feldern (Territories)
 * - Jedes Hex ist einnehmbar (Besitzer + Armeen)
 * - 2 Spieler: Rot & Blau
 * - Sidebar mit Buttons: Zug beenden, +1 Armee, Gebiet übernehmen
 * - Keine Kreise mit Inselnamen mehr
 * - Linien zwischen Hexagons ungleichmäßig -> "Kartenspiel-Look"
 */
public class Main extends Application {

    private enum Player {
        PLAYER1, PLAYER2
    }

    private Player currentPlayer = Player.PLAYER1;
    private Label currentPlayerLabel;
    private Territory selectedTerritory;

    @Override
    public void start(Stage stage) {
        BorderPane root = new BorderPane();

        // Mitte: Karte mit 3 unregelmäßigen Hex-Inseln
        Pane mapPane = createMapPane();
        root.setCenter(mapPane);

        // Links: Sidebar / UI
        VBox sidebar = createSidebar();
        root.setLeft(sidebar);

        Scene scene = new Scene(root, 1000, 600);
        stage.setTitle("Risiko – 2 Spieler, Hex-Inseln");
        stage.setScene(scene);
        stage.show();
    }

    private Pane createMapPane() {
        Pane mapPane = new Pane();
        mapPane.setPrefSize(800, 600);
        // Meer-Hintergrund
        mapPane.setStyle("-fx-background-color: linear-gradient(#3b83bd, #0f3e5d);");

        // Insel 1: relativ groß
        createHexIsland(
                mapPane,
                220, 230,      // Mittelpunkt
                25,            // Hex-Größe
                3,             // Radius (mehr Felder)
                0.15           // Wie stark der Rand "ausgefranst" wird (0–1)
        );

        // Insel 2: mittelgroß, etwas ausgedünnt
        createHexIsland(
                mapPane,
                520, 170,
                25,
                2,
                0.35
        );

        // Insel 3: kleiner, aber kompakt (viele einnehmbare Felder auf kleiner Fläche)
        createHexIsland(
                mapPane,
                460, 410,
                22,   // etwas kleinere Hex-Felder -> wirkt "dichter"
                2,
                0.2
        );

        return mapPane;
    }

    /**
     * Erzeugt eine Insel aus Hexagon-Tiles (Territories) mit axialem Koordinatensystem (q,r).
     * radius = Grundgröße der Insel
     * trimChance = wie viele äußere Hex-Felder zufällig weggelassen werden (macht die Form unregelmäßiger).
     */
    private void createHexIsland(Pane mapPane, double centerX, double centerY,
                                 double size, int radius, double trimChance) {

        Color landColor = Color.web("#d9c38a");
        Color strokeColor = Color.web("#ad8b4b");

        for (int q = -radius; q <= radius; q++) {
            for (int r = -radius; r <= radius; r++) {
                int s = -q - r;
                if (Math.abs(s) > radius) {
                    continue;
                }

                // Unregelmäßige Form: äußeren Ring teilweise wegschneiden
                boolean isOuterRing = (Math.abs(q) == radius) || (Math.abs(r) == radius) || (Math.abs(s) == radius);
                if (isOuterRing && Math.random() < trimChance) {
                    continue;
                }

                // Axiale Koordinaten -> Pixel-Koordinaten (pointy-top Hex)
                double x = size * Math.sqrt(3) * (q + r / 2.0);
                double y = size * 1.5 * r;

                double tileCenterX = centerX + x;
                double tileCenterY = centerY + y;

                // Territory-Objekt (ein Hex-Feld)
                Territory tile = new Territory("T_" + centerX + "_" + centerY + "_" + q + "_" + r,
                        tileCenterX, tileCenterY, size, landColor, strokeColor);

                mapPane.getChildren().add(tile);
            }
        }
    }

    private VBox createSidebar() {
        VBox box = new VBox(15);
        box.setPadding(new Insets(15));
        box.setAlignment(Pos.TOP_CENTER);
        box.setPrefWidth(220);
        box.setStyle("-fx-background-color: #222;");

        Label title = new Label("RISIKO UI");
        title.setStyle("-fx-text-fill: white; -fx-font-size: 20px; -fx-font-weight: bold;");

        currentPlayerLabel = new Label();
        updateCurrentPlayerLabel();

        Label selectionLabel = new Label("Ausgewähltes Feld:");
        selectionLabel.setStyle("-fx-text-fill: #cccccc; -fx-font-size: 12px;");

        Label selectedTerritoryLabel = new Label("Keines");
        selectedTerritoryLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14px;");

        // Buttons
        Button endTurnBtn = new Button("Zug beenden");
        Button addArmyBtn = new Button("+1 Armee");
        Button takeOwnershipBtn = new Button("Feld übernehmen");

        endTurnBtn.setMaxWidth(Double.MAX_VALUE);
        addArmyBtn.setMaxWidth(Double.MAX_VALUE);
        takeOwnershipBtn.setMaxWidth(Double.MAX_VALUE);

        // Aktionen
        endTurnBtn.setOnAction(e -> {
            currentPlayer = (currentPlayer == Player.PLAYER1) ? Player.PLAYER2 : Player.PLAYER1;
            updateCurrentPlayerLabel();
        });

        addArmyBtn.setOnAction(e -> {
            if (selectedTerritory != null) {
                selectedTerritory.addArmy(1);
            }
        });

        takeOwnershipBtn.setOnAction(e -> {
            if (selectedTerritory != null) {
                selectedTerritory.setOwner(currentPlayer);
            }
        });

        // Callback für Territory-Auswahl (von jedem Hex aufgerufen)
        Territory.setOnTerritorySelectedListener(territory -> {
            selectedTerritory = territory;
            if (territory == null) {
                selectedTerritoryLabel.setText("Keines");
            } else {
                selectedTerritoryLabel.setText("Feld (" + territory.getArmyCount() + " Armeen)");
            }
        });

        box.getChildren().addAll(
                title,
                currentPlayerLabel,
                new SeparatorLike(),
                selectionLabel,
                selectedTerritoryLabel,
                new SeparatorLike(),
                endTurnBtn,
                addArmyBtn,
                takeOwnershipBtn
        );

        return box;
    }

    private void updateCurrentPlayerLabel() {
        String text;
        String color;
        if (currentPlayer == Player.PLAYER1) {
            text = "Aktueller Spieler: Rot";
            color = "#ff5555";
        } else {
            text = "Aktueller Spieler: Blau";
            color = "#5599ff";
        }
        currentPlayerLabel.setText(text);
        currentPlayerLabel.setStyle(
                "-fx-text-fill: " + color + "; " +
                        "-fx-font-size: 16px; " +
                        "-fx-font-weight: bold;"
        );
    }

    /**
     * Ein Territory ist jetzt ein Hex-Feld (Polygon) mit Armee-Zahl.
     * Kein Kreis, kein Inselname mehr auf der Karte.
     */
    private static class Territory extends Group {

        // Globaler Listener für Auswahl
        private static TerritorySelectedListener globalListener;

        public static void setOnTerritorySelectedListener(TerritorySelectedListener listener) {
            globalListener = listener;
        }

        private final String name;
        private Player owner;
        private final IntegerProperty armies = new SimpleIntegerProperty(1);
        private final Label label;
        private final Polygon hex;

        public Territory(String name, double centerX, double centerY, double size,
                         Color landColor, Color strokeColor) {
            this.name = name;

            // Hexagon (pointy-top)
            hex = new Polygon();
            for (int i = 0; i < 6; i++) {
                double angleDeg = 60 * i - 30;
                double angleRad = Math.toRadians(angleDeg);
                double x = centerX + size * Math.cos(angleRad);
                double y = centerY + size * Math.sin(angleRad);
                hex.getPoints().addAll(x, y);
            }

            // Unregelmäßige Linien: manche Ränder sind schwächer sichtbar
            boolean strongBorder = Math.random() < 0.7; // 70% normale Linien, 30% fast weg
            if (strongBorder) {
                hex.setStroke(strokeColor);
                hex.setStrokeWidth(1.4);
            } else {
                // Fast keine sichtbare Linie -> sieht aus wie "zusammengewachsen"
                hex.setStroke(landColor);
                hex.setStrokeWidth(0.3);
            }

            hex.setFill(landColor);

            // Armee-Label (nur Zahl, kein Inselname)
            label = new Label();
            label.textProperty().bind(armies.asString("%d"));
            label.setStyle("-fx-text-fill: black; -fx-font-weight: bold; -fx-font-size: 11px;");
            label.setLayoutX(centerX - 5);
            label.setLayoutY(centerY - 8);

            getChildren().addAll(hex, label);

            // Start: neutral
            setOwner(null);

            // Hover-Effekt
            setOnMouseEntered(e -> setEffect(new DropShadow(10, Color.WHITE)));
            setOnMouseExited(e -> setEffect(null));

            // Klick: Territory auswählen
            setOnMouseClicked(e -> {
                if (globalListener != null) {
                    globalListener.onTerritorySelected(this);
                }
                // kleine visuelle Rückmeldung
                setScaleX(1.05);
                setScaleY(1.05);
            });
        }

        public String getName() {
            return name;
        }

        public void setOwner(Player owner) {
            this.owner = owner;
            if (owner == null) {
                hex.setFill(Color.LIGHTGRAY);
            } else if (owner == Player.PLAYER1) {
                hex.setFill(Color.SALMON);
            } else {
                hex.setFill(Color.LIGHTBLUE);
            }
        }

        public Player getOwner() {
            return owner;
        }

        public void addArmy(int amount) {
            armies.set(armies.get() + amount);
        }

        public int getArmyCount() {
            return armies.get();
        }
    }

    /**
     * Einfacher optischer Trenner für die Sidebar.
     */
    private static class SeparatorLike extends Region {
        public SeparatorLike() {
            setPrefHeight(1);
            setMaxWidth(Double.MAX_VALUE);
            setStyle("-fx-background-color: #555;");
            VBox.setMargin(this, new Insets(5, 0, 5, 0));
        }
    }

    /**
     * Interface für Auswahl-Callback.
     */
    private interface TerritorySelectedListener {
        void onTerritorySelected(Territory territory);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
        

