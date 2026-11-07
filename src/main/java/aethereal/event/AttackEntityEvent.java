package aethereal.event;

import net.minecraft.entity.Entity;

public final class AttackEntityEvent implements Event {
    public final Entity attacker;

    public AttackEntityEvent(Entity entity) {
        this.attacker = entity;
    }

        @Override
    public final String toString() {
        return getClass().getSimpleName() + "[" + "attacker=" + this.attacker + "]";
    }
    @Override
    public final int hashCode() {
        return java.util.Objects.hash(this.attacker);
    }
    @Override
    public final boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof AttackEntityEvent)) return false;
        AttackEntityEvent o = (AttackEntityEvent) obj;
        return java.util.Objects.equals(this.attacker, o.attacker);
    }
public Entity attacker() {
        return this.attacker;
    }
}
