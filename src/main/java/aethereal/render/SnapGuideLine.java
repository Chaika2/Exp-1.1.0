package aethereal.render;

public class SnapGuideLine {
    public final float position;
    public final boolean horizontal;
    public final float start;
    public final float end;

    public SnapGuideLine(float f, boolean z, float f2, float f3) {
        this.position = f;
        this.horizontal = z;
        this.start = f2;
        this.end = f3;
    }
}
