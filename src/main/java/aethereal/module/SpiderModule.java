package aethereal.module;
import aethereal.util.ActionScheduler;
import aethereal.event.BlockCollisionEvent;
import aethereal.type.Mc;
import aethereal.ui.ModuleTab;
import aethereal.event.PacketReceiveEvent;
import aethereal.event.PlayerTickEvent;
import aethereal.math.Stopwatch;

import java.util.concurrent.TimeUnit;
import net.minecraft.block.Blocks;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.util.math.Vec3d;

public class SpiderModule extends Module {
    public final ActionScheduler scheduler;
    public final Stopwatch timer;
    public final Mc mc;

    public SpiderModule() {
        super(ModuleTab.MOVEMENT, "Spider");
        this.scheduler = new ActionScheduler();
        this.timer = new Stopwatch();
        this.mc = Mc.INSTANCE;
        register(PlayerTickEvent.class, class130Var -> {
            if (isState() && this.mc.isWorldLoaded()) {
                this.scheduler.update();
            }
        });
        register(PacketReceiveEvent.class, class051Var -> {
            if (isState() && this.mc.isWorldLoaded() && (class051Var.getPacket() instanceof PlayerPositionLookS2CPacket) && this.timer.hasElapsed(600L, TimeUnit.MILLISECONDS)) {
                this.scheduler.cleanup().addTickStep(0, () -> {
                    this.mc.getPlayer().setOnGround(true);
                    this.mc.getPlayer().setVelocity(new Vec3d(this.mc.getPlayer().getVelocity().x, 0.6d, this.mc.getPlayer().getVelocity().z));
                });
                this.timer.reset();
            }
        });
        register(BlockCollisionEvent.class, class114Var -> {
            if (isState() && this.mc.isWorldLoaded() && class114Var.getPos().getY() >= this.mc.getPlayer().getBlockY() && this.mc.getPlayer().horizontalCollision && this.timer.hasElapsed(600L, TimeUnit.MILLISECONDS)) {
                class114Var.setState(Blocks.AIR.getDefaultState());
            }
        });
    }
}
