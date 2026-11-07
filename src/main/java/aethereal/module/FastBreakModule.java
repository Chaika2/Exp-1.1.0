package aethereal.module;
import aethereal.annotation.Aliases;
import aethereal.type.Mc;
import aethereal.ui.ModuleTab;
import aethereal.net.PacketSender;
import aethereal.event.PlayerTickEvent;

import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;

@Aliases(aliases = {"Fast Break", "Quick Break", "Fast Mine", "Speed Mine", "Quick Mine", "Instant Break", "Instant Mine", "Fast Dig"})
public class FastBreakModule extends Module {
    public FastBreakModule() {
        super(ModuleTab.PLAYER, "Fast Break");
        register(PlayerTickEvent.class, class130Var -> {
            Mc class815Var = Mc.INSTANCE;
            if (isState() && class815Var.isWorldLoaded() && class130Var.isPre()) {
                ClientPlayerInteractionManager interactionManager = class815Var.getInteractionManager();
                HitResult crosshairTarget = class815Var.getCrosshairTarget();
                float f = interactionManager.currentBreakingProgress;
                if (crosshairTarget instanceof BlockHitResult blockHitResult) {
                    if (f > 0.3d) {
                        PacketSender.sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, blockHitResult.getBlockPos(), blockHitResult.getSide()));
                    }
                }
            }
        });
    }
}
