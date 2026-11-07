package aethereal.model;

import net.minecraft.util.math.Vec3d;

public class TrajectoryPoint {
    public final int tick;
    public final Vec3d velocity;
    public final Vec3d position;
    public final Vec3d pos;

    public TrajectoryPoint(int i, Vec3d vec3d, Vec3d vec3d2, Vec3d vec3d3) {
        this.tick = i;
        this.velocity = vec3d;
        this.position = vec3d2;
        this.pos = vec3d3;
    }

    public Vec3d pos() {
        return this.pos;
    }

    public int tick() {
        return this.tick;
    }
}
