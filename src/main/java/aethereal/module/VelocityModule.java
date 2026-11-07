package aethereal.module;
import aethereal.Lang;
import aethereal.type.Mc;
import aethereal.ui.setting.ModeSetting;
import aethereal.ui.ModuleTab;
import aethereal.event.MovementInputEvent;
import aethereal.event.PacketReceiveEvent;
import aethereal.net.PacketSender;
import aethereal.event.PlayerTickEvent;
import aethereal.type.VelocityMode;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class VelocityModule extends Module {
    ModeSetting<VelocityMode> mode;
    final Mc mc;
    public boolean verticalKnockback;
    public int grimTicks;
    boolean pendingGrim;

    public VelocityModule() {
        super(ModuleTab.COMBAT, "Velocity");
        this.mode = new ModeSetting(Lang.MODE).values(VelocityMode.class);
        this.mc = Mc.INSTANCE;
        this.verticalKnockback = false;
        this.grimTicks = 0;
        addSettings(this.mode);
        register(PlayerTickEvent.class, class130Var -> {
            this.grimTicks--;
            if (isState() && this.mc.isWorldLoaded() && this.mode.isSelected(VelocityMode.GRIM) && this.pendingGrim) {
                ClientPlayerEntity player = this.mc.getPlayer();
                BlockPos blockPos = player.getBlockPos();
                PacketSender.sendPacket(new PlayerMoveC2SPacket.Full(player.getX(), player.getY(), player.getZ(), player.getYaw(), player.getPitch(), player.isOnGround(), player.horizontalCollision));
                PacketSender.sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, blockPos, Direction.UP));
                this.pendingGrim = false;
            }
        });
        register(PacketReceiveEvent.class, class051Var -> {
            if (isState() && this.mc.isWorldLoaded()) {
                if ((class051Var.getPacket()) instanceof EntityVelocityUpdateS2CPacket packet ) {
                    EntityVelocityUpdateS2CPacket entityVelocityUpdateS2CPacket = packet;
                    if (entityVelocityUpdateS2CPacket.getEntityId() == this.mc.getPlayer().getId()) {
                        switch (((VelocityMode) this.mode.currentValue()).ordinal()) {
                            case 0:
                                class051Var.cancel();
                                break;
                            case 1:
                                this.verticalKnockback = entityVelocityUpdateS2CPacket.getVelocityX() / 8000.0d == 0.0d && entityVelocityUpdateS2CPacket.getVelocityZ() / 8000.0d == 0.0d && entityVelocityUpdateS2CPacket.getVelocityY() / 8000.0d < 0.0d;
                                break;
                            case 2:
                                if (this.grimTicks >= 2) {
                                    return;
                                }
                                class051Var.cancel();
                                this.pendingGrim = true;
                                break;
                        }
                    } else {
                        return;
                    }
                }
                if ((class051Var.getPacket() instanceof PlayerPositionLookS2CPacket) && this.mode.isSelected(VelocityMode.GRIM)) {
                    this.grimTicks = 3;
                }
            }
        });
        register(MovementInputEvent.class, class040Var -> {
            if (isState() && this.mc.isWorldLoaded() && this.mode.isSelected(VelocityMode.JUMP_RESET)) {
                ClientPlayerEntity player = this.mc.getPlayer();
                if (player.hurtTime == 9 && player.isOnGround() && player.isSprinting() && !this.verticalKnockback) {
                    class040Var.setJumping(true);
                }
            }
        });
    }

    @Override
    public void deactivate() {
        this.pendingGrim = false;
        this.grimTicks = 0;
        this.verticalKnockback = false;
        super.deactivate();
    }
}
