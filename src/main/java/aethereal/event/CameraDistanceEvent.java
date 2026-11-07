package aethereal.event;
import aethereal.render.ToggleAnimator;

public class CameraDistanceEvent extends CancellableEvent {
    public float pitch;
    public float distance;
    public ToggleAnimator frontAnim;
    public ToggleAnimator backAnim;

    public CameraDistanceEvent(float f) {
        this.pitch = f;
    }

    public float getPitch() {
        return this.pitch;
    }

    public float getDistance() {
        return this.distance;
    }

    public ToggleAnimator getFrontAnim() {
        return this.frontAnim;
    }

    public ToggleAnimator getBackAnim() {
        return this.backAnim;
    }

    public void setPitch(float f) {
        this.pitch = f;
    }

    public void setDistance(float f) {
        this.distance = f;
    }

    public void setFrontAnim(ToggleAnimator class323Var) {
        this.frontAnim = class323Var;
    }

    public void setBackAnim(ToggleAnimator class323Var) {
        this.backAnim = class323Var;
    }
}
