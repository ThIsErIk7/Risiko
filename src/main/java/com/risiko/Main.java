package com.risiko;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javafx.application.Application;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Shape;
import javafx.stage.Stage;

public class Main extends Application {

    // Basisgrößen, auf denen die Map gebaut wurde
    private static final double BASE_SCENE_WIDTH  = 1000;
    private static final double BASE_SCENE_HEIGHT = 600;
    private static final double BASE_MAP_WIDTH    = 800;
    private static final double BASE_MAP_HEIGHT   = 600;
    private static final double BASE_SIDEBAR_WIDTH = 260;

    // einheitlicher Meer-Verlauf
    private static final String BG =
            "-fx-background-color: linear-gradient(#3b83bd, #0f3e5d);";

    // ======= Risiko-Kampfsystem =======

    private static final Random RANDOM = new Random();

    public static int rollDice() {
        return RANDOM.nextInt(6) + 1;
    }

    private static class BattleResult {
        final int attackerRemaining;
        final int defenderRemaining;
        BattleResult(int a, int d) {
            this.attackerRemaining = a;
            this.defenderRemaining = d;
        }
    }

    private static BattleResult simulateBattleDetailed(int attackerStart, int defenderStart) {
        int attacker = attackerStart;
        int defender = defenderStart;

        while (attacker > 1 && defender > 0) {
            int[] aRolls = new int[3];
            int[] dRolls = new int[2];

            if (attacker >= 2) aRolls[0] = rollDice();
            if (attacker >= 3) aRolls[1] = rollDice();
            if (attacker >= 4) aRolls[2] = rollDice();

            if (defender >= 1) dRolls[0] = rollDice();
            if (defender >= 2) dRolls[1] = rollDice();

            Arrays.sort(aRolls);
            Arrays.sort(dRolls);

            int aHighest = aRolls[2];
            int aSecond  = aRolls[1];
            int dHighest = dRolls[1];
            int dSecond  = dRolls[0];

            if (attacker >= 3 && defender >= 2) {
                if (aHighest > dHighest) defender--; else attacker--;
                if (aSecond  > dSecond)  defender--; else attacker--;
            } else {
                if (aHighest > dHighest) defender--; else attacker--;
            }
        }

        return new BattleResult(attacker, defender);
    }

    public static double calculateWinProbability(int attacker, int defender) {
        if (attacker <= 1) return 0.0;
        if (defender == 0)  return 100.0;

        int simulations = 5000;
        int wins = 0;

        for (int i = 0; i < simulations; i++) {
            BattleResult r = simulateBattleDetailed(attacker, defender);
            if (r.defenderRemaining == 0) wins++;
        }
        return wins * 100.0 / simulations;
    }

    // ======= UI-Teil =======

    private enum Player { PLAYER1, PLAYER2 }

    private Player currentPlayer = Player.PLAYER1;

    private Label currentPlayerLabel;
    private Territory selectedTerritory;
    private Territory selectedAttacker;
    private Territory selectedDefender;

    private Label selectedTerritoryLabel;
    private Label attackerFieldLabel;
    private Label defenderFieldLabel;
    private Label winChanceLabel;
    private Label battleInfoLabel;

    private Pane mapPane;
    private Pane mapContainer;
    private VBox sidebar;

    @Override
    public void start(Stage stage) {
        BorderPane root = new BorderPane();
        root.setStyle(BG);    // kompletter Fenster-Hintergrund

        // Map + Container
        mapPane = createMapPane();
        mapContainer = new Pane();
        mapContainer.setStyle(BG); // Meer auch hinter der Map
        mapContainer.getChildren().add(mapPane);
        root.setCenter(mapContainer);

        // Sidebar
        sidebar = createSidebar();
        sidebar.setPrefWidth(BASE_SIDEBAR_WIDTH);
        root.setLeft(sidebar);

        Scene scene = new Scene(root, BASE_SCENE_WIDTH, BASE_SCENE_HEIGHT);
        stage.setScene(scene);
        stage.setTitle("Risiko – 2 Spieler, Hex-Inseln (Staaten)");
        stage.show();

        updateLayout(scene);

        scene.widthProperty().addListener((obs, o, n) -> updateLayout(scene));
        scene.heightProperty().addListener((obs, o, n) -> updateLayout(scene));
    }

