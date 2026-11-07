package aethereal.type;
import aethereal.math.Rotation;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;

public abstract class RotationMode {
    public final String name;

    public Rotation process(Rotation class007Var, Rotation class007Var2) {
        return process(class007Var, class007Var2, null, null);
    }

    public Rotation process(Rotation class007Var, Rotation class007Var2, Vec3d vec3d) {
        return process(class007Var, class007Var2, vec3d, null);
    }

    public abstract Rotation process(Rotation class007Var, Rotation class007Var2, Vec3d vec3d, Entity entity);

    public abstract Vec3d randomValue();

    public RotationMode(String str) {
        this.name = str;
    }
}
