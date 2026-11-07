package aethereal.config;
import aethereal.type.BlockRotationMode;
import aethereal.math.Rotation;
import aethereal.type.RotationMode;
import aethereal.math.ScheduledRotation;
import aethereal.type.SpeedRotationMode;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;

public class RotationConfig {
    public static final RotationConfig LINEAR = new RotationConfig(new SpeedRotationMode(200.0f, 180.0f), false, false, false);
    public static final RotationConfig LINEAR_WITH_CORRECTION = new RotationConfig(new SpeedRotationMode(180.0f, 180.0f), false, true, false);
    public static final RotationConfig BLOCK_WITH_CORRECTION = new RotationConfig(new BlockRotationMode(), false, true, false);
    public static final RotationConfig LINEAR_WITH_FOCUSED_CORRECTION = new RotationConfig(new SpeedRotationMode(180.0f, 180.0f), false, true, true);
    public final RotationMode rotationMode;
    public final boolean clientRotation;
    public final boolean moveCorrection;
    public final boolean focusedCorrection;

    public RotationConfig() {
        this(new SpeedRotationMode(180.0f, 180.0f), false, false, false);
    }

    public RotationConfig(RotationMode class405Var, boolean z, boolean z2) {
        this(class405Var, false, z, z2);
    }

    public RotationConfig(boolean z, boolean z2, boolean z3) {
        this(new SpeedRotationMode(180.0f, 180.0f), z, z2, z3);
    }

    public RotationConfig(boolean z, boolean z2) {
        this(new SpeedRotationMode(180.0f, 180.0f), z, true, z2);
    }

    public ScheduledRotation createRotationStrategy(Rotation class007Var, Vec3d vec3d, Entity entity, int i) {
        return new ScheduledRotation(class007Var, this.rotationMode, vec3d, entity, 3, i, this.clientRotation, this.moveCorrection, this.focusedCorrection);
    }

    public ScheduledRotation createRotationStrategy(Rotation class007Var, int i) {
        return new ScheduledRotation(class007Var, this.rotationMode, null, null, 3, i, this.clientRotation, this.moveCorrection, this.focusedCorrection);
    }

    public ScheduledRotation createRotationStrategy(Rotation class007Var, Vec3d vec3d, Entity entity, boolean z, boolean z2, boolean z3, int i) {
        return new ScheduledRotation(class007Var, this.rotationMode, vec3d, entity, 3, i, z, z2, z3);
    }

    public RotationConfig(RotationMode class405Var, boolean z, boolean z2, boolean z3) {
        this.rotationMode = class405Var;
        this.clientRotation = z;
        this.moveCorrection = z2;
        this.focusedCorrection = z3;
    }
}
