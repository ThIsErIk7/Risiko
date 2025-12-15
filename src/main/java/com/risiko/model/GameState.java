package com.risiko.model;

import java.util.EnumMap;
import java.util.Map;

public class GameState {

    private Player currentPlayer = Player.RED;

    // Bank: verfügbare Truppen pro Spieler
    private final Map<Player, Integer> bank = new EnumMap<>(Player.class);

    // Spar-Streak: wie viele eigene Züge in Folge 0 ausgegeben
    private final Map<Player, Integer> saveStreak = new EnumMap<>(Player.class);

    public GameState() {
        bank.put(Player.RED, 18);
        bank.put(Player.BLUE, 18);
        bank.put(Player.NONE, 0);

        saveStreak.put(Player.RED, 0);
        saveStreak.put(Player.BLUE, 0);
        saveStreak.put(Player.NONE, 0);
    }

    public Player getCurrentPlayer() {
        return currentPlayer;
    }

    public void nextPlayer() {
        currentPlayer = (currentPlayer == Player.RED) ? Player.BLUE : Player.RED;
    }

    public int getBank(Player p) {
        return bank.getOrDefault(p, 0);
    }

    public void setBank(Player p, int value) {
        bank.put(p, Math.max(0, value));
    }

    public void addToBank(Player p, int delta) {
        setBank(p, getBank(p) + delta);
    }

    public int getSaveStreak(Player p) {
        return saveStreak.getOrDefault(p, 0);
    }

    public void setSaveStreak(Player p, int streak) {
        saveStreak.put(p, Math.max(0, streak));
    }
}