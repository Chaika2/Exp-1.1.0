package aethereal.event;

import net.minecraft.util.math.Vec3d;

public class TravelEvent implements Event {
    Vec3d vec3d;

    public TravelEvent(Vec3d vec3d) {
        this.vec3d = vec3d;
    }

    public Vec3d vec3d() {
        return this.vec3d;
    }

    public TravelEvent vec3d(Vec3d vec3d) {
        this.vec3d = vec3d;
        return this;
    }
}
