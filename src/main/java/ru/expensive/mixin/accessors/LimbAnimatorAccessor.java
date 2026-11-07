package ru.expensive.mixin.accessors;

import net.minecraft.entity.LimbAnimator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin({LimbAnimator.class})
public interface LimbAnimatorAccessor {
    @Accessor("prevSpeed")
    float getPrevSpeed();

    @Accessor("prevSpeed")
    void setPrevSpeed(float f);

    @Accessor("pos")
    void setPos(float f);
}