    /** Fenster- / Fullscreen-Scaling */
    private void updateLayout(Scene scene) {
        double sceneWidth  = scene.getWidth();
        double sceneHeight = scene.getHeight();

        // Sidebar-Skalierung (ein bisschen breiter im Fullscreen)
        double widthScale = sceneWidth / BASE_SCENE_WIDTH;
        double sidebarWidth = BASE_SIDEBAR_WIDTH * Math.max(1.0, Math.min(widthScale, 1.7));
        sidebar.setPrefWidth(sidebarWidth);

        // verfügbare Fläche für die Map
        double availableWidth  = sceneWidth - sidebarWidth;
        double availableHeight = sceneHeight;

        mapContainer.setPrefSize(availableWidth, availableHeight);

        // Grund-Skalierung (so, dass alles reinpasst)
        double baseScale = Math.min(
                availableWidth  / BASE_MAP_WIDTH,
                availableHeight / BASE_MAP_HEIGHT
        );

        // Sicherheits-Rand: Map nutzt max. 90% der Fläche
        double scale = baseScale * 0.9;

        // aber nicht zu klein, sonst wird’s murks im kleinen Fenster
        if (scale < 0.55) scale = 0.55;

        double scaledW = BASE_MAP_WIDTH  * scale;
        double scaledH = BASE_MAP_HEIGHT * scale;

        mapPane.setScaleX(scale);
        mapPane.setScaleY(scale);

        // Map zentrieren
        double offsetX = (availableWidth  - scaledW) / 2.0;
        double offsetY = (availableHeight - scaledH) / 2.0;

        mapPane.setLayoutX(offsetX);
        mapPane.setLayoutY(offsetY);
    }

    /** ======= MAP ======= */

    private Pane createMapPane() {
        Pane pane = new Pane();
        // Map selbst transparent – kein eigener, dunklerer Kasten mehr
        pane.setStyle("-fx-background-color: transparent;");
        pane.setPrefSize(BASE_MAP_WIDTH, BASE_MAP_HEIGHT);

        double w = BASE_MAP_WIDTH;
        double h = BASE_MAP_HEIGHT;

        // Insel 1 – links-mittig
        createHexIslandWithStates(pane, 1, w * 0.30, h * 0.35, 25, 3, 0.18);

        // Insel 2 – rechts oben
        createHexIslandWithStates(pane, 2, w * 0.75, h * 0.30, 25, 3, 0.28);

        // Insel 3 – weiter rechts & weiter unten
        createHexIslandWithStates(pane, 3, w * 0.62, h * 0.82, 23, 3, 0.25);

        return pane;
    }

    /** Hilfsobjekt für die Hex-Zentren */
    private static class HexData {
        final double x, y;
        HexData(double x, double y) {
            this.x = x; this.y = y;
        }
    }

    /** Insel: Hexes zu Staaten gruppiert und zu einer Fläche vereinigt */
    private void createHexIslandWithStates(Pane mapPane, int islandId,
                                           double centerX, double centerY,
                                           double size, int radius, double trimChance) {

        Map<Integer, List<HexData>> regionMap = new HashMap<>();

        for (int q = -radius; q <= radius; q++) {
            for (int r = -radius; r <= radius; r++) {
                int s = -q - r;
                if (Math.abs(s) > radius) continue;

                boolean isOuterRing =
                        Math.abs(q) == radius || Math.abs(r) == radius || Math.abs(s) == radius;

                // äußere Randfelder zufällig wegschneiden
                if (isOuterRing && Math.random() < trimChance) continue;

                double x = size * Math.sqrt(3) * (q + r / 2.0);
                double y = size * 1.5 * r;

                int regionId = assignRegion(islandId, q, r, s);

                regionMap.computeIfAbsent(regionId, k -> new ArrayList<>())
                         .add(new HexData(centerX + x, centerY + y));
            }
        }

        for (Map.Entry<Integer, List<HexData>> entry : regionMap.entrySet()) {
            String name = "Island" + islandId + "_Region" + entry.getKey();
            Territory territory = new Territory(name, entry.getValue(), size);
            mapPane.getChildren().add(territory);
        }
    }

