package com.risiko.logic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.risiko.model.GameState;
import com.risiko.model.Player;
import com.risiko.view.Territory;

public class GameController {

    // ===== Balance =====
    public static final int START_TROOPS_TO_PLACE = 18;
    public static final int BASE_INCOME = 4;

    private final GameState state;

    private List<Territory> territories = new ArrayList<>();
    private Territory selectedTerritory;

    // Placement tracking
    private int placedThisTurn = 0;

    // ===== Start-Phase =====
    private boolean setupPhase = true;
    private int setupRemainingRed = START_TROOPS_TO_PLACE;
    private int setupRemainingBlue = START_TROOPS_TO_PLACE;

    // ===== Rounds / Bonus unlock =====
    // Eine "Runde" = beide Spieler haben je einmal gezogen.
    // Wir zählen eine Runde immer dann hoch, wenn nach `endTurn()` wieder RED dran ist.
    private int roundsPlayed = 0;

    // ===== Game Over =====
    private Player winner = Player.NONE;
    private String winnerReason = "";

    public GameController(GameState state) {
        this.state = state;

        // Während Setup: Bank = exakt verbleibende Starttruppen
        state.setBank(Player.RED, START_TROOPS_TO_PLACE);
        state.setBank(Player.BLUE, START_TROOPS_TO_PLACE);

        // WICHTIG: KEIN Einkommen am Anfang!
        // grantIncomeFor(...) passiert erst, wenn Setup vorbei ist.
    }

    // ===================== Territories / Auswahl =====================

    public void setTerritories(List<Territory> territories) {
        this.territories = (territories == null) ? new ArrayList<>() : territories;
        updateWinnerIfNeeded();
    }

    public List<Territory> getTerritories() {
        return territories;
    }

    public void selectTerritory(Territory t) {
        this.selectedTerritory = t;
    }

    public Territory getSelectedTerritory() {
        return selectedTerritory;
    }

    public Player getCurrentPlayer() {
        return state.getCurrentPlayer();
    }

    // ===================== Setup helpers =====================

    public boolean isSetupPhase() {
        return setupPhase;
    }

    public int getSetupRemaining(Player p) {
        if (p == Player.RED) return setupRemainingRed;
        if (p == Player.BLUE) return setupRemainingBlue;
        return 0;
    }

    // ===================== Bank / Placement =====================

    public int getBank(Player p) {
        return state.getBank(p);
    }

    private boolean spendFromBank(Player p, int amount) {
        if (amount <= 0) return false;
        int bank = state.getBank(p);
        if (bank < amount) return false;
        state.setBank(p, bank - amount);
        return true;
    }

    public String addArmy() {
        Territory t = getSelectedTerritory();
        if (t == null) return "Kein Feld ausgewählt.";

        Player me = state.getCurrentPlayer();

        // Im Setup darf man NUR auf NONE platzieren (damit Claiming sauber ist)
        if (setupPhase) {
            if (t.getOwner() != Player.NONE && t.getOwner() != me) return "Setup: nur auf leeren Feldern platzieren.";
            if (getSetupRemaining(me) <= 0) return "Du hast deine 18 Starttruppen bereits platziert.";
            return placeTroops(t, 1);
        }

        // Normalphase: nur eigene Felder (Legacy-Button)
        if (t.getOwner() != me) return "Du kannst nur auf eigenen Feldern platzieren.";
        if (state.getBank(me) <= 0) return "Keine Truppen mehr in der Bank.";

        return placeTroops(t, 1);
    }

    public String placeTroops(Territory target, int amount) {
        if (target == null) return "Kein Ziel.";
        if (amount <= 0) return "Ungültige Anzahl.";

        Player me = state.getCurrentPlayer();

        if (setupPhase) {
            // Setup: nur auf NONE oder schon-claimed von mir
            if (target.getOwner() != Player.NONE && target.getOwner() != me) {
                return "Setup: du kannst nur auf leere oder deine Felder platzieren.";
            }

            int remaining = getSetupRemaining(me);
            if (amount > remaining) {
                return "Setup: du hast nur noch " + remaining + " Starttruppen übrig.";
            }
        } else {
            // Normal: nur OWN oder NONE (falls ihr später Expand/Claim wollt)
            if (target.getOwner() != Player.NONE && target.getOwner() != me) {
                return "Du kannst nur auf leeren oder eigenen Feldern platzieren.";
            }
        }

        // Bank prüfen & abziehen
        if (!spendFromBank(me, amount)) {
            return "Nicht genug Truppen in der Bank.";
        }

        // Claim wenn NONE
        if (target.getOwner() == Player.NONE) {
            target.setOwner(me);
        }

        target.setArmyCount(target.getArmyCount() + amount);
        placedThisTurn += amount;

        // Setup remaining reduzieren
        if (setupPhase) {
            if (me == Player.RED) setupRemainingRed -= amount;
            if (me == Player.BLUE) setupRemainingBlue -= amount;
        }

        updateWinnerIfNeeded();

        return "Platziert: +" + amount + " auf " + target.getName() + ". Bank: " + state.getBank(me);
    }

