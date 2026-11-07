package ru.expensive.mixin;

import aethereal.event.BlockCollisionEvent;
import aethereal.Expensive;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockCollisionSpliterator;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin({BlockCollisionSpliterator.class})
public abstract class BlockCollisionSpliteratorMixin {
    @Redirect(method = {"computeNext"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/BlockView;getBlockState(Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/block/BlockState;"))
    private BlockState computeNext(BlockView blockView, BlockPos blockPos) {
        BlockCollisionEvent class114Var = new BlockCollisionEvent(blockView.getBlockState(blockPos), blockPos);
        Expensive.INSTANCE.eventDispatcher().dispatch(class114Var);
        return class114Var.getState();
    }
}