    /** 5 Territorien pro Insel (0–4) */
    private int assignRegion(int islandId, int q, int r, int s) {
        if (islandId == 1) {
            if (q <= -1 && r <= 0) return 0;   // links-oben
            if (q <= -1 && r >  0) return 1;   // links-unten
            if (r >=  2)          return 2;   // unten
            if (q >=  2)          return 3;   // rechts
            return 4;                          // Mitte

        } else if (islandId == 2) {
            if (r <= -1 && q <= 0) return 0;
            if (r <= -1 && q >  0) return 1;
            if (r >=  2)          return 2;
            if (q <= -2)          return 3;
            return 4;

        } else { // Insel 3
            if (q <= -1 && r >= 0) return 0;   // links-unten
            if (q <= -1 && r <  0) return 1;   // links-oben
            if (q >=  2)          return 2;   // rechts
            if (r >=  2)          return 3;   // unten
            return 4;                          // Mitte
        }
    }

    /** ======= SIDEBAR ======= */

    private VBox createSidebar() {
        VBox box = new VBox(10);
        box.setPadding(new Insets(15));
        box.setAlignment(Pos.TOP_CENTER);
        box.setStyle("-fx-background-color: #222;");

        Label title = new Label("RISIKO UI");
        title.setStyle("-fx-text-fill: white; -fx-font-size: 20px; -fx-font-weight: bold;");

        currentPlayerLabel = new Label();
        updateCurrentPlayerLabel();

        selectedTerritoryLabel = new Label("Keines");
        selectedTerritoryLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14px;");

        attackerFieldLabel = new Label("Angreifer: -");
        attackerFieldLabel.setStyle("-fx-text-fill: #ffaaaa; -fx-font-size: 13px;");

        defenderFieldLabel = new Label("Verteidiger: -");
        defenderFieldLabel.setStyle("-fx-text-fill: #aaaaff; -fx-font-size: 13px;");

        winChanceLabel = new Label("Siegchance: -");
        winChanceLabel.setStyle("-fx-text-fill: #ffffaa; -fx-font-size: 13px;");

        battleInfoLabel = new Label("");
        battleInfoLabel.setWrapText(true);
        battleInfoLabel.setStyle("-fx-text-fill: #dddddd; -fx-font-size: 12px;");

        Button setAsAttackerBtn = new Button("Als Angreifer wählen");
        Button setAsDefenderBtn = new Button("Als Verteidiger wählen");
        Button attackBtn        = new Button("ANGREIFEN");
        Button endTurnBtn       = new Button("Zug beenden");
        Button addArmyBtn       = new Button("+1 Armee (auf Auswahl)");
        Button takeOwnershipBtn = new Button("Gebiet übernehmen (aktueller Spieler)");

        setAsAttackerBtn.setMaxWidth(Double.MAX_VALUE);
        setAsDefenderBtn.setMaxWidth(Double.MAX_VALUE);
        attackBtn.setMaxWidth(Double.MAX_VALUE);
        endTurnBtn.setMaxWidth(Double.MAX_VALUE);
        addArmyBtn.setMaxWidth(Double.MAX_VALUE);
        takeOwnershipBtn.setMaxWidth(Double.MAX_VALUE);

        setAsAttackerBtn.setOnAction(e -> {
            if (selectedTerritory == null) {
                battleInfoLabel.setText("Kein Gebiet ausgewählt.");
                return;
            }
            if (selectedTerritory.getOwner() != currentPlayer) {
                battleInfoLabel.setText("Angreifer-Gebiet muss dir gehören.");
                return;
            }
            if (selectedTerritory.getArmyCount() <= 1) {
                battleInfoLabel.setText("Angreifer braucht mindestens 2 Truppen.");
                return;
            }
            selectedAttacker = selectedTerritory;
            battleInfoLabel.setText("Angreifer-Gebiet gesetzt.");
            updateSelectionInfo();
        });

        setAsDefenderBtn.setOnAction(e -> {
            if (selectedTerritory == null) {
                battleInfoLabel.setText("Kein Gebiet ausgewählt.");
                return;
            }
            if (selectedTerritory == selectedAttacker) {
                battleInfoLabel.setText("Verteidiger kann nicht gleich Angreifer sein.");
                return;
            }
            if (selectedTerritory.getArmyCount() <= 0) {
                battleInfoLabel.setText("Verteidiger braucht mindestens 1 Truppe.");
                return;
            }
            selectedDefender = selectedTerritory;
            battleInfoLabel.setText("Verteidiger-Gebiet gesetzt.");
            updateSelectionInfo();
        });

        attackBtn.setOnAction(e -> {
            if (selectedAttacker == null || selectedDefender == null) {
                battleInfoLabel.setText("Bitte Angreifer und Verteidiger wählen.");
                return;
            }
            int attackerTroops = selectedAttacker.getArmyCount();
            int defenderTroops = selectedDefender.getArmyCount();

            if (attackerTroops <= 1) {
                battleInfoLabel.setText("Angreifer braucht mindestens 2 Truppen.");
                return;
            }
            if (defenderTroops <= 0) {
                battleInfoLabel.setText("Verteidiger muss mindestens 1 Truppe haben.");
                return;
            }

            BattleResult result = simulateBattleDetailed(attackerTroops, defenderTroops);

            if (result.defenderRemaining == 0) {
                int moving = Math.max(1, result.attackerRemaining - 1);
                selectedAttacker.setArmyCount(1);
                selectedDefender.setArmyCount(moving);
                selectedDefender.setOwner(selectedAttacker.getOwner());
                battleInfoLabel.setText("Angreifer gewinnt! " + moving + " Truppen besetzen das Gebiet.");
            } else {
                selectedAttacker.setArmyCount(result.attackerRemaining);
                selectedDefender.setArmyCount(result.defenderRemaining);
                battleInfoLabel.setText("Angriff gescheitert! Verteidiger halten das Gebiet.");
            }

            updateSelectionInfo();
        });

        endTurnBtn.setOnAction(e -> {
            currentPlayer = (currentPlayer == Player.PLAYER1) ? Player.PLAYER2 : Player.PLAYER1;
            updateCurrentPlayerLabel();
            battleInfoLabel.setText("Spieler gewechselt.");
        });

        addArmyBtn.setOnAction(e -> {
            if (selectedTerritory != null) {
                selectedTerritory.addArmy(1);
                battleInfoLabel.setText("1 Armee hinzugefügt.");
                updateSelectionInfo();
            } else {
                battleInfoLabel.setText("Kein Gebiet ausgewählt.");
            }
        });

        takeOwnershipBtn.setOnAction(e -> {
            if (selectedTerritory != null) {
                selectedTerritory.setOwner(currentPlayer);
                battleInfoLabel.setText("Gebiet gehört jetzt dem aktuellen Spieler.");
                updateSelectionInfo();
            } else {
                battleInfoLabel.setText("Kein Gebiet ausgewählt.");
            }
        });

        Territory.setOnTerritorySelectedListener(territory -> {
            selectedTerritory = territory;
            updateSelectionInfo();
        });

        box.getChildren().addAll(
                title,
                currentPlayerLabel,
                new SeparatorLike(),
                new Label("Ausgewähltes Gebiet:"),
                selectedTerritoryLabel,
                new SeparatorLike(),
                attackerFieldLabel,
                defenderFieldLabel,
                winChanceLabel,
                setAsAttackerBtn,
                setAsDefenderBtn,
                attackBtn,
                battleInfoLabel,
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
            text  = "Aktueller Spieler: Rot";
            color = "#ff5555";
        } else {
            text  = "Aktueller Spieler: Blau";
            color = "#5599ff";
        }
        currentPlayerLabel.setText(text);
        currentPlayerLabel.setStyle(
                "-fx-text-fill: " + color + "; " +
                "-fx-font-size: 16px; -fx-font-weight: bold;"
        );
    }

