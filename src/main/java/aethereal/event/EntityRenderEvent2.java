package aethereal.event;

import net.minecraft.entity.Entity;

public class EntityRenderEvent2 extends CancellableEvent {
    public Entity entity;

    public Entity entity() {
        return this.entity;
    }

    public EntityRenderEvent2(Entity entity) {
        this.entity = entity;
    }
}
