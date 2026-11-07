package ru.expensive.mixin.accessors;

import net.minecraft.client.render.item.ItemRenderState;
import net.minecraft.item.ModelTransformationMode;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin({ItemRenderState.class})
public interface ItemRenderStateAccessor {
    @Accessor("modelTransformationMode")
    ModelTransformationMode expensive$getModelTransformationMode();
}