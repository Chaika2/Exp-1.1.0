package aethereal.model;

import net.minecraft.util.math.Vec3d;

public final class FtTrackedBoss {
    public final String name;
    public final String lvl;
    public final String owner;
    public final Vec3d vec;
    public final String world;
    public final int anarchy;
    public final double timeOpen;
    public final double timeEnd;

    public FtTrackedBoss(String str, String str2, String str3, Vec3d vec3d, String str4, int i, double d, double d2) {
        this.name = str;
        this.lvl = str2;
        this.owner = str3;
        this.vec = vec3d;
        this.world = str4;
        this.anarchy = i;
        this.timeOpen = d;
        this.timeEnd = d2;
    }

        @Override
    public final String toString() {
        return getClass().getSimpleName() + "[" + "name=" + this.name + ", " + "lvl=" + this.lvl + ", " + "owner=" + this.owner + ", " + "vec=" + this.vec + ", " + "world=" + this.world + ", " + "anarchy=" + this.anarchy + ", " + "timeOpen=" + this.timeOpen + ", " + "timeEnd=" + this.timeEnd + "]";
    }
    @Override
    public final int hashCode() {
        return java.util.Objects.hash(this.name, this.lvl, this.owner, this.vec, this.world, this.anarchy, this.timeOpen, this.timeEnd);
    }
    @Override
    public final boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof FtTrackedBoss)) return false;
        FtTrackedBoss o = (FtTrackedBoss) obj;
        return java.util.Objects.equals(this.name, o.name) && java.util.Objects.equals(this.lvl, o.lvl) && java.util.Objects.equals(this.owner, o.owner) && java.util.Objects.equals(this.vec, o.vec) && java.util.Objects.equals(this.world, o.world) && java.util.Objects.equals(this.anarchy, o.anarchy) && java.util.Objects.equals(this.timeOpen, o.timeOpen) && java.util.Objects.equals(this.timeEnd, o.timeEnd);
    }
public String name() {
        return this.name;
    }

    public String lvl() {
        return this.lvl;
    }

    public String owner() {
        return this.owner;
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

    public double timeOpen() {
        return this.timeOpen;
    }

    public double timeEnd() {
        return this.timeEnd;
    }
}
