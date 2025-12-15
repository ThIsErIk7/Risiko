package com.risiko.logic;

import java.util.Random;

public final class BattleSimulator {

    private BattleSimulator() {}

    private static final Random RNG = new Random();

    private static int roll() {
        return RNG.nextInt(6) + 1;
    }

    public static double calculateWinProbability(int attacker, int defender) {
        if (attacker <= 1) return 0.0;
        if (defender <= 0) return 100.0;

        int sims = 10000;
        int wins = 0;
        for (int i = 0; i < sims; i++) {
            BattleResult r = simulateBattleDetailed(attacker, defender);
            if (r.defenderRemaining == 0) wins++;
        }
        return (wins * 100.0) / sims;
    }

    public static BattleResult simulateBattleDetailed(int attacker, int defender) {
        int a = attacker;
        int d = defender;

        while (a > 1 && d > 0) {
            int aDice = Math.min(3, a - 1);
            int dDice = Math.min(2, d);

            int[] aRolls = new int[aDice];
            int[] dRolls = new int[dDice];

            for (int i = 0; i < aDice; i++) aRolls[i] = roll();
            for (int i = 0; i < dDice; i++) dRolls[i] = roll();

            java.util.Arrays.sort(aRolls);
            java.util.Arrays.sort(dRolls);

            int comparisons = Math.min(aDice, dDice);
            for (int i = 0; i < comparisons; i++) {
                int aBest = aRolls[aDice - 1 - i];
                int dBest = dRolls[dDice - 1 - i];
                if (aBest > dBest) d--;
                else a--;
                if (a <= 1 || d <= 0) break;
            }
        }

        return new BattleResult(a, d);
    }

    public static final class BattleResult {
        public final int attackerRemaining;
        public final int defenderRemaining;

        public BattleResult(int attackerRemaining, int defenderRemaining) {
            this.attackerRemaining = attackerRemaining;
            this.defenderRemaining = defenderRemaining;
        }
    }
}