    // ===================== Angriff / Popup-Helpers =====================

    public List<Territory> getAttackersFor(Territory defender) {
        if (setupPhase) return Collections.emptyList();
        if (defender == null) return Collections.emptyList();

        Player me = state.getCurrentPlayer();
        if (defender.getOwner() == Player.NONE) return Collections.emptyList();
        if (defender.getOwner() == me) return Collections.emptyList();

        List<Territory> out = new ArrayList<>();
        if (territories == null) return out;

        for (Territory t : territories) {
            if (t.getOwner() == me && t.getArmyCount() >= 2 && t.isNeighborOf(defender)) {
                out.add(t);
            }
        }
        return out;
    }

    public void highlightAttackersFor(Territory defender) {
        clearHighlights();
        for (Territory t : getAttackersFor(defender)) {
            t.setHighlighted(true);
        }
    }

    public void clearHighlights() {
        if (territories == null) return;
        for (Territory t : territories) {
            if (t.isHighlighted()) t.setHighlighted(false);
        }
    }

    public Double getAttackChance(Territory attacker, Territory defender) {
        if (setupPhase) return null;
        if (attacker == null || defender == null) return null;
        if (attacker.getOwner() != state.getCurrentPlayer()) return null;
        if (defender.getOwner() == Player.NONE) return null;
        if (!attacker.isNeighborOf(defender)) return null;
        if (attacker.getArmyCount() < 2) return null;

        return BattleSimulator.calculateWinProbability(attacker.getArmyCount(), defender.getArmyCount());
    }

    public String attack(Territory attacker, Territory defender) {
        if (setupPhase) return "Angriff erst nach der Startphase möglich.";
        if (attacker == null || defender == null) return "Ungültiger Angriff.";

        Player me = state.getCurrentPlayer();

        if (defender.getOwner() == Player.NONE) return "Du kannst kein leeres Feld angreifen.";
        if (defender.getOwner() == me) return "Du kannst nicht dein eigenes Feld angreifen.";
        if (attacker.getOwner() != me) return "Angreifer gehört dir nicht.";
        if (attacker.getArmyCount() < 2) return "Angreifer braucht mind. 2 Armeen.";
        if (!attacker.isNeighborOf(defender)) return "Angriff nur auf Nachbarfelder erlaubt.";

        int atkBefore = attacker.getArmyCount();
        int defBefore = defender.getArmyCount();

        double chance = BattleSimulator.calculateWinProbability(atkBefore, defBefore);
        boolean attackerWins = Math.random() < (chance / 100.0);

        if (attackerWins) {
            // ===== Angreifer gewinnt: 25% Verluste (aufgerundet), dann mind. 5 rüber =====
            int atkLoss = (int) Math.ceil(atkBefore * 0.25);
            atkLoss = Math.max(1, atkLoss);

            int atkAfterLoss = atkBefore - atkLoss;

            // muss mindestens 1 zurücklassen und 5 bewegen können
            int maxMovable = atkAfterLoss - 1;
            if (maxMovable < 5) {
                // zu wenige übrig, um das Feld sinnvoll zu halten
                attacker.setArmyCount(Math.max(1, atkAfterLoss));
                return "Gewonnen, aber zu hohe Verluste: Du kannst das Feld nicht halten. (Chance: "
                        + String.format("%.1f", chance) + "% )";
            }

            int move = 5;

            attacker.setArmyCount(atkAfterLoss - move);
            defender.setOwner(me);
            defender.setArmyCount(move);

            updateWinnerIfNeeded();

            return "Gewonnen! " + defender.getName()
                    + " erobert. Verlust: -" + atkLoss
                    + ", Transfer: " + move
                    + " (Chance: " + String.format("%.1f", chance) + "% )";
        } else {
            // ===== Verteidiger gewinnt: Verteidiger verliert 15% (aufgerundet), Angreifer -1 =====
            attacker.setArmyCount(attacker.getArmyCount() - 1);

            int defLoss = (int) Math.ceil(defBefore * 0.15);
            defLoss = Math.max(1, defLoss);

            int defAfter = defBefore - defLoss;
            defAfter = Math.max(1, defAfter);
            defender.setArmyCount(defAfter);

            updateWinnerIfNeeded();

            return "Verloren. Angreifer -1, Verteidiger -" + defLoss
                    + " (Chance: " + String.format("%.1f", chance) + "% )";
        }
    }

    // ===================== Turn / Sparsystem =====================

