package aethereal.event;

public abstract class CancellableEvent implements Event {
    boolean cancelled;
    boolean stopProgression;

    public void cancel() {
        this.cancelled = true;
    }

    public boolean isCancelled() {
        return this.cancelled;
    }

    public boolean isStopProgression() {
        return this.stopProgression;
    }

    public void setStopProgression(boolean z) {
        this.stopProgression = z;
    }
}
