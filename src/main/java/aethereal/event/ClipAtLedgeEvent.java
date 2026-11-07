package aethereal.event;

public class ClipAtLedgeEvent implements Event {
    public boolean clip;

    public boolean isClip() {
        return this.clip;
    }

    public void setClip(boolean z) {
        this.clip = z;
    }

    public ClipAtLedgeEvent(boolean z) {
        this.clip = z;
    }
}
