package aethereal.type;

public class CountLoopStrategy extends LoopStrategy {
    public final int maxLoops;
    public int loops;

    public CountLoopStrategy() {
        this(1);
    }

    public CountLoopStrategy(int i) {
        this.maxLoops = i;
        this.loops = 0;
    }

    @Override
    public boolean shouldLoop(int i, int i2) {
        return i2 > 0 && i >= i2 && this.loops < this.maxLoops - 1;
    }

    @Override
    public void onLoop() {
        this.loops++;
    }

    @Override
    public boolean isFinished() {
        return this.loops >= this.maxLoops - 1;
    }
}
