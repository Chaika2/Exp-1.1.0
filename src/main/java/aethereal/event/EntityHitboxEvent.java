package aethereal.event;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.Box;

public class EntityHitboxEvent extends CancellableEvent {
    public final Box box;
    public final Entity entity;

    public Box changedBox;

    public void setChangedBox(Box box) {
        this.changedBox = box;
    }

    public Box getBox() {
        return this.box;
    }

    public Entity getEntity() {
        return this.entity;
    }

    public Box getChangedBox() {
        return this.changedBox;
    }

    public EntityHitboxEvent(Box box, Entity entity) {
        this.box = box;
        this.entity = entity;
    }
}
