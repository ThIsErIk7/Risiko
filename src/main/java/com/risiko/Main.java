package com.risiko;

import javafx.application.Application;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;

/**
 * Einfache Risiko-UI für 2 Spieler mit 3 Inseln.
 * - Nur JavaFX
 * - 3 Inseln (als Kreise)
 * - Pro Insel ein klickbares Gebiet mit Armee-Anzahl
 * - 2 Spieler: Rot & Blau
 * - Sidebar mit Buttons: Zug beenden, +1 Armee, Gebiet übernehmen
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

        // Mitte: Karte mit 3 Inseln
        Pane mapPane = createMapPane();
        root.setCenter(mapPane);

        // Links: Sidebar / UI
        VBox sidebar = createSidebar();
        root.setLeft(sidebar);

        Scene scene = new Scene(root, 1000, 600);
        stage.setTitle("Risiko – 2 Spieler, 3 Inseln");
        stage.setScene(scene);
        stage.show();
    }

    private Pane createMapPane() {
        Pane mapPane = new Pane();
        mapPane.setPrefSize(800, 600);
        // Meer-Hintergrund
        mapPane.setStyle("-fx-background-color: linear-gradient(#3b83bd, #0f3e5d);");

        // 3 Inseln als Kreise (Deko)
        Circle island1 = new Circle(200, 200, 120, Color.BEIGE);
        Circle island2 = new Circle(500, 150, 110, Color.BEIGE);
        Circle island3 = new Circle(450, 400, 130, Color.BEIGE);

        island1.setStroke(Color.DARKGOLDENROD);
        island2.setStroke(Color.DARKGOLDENROD);
        island3.setStroke(Color.DARKGOLDENROD);

        // 3 Territories (je eins pro Insel)
        Territory t1 = new Territory("Insel Nord", 200, 200);
        Territory t2 = new Territory("Insel Ost", 500, 150);
        Territory t3 = new Territory("Insel Süd", 450, 400);

        // Start-Besitz (nur Beispiel)
        t1.setOwner(Player.PLAYER1);
        t2.setOwner(Player.PLAYER2);
        t3.setOwner(null); // neutral

        mapPane.getChildren().addAll(island1, island2, island3, t1, t2, t3);

        return mapPane;
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

        Label selectionLabel = new Label("Ausgewähltes Gebiet:");
        selectionLabel.setStyle("-fx-text-fill: #cccccc; -fx-font-size: 12px;");

        Label selectedTerritoryLabel = new Label("Keines");
        selectedTerritoryLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14px;");

        // Buttons
        Button endTurnBtn = new Button("Zug beenden");
        Button addArmyBtn = new Button("+1 Armee");
        Button takeOwnershipBtn = new Button("Gebiet übernehmen");

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

        // Callback für Territory-Auswahl
        Territory.setOnTerritorySelectedListener(territory -> {
            selectedTerritory = territory;
            if (territory == null) {
                selectedTerritoryLabel.setText("Keines");
            } else {
                selectedTerritoryLabel.setText(territory.getName());
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
     * Klickbares Gebiet auf einer Insel.
     */
    private static class Territory extends StackPane {

        // Globaler Listener für Auswahl
        private static TerritorySelectedListener globalListener;

        public static void setOnTerritorySelectedListener(TerritorySelectedListener listener) {
            globalListener = listener;
        }

        private final String name;
        private Player owner;
        private final IntegerProperty armies = new SimpleIntegerProperty(1);
        private final Label label;
        private final Circle circle;

        public Territory(String name, double centerX, double centerY) {
            this.name = name;

            circle = new Circle(40);
            circle.setStroke(Color.BLACK);
            circle.setStrokeWidth(2);

            label = new Label();
            label.setStyle("-fx-text-fill: black; -fx-font-weight: bold; -fx-font-size: 12px;");
            // Text: Name + Armeeanzahl
            label.textProperty().bind(armies.asString(name + "\nArmeen: %d"));

            setOwner(null); // neutral

            // Position auf der Karte
            setLayoutX(centerX - 40);
            setLayoutY(centerY - 40);

            setAlignment(Pos.CENTER);
            getChildren().addAll(circle, label);

            // Hover-Effekt
            setOnMouseEntered(e -> setScale(1.05));
            setOnMouseExited(e -> setScale(1.0));

            // Klick: Territory auswählen
            setOnMouseClicked(e -> {
                if (globalListener != null) {
                    globalListener.onTerritorySelected(this);
                }
                highlightSelection();
            });
        }

        private void setScale(double scale) {
            setScaleX(scale);
            setScaleY(scale);
        }

        private void highlightSelection() {
            // kleiner Glow-Effekt
            setStyle("-fx-effect: dropshadow(gaussian, rgba(255,255,255,0.7), 10, 0, 0, 0);");
        }

        public String getName() {
            return name;
        }

        public void setOwner(Player owner) {
            this.owner = owner;
            if (owner == null) {
                circle.setFill(Color.LIGHTGRAY);
            } else if (owner == Player.PLAYER1) {
                circle.setFill(Color.SALMON);
            } else {
                circle.setFill(Color.LIGHTBLUE);
            }
        }

        public void addArmy(int amount) {
            armies.set(armies.get() + amount);
        }

        public Player getOwner() {
            return owner;
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
        