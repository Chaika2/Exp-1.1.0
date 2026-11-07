package aethereal.event;

public class GammaEvent extends CancellableEvent {
    public double gamma;

    public double getGamma() {
        return this.gamma;
    }

    public void setGamma(double d) {
        this.gamma = d;
    }
}
