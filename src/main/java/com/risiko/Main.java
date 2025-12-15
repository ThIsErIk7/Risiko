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

    private final Label currentPlayerLabel = new Label();
    private final Label selectedFieldLabel = new Label("Ausgewähltes Feld:\n–");
    private final Label infoLabel = new Label();

    @Override
    public void start(Stage stage) {

        // ================= GAME =================
        controller = new GameController(new GameState());

        // ================= ROOT =================
        BorderPane root = new BorderPane();

        // ================= SIDEBAR =================
        VBox sidebar = new VBox(14);
        sidebar.setPadding(new Insets(20));
        sidebar.setPrefWidth(260);
        sidebar.setBackground(new Background(
                new BackgroundFill(Color.web("#111"), CornerRadii.EMPTY, Insets.EMPTY)
        ));

        Label title = new Label("RISIKO");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: white;");

        currentPlayerLabel.setStyle("-fx-text-fill: white;");
        selectedFieldLabel.setStyle("-fx-text-fill: white;");
        infoLabel.setStyle("-fx-text-fill: lightgray;");

        updateCurrentPlayerLabel();

        Button endTurnBtn = new Button("Zug beenden");
        endTurnBtn.setPrefWidth(200);
        endTurnBtn.setOnAction(e -> {
            controller.endTurn();
            updateCurrentPlayerLabel();
            updateSelectedLabel();
            infoLabel.setText("");
        });

        sidebar.getChildren().addAll(
                title,
                currentPlayerLabel,
                selectedFieldLabel,
                infoLabel,
                new Separator(),
                endTurnBtn
        );

        // ================= MAP =================
        Pane mapPane = new Pane();
        mapPane.setBackground(new Background(
                new BackgroundFill(Color.web("#dfe6ec"), CornerRadii.EMPTY, Insets.EMPTY)
        ));

        StackPane mapWrapper = new StackPane(mapPane);
        mapWrapper.setAlignment(Pos.CENTER);
        mapWrapper.setPadding(Insets.EMPTY); // ❗ kein weißer Rand

        MapGenerator generator = new MapGenerator();
        List<Territory> territories = generator.generate(mapPane);
        controller.setTerritories(territories);

        // ================= CLICK =================
        for (Territory t : territories) {
            t.setOnTerritorySelectedListener(sel -> {
                controller.selectTerritory(sel);
                updateSelectedLabel();

                if (shouldOpenAttackPopup(sel)) {
                    showAttackPopup(stage, sel);
                }
            });
        }

        // ================= ROBUSTER HOVER =================
        final Territory[] lastHover = new Territory[1];

        mapPane.setOnMouseMoved(e -> {
            Territory hit = null;

            // rückwärts → oberstes zuerst
            for (int i = territories.size() - 1; i >= 0; i--) {
                Territory t = territories.get(i);
                if (t.getArea().contains(e.getX(), e.getY())) {
                    hit = t;
                    break;
                }
            }

            if (hit != lastHover[0]) {
                if (lastHover[0] != null) lastHover[0].setHovered(false);
                if (hit != null) hit.setHovered(true);
                lastHover[0] = hit;
            }
        });

        mapPane.setOnMouseExited(e -> {
            if (lastHover[0] != null) {
                lastHover[0].setHovered(false);
                lastHover[0] = null;
            }
        });

        // ================= LAYOUT =================
        root.setLeft(sidebar);
        root.setCenter(mapWrapper);

        Scene scene = new Scene(root, 1400, 900);
        stage.setTitle("Risiko");
        stage.setScene(scene);
        stage.show();
    }

    // ================= POPUP LOGIC =================
    private boolean shouldOpenAttackPopup(Territory clicked) {
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

        VBox box = new VBox(12);
        box.setPadding(new Insets(16));

        Label defLabel = new Label(
                "Verteidiger: " + defender.getName() +
                " (" + defender.getOwner() + "), Armeen: " + defender.getArmyCount()
        );

        ChoiceBox<Territory> attackerChoice = new ChoiceBox<>();
        attackerChoice.getItems().addAll(attackers);
        attackerChoice.getSelectionModel().selectFirst();

        attackerChoice.setConverter(new javafx.util.StringConverter<>() {
            @Override public String toString(Territory t) {
                return t == null ? "" : t.getName() + " (" + t.getArmyCount() + ")";
            }
            @Override public Territory fromString(String s) { return null; }
        });

        Label chanceLabel = new Label("Siegchance: –");

        Runnable updateChance = () -> {
            Territory atk = attackerChoice.getValue();
            Double chance = controller.getAttackChance(atk, defender);
            chanceLabel.setText(
                    chance == null ? "Siegchance: –"
                            : String.format("Siegchance: %.1f%%", chance)
            );
        };

        attackerChoice.getSelectionModel()
                .selectedItemProperty()
                .addListener((obs, o, n) -> updateChance.run());

        updateChance.run();

        Button attackBtn = new Button("Angriff");
        Button cancelBtn = new Button("Abbrechen");

        attackBtn.setOnAction(e -> {
            Territory atk = attackerChoice.getValue();
            String msg = controller.attack(atk, defender);
            infoLabel.setText(msg);
            updateSelectedLabel();
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
                new Separator(),
                new Label("Angreifer wählen:"),
                attackerChoice,
                chanceLabel,
                new Separator(),
                buttons
        );

        dialog.setScene(new Scene(box, 420, 240));
        dialog.showAndWait();

        controller.clearHighlights();
    }

    // ================= UI UPDATE =================
    private void updateCurrentPlayerLabel() {
        Player p = controller.getCurrentPlayer();
        currentPlayerLabel.setText("Aktueller Spieler: " + p.name());
        currentPlayerLabel.setStyle(
                "-fx-text-fill: " + (p == Player.RED ? "salmon" : "steelblue") + ";"
        );
    }

    private void updateSelectedLabel() {
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

    public static void main(String[] args) {
        launch();
    }
}