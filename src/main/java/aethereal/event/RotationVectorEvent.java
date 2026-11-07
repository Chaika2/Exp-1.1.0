package aethereal.event;

public class RotationVectorEvent implements Event {
    public float yaw;
    public float pitch;

    public float getYaw() {
        return this.yaw;
    }

    public float getPitch() {
        return this.pitch;
    }

    public void setYaw(float f) {
        this.yaw = f;
    }

    public void setPitch(float f) {
        this.pitch = f;
    }

    public RotationVectorEvent(float f, float f2) {
        this.yaw = f;
        this.pitch = f2;
    }
}
