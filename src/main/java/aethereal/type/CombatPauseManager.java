package aethereal.type;

public enum CombatPauseManager {
    INSTANCE;

    public int rotationPauseTicks;
    public int breakingPauseTicks;
    public int combatPauseTicks;
    public int inCombatTicks;
    public int autoSwapPauseTicks;
    public int swapPauseTicks;

    public void tickSwapPause() {
        if (this.swapPauseTicks <= 0) {
            return;
        }
        this.swapPauseTicks--;
    }

    public void tickRotationPause() {
        if (this.rotationPauseTicks <= 0) {
            return;
        }
        this.rotationPauseTicks--;
    }

    public void tickCombatPause() {
        if (this.combatPauseTicks <= 0) {
            return;
        }
        this.combatPauseTicks--;
    }

    public void tickBreakingPause() {
        if (this.breakingPauseTicks <= 0) {
            return;
        }
        this.breakingPauseTicks--;
    }

    public void tickInCombat() {
        if (this.inCombatTicks <= 0) {
            return;
        }
        this.inCombatTicks--;
    }

    public void tickAutoSwapPause() {
        if (this.autoSwapPauseTicks <= 0) {
            return;
        }
        this.autoSwapPauseTicks--;
    }

    public boolean shouldPauseAutoSwap() {
        return this.autoSwapPauseTicks > 0;
    }

    public boolean shouldPauseCombat() {
        return this.combatPauseTicks > 0;
    }

    public boolean shouldPauseSwaps() {
        return this.swapPauseTicks > 0;
    }

    public boolean shouldPauseRotation() {
        return this.rotationPauseTicks > 0;
    }

    public boolean shouldPauseBreaking() {
        return this.breakingPauseTicks > 0;
    }

    public boolean isInCombat() {
        return this.inCombatTicks > 0;
    }

    public void update() {
        tickRotationPause();
        tickCombatPause();
        tickBreakingPause();
        tickInCombat();
        tickAutoSwapPause();
        tickSwapPause();
    }

    public void pauseSwapsForAtLeast(int i) {
        this.swapPauseTicks = Math.max(this.swapPauseTicks, i);
    }

    public void pauseCombatForAtLeast(int i) {
        this.combatPauseTicks = Math.max(this.combatPauseTicks, i);
    }

    public void inCombatForAtLeast(int i) {
        this.inCombatTicks = Math.max(this.inCombatTicks, i);
    }

    public void pauseRotationForAtLeast(int i) {
        this.rotationPauseTicks = Math.max(this.rotationPauseTicks, i);
    }

    public void pauseBreakingForAtLeast(int i) {
        this.breakingPauseTicks = Math.max(this.breakingPauseTicks, i);
    }

    public void pauseAutoSwapForAtLeast(int i) {
        this.autoSwapPauseTicks = Math.max(this.autoSwapPauseTicks, i);
    }
}
