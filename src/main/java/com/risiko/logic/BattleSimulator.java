package com.risiko.logic;

import java.util.Random;

public final class BattleSimulator {

    private static final Random RNG = new Random();

    private BattleSimulator() {}

    public static final class BattleResult {
        public final int attackerRemaining;
        public final int defenderRemaining;
        public final boolean attackerWon;

        public BattleResult(int a, int d) {
            this.attackerRemaining = a;
            this.defenderRemaining = d;
            this.attackerWon = (d == 0);
        }
    }

    private static int roll() {
        return RNG.nextInt(6) + 1;
    }

    public static double calculateWinProbability(int attackerTroops, int defenderTroops) {
        if (attackerTroops <= 1) return 0.0;
        if (defenderTroops <= 0) return 100.0;

        int simulations = 10000;
        int wins = 0;

        for (int i = 0; i < simulations; i++) {
            BattleResult res = simulateBattleDetailed(attackerTroops, defenderTroops);
            if (res.attackerWon) wins++;
        }
        return (wins * 100.0) / simulations;
    }

    public static BattleResult simulateBattleDetailed(int attackerTroops, int defenderTroops) {
        int a = attackerTroops;
        int d = defenderTroops;

        while (a > 1 && d > 0) {
            int aDice = Math.min(3, a - 1);
            int dDice = Math.min(2, d);

            int[] aRolls = new int[aDice];
            int[] dRolls = new int[dDice];

            for (int i = 0; i < aDice; i++) aRolls[i] = roll();
            for (int i = 0; i < dDice; i++) dRolls[i] = roll();

            java.util.Arrays.sort(aRolls);
            java.util.Arrays.sort(dRolls);

            int comps = Math.min(aDice, dDice);
            for (int i = 0; i < comps; i++) {
                int aVal = aRolls[aRolls.length - 1 - i];
                int dVal = dRolls[dRolls.length - 1 - i];

                if (aVal > dVal) d--;
                else a--;
                if (a <= 1 || d <= 0) break;
            }
        }

        return new BattleResult(a, d);
    }
}