package aethereal.event;

public class AspectRatioEvent implements Event {
    public float aspectRatio;

    public float getAspectRatio() {
        return this.aspectRatio;
    }

    public void setAspectRatio(float f) {
        this.aspectRatio = f;
    }

    public AspectRatioEvent(float f) {
        this.aspectRatio = f;
    }
}
