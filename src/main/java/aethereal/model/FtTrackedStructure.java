package aethereal.model;

import net.minecraft.item.Item;
import net.minecraft.util.math.Vec3d;

public final class FtTrackedStructure {
    public final Item item;
    public final Vec3d vec;
    public final String world;
    public final int anarchy;
    public final double time;

    public FtTrackedStructure(Item item, Vec3d vec3d, String str, int i, double d) {
        this.item = item;
        this.vec = vec3d;
        this.world = str;
        this.anarchy = i;
        this.time = d;
    }

        @Override
    public final String toString() {
        return getClass().getSimpleName() + "[" + "item=" + this.item + ", " + "vec=" + this.vec + ", " + "world=" + this.world + ", " + "anarchy=" + this.anarchy + ", " + "time=" + this.time + "]";
    }
    @Override
    public final int hashCode() {
        return java.util.Objects.hash(this.item, this.vec, this.world, this.anarchy, this.time);
    }
    @Override
    public final boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof FtTrackedStructure)) return false;
        FtTrackedStructure o = (FtTrackedStructure) obj;
        return java.util.Objects.equals(this.item, o.item) && java.util.Objects.equals(this.vec, o.vec) && java.util.Objects.equals(this.world, o.world) && java.util.Objects.equals(this.anarchy, o.anarchy) && java.util.Objects.equals(this.time, o.time);
    }
public Item item() {
        return this.item;
    }

    public Vec3d vec() {
        return this.vec;
    }

    public String world() {
        return this.world;
    }

    public int anarchy() {
        return this.anarchy;
    }

    public double time() {
        return this.time;
    }
}
