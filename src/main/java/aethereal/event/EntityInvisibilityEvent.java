package aethereal.event;

import net.minecraft.entity.Entity;

public class EntityInvisibilityEvent extends CancellableEvent {
    public final Entity entity;

    public Entity entity() {
        return this.entity;
    }

    public EntityInvisibilityEvent(Entity entity) {
        this.entity = entity;
    }
}
