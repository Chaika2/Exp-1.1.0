package ru.expensive.mixin;

import aethereal.Expensive;
import aethereal.event.PushEvent;
import aethereal.type.PushType;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.block.AbstractBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin({AbstractBlock.AbstractBlockState.class})
public class AbstractBlockMixin {
    @ModifyReturnValue(method = {"shouldBlockVision"}, at = {@At("RETURN")})
    public boolean shouldBlockVision(boolean z) {
        PushEvent class231Var = new PushEvent(PushType.BLOCKS);
        Expensive.INSTANCE.eventDispatcher().dispatch(class231Var);
        if (class231Var.isCancelled()) {
            return false;
        }
        return z;
    }
}
