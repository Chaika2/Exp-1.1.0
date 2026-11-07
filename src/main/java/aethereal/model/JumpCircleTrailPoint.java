package aethereal.model;
import aethereal.module.JumpCircleModule;

import net.minecraft.util.math.Vec3d;

public final class JumpCircleTrailPoint {
    public final long spawnTime = System.currentTimeMillis();
    public final Vec3d position;
    final JumpCircleModule module;

    public JumpCircleTrailPoint(JumpCircleModule class555Var, Vec3d vec3d) {
        this.module = class555Var;
        this.position = vec3d;
    }

    public float getProgress() {
        return (System.currentTimeMillis() - this.spawnTime) / this.module.lifetimeSetting.currentValue();
    }
}
