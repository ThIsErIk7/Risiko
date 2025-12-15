package com.risiko;

import java.util.List;

import com.risiko.logic.GameController;
import com.risiko.map.MapGenerator;
import com.risiko.model.GameState;
import com.risiko.model.Player;
import com.risiko.view.Territory;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
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

        TextField redField = new TextField("Rot");
        redField.setMaxWidth(240);
        redField.setStyle("-fx-font-size: 14px;");

        TextField blueField = new TextField("Blau");
        blueField.setMaxWidth(240);
        blueField.setStyle("-fx-font-size: 14px;");

        redField.setPromptText("Name Spieler Rot");
        blueField.setPromptText("Name Spieler Blau");

        Button startBtn = new Button("Spiel starten");
        startBtn.setPrefWidth(240);

        startBtn.setOnAction(e -> {
            String redName = redField.getText().isBlank() ? "Rot" : redField.getText();
            String blueName = blueField.getText().isBlank() ? "Blau" : blueField.getText();
            showGame(stage, redName, blueName);
        });

        Label redLabel = new Label("Spieler Rot");
        redLabel.setStyle("-fx-text-fill: salmon; -fx-font-weight: bold;");

        Label blueLabel = new Label("Spieler Blau");
        blueLabel.setStyle("-fx-text-fill: lightblue; -fx-font-weight: bold;");

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

        Label title = new Label("RISIKO UI");
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

        StackPane mapWrapper = new StackPane(mapPane);
        mapWrapper.setAlignment(Pos.CENTER);
        mapWrapper.setPadding(new Insets(0)); // wichtig: kein Rand

        MapGenerator generator = new MapGenerator();
        List<Territory> territories = generator.generate(mapPane);
        controller.setTerritories(territories);

        for (Territory t : territories) {
            t.setOnTerritorySelectedListener(sel -> {
                if (gameOver) return;

                controller.selectTerritory(sel);
                refreshLabels();

                if (shouldOpenPlacePopup(sel)) {
                    showPlacePopup(stage, sel);
                    refreshLabels();
                    checkGameOver(stage);
                } else if (shouldOpenAttackPopup(sel)) {
                    showAttackPopup(stage, sel);
                    refreshLabels();
                    checkGameOver(stage);
                }
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
            // zurück zum Startscreen
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
    private boolean shouldOpenPlacePopup(Territory clicked) {
        if (clicked == null) return false;

        Player me = controller.getCurrentPlayer();

        // In der Startphase darfst du auch auf bereits eigene Felder weiter platzieren.
        if (controller.isSetupPhase()) {
            if (controller.getSetupRemaining(me) <= 0) return false;
            return clicked.getOwner() == Player.NONE || clicked.getOwner() == me;
        }

        // Normalphase: platzieren auf eigenen Feldern ODER leeren Feldern (wenn Bank > 0)
        if (controller.getBank(me) <= 0) return false;
        return clicked.getOwner() == Player.NONE || clicked.getOwner() == me;
    }

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

        int max;
        if (controller.isSetupPhase()) {
            int remaining = controller.getSetupRemaining(me);
            max = Math.max(1, Math.min(bank, remaining));
            Label t2 = new Label("Startphase – übrig: " + remaining + " (Bank: " + bank + ")");
            // Spinner muss nach t2 erstellt werden, also fügen wir t2 später hinzu.
            // (Wir ersetzen unten die addAll-Reihenfolge.)

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

        // Normalphase
        max = Math.max(1, bank);
        Label t2 = new Label("Verfügbar in Bank: " + bank);

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

        box.getChildren().addAll(t1, t2, new Label("Wie viele Truppen?"), amount, new Separator(), buttons);
        dialog.setScene(new Scene(box, 360, 210));
        dialog.showAndWait();
    }

    // ===== Angriff Popup =====
    private boolean shouldOpenAttackPopup(Territory clicked) {
        if (controller.isSetupPhase()) return false;
        if (clicked == null) return false;
        Player me = controller.getCurrentPlayer();

        if (clicked.getOwner() == Player.NONE) return false;
        if (clicked.getOwner() == me) return false;

        return !controller.getAttackersFor(clicked).isEmpty();
    }

    private void showAttackPopup(Stage ownerStage, Territory defender) {
        controller.highlightAttackersFor(defender);

        List<Territory> attackers = controller.getAttackersFor(defender);

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

    public static void main(String[] args) {
        launch();
    }
}