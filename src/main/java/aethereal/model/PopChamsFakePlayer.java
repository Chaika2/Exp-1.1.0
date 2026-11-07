package aethereal.model;
import aethereal.module.PopChamsModule;

import com.mojang.authlib.GameProfile;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class PopChamsFakePlayer extends PlayerEntity {
    public PopChamsFakePlayer(PopChamsModule class561Var, World world, BlockPos blockPos, float f, GameProfile gameProfile) {
        super(world, blockPos, f, gameProfile);
    }

    public boolean isSpectator() {
        return false;
    }

    public boolean isCreative() {
        return false;
    }
}
