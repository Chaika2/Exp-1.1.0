package ru.expensive.mixin;

import aethereal.event.StopUsingItemEvent2;
import aethereal.event.AttackEntityEvent;
import aethereal.Expensive;
import aethereal.accessor.ClientPlayerInteractionManagerAccessor;
import aethereal.util.ItemUseController;
import aethereal.event.InteractBlockEvent;
import aethereal.event.SlotClickEvent;
import aethereal.event.BlockBreakEvent;
import aethereal.type.BlockBreakStage;
import aethereal.event.BlockBreakEvent2;
import aethereal.module.ScreenWalkModule;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import java.util.List;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin({ClientPlayerInteractionManager.class})
public abstract class ClientPlayerInteractionManagerMixin implements ClientPlayerInteractionManagerAccessor {
    @Inject(method = {"stopUsingItem"}, at = {@At("HEAD")}, cancellable = true)
    public void stopUsingItem(CallbackInfo callbackInfo) {
        StopUsingItemEvent2 class134Var = new StopUsingItemEvent2();
        Expensive.INSTANCE.eventDispatcher().dispatch(class134Var);
        if (class134Var.isCancelled() || ItemUseController.INSTANCE.isUseItem()) {
            ItemUseController.INSTANCE.setUseItem(false);
            callbackInfo.cancel();
        }
    }

    @Inject(method = {"updateBlockBreakingProgress"}, at = {@At("HEAD")}, cancellable = true)
    private void injectBlockBreakingHead(BlockPos blockPos, Direction direction, CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
        BlockBreakEvent2 class393Var = new BlockBreakEvent2();
        Expensive.INSTANCE.eventDispatcher().dispatch(class393Var);
        if (class393Var.isCancelled()) {
            callbackInfoReturnable.setReturnValue(false);
        }
    }

    @Inject(method = {"clickSlot"}, at = {@At("HEAD")}, cancellable = true)
    public void clickSlotHook(int i, int i2, int i3, SlotActionType slotActionType, PlayerEntity playerEntity, CallbackInfo callbackInfo) {
        SlotClickEvent class233Var = new SlotClickEvent(i, i2, i3, slotActionType);
        Expensive.INSTANCE.eventDispatcher().dispatch(class233Var);
        if (class233Var.isCancelled()) {
            callbackInfo.cancel();
        }
    }

    @ModifyExpressionValue(method = {"clickSlot"}, at = {@At(value = "INVOKE", target = "Lnet/minecraft/screen/ScreenHandler;getCursorStack()Lnet/minecraft/item/ItemStack;")})
    public ItemStack clickSlotHook(ItemStack itemStack, @Local(ordinal = 1, argsOnly = true) int i, @Local List<ItemStack> list, @Local(argsOnly = true) SlotActionType slotActionType) {
        return (i < 0 || i >= list.size() || !((slotActionType == SlotActionType.PICKUP || slotActionType == SlotActionType.PICKUP_ALL) && ((ScreenWalkModule) Expensive.INSTANCE.moduleRepository().get(ScreenWalkModule.class)).isState())) ? itemStack : list.get(i);
    }

    @Inject(method = {"attackBlock"}, at = {@At("HEAD")}, cancellable = true)
    private void attackBlock(BlockPos blockPos, Direction direction, CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
        BlockBreakEvent2 class393Var = new BlockBreakEvent2();
        Expensive.INSTANCE.eventDispatcher().dispatch(class393Var);
        if (class393Var.isCancelled()) {
            callbackInfoReturnable.setReturnValue(false);
        }
    }

    @Inject(method = {"updateBlockBreakingProgress"}, at = {@At(value = "INVOKE", target = "Lnet/minecraft/client/tutorial/TutorialManager;onBlockBreaking(Lnet/minecraft/client/world/ClientWorld;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;F)V")})
    private void injectBlockBreaking(BlockPos blockPos, Direction direction, CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
        Expensive.INSTANCE.eventDispatcher().dispatch(new BlockBreakEvent(blockPos, direction, BlockBreakStage.PRE));
    }

    @Inject(method = {"breakBlock"}, at = {@At("RETURN")})
    private void injectBreakBlock(BlockPos blockPos, CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
        Expensive.INSTANCE.eventDispatcher().dispatch(new BlockBreakEvent(blockPos, Direction.UP, BlockBreakStage.POST));
    }

    @Inject(method = {"interactBlock"}, at = {@At("HEAD")}, cancellable = true)
    public void interactBlock(ClientPlayerEntity clientPlayerEntity, Hand hand, BlockHitResult blockHitResult, CallbackInfoReturnable<ActionResult> callbackInfoReturnable) {
        InteractBlockEvent class217Var = new InteractBlockEvent(hand, blockHitResult, (ActionResult) callbackInfoReturnable.getReturnValue());
        Expensive.INSTANCE.eventDispatcher().dispatch(class217Var);
        if (class217Var.isCancelled()) {
            callbackInfoReturnable.setReturnValue(ActionResult.FAIL);
        }
    }

    @ModifyExpressionValue(method = {"syncSelectedSlot"}, at = {@At(value = "FIELD", target = "Lnet/minecraft/entity/player/PlayerInventory;selectedSlot:I")})
    private int hookCustomSelectedSlot(int i) {
        return Expensive.INSTANCE.inventoryService().hotbarSlotSwapper().getServersideSlot();
    }

    @Inject(method = {"attackEntity"}, at = {@At("HEAD")})
    private void attackEntity(PlayerEntity playerEntity, Entity entity, CallbackInfo callbackInfo) {
        Expensive.INSTANCE.eventDispatcher().dispatch(new AttackEntityEvent(entity));
    }

    @Accessor("lastSelectedSlot")
    public abstract void expensive_ru$setLastSelectedSlot(int slot);

    @Accessor("lastSelectedSlot")
    public abstract int expensive_ru$getLastSelectedSlot();
}
