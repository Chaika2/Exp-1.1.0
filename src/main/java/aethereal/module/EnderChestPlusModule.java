package aethereal.module;
import aethereal.event.CloseScreenEvent;
import aethereal.ui.InventoryActionsScreen;
import aethereal.type.Mc;
import aethereal.ui.ModuleTab;
import aethereal.event.PacketReceiveEvent;
import aethereal.event.PacketSendEvent;
import aethereal.util.PlayerActionUtil;
import aethereal.event.ScreenOpenEvent;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.runtime.SwitchBootstraps;
import java.util.Objects;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.s2c.play.CloseScreenS2CPacket;
import net.minecraft.network.packet.s2c.play.GameJoinS2CPacket;
import net.minecraft.network.packet.s2c.play.OpenScreenS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerRespawnS2CPacket;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;

public class EnderChestPlusModule extends Module {
    public HandledScreen<?> screen;
    public final Mc mc;

    public EnderChestPlusModule() {
        super(ModuleTab.MISC, "Ender Chest Plus");
        this.mc = Mc.INSTANCE;
        register(PacketSendEvent.class, class037Var -> {
            if (isState() && this.mc.isWorldLoaded() && this.screen != null) {
                net.minecraft.network.packet.Packet<?> packet = class037Var.getPacket();
                Objects.requireNonNull(packet);
                if (packet instanceof PlayerActionC2SPacket) {
                    if (((PlayerActionC2SPacket) packet).getAction().equals(PlayerActionC2SPacket.Action.SWAP_ITEM_WITH_OFFHAND)) {
                        PlayerActionUtil.INSTANCE.swapHand(PlayerActionUtil.INSTANCE.mainHandSlot().id, Hand.OFF_HAND);
                        class037Var.cancel();
                    }
                } else if (packet instanceof CloseHandledScreenC2SPacket) {
                    class037Var.cancel();
                } else if (packet instanceof CloseScreenS2CPacket) {
                    class037Var.cancel();
                }
            }
        });
        register(PacketReceiveEvent.class, class051Var -> {
            if (isState() && this.mc.isWorldLoaded() && this.screen != null) {
                net.minecraft.network.packet.Packet<?> packet = class051Var.getPacket();
                Objects.requireNonNull(packet);
                if (packet instanceof CloseScreenS2CPacket) {
                    class051Var.cancel();
                } else if (packet instanceof GameJoinS2CPacket) {
                    deactivate();
                } else if (packet instanceof OpenScreenS2CPacket) {
                    deactivate();
                } else if (packet instanceof PlayerRespawnS2CPacket) {
                    deactivate();
                } else if (packet instanceof PlayerActionC2SPacket) {
                    if (((PlayerActionC2SPacket) packet).getAction().equals(PlayerActionC2SPacket.Action.SWAP_ITEM_WITH_OFFHAND)) {
                        PlayerActionUtil.INSTANCE.swapHand(PlayerActionUtil.INSTANCE.mainHandSlot().id, Hand.OFF_HAND);
                        class051Var.cancel();
                    }
                }
            }
        });
        register(ScreenOpenEvent.class, class135Var -> {
            if (isState() && this.mc.isWorldLoaded()) {
                if (((class135Var.screen() instanceof InventoryActionsScreen) || (class135Var.screen() instanceof InventoryScreen)) && this.screen != null) {
                    Mc.INSTANCE.getMinecraft().setScreen(this.screen);
                    class135Var.cancel();
                }
            }
        });
        register(CloseScreenEvent.class, class270Var -> {
            if (isState() && this.mc.isWorldLoaded()) {
                if ((class270Var.getScreen()) instanceof GenericContainerScreen screen ) {
                    GenericContainerScreen genericContainerScreen = screen;
                    if (genericContainerScreen.getTitle().getString().contains(Text.translatable("container.enderchest").getString())) {
                        this.screen = genericContainerScreen;
                    }
                }
                if (this.screen != null) {
                    Mc.INSTANCE.getMinecraft().setScreen((Screen) null);
                    class270Var.cancel();
                }
            }
        });
    }

    @Override
    public void deactivate() {
        if (this.screen != null && this.mc.isWorldLoaded()) {
            this.screen = null;
            this.mc.getNetworkHandler().sendPacket(new CloseHandledScreenC2SPacket(this.mc.getPlayer().currentScreenHandler.syncId));
        }
        super.deactivate();
    }

    public HandledScreen<?> getScreen() {
        return this.screen;
    }
}
