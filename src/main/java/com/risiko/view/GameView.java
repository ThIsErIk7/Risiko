package com.risiko.view;

import javafx.scene.layout.BorderPane;
import javafx.scene.control.Label;

public class GameView extends BorderPane {
    public GameView() {
        Label label = new Label("Willkommen bei Risiko Light!");
        this.setCenter(label);
    }
}
