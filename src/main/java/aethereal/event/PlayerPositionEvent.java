package aethereal.event;
import aethereal.type.PlayerPositionStage;

public class PlayerPositionEvent extends CancellableEvent {
    public PlayerPositionStage stage;
    public double x;
    public double y;
    public double z;
    public float yaw;
    public float pitch;
    public boolean onGround;

    public PlayerPositionStage stage() {
        return this.stage;
    }

    public double x() {
        return this.x;
    }

    public double y() {
        return this.y;
    }

    public double z() {
        return this.z;
    }

    public float yaw() {
        return this.yaw;
    }

    public float pitch() {
        return this.pitch;
    }

    public boolean onGround() {
        return this.onGround;
    }

    public PlayerPositionEvent stage(PlayerPositionStage class317Var) {
        this.stage = class317Var;
        return this;
    }

    public PlayerPositionEvent x(double d) {
        this.x = d;
        return this;
    }

    public PlayerPositionEvent y(double d) {
        this.y = d;
        return this;
    }

    public PlayerPositionEvent z(double d) {
        this.z = d;
        return this;
    }

    public PlayerPositionEvent yaw(float f) {
        this.yaw = f;
        return this;
    }

    public PlayerPositionEvent pitch(float f) {
        this.pitch = f;
        return this;
    }

    public PlayerPositionEvent onGround(boolean z) {
        this.onGround = z;
        return this;
    }

    public PlayerPositionEvent(PlayerPositionStage class317Var, double d, double d2, double d3, float f, float f2, boolean z) {
        this.stage = class317Var;
        this.x = d;
        this.y = d2;
        this.z = d3;
        this.yaw = f;
        this.pitch = f2;
        this.onGround = z;
    }
}
