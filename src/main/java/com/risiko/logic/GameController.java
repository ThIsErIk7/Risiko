package com.risiko.logic;

import java.util.ArrayList;
import java.util.List;

import com.risiko.model.GameState;
import com.risiko.model.Player;
import com.risiko.view.Territory;

public class GameController {

    private final GameState state;

    private List<Territory> allTerritories = new ArrayList<>();
    private Territory selected;

    public GameController(GameState state) {
        this.state = state;
    }

    // Damit der Controller Nachbarn / Kandidaten finden kann
    public void setTerritories(List<Territory> territories) {
        this.allTerritories = territories == null ? new ArrayList<>() : territories;
    }

    public Player getCurrentPlayer() {
        return state.getCurrentPlayer();
    }

    public Territory getSelectedTerritory() {
        return selected;
    }

    public void selectTerritory(Territory t) {
        this.selected = t;
    }

    public String addArmy() {
        if (selected == null) return "Kein Feld ausgewählt.";
        if (selected.getOwner() != state.getCurrentPlayer()) return "Du kannst nur auf eigene Felder setzen.";
        selected.setArmyCount(selected.getArmyCount() + 1);
        return "+1 Armee gesetzt.";
    }

    public void endTurn() {
        state.nextPlayer();
        selected = null;
        clearHighlights();
    }

    // ====== ANGRIFF: Kandidaten finden ======
    public List<Territory> getAttackersFor(Territory defender) {
        List<Territory> out = new ArrayList<>();
        if (defender == null) return out;

        Player me = state.getCurrentPlayer();

        // Nur angreifen, wenn Verteidiger NICHT mir gehört und nicht NONE ist
        if (defender.getOwner() == me) return out;
        if (defender.getOwner() == Player.NONE) return out;

        for (Territory t : allTerritories) {
            if (t.getOwner() != me) continue;
            if (t.getArmyCount() < 2) continue; // Mind. 2 Armeen zum Angreifen
            if (t.isNeighborOf(defender)) out.add(t);
        }
        return out;
    }

    public Double getAttackChance(Territory attacker, Territory defender) {
        if (attacker == null || defender == null) return null;
        if (!attacker.isNeighborOf(defender)) return null;
        if (attacker.getArmyCount() < 2) return null;
        if (defender.getArmyCount() < 1) return null;
        if (defender.getOwner() == Player.NONE) return null;
        if (defender.getOwner() == attacker.getOwner()) return null;

        return BattleSimulator.calculateWinProbability(attacker.getArmyCount(), defender.getArmyCount());
    }

    public String attack(Territory attacker, Territory defender) {
        if (attacker == null || defender == null) return "Ungültige Auswahl.";
        if (!attacker.isNeighborOf(defender)) return "Angriff nur auf Nachbarfelder erlaubt.";
        if (attacker.getOwner() != state.getCurrentPlayer()) return "Du kannst nur mit deinen Feldern angreifen.";
        if (defender.getOwner() == Player.NONE) return "Du kannst kein neutrales Feld angreifen.";
        if (defender.getOwner() == attacker.getOwner()) return "Du kannst kein eigenes Feld angreifen.";
        if (attacker.getArmyCount() < 2) return "Du brauchst mind. 2 Armeen zum Angreifen.";
        if (defender.getArmyCount() < 1) return "Verteidiger braucht mind. 1 Armee.";

        BattleSimulator.BattleResult res =
                BattleSimulator.simulateBattleDetailed(attacker.getArmyCount(), defender.getArmyCount());

        attacker.setArmyCount(res.attackerRemaining);
        defender.setArmyCount(res.defenderRemaining);

        if (res.defenderRemaining == 0) {
            // Eroberung: Verteidiger gehört Angreifer
            defender.setOwner(attacker.getOwner());

            // Minimaler “Move”: 1 Einheit rüber, 1 bleibt stehen (kannst du später UX-mäßig wählen lassen)
            int movable = Math.max(1, attacker.getArmyCount() - 1);
            int move = Math.min(1, movable);

            attacker.setArmyCount(attacker.getArmyCount() - move);
            defender.setArmyCount(move);

            return "Angreifer gewinnt! Gebiet erobert.";
        }

        return "Angriff beendet.";
    }

    // ===== Highlighting (optional) =====
    public void highlightAttackersFor(Territory defender) {
        clearHighlights();
        for (Territory t : getAttackersFor(defender)) t.setHighlighted(true);
    }

    public void clearHighlights() {
        for (Territory t : allTerritories) {
            if (t.isHighlighted()) t.setHighlighted(false);
        }
    }
}