package ru.expensive.mixin;

import aethereal.Expensive;
import aethereal.event.BlockCollisionEvent2;
import aethereal.type.Mc;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({Block.class})
public class BlockMixin {
    @Inject(method = {"onPlaced"}, at = {@At("HEAD")})
    public void onPlaced(World world, BlockPos blockPos, BlockState blockState, LivingEntity livingEntity, ItemStack itemStack, CallbackInfo callbackInfo) {
        Block block = (Block) (Object) this;
        if (blockState.getBlock() == Blocks.OBSIDIAN && livingEntity == Mc.INSTANCE.getPlayer()) {
            Expensive.INSTANCE.eventDispatcher().dispatch(new BlockCollisionEvent2(block, blockPos));
        }
    }
}
