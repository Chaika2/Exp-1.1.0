package aethereal.module;
import aethereal.annotation.Aliases;
import aethereal.Lang;
import aethereal.type.Mc;
import aethereal.ui.setting.ModeSetting;
import aethereal.ui.ModuleTab;
import aethereal.type.NoFallMode;
import aethereal.event.PacketSendEvent;
import aethereal.net.PacketSender;
import aethereal.event.PlayerTickEvent;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.Hand;

@Aliases(aliases = {"No Fall", "No Fall Damage", "Anti Fall", "No Damage Fall", "Fall Damage Prevent"})
public class NoFallModule extends Module {
    public final ModeSetting<NoFallMode> modeSetting;
    public final Mc mc;

    public NoFallModule() {
        super(ModuleTab.MOVEMENT, "No Fall");
        this.modeSetting = new ModeSetting(Lang.MODE).values(NoFallMode.class);
        this.mc = Mc.INSTANCE;
        addSettings(this.modeSetting);
        register(PlayerTickEvent.class, class130Var -> {
            if (isState() && this.mc.isWorldLoaded() && class130Var.isPre()) {
                ClientPlayerEntity player = this.mc.getPlayer();
                if (!this.modeSetting.isSelected(NoFallMode.PACKET) || player.fallDistance <= 2.4f) {
                    return;
                }
                PacketSender.sendPacket(new PlayerMoveC2SPacket.Full(player.getX(), player.getY() + 1.0E-6d, player.getZ(), player.getYaw(), player.getPitch(), false, player.horizontalCollision));
                PacketSender.sendSequencedPacket(i -> {
                    return new PlayerInteractItemC2SPacket(Hand.OFF_HAND, i, player.getYaw(), player.getPitch());
                });
                player.fallDistance = 0.0f;
            }
        });
        register(PacketSendEvent.class, class037Var -> {
            if (isState() && this.mc.isWorldLoaded()) {
                ClientPlayerEntity player = this.mc.getPlayer();
                if ((class037Var.getPacket()) instanceof PlayerMoveC2SPacket packet ) {
                    PlayerMoveC2SPacket playerMoveC2SPacket = packet;
                    switch ((NoFallMode) this.modeSetting.currentValue()) {
                        case SPOOF_GROUND:
                            if (player.fallDistance >= 1.7f) {
                                playerMoveC2SPacket.onGround = true;
                            }
                            break;
                        case NO_GROUND:
                            playerMoveC2SPacket.onGround = false;
                            break;
                    }
                }
            }
        });
    }
}
