package ru.expensive.mixin;

import aethereal.event.EntityRenderEvent;
import aethereal.Expensive;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin({EntityRenderDispatcher.class})
public class EntityRenderDispatcherMixin {
    @WrapOperation(method = {"render(Lnet/minecraft/entity/Entity;DDDFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;ILnet/minecraft/client/render/entity/EntityRenderer;)V"}, at = {@At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/EntityRenderer;getAndUpdateRenderState(Lnet/minecraft/entity/Entity;F)Lnet/minecraft/client/render/entity/state/EntityRenderState;")})
    private EntityRenderState expensive$hidePlayerLabel(EntityRenderer<?, ?> entityRenderer, Entity entity, float f, Operation<EntityRenderState> operation) {
        EntityRenderState entityRenderState = (EntityRenderState) operation.call(new Object[]{entityRenderer, entity, Float.valueOf(f)});
        EntityRenderEvent class147Var = new EntityRenderEvent(entity);
        Expensive.INSTANCE.eventDispatcher().dispatch(class147Var);
        if (class147Var.isCancelled()) {
            entityRenderState.displayName = null;
            entityRenderState.nameLabelPos = null;
        }
        return entityRenderState;
    }
}
