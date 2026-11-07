package ru.expensive.mixin;

import aethereal.Expensive;
import aethereal.event.EntityLifecycleEvent;
import aethereal.type.EntityLifecycleAction;
import net.minecraft.entity.Entity;
import net.minecraft.world.entity.ClientEntityManager;
import net.minecraft.world.entity.EntityLike;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({ClientEntityManager.Listener.class})
public class ClientEntityManagerMixin<T extends EntityLike> {

    @Shadow
    @Final
    private T field_27286;

    @Inject(method = {"<init>"}, at = {@At("TAIL")})
    protected void init(CallbackInfo callbackInfo) {
        if (this.field_27286 instanceof Entity entity) {
            Expensive.INSTANCE.eventDispatcher().dispatch(new EntityLifecycleEvent(entity, EntityLifecycleAction.ADD));
        }
    }

    @Inject(method = {"remove"}, at = {@At("HEAD")})
    public void removeEntityHook(Entity.RemovalReason removalReason, CallbackInfo callbackInfo) {
        if (this.field_27286 instanceof Entity entity) {
            Expensive.INSTANCE.eventDispatcher().dispatch(new EntityLifecycleEvent(entity, EntityLifecycleAction.REMOVE));
        }
    }
}
