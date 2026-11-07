package aethereal.event;

import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Arm;

public class ArmRenderEvent extends CancellableEvent {
    public final Arm arm;
    public final MatrixStack matrixStack;
    public final float swingProgress;
    public final float equipProgress;

    public ArmRenderEvent(Arm arm, MatrixStack matrixStack, float f) {
        this(arm, matrixStack, f, 0.0f);
    }

    public ArmRenderEvent(Arm arm, MatrixStack matrixStack, float f, float f2) {
        this.arm = arm;
        this.matrixStack = matrixStack;
        this.swingProgress = f;
        this.equipProgress = f2;
    }

    public Arm arm() {
        return this.arm;
    }

    public MatrixStack matrixStack() {
        return this.matrixStack;
    }

    public float swingProgress() {
        return this.swingProgress;
    }

    public float equipProgress() {
        return this.equipProgress;
    }
}
