package aethereal.module;
import aethereal.annotation.Aliases;
import aethereal.type.Mc;
import aethereal.ui.ModuleTab;
import aethereal.event.MovementInputEvent;
import aethereal.math.SimulatedPlayer;

import net.minecraft.block.Blocks;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.GameOptions;
import net.minecraft.util.math.BlockPos;

@Aliases(aliases = {"Edge Jump", "Auto Parkour"})
public class EdgeJumpModule extends Module {
    final Mc mc;

    public EdgeJumpModule() {
        super(ModuleTab.MOVEMENT, "Edge Jump");
        this.mc = Mc.INSTANCE;
        register(MovementInputEvent.class, class040Var -> {
            if (isState() && this.mc.isWorldLoaded()) {
                ClientPlayerEntity player = this.mc.getPlayer();
                GameOptions gameOptions = this.mc.getGameOptions();
                SimulatedPlayer class136VarSimulateLocalPlayer = SimulatedPlayer.simulateLocalPlayer(1);
                if (class136VarSimulateLocalPlayer.pos.subtract(player.getPos()).normalize().dotProduct(player.getRotationVector().normalize()) > 0.0d && player.isOnGround() && !player.isSneaking() && !gameOptions.sneakKey.isPressed() && !gameOptions.jumpKey.isPressed() && (!isOnSlimeBlock(player) ? class136VarSimulateLocalPlayer.onGround : this.mc.getWorld().getBlockState(BlockPos.ofFloored(class136VarSimulateLocalPlayer.pos).down()).isSolid())) {
                    class040Var.setJumping(true);
                }
            }
        });
    }

    public boolean isOnSlimeBlock(ClientPlayerEntity clientPlayerEntity) {
        return clientPlayerEntity.getWorld().getBlockState(BlockPos.ofFloored(clientPlayerEntity.getX(), clientPlayerEntity.getY() - 0.1d, clientPlayerEntity.getZ())).getBlock() == Blocks.SLIME_BLOCK;
    }
}