    public String endTurn() {
        Player me = state.getCurrentPlayer();

        if (isGameOver()) {
            return "Spiel beendet: " + getWinner() + (winnerReason.isBlank() ? "" : (" (" + winnerReason + ")"));
        }

        // ===== Setup-Phase: EndTurn nur wenn 18 platziert sind =====
        if (setupPhase) {
            if (getSetupRemaining(me) > 0) {
                return "Du musst erst alle 18 Starttruppen platzieren. Übrig: " + getSetupRemaining(me);
            }

            updateWinnerIfNeeded();

            // Spieler wechseln
            placedThisTurn = 0;
            selectedTerritory = null;
            clearHighlights();
            state.nextPlayer();

            updateWinnerIfNeeded();

            // Wenn auch der zweite Spieler fertig ist -> Setup endet, Income starten
            if (setupRemainingRed == 0 && setupRemainingBlue == 0) {
                setupPhase = false;
                roundsPlayed = 0;

                // Wichtig: KEIN sofortiges Einkommen beim Ende der Startphase,
                // sonst hat der Startspieler effektiv +4 extra.
                // Einkommen gibt es erst beim nächsten regulären Rundenwechsel.
                state.setSaveStreak(Player.RED, 0);
                state.setSaveStreak(Player.BLUE, 0);

                updateWinnerIfNeeded();

                return "Startphase abgeschlossen! Neuer Spieler: " + state.getCurrentPlayer()
                        + " | Bank: " + state.getBank(state.getCurrentPlayer())
                        + " (Einkommen kommt erst nach dem nächsten Zugende)";
            }

            return "Startphase: Spielerwechsel. Neuer Spieler: " + state.getCurrentPlayer()
                    + " | Übrig: " + getSetupRemaining(state.getCurrentPlayer());
        }

        // ===== Normalphase =====
        if (placedThisTurn == 0) {
            int streak = state.getSaveStreak(me);
            streak = Math.min(3, streak + 1);
            state.setSaveStreak(me, streak);
        } else {
            state.setSaveStreak(me, 0);
        }

        placedThisTurn = 0;
        selectedTerritory = null;
        clearHighlights();

        state.nextPlayer();

        // Runde zählen: immer wenn nach dem Wechsel wieder RED dran ist, ist eine volle Runde vorbei.
        if (state.getCurrentPlayer() == Player.RED) {
            roundsPlayed++;
        }

        grantIncomeFor(state.getCurrentPlayer());

        updateWinnerIfNeeded();

        Player now = state.getCurrentPlayer();
        return "Zug beendet. Neuer Spieler: " + now + " | Bank: " + state.getBank(now);
    }

    private void grantIncomeFor(Player p) {
        if (p == Player.NONE) return;

        // Grund-Einkommen gibt es immer.
        int income = BASE_INCOME;

        // Sammelbonus erst ab Runde 5 freischalten.
        // Vorher wird nur das Grund-Einkommen gegeben.
        if (roundsPlayed >= 5) {
            int streak = state.getSaveStreak(p);

            // ===== Sparsystem A3 (nerfed) =====
            // Wenn ein Spieler 3 Züge hintereinander 0 platziert hat,
            // bekommt er EINMALIG +BASE_INCOME extra (also 8 statt 4) und der Streak resetet.
            if (streak >= 3) {
                income += BASE_INCOME;
                state.setSaveStreak(p, 0);
            }
        }

        state.addToBank(p, income);
    }

    // ===================== Game Over =====================

    public boolean isGameOver() {
        updateWinnerIfNeeded();
        return winner != Player.NONE;
    }

    public Player getWinner() {
        updateWinnerIfNeeded();
        return winner;
    }

    public String getWinnerReason() {
        updateWinnerIfNeeded();
        return winnerReason;
    }

    /**
     * Win condition:
     * - A player has 0 territories
     * - or a player owns all territories
     */
    private void updateWinnerIfNeeded() {
        if (setupPhase) {
            // Während Setup noch kein Game Over
            winner = Player.NONE;
            winnerReason = "";
            return;
        }

        if (territories == null || territories.isEmpty()) {
            winner = Player.NONE;
            winnerReason = "";
            return;
        }

        int red = 0;
        int blue = 0;
        int total = 0;

        for (Territory t : territories) {
            if (t == null) continue;
            Player o = t.getOwner();
            if (o == Player.NONE) continue; // unclaimed zählt nicht
            total++;
            if (o == Player.RED) red++;
            else if (o == Player.BLUE) blue++;
        }

        // Wenn es noch unclaimed gibt (total < territories.size()), kann trotzdem jemand 0 Felder haben.
        if (red == 0 && blue > 0) {
            winner = Player.BLUE;
            winnerReason = "Rot hat keine Staaten mehr";
            return;
        }
        if (blue == 0 && red > 0) {
            winner = Player.RED;
            winnerReason = "Blau hat keine Staaten mehr";
            return;
        }

        // Alle geclaimten gehören einem Spieler UND es gibt mindestens 1 geclaimtes Feld.
        if (total > 0 && red == total && blue == 0) {
            winner = Player.RED;
            winnerReason = "Alle Staaten gehören Rot";
            return;
        }
        if (total > 0 && blue == total && red == 0) {
            winner = Player.BLUE;
            winnerReason = "Alle Staaten gehören Blau";
            return;
        }

        // Wenn ALLE Territorien geclaimt sind, ist es eindeutiger: einer besitzt wirklich alle.
        if (total == territories.size()) {
            if (red == total) {
                winner = Player.RED;
                winnerReason = "Alle Staaten gehören Rot";
                return;
            }
            if (blue == total) {
                winner = Player.BLUE;
                winnerReason = "Alle Staaten gehören Blau";
                return;
            }
        }

        winner = Player.NONE;
        winnerReason = "";
    }
}