package aethereal.model;
import aethereal.type.TpLootStage;

public class CreeperFarmStats {
    long startTime;
    public int kills;
    public int gunpowder;
    public double money;
    public TpLootStage currentPhase;

    public long getUptimeSeconds() {
        return (System.currentTimeMillis() - this.startTime) / 1000;
    }

    public String formattedTime() {
        int uptimeSeconds = (int) getUptimeSeconds();
        return String.format("%02d:%02d", Integer.valueOf(uptimeSeconds / 60), Integer.valueOf(uptimeSeconds % 60));
    }

    public CreeperFarmStats(long j, int i, int i2, double d, TpLootStage class529Var) {
        this.startTime = j;
        this.kills = i;
        this.gunpowder = i2;
        this.money = d;
        this.currentPhase = class529Var;
    }
}
