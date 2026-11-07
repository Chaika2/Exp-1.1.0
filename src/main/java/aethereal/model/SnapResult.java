package aethereal.model;

public class SnapResult {
    public final float x;
    public final float y;
    public final boolean snappedX;
    public final boolean snappedY;

    public SnapResult(float f, float f2, boolean z, boolean z2) {
        this.x = f;
        this.y = f2;
        this.snappedX = z;
        this.snappedY = z2;
    }
}
