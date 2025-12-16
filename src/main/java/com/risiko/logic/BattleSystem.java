package com.risiko.logic;

import java.util.Arrays;
import java.util.Random;


public final class BattleSystem {

    private static final Random RANDOM = new Random();

    

    // Würfelt einen sechsseitigen Würfel (1–6). 
    public static int rollDice() {
        return RANDOM.nextInt(6) + 1;
    }

    //Führt einen kompletten Kampf durch und gibt Rest-Armeen zurück.
     
    public static BattleResult simulateBattleDetailed(int attackerStart, int defenderStart) {
        int attacker = attackerStart;
        int defender = defenderStart;

        while (attacker > 1 && defender > 0) {
            int aRoll1 = 0, aRoll2 = 0, aRoll3 = 0;
            int dRoll1 = 0, dRoll2 = 0;

            // Angreifer würfelt
            if (attacker == 2) {
                aRoll1 = rollDice();
            } else if (attacker == 3) {
                aRoll1 = rollDice();
                aRoll2 = rollDice();
            } else if (attacker >= 4) {
                aRoll1 = rollDice();
                aRoll2 = rollDice();
                aRoll3 = rollDice();
            }

            // Verteidiger würfelt
            if (defender == 1) {
                dRoll1 = rollDice();
            } else if (defender >= 2) {
                dRoll1 = rollDice();
                dRoll2 = rollDice();
            }

            if (attacker >= 3 && defender >= 2) {
                int[] aRolls = {aRoll1, aRoll2, aRoll3};
                Arrays.sort(aRolls);
                int aHighest = aRolls[2];
                int aSecondHighest = aRolls[1];

                int dHighest = Math.max(dRoll1, dRoll2);
                int dLowest = Math.min(dRoll1, dRoll2);

                if (aHighest > dHighest) {
                    defender--;
                } else {
                    attacker--;
                }

                if (aSecondHighest > dLowest) {
                    defender--;
                } else {
                    attacker--;
                }
            } else {
                int aHighest = Math.max(Math.max(aRoll1, aRoll2), aRoll3);
                int dHighest = Math.max(dRoll1, dRoll2);

                if (aHighest > dHighest) {
                    defender--;
                } else {
                    attacker--;
                }
            }
        }

        return new BattleResult(attacker, defender);
    }

   // ist True, wenn Angreifer gewinnt
    public static boolean simulateBattle(int attacker, int defender) {
        BattleResult result = simulateBattleDetailed(attacker, defender);
        return result.defenderRemaining == 0;
    }

    //Simuliert Kämpfe und berechnet die Gewinnwahrscheinlichkeit
     
    public static double calculateWinProbability(int attacker, int defender) {
        if (attacker <= 1) return 0.0;
        if (defender == 0) return 100.0;

        int simulations = 10_000;
        int wins = 0;

        for (int i = 0; i < simulations; i++) {
            if (simulateBattle(attacker, defender)) {
                wins++;
            }
        }

        return (double) wins / simulations * 100.0;
    }

    // Ergebnis eines Kampfes. 
    public static class BattleResult {
        public final int attackerRemaining;
        public final int defenderRemaining;

        public BattleResult(int attackerRemaining, int defenderRemaining) {
            this.attackerRemaining = attackerRemaining;
            this.defenderRemaining = defenderRemaining;
        }

        public boolean attackerWon() {
            return defenderRemaining == 0 && attackerRemaining > 1;
        }
    }
}