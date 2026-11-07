package aethereal.ui;
import aethereal.render.AnimatedFloat;
import aethereal.type.NotificationType;

import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

public final class Notification {
    public final AnimatedFloat valueAnimation;
    public final NotificationType type;
    public final Text message;
    public final long timestamp;
    public final long duration;
    public final ItemStack itemStack;

    public Notification(AnimatedFloat class042Var, NotificationType class659Var, Text text, long j, long j2, ItemStack itemStack) {
        this.valueAnimation = class042Var;
        this.type = class659Var;
        this.message = text;
        this.timestamp = j;
        this.duration = j2;
        this.itemStack = itemStack;
    }

    public boolean isExpired() {
        return System.currentTimeMillis() - this.timestamp >= this.duration;
    }

    public float progress() {
        return Math.min(1.0f, (System.currentTimeMillis() - this.timestamp) / this.duration);
    }

    public int alpha() {
        return (int) (255.0f * (1.0f - progress()));
    }

        @Override
    public final String toString() {
        return getClass().getSimpleName() + "[" + "valueAnimation=" + this.valueAnimation + ", " + "type=" + this.type + ", " + "message=" + this.message + ", " + "timestamp=" + this.timestamp + ", " + "duration=" + this.duration + ", " + "itemStack=" + this.itemStack + "]";
    }
    @Override
    public final int hashCode() {
        return java.util.Objects.hash(this.valueAnimation, this.type, this.message, this.timestamp, this.duration, this.itemStack);
    }
    @Override
    public final boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Notification)) return false;
        Notification o = (Notification) obj;
        return java.util.Objects.equals(this.valueAnimation, o.valueAnimation) && java.util.Objects.equals(this.type, o.type) && java.util.Objects.equals(this.message, o.message) && java.util.Objects.equals(this.timestamp, o.timestamp) && java.util.Objects.equals(this.duration, o.duration) && java.util.Objects.equals(this.itemStack, o.itemStack);
    }
public AnimatedFloat valueAnimation() {
        return this.valueAnimation;
    }

    public NotificationType type() {
        return this.type;
    }

    public Text message() {
        return this.message;
    }

    public long timestamp() {
        return this.timestamp;
    }

    public long duration() {
        return this.duration;
    }

    public ItemStack itemStack() {
        return this.itemStack;
    }
}
