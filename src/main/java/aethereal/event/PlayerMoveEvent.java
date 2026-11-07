package aethereal.event;

import net.minecraft.util.math.Vec3d;

public class PlayerMoveEvent extends CancellableEvent {
    public Vec3d movement;

    public PlayerMoveEvent(Vec3d vec3d) {
        this.movement = vec3d;
    }

    public Vec3d movement() {
        return this.movement;
    }

    public PlayerMoveEvent movement(Vec3d vec3d) {
        this.movement = vec3d;
        return this;
    }
}
