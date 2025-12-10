package com.risiko;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import com.risiko.view.StartView;
import com.risiko.model.Player;

public class Main extends Application {

    @Override
    public void start(Stage stage) {
        // --- Player-Test ---
        Player p1 = new Player("Erik", "Rot", 20);
        System.out.println("=== Player Test ===");
        System.out.println("Name: " + p1.getName());
        System.out.println("Farbe: " + p1.getColor());
        System.out.println("Armeen: " + p1.getArmies());

        // Verstärkung simulieren
        p1.addArmies(5);
        System.out.println("Nach Verstärkung: " + p1.getArmies());

        // Verlust simulieren
        p1.removeArmies(10);
        System.out.println("Nach Angriff: " + p1.getArmies());
        System.out.println("==================\n");

               
                // Start with StartView (Name-Eingabe) and switch to GameView on Start
                StartView start = new StartView(stage);
                Scene scene = new Scene(start, 800, 600);
                stage.setTitle("Risiko");
                stage.setScene(scene);
                stage.show();
            }
        
            public static void main(String[] args) {
                launch(args);
            }
        }
        