    private void updateSelectionInfo() {
        if (selectedTerritory == null) {
            selectedTerritoryLabel.setText("Keines");
        } else {
            selectedTerritoryLabel.setText(
                    "Gebiet (" + selectedTerritory.getArmyCount() + " Armeen)"
            );
        }

        if (selectedAttacker == null) {
            attackerFieldLabel.setText("Angreifer: -");
        } else {
            attackerFieldLabel.setText(
                    "Angreifer: " + selectedAttacker.getArmyCount() + " Truppen"
            );
        }

        if (selectedDefender == null) {
            defenderFieldLabel.setText("Verteidiger: -");
        } else {
            defenderFieldLabel.setText(
                    "Verteidiger: " + selectedDefender.getArmyCount() + " Truppen"
            );
        }

        if (selectedAttacker != null && selectedDefender != null) {
            double p = calculateWinProbability(
                    selectedAttacker.getArmyCount(),
                    selectedDefender.getArmyCount()
            );
            winChanceLabel.setText(String.format("Siegchance: %.1f%%", p));
        } else {
            winChanceLabel.setText("Siegchance: -");
        }
    }

    /** ======= TERRITORY ======= */
    private static class Territory extends Group {

        private static TerritorySelectedListener globalListener;

        public static void setOnTerritorySelectedListener(TerritorySelectedListener listener) {
            globalListener = listener;
        }

