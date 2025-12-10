package com.risiko.view;

import com.risiko.model.Player;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class StartView extends Pane {

    public StartView(Stage stage) {
        setPrefSize(800, 600);

        VBox root = new VBox(12);
        root.setPadding(new Insets(20));
        root.setAlignment(Pos.CENTER);

        Label header = new Label("Risiko - Spiel starten");
        header.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        HBox playerBox1 = new HBox(8);
        playerBox1.setAlignment(Pos.CENTER);
        Label p1Label = new Label("Spieler 1:");
        TextField p1Field = new TextField();
        p1Field.setPromptText("Name Spieler 1");
        playerBox1.getChildren().addAll(p1Label, p1Field);

        HBox playerBox2 = new HBox(8);
        playerBox2.setAlignment(Pos.CENTER);
        Label p2Label = new Label("Spieler 2:");
        TextField p2Field = new TextField();
        p2Field.setPromptText("Name Spieler 2");
        playerBox2.getChildren().addAll(p2Label, p2Field);

        Button startBtn = new Button("Start");
        startBtn.setOnAction(ev -> {
            String name1 = p1Field.getText() == null ? "" : p1Field.getText().trim();
            String name2 = p2Field.getText() == null ? "" : p2Field.getText().trim();
            if (name1.isEmpty() || name2.isEmpty()) {
                Alert a = new Alert(Alert.AlertType.ERROR, "Bitte beide Spielernamen eingeben.");
                a.initOwner(stage);
                a.showAndWait();
                return;
            }

            // Erstelle Player-Objekte (einfaches Beispiel)
            Player p1 = new Player(name1, "Rot", 20);
            Player p2 = new Player(name2, "Blau", 20);
            System.out.println("Spiel gestartet: " + p1 + " vs " + p2);

            // Wechsel zur GameView (aktuelle Implementierung verwendet einfachen Platzhalter)
            Scene scene = new Scene(new GameView(), 1200, 800);
            stage.setScene(scene);
        });

        root.getChildren().addAll(header, playerBox1, playerBox2, startBtn);

        // Füge das VBox-Layout zur Pane hinzu
        getChildren().add(root);
    }
}
