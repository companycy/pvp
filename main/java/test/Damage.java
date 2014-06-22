package test;

/**
 * Created by bjcheny on 5/31/14.
 */
class Damage {
    public static long getDamage() {
        double randomPhysicalAtk = 132.0;
        int currentDmgReduceRatio = 1;
        double baseDmgReduceRatio = 0.6;
        int levelDiff = 6;
        int baseLevelDiff = 7;
        double basicDmg = 0;
        long now = System.currentTimeMillis();
        for (int i = 0; i < 10000000; ++i) {
            basicDmg = randomPhysicalAtk * (1 - currentDmgReduceRatio * (baseDmgReduceRatio-(1/currentDmgReduceRatio-baseDmgReduceRatio) * levelDiff / (1.001*baseLevelDiff)));
        }
        long timelapsed = System.currentTimeMillis() - now;
        System.out.println(timelapsed);
        return timelapsed;
    }
}