        private final String name;
        private Player owner;
        private final IntegerProperty armies = new SimpleIntegerProperty(1);
        private final Shape countryShape;
        private final Label label;

        public Territory(String name, List<HexData> centers, double size) {
            this.name = name;

            double sumX = 0;
            double sumY = 0;

            Shape shape = null;

            for (HexData data : centers) {
                double centerX = data.x;
                double centerY = data.y;
                sumX += centerX;
                sumY += centerY;

                Polygon hex = new Polygon();
                for (int i = 0; i < 6; i++) {
                    double angleDeg = 60 * i - 30;
                    double angleRad = Math.toRadians(angleDeg);
                    double x = centerX + size * Math.cos(angleRad);
                    double y = centerY + size * Math.sin(angleRad);
                    hex.getPoints().addAll(x, y);
                }

                hex.setFill(Color.WHITE);
                hex.setStroke(null); // keine inneren Linien

                if (shape == null) shape = hex;
                else shape = Shape.union(shape, hex);
            }

            this.countryShape = shape;
            countryShape.setStroke(Color.BLACK);
            countryShape.setStrokeWidth(1.6);
            getChildren().add(countryShape);

            double avgX = sumX / centers.size();
            double avgY = sumY / centers.size();

            label = new Label();
            label.textProperty().bind(armies.asString("%d"));
            label.setStyle("-fx-text-fill: black; -fx-font-weight: bold; -fx-font-size: 11px;");
            label.setLayoutX(avgX - 5);
            label.setLayoutY(avgY - 8);
            getChildren().add(label);

            setOwner(null);

            setOnMouseEntered(e -> setEffect(new DropShadow(10, Color.WHITE)));
            setOnMouseExited(e -> setEffect(null));
            setOnMouseClicked(e -> {
                if (globalListener != null) {
                    globalListener.onTerritorySelected(this);
                }
            });
        }

        public String getName() {
            return name;
        }

        public void setOwner(Player owner) {
            this.owner = owner;
            Color fillColor;
            if (owner == null) {
                fillColor = Color.LIGHTGRAY;
            } else if (owner == Player.PLAYER1) {
                fillColor = Color.SALMON;
            } else {
                fillColor = Color.LIGHTBLUE;
            }
            countryShape.setFill(fillColor);
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

        public void setArmyCount(int value) {
            armies.set(value);
        }
    }

    private static class SeparatorLike extends Region {
        public SeparatorLike() {
            setPrefHeight(1);
            setMaxWidth(Double.MAX_VALUE);
            setStyle("-fx-background-color: #555;");
            VBox.setMargin(this, new Insets(5, 0, 5, 0));
        }
    }

    private interface TerritorySelectedListener {
        void onTerritorySelected(Territory territory);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
