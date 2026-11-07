package aethereal.module;
import aethereal.util.ActionScheduler;
import aethereal.annotation.Aliases;
import aethereal.Lang;
import aethereal.type.Mc;
import aethereal.ui.ModuleTab;
import aethereal.ui.setting.NumberSetting;
import aethereal.net.PacketSender;
import aethereal.event.PlayerTickEvent;
import aethereal.type.SettingUnit;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

@Aliases(aliases = {"Fast Bow", "Quick Bow", "Rapid Bow", "Bow Speed", "Fast Shooting", "Bow Spam", "Quick Arrow", "Rapid Fire", "Bow Booster", "Auto Bow"})
public class FastBowModule extends Module {
    public final NumberSetting ticksSetting;

    public final ActionScheduler scheduler;

    public FastBowModule() {
        super(ModuleTab.COMBAT, "Fast Bow");
        this.ticksSetting = new NumberSetting(Lang.COMBAT_FASTBOW_TICKS, Lang.COMBAT_FASTBOW_TICKS_DESC).currentValue(1.0f).range(1.0f, 15.0f).unit(SettingUnit.TICKS).step(1.0f);
        this.scheduler = new ActionScheduler();
        addSettings(this.ticksSetting);
        register(PlayerTickEvent.class, class130Var -> {
            if (class130Var.isPre()) {
                this.scheduler.update().cleanupIfFinished();
                if (isState() && Mc.INSTANCE.isWorldLoaded()) {
                    ClientPlayerEntity player = Mc.INSTANCE.getPlayer();
                    if ((player.getMainHandStack().getItem() == Items.BOW || player.getOffHandStack().getItem() == Items.BOW) && player.isUsingItem() && player.getItemUseTime() >= this.ticksSetting.currentValue() && this.scheduler.isFinished()) {
                        Hand hand = player.getOffHandStack().getItem() == Items.BOW ? Hand.OFF_HAND : Hand.MAIN_HAND;
                        PacketSender.sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, Direction.DOWN));
                        player.stopUsingItem();
                        this.scheduler.addStep(0, () -> {
                            PacketSender.sendSequencedPacket(i -> {
                                return new PlayerInteractItemC2SPacket(hand, i, player.getYaw(), player.getPitch());
                            });
                        });
                    }
                }
            }
        });
    }
}
