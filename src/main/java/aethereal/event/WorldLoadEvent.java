package aethereal.event;

import net.minecraft.world.World;

public final class WorldLoadEvent implements Event {
    public final World world;

    public WorldLoadEvent(World world) {
        this.world = world;
    }

        @Override
    public final String toString() {
        return getClass().getSimpleName() + "[" + "world=" + this.world + "]";
    }
    @Override
    public final int hashCode() {
        return java.util.Objects.hash(this.world);
    }
    @Override
    public final boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof WorldLoadEvent)) return false;
        WorldLoadEvent o = (WorldLoadEvent) obj;
        return java.util.Objects.equals(this.world, o.world);
    }
public World world() {
        return this.world;
    }
}
