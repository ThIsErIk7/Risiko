package com.risiko;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import com.risiko.view.GameView;

public class Main extends Application {
    @Override
    public void start(Stage stage) {
        GameView gameView = new GameView();
        Scene scene = new Scene(gameView, 1200, 800);
        stage.setTitle("Risiko Light");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
