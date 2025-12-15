package com.risiko.model;

import java.util.ArrayList;
import java.util.List;

import com.risiko.view.Territory;

public class GameState {

    private Player currentPlayer = Player.RED;

    private Territory selected;
    private Territory attacker;
    private Territory defender;

    // Wenn man ein feindliches Feld gewählt hat und mehrere Angreifer möglich sind
    private final List<Territory> attackerCandidates = new ArrayList<>();

    public Player getCurrentPlayer() {
        return currentPlayer;
    }

    public void nextPlayer() {
        currentPlayer = currentPlayer.other();
        attacker = null;
        defender = null;
        selected = null;
        attackerCandidates.clear();
    }

    public Territory getSelected() { return selected; }
    public void setSelected(Territory selected) { this.selected = selected; }

    public Territory getAttacker() { return attacker; }
    public void setAttacker(Territory attacker) { this.attacker = attacker; }

    public Territory getDefender() { return defender; }
    public void setDefender(Territory defender) { this.defender = defender; }

    public List<Territory> getAttackerCandidates() { return attackerCandidates; }

    public void setAttackerCandidates(List<Territory> candidates) {
        attackerCandidates.clear();
        attackerCandidates.addAll(candidates);
    }

    public void clearAttackSelection() {
        attacker = null;
        defender = null;
        attackerCandidates.clear();
    }
}