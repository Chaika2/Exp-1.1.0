package aethereal.event;

import net.minecraft.entity.Entity;

public final class EntityRenderEvent3 implements Event {
    public final Entity entity;
    public final float tickDelta;

    public EntityRenderEvent3(Entity entity, float f) {
        this.entity = entity;
        this.tickDelta = f;
    }

        @Override
    public final String toString() {
        return getClass().getSimpleName() + "[" + "entity=" + this.entity + ", " + "tickDelta=" + this.tickDelta + "]";
    }
    @Override
    public final int hashCode() {
        return java.util.Objects.hash(this.entity, this.tickDelta);
    }
    @Override
    public final boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof EntityRenderEvent3)) return false;
        EntityRenderEvent3 o = (EntityRenderEvent3) obj;
        return java.util.Objects.equals(this.entity, o.entity) && java.util.Objects.equals(this.tickDelta, o.tickDelta);
    }
public Entity entity() {
        return this.entity;
    }

    public float tickDelta() {
        return this.tickDelta;
    }
}
