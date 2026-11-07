package ru.expensive.mixin;

import aethereal.Expensive;
import aethereal.event.BlockUpdateEvent;
import aethereal.model.BlockUpdateEntry;
import aethereal.type.BlockUpdateType;
import java.util.List;
import net.minecraft.block.BlockState;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({World.class})
public abstract class WorldMixin {
    @Inject(method = {"setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;II)Z"}, at = {@At("RETURN")})
    private void setBlockStateHook(BlockPos blockPos, BlockState blockState, int i, int i2, CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
        if ((Object) this instanceof ClientWorld) {
            BlockPos immutable = blockPos.toImmutable();
            Expensive.INSTANCE.eventDispatcher().dispatch(new BlockUpdateEvent(List.of(new BlockUpdateEntry(immutable, blockState, 0)), BlockUpdateType.UPDATE));
        }
    }
}
