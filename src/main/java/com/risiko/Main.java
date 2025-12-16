package com.risiko;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.risiko.logic.GameController;
import com.risiko.map.MapGenerator;
import com.risiko.model.GameState;
import com.risiko.model.Player;
import com.risiko.view.Territory;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextField;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class Main extends Application {

    private GameController controller;

    private String redName;
    private String blueName;

    private final Label currentPlayerLabel = new Label();
    private final Label bankLabel = new Label();
    private final Label selectedFieldLabel = new Label("Ausgewähltes Feld:\n–");
    private final Label infoLabel = new Label("");

    private Button endTurnBtn;
    private boolean gameOver = false;

    @Override
    public void start(Stage stage) {
        showStartScreen(stage);
        stage.show();
    }

    private void showStartScreen(Stage stage) {
        VBox root = new VBox(16);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(40));
        root.setBackground(new Background(
                new BackgroundFill(Color.web("#111"), CornerRadii.EMPTY, Insets.EMPTY)
        ));

        Label title = new Label("RISIKO");
        title.setStyle("-fx-font-size: 32px; -fx-font-weight: bold; -fx-text-fill: white;");

        Label redLabel = new Label("Spieler Rot");
        redLabel.setStyle("-fx-text-fill: salmon; -fx-font-weight: bold;");

        TextField redField = new TextField("Rot");
        redField.setPromptText("Name Spieler Rot");
        redField.setMaxWidth(240);
        redField.setStyle("-fx-font-size: 14px;");

        Label blueLabel = new Label("Spieler Blau");
        blueLabel.setStyle("-fx-text-fill: lightblue; -fx-font-weight: bold;");

        TextField blueField = new TextField("Blau");
        blueField.setPromptText("Name Spieler Blau");
        blueField.setMaxWidth(240);
        blueField.setStyle("-fx-font-size: 14px;");

        Button startBtn = new Button("Spiel starten");
        startBtn.setPrefWidth(240);

        startBtn.setOnAction(e -> {
            String rn = redField.getText().isBlank() ? "Rot" : redField.getText();
            String bn = blueField.getText().isBlank() ? "Blau" : blueField.getText();
            showGame(stage, rn, bn);
        });

        root.getChildren().addAll(
                title,
                redLabel,
                redField,
                blueLabel,
                blueField,
                startBtn
        );

        Scene scene = new Scene(root, 500, 400);
        stage.setScene(scene);
    }

    private void showGame(Stage stage, String redName, String blueName) {

        this.redName = redName;
        this.blueName = blueName;

        controller = new GameController(new GameState());

        BorderPane root = new BorderPane();

        // =================== SIDEBAR ===================
        VBox sidebar = new VBox(14);
        sidebar.setPadding(new Insets(20));
        sidebar.setPrefWidth(280);
        sidebar.setBackground(new Background(
                new BackgroundFill(Color.web("#111"), CornerRadii.EMPTY, Insets.EMPTY)
        ));

        Label title = new Label("RISIKO");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: white;");

        currentPlayerLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
        bankLabel.setStyle("-fx-text-fill: #dddddd;");
        selectedFieldLabel.setStyle("-fx-text-fill: white;");
        infoLabel.setStyle("-fx-text-fill: #bbbbbb;");
        infoLabel.setWrapText(true);
        infoLabel.setMinHeight(80);
        infoLabel.setPrefHeight(120);

        endTurnBtn = new Button("Zug beenden");
        endTurnBtn.setPrefWidth(200);
        endTurnBtn.setOnAction(e -> {
            if (gameOver) return;
            infoLabel.setText(controller.endTurn());
            refreshLabels();
            checkGameOver(stage);
        });

        sidebar.getChildren().addAll(
                title,
                currentPlayerLabel,
                bankLabel,
                selectedFieldLabel,
                new Separator(),
                endTurnBtn,
                new Separator(),
                infoLabel
        );

        // ==================== MAP =====================
        Pane mapPane = new Pane();
        mapPane.setBackground(new Background(
                new BackgroundFill(Color.web("#dfe6ec"), CornerRadii.EMPTY, Insets.EMPTY)
        ));

        // Overlay-Layer für Brückenpfeile (klickt nicht dazwischen)
        Pane bridgePane = new Pane();
        bridgePane.setMouseTransparent(true);
        bridgePane.setPickOnBounds(false);

        StackPane mapWrapper = new StackPane(mapPane, bridgePane);
        mapWrapper.setAlignment(Pos.CENTER);
        mapWrapper.setPadding(new Insets(0)); // wichtig: kein Rand

        MapGenerator generator = new MapGenerator();
        List<Territory> territories = generator.generate(mapPane);

        // Brückenpfeile zeichnen (nach Layout, damit Bounds stimmen)
        bridgePane.prefWidthProperty().bind(mapPane.widthProperty());
        bridgePane.prefHeightProperty().bind(mapPane.heightProperty());
        Platform.runLater(() -> drawBridges(bridgePane, generator, territories));
        controller.setTerritories(territories);

        for (Territory t : territories) {

            // LINKS: normaler Flow (select -> place/attack)
            t.setOnTerritorySelectedListener(sel -> {
                if (gameOver) return;

                controller.selectTerritory(sel);
                refreshLabels();

                // Setup: nur platzieren
                if (controller.isSetupPhase()) {
                    if (sel.getOwner() == Player.NONE || sel.getOwner() == controller.getCurrentPlayer()) {
                        showPlacePopup(stage, sel);
                        refreshLabels();
                        checkGameOver(stage);
                    } else {
                        infoLabel.setText("In der Startphase kannst du nur auf leeren oder eigenen Feldern platzieren.");
                    }
                    return;
                }

                // Normalphase: Gegnerfeld -> Angriff-Popup
                if (sel.getOwner() != Player.NONE && sel.getOwner() != controller.getCurrentPlayer()) {
                    showAttackPopup(stage, sel);
                    refreshLabels();
                    checkGameOver(stage);
                    return;
                }

                // Normalphase: eigenes oder leeres Feld -> Platzieren-Popup
                if (sel.getOwner() == Player.NONE || sel.getOwner() == controller.getCurrentPlayer()) {
                    showPlacePopup(stage, sel);
                    refreshLabels();
                    checkGameOver(stage);
                }
            });

            // RECHTS: Move-Popup (ZIEL-basiert)
            // Rechtsklick auf das Ziel-Feld -> wähle Quelle aus benachbarten eigenen Feldern
            t.setOnTerritoryRightClickListener(target -> {
                if (gameOver) return;
                if (controller.isSetupPhase()) {
                    infoLabel.setText("Verschieben ist erst nach der Startphase möglich.");
                    return;
                }

                Player me = controller.getCurrentPlayer();

                if (target.getOwner() != me) {
                    infoLabel.setText("Du kannst nur in eigene Felder verschieben.");
                    return;
                }

                showMovePopup(stage, target);
                refreshLabels();
                checkGameOver(stage);
            });
        }

        root.setLeft(sidebar);
        root.setCenter(mapWrapper);

        Scene scene = new Scene(root, 1400, 900);
        stage.setTitle("Risiko");
        stage.setScene(scene);

        gameOver = false;
        refreshLabels();
        checkGameOver(stage);
    }

    private void checkGameOver(Stage stage) {
        if (controller == null) return;
        if (gameOver) return;
        if (!controller.isGameOver()) return;

        gameOver = true;

        Player winner = controller.getWinner();
        String reason = controller.getWinnerReason();

        String winnerName;
        if (winner == Player.RED) winnerName = redName;
        else if (winner == Player.BLUE) winnerName = blueName;
        else winnerName = "Unentschieden";

        if (endTurnBtn != null) endTurnBtn.setDisable(true);

        Stage dialog = new Stage();
        dialog.initOwner(stage);
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.setTitle("Spiel beendet");

        VBox box = new VBox(12);
        box.setPadding(new Insets(16));

        Label headline = new Label("Spiel beendet!");
        headline.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        Label winLabel = new Label("Gewinner: " + winnerName);
        Label reasonLabel = new Label(reason == null ? "" : reason);
        reasonLabel.setWrapText(true);

        Button newGameBtn = new Button("Neues Spiel");
        Button closeBtn = new Button("Schließen");

        newGameBtn.setOnAction(e -> {
            dialog.close();
            showStartScreen(stage);
        });

        closeBtn.setOnAction(e -> dialog.close());

        HBox buttons = new HBox(10, newGameBtn, closeBtn);

        box.getChildren().addAll(headline, winLabel, reasonLabel, new Separator(), buttons);

        dialog.setScene(new Scene(box, 420, 220));
        dialog.showAndWait();
    }

    private void refreshLabels() {
        Player p = controller.getCurrentPlayer();
        String playerName = (p == Player.RED) ? redName : blueName;
        currentPlayerLabel.setText("Aktueller Spieler: " + playerName);
        currentPlayerLabel.setStyle(
                "-fx-text-fill: " + (p == Player.RED ? "salmon" : "lightblue") + "; -fx-font-weight: bold;"
        );

        if (controller.isSetupPhase()) {
            int remaining = controller.getSetupRemaining(p);
            bankLabel.setText("Startphase: noch " + remaining + "/18 platzieren");
        } else {
            bankLabel.setText("Bank: " + controller.getBank(p) + " Truppen");
        }

        Territory t = controller.getSelectedTerritory();
        if (t == null) {
            selectedFieldLabel.setText("Ausgewähltes Feld:\n–");
        } else {
            selectedFieldLabel.setText(
                    "Ausgewähltes Feld:\n" +
                    "Name: " + t.getName() + "\n" +
                    "Spieler: " + t.getOwner() + "\n" +
                    "Armeen: " + t.getArmyCount()
            );
        }
    }

    // ===== Platzieren Popup =====
    private void showPlacePopup(Stage ownerStage, Territory target) {
        Stage dialog = new Stage();
        dialog.initOwner(ownerStage);
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.setTitle("Truppen platzieren");

        VBox box = new VBox(10);
        box.setPadding(new Insets(14));

        Player me = controller.getCurrentPlayer();
        int bank = controller.getBank(me);

        String header = (target.getOwner() == Player.NONE) ? "Leeres Feld: " : "Eigenes Feld: ";
        Label t1 = new Label(header + target.getName());

        if (controller.isSetupPhase()) {
            int remaining = controller.getSetupRemaining(me);
            int max = Math.min(bank, remaining);
            if (max <= 0) {
                infoLabel.setText("Keine Truppen verfügbar zum Platzieren.");
                dialog.close();
                return;
            }

            Label t2 = new Label("Startphase – übrig: " + remaining + " (Bank: " + bank + ")");

            Spinner<Integer> amount = new Spinner<>(1, max, 1);
            amount.setEditable(true);

            Button ok = new Button("Platzieren");
            Button cancel = new Button("Abbrechen");

            ok.setOnAction(e -> {
                int val = amount.getValue();
                String msg = controller.placeTroops(target, val);
                infoLabel.setText(msg);
                refreshLabels();
                dialog.close();
            });

            cancel.setOnAction(e -> dialog.close());

            HBox buttons = new HBox(10, ok, cancel);

            box.getChildren().addAll(
                    t1,
                    t2,
                    new Label("Wie viele Truppen?"),
                    amount,
                    new Separator(),
                    buttons
            );

            dialog.setScene(new Scene(box, 360, 230));
            dialog.showAndWait();
            return;
        }

        if (bank <= 0) {
            infoLabel.setText("Keine Truppen mehr in der Bank.");
            dialog.close();
            return;
        }

        Label t2 = new Label("Verfügbar in Bank: " + bank);

        Spinner<Integer> amount = new Spinner<>(1, bank, 1);
        amount.setEditable(true);

        Button ok = new Button("Platzieren");
        Button cancel = new Button("Abbrechen");

        ok.setOnAction(e -> {
            int val = amount.getValue();
            String msg = controller.placeTroops(target, val);
            infoLabel.setText(msg);
            refreshLabels();
            dialog.close();
        });

        cancel.setOnAction(e -> dialog.close());

        HBox buttons = new HBox(10, ok, cancel);

        box.getChildren().addAll(t1, t2, new Label("Wie viele Truppen?"), amount, new Separator(), buttons);
        dialog.setScene(new Scene(box, 360, 210));
        dialog.showAndWait();
    }

    // ===== Angriff Popup =====
    private void showAttackPopup(Stage ownerStage, Territory defender) {
        controller.highlightAttackersFor(defender);

        List<Territory> attackers = controller.getAttackersFor(defender);
        if (attackers == null || attackers.isEmpty()) {
            infoLabel.setText("Kein Angriff möglich: Kein benachbartes eigenes Feld mit mindestens 2 Armeen.");
            controller.clearHighlights();
            return;
        }

        Stage dialog = new Stage();
        dialog.initOwner(ownerStage);
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.setTitle("Angriff");

        VBox box = new VBox(10);
        box.setPadding(new Insets(14));

        Label defLabel = new Label("Verteidiger: " + defender.getName()
                + " (" + defender.getOwner() + "), Armeen: " + defender.getArmyCount());

        ChoiceBox<Territory> attackerChoice = new ChoiceBox<>();
        attackerChoice.getItems().addAll(attackers);
        attackerChoice.getSelectionModel().selectFirst();

        attackerChoice.setConverter(new javafx.util.StringConverter<>() {
            @Override public String toString(Territory t) {
                if (t == null) return "";
                return t.getName() + " (Armeen: " + t.getArmyCount() + ")";
            }
            @Override public Territory fromString(String s) { return null; }
        });

        Label chanceLabel = new Label("Siegchance: –");

        Runnable updateChance = () -> {
            Territory atk = attackerChoice.getValue();
            Double chance = controller.getAttackChance(atk, defender);
            chanceLabel.setText(chance == null ? "Siegchance: –" : String.format("Siegchance: %.1f%%", chance));
        };

        attackerChoice.getSelectionModel().selectedItemProperty().addListener((obs, o, n) -> updateChance.run());
        updateChance.run();

        Button attackBtn = new Button("Angriff ausführen");
        Button cancelBtn = new Button("Abbrechen");

        attackBtn.setOnAction(e -> {
            Territory atk = attackerChoice.getValue();
            String msg = controller.attack(atk, defender);
            infoLabel.setText(msg);
            refreshLabels();
            controller.clearHighlights();
            dialog.close();
        });

        cancelBtn.setOnAction(e -> {
            controller.clearHighlights();
            dialog.close();
        });

        HBox buttons = new HBox(10, attackBtn, cancelBtn);

        box.getChildren().addAll(
                defLabel,
                new Label("Wähle Angreifer (benachbart, mind. 2 Armeen):"),
                attackerChoice,
                chanceLabel,
                new Separator(),
                buttons
        );

        dialog.setScene(new Scene(box, 420, 240));
        dialog.showAndWait();

        controller.clearHighlights();
    }

    // ===== Verschieben Popup (Rechtsklick) =====
    // Rechtsklick auf ZIEL -> Quelle aus Nachbarn wählen -> X Truppen in das Ziel verschieben
    private void showMovePopup(Stage ownerStage, Territory target) {
        Player me = controller.getCurrentPlayer();

        // Quellen = benachbarte eigene Felder mit mind. 2 Armeen (weil 1 bleiben muss)
        List<Territory> sources = target.getNeighbors().stream()
                .filter(t -> t != null && t.getOwner() == me && t.getArmyCount() >= 2)
                .toList();

        if (sources.isEmpty()) {
            infoLabel.setText("Keine benachbarten eigenen Felder mit mindestens 2 Armeen gefunden.");
            return;
        }

        Stage dialog = new Stage();
        dialog.initOwner(ownerStage);
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.setTitle("Truppen verschieben");

        VBox box = new VBox(10);
        box.setPadding(new Insets(14));

        Label head = new Label("Ziel: " + target.getName() + " (Armeen: " + target.getArmyCount() + ")");

        ChoiceBox<Territory> sourceChoice = new ChoiceBox<>();
        sourceChoice.getItems().addAll(sources);
        sourceChoice.getSelectionModel().selectFirst();
        sourceChoice.setConverter(new javafx.util.StringConverter<>() {
            @Override public String toString(Territory t) {
                if (t == null) return "";
                return t.getName() + " (Armeen: " + t.getArmyCount() + ")";
            }
            @Override public Territory fromString(String s) { return null; }
        });

        Territory initialSource = sourceChoice.getValue();
        int initialMax = (initialSource == null) ? 1 : Math.max(1, initialSource.getArmyCount() - 1);
        Spinner<Integer> amount = new Spinner<>(1, initialMax, 1);
        amount.setEditable(true);

        Runnable updateMax = () -> {
            Territory src = sourceChoice.getValue();
            int maxMove = (src == null) ? 1 : Math.max(1, src.getArmyCount() - 1);
            int cur = amount.getValue();
            amount.setValueFactory(new javafx.scene.control.SpinnerValueFactory.IntegerSpinnerValueFactory(
                    1, maxMove, Math.min(cur, maxMove)
            ));
        };

        sourceChoice.getSelectionModel().selectedItemProperty().addListener((obs, o, n) -> updateMax.run());

        Button ok = new Button("Verschieben");
        Button cancel = new Button("Abbrechen");

        ok.setOnAction(e -> {
            Territory source = sourceChoice.getValue();
            if (source == null) {
                infoLabel.setText("Keine Quelle ausgewählt.");
                dialog.close();
                return;
            }

            int val = amount.getValue();
            int canMove = source.getArmyCount() - 1;
            if (val < 1 || val > canMove) {
                Alert a = new Alert(AlertType.WARNING,
                        "Ungültige Anzahl. Es muss mindestens 1 Armee im Startfeld bleiben.");
                a.initOwner(dialog);
                a.showAndWait();
                return;
            }

            if (source.getOwner() != me || target.getOwner() != me || !source.isNeighborOf(target)) {
                infoLabel.setText("Verschieben nur von benachbarten eigenen Feldern ins Ziel.");
                dialog.close();
                return;
            }

            source.setArmyCount(source.getArmyCount() - val);
            target.setArmyCount(target.getArmyCount() + val);

            infoLabel.setText("Verschoben: " + val + " von " + source.getName() + " nach " + target.getName());
            dialog.close();
        });

        cancel.setOnAction(e -> dialog.close());

        HBox buttons = new HBox(10, ok, cancel);

        box.getChildren().addAll(
                head,
                new Label("Quelle (benachbart, eigene Felder, mind. 2 Armeen):"),
                sourceChoice,
                new Label("Wie viele Truppen? (1 muss in der Quelle bleiben)"),
                amount,
                new Separator(),
                buttons
        );

        dialog.setScene(new Scene(box, 440, 270));
        dialog.showAndWait();
    }

    // ===== Brückenpfeile (Insel-übergreifende Nachbarschaften) =====
    private void drawBridges(Pane bridgePane, MapGenerator generator, List<Territory> territories) {
        if (bridgePane == null || generator == null || territories == null) return;

        Map<String, Territory> byName = new HashMap<>();
        for (Territory t : territories) {
            if (t != null && t.getName() != null) byName.put(t.getName(), t);
        }

        List<?> bridges = null;
        try {
            Object result = generator.getClass().getMethod("getBridges").invoke(generator);
            if (result instanceof List<?>) bridges = (List<?>) result;
        } catch (Throwable ignored) {
            bridges = null;
        }

        if (bridges == null || bridges.isEmpty()) {
            String[][] fallback = new String[][]{
                    {"C", "H"},
                    {"E", "H"},
                    {"N", "I"}
            };
            bridgePane.getChildren().clear();
            for (String[] p : fallback) {
                Territory a = byName.get(p[0]);
                Territory b = byName.get(p[1]);
                if (a == null || b == null) continue;
                addArrowBetween(bridgePane, a, b);
            }
            return;
        }

        bridgePane.getChildren().clear();

        for (Object b : bridges) {
            try {
                String from = (String) b.getClass().getMethod("from").invoke(b);
                String to = (String) b.getClass().getMethod("to").invoke(b);

                Territory a = byName.get(from);
                Territory c = byName.get(to);
                if (a == null || c == null) continue;

                addArrowBetween(bridgePane, a, c);

            } catch (Throwable ignored) {
            }
        }
    }

    private void addArrowBetween(Pane pane, Territory a, Territory b) {
        if (pane == null || a == null || b == null) return;

        var ba = a.getArea().getBoundsInParent();
        var bb = b.getArea().getBoundsInParent();

        double cx1 = ba.getCenterX();
        double cy1 = ba.getCenterY();
        double cx2 = bb.getCenterX();
        double cy2 = bb.getCenterY();

        double dx = cx2 - cx1;
        double dy = cy2 - cy1;
        double len = Math.hypot(dx, dy);
        if (len < 0.0001) return;

        double ux = dx / len;
        double uy = dy / len;

        double ra = Math.max(ba.getWidth(), ba.getHeight()) / 2.0;
        double rb = Math.max(bb.getWidth(), bb.getHeight()) / 2.0;

        double pad = 26;

        double x1 = cx1 + ux * (ra + pad);
        double y1 = cy1 + uy * (ra + pad);
        double x2 = cx2 - ux * (rb + pad);
        double y2 = cy2 - uy * (rb + pad);

        addArrow(pane, x1, y1, x2, y2);
    }

    private void addArrow(Pane pane, double x1, double y1, double x2, double y2) {
        double dx = x2 - x1;
        double dy = y2 - y1;
        double len = Math.hypot(dx, dy);
        if (len < 0.0001) return;

        Line line = new Line(x1, y1, x2, y2);
        line.setStroke(Color.color(0, 0, 0, 0.45));
        line.setStrokeWidth(4);
        line.getStrokeDashArray().addAll(10.0, 10.0);

        pane.getChildren().add(line);
    }

    public static void main(String[] args) {
        launch();
    }
}