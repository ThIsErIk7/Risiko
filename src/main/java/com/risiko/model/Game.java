package com.risiko.model;

import java.util.List;

public class Game {
    private List<Player> players;
    private Phase currentPhase;
    private int currentPlayerIndex = 0;

    public Game(List<Player> players) {
        this.players = players;
        this.currentPhase = Phase.REINFORCEMENT;
    }

    public Player getCurrentPlayer() {
        return players.get(currentPlayerIndex);
    }

    public void nextPlayer() {
        currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
        currentPhase = Phase.REINFORCEMENT;
    }

    public void setPhase(Phase phase) {
        this.currentPhase = phase;
    }

    public Phase getPhase() {
        return currentPhase;
    }
}
