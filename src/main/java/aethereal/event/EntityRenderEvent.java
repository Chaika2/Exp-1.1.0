package aethereal.event;

import net.minecraft.entity.Entity;

public class EntityRenderEvent extends CancellableEvent {
    public final Entity entity;

    public Entity getEntity() {
        return this.entity;
    }

    public EntityRenderEvent(Entity entity) {
        this.entity = entity;
    }
}
