package aethereal.module;
import aethereal.annotation.Aliases;
import aethereal.Expensive;
import aethereal.event.CloseScreenEvent;
import aethereal.math.DirectionalInput;
import aethereal.util.GrimDelayHandler;
import aethereal.Lang;
import aethereal.type.Mc;
import aethereal.ui.setting.ModeSetting;
import aethereal.ui.ModuleTab;
import aethereal.util.MovementInputHelper;
import aethereal.event.PacketReceiveEvent;
import aethereal.event.PacketSendEvent;
import aethereal.net.PacketSender;
import aethereal.event.PlayerTickEvent;
import aethereal.type.RotationDispatchMode;
import aethereal.event.ScreenOpenEvent;
import aethereal.util.ServerUtil;
import aethereal.event.SlotClickEvent;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.runtime.SwitchBootstraps;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.AbstractCommandBlockScreen;
import net.minecraft.client.gui.screen.ingame.AnvilScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.gui.screen.ingame.SignEditScreen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;
import net.minecraft.network.packet.s2c.play.CloseScreenS2CPacket;
import net.minecraft.screen.slot.SlotActionType;

@Aliases(aliases = {"Screen Walk", "Screen Move", "Gui Move", "Inventory Move", "Gui Walk", "Move in GUI", "Menu Walk", "Walk in Inventory", "Container Movement", "Walking in GUI"})
public class ScreenWalkModule extends Module {
    public final ModeSetting<RotationDispatchMode> swapMethodSetting;
    public final List<ClickSlotC2SPacket> pendingClicks;
    public final Mc mc;
    public boolean closeOnMove;

    public ScreenWalkModule() {
        super(ModuleTab.MOVEMENT, "Screen Walk");
        this.swapMethodSetting = new ModeSetting(Lang.SWAP_METHOD, Lang.SWAP_METHOD_DESC).values(RotationDispatchMode.class);
        this.pendingClicks = new ArrayList();
        this.mc = Mc.INSTANCE;
        addSettings(this.swapMethodSetting);
        register(PacketSendEvent.class, class037Var -> {
            if (isState() && this.mc.isWorldLoaded() && isNonVanillaMode()) {
                if ((class037Var.getPacket()) instanceof ClickSlotC2SPacket packet ) {
                    ClickSlotC2SPacket clickSlotC2SPacket = packet;
                    ClientPlayerEntity player = Mc.INSTANCE.getPlayer();
                    Expensive.LOGGER.info("[DBG] SW ClickSlot screen={} moving={} pending={}", this.mc.getCurrentScreen(), DirectionalInput.fromPlayerInput(player).isMoving(), this.pendingClicks.size());
                    if ((this.mc.getCurrentScreen() instanceof InventoryScreen) && !DirectionalInput.fromPlayerInput(player).isMoving() && !player.input.playerInput.jump() && this.pendingClicks.isEmpty()) {
                        this.closeOnMove = true;
                    } else if (isMovingInInventory()) {
                        Expensive.LOGGER.info("[DBG] SW CANCEL+queue ClickSlot");
                        this.pendingClicks.add(clickSlotC2SPacket);
                        class037Var.cancel();
                    }
                }
            }
        });
        register(ScreenOpenEvent.class, class135Var -> {
            if (isState() && this.mc.isWorldLoaded() && isNonVanillaMode()) {
                this.mc.getPlayer().currentScreenHandler.setCursorStack(ItemStack.EMPTY);
            }
        });
        register(PacketReceiveEvent.class, class051Var -> {
            if (isState() && this.mc.isWorldLoaded() && isNonVanillaMode()) {
                if ((class051Var.getPacket()) instanceof CloseScreenS2CPacket packet && packet.getSyncId() == 0) {
                    class051Var.cancel();
                }
            }
        });
        register(PlayerTickEvent.class, class130Var -> {
            if (isState() && this.mc.isWorldLoaded() && class130Var.isPre()) {
                ClientPlayerEntity player = this.mc.getPlayer();
                if (this.closeOnMove && (this.mc.getCurrentScreen() instanceof InventoryScreen) && (DirectionalInput.fromPlayerInput(player).isMoving() || player.input.playerInput.jump())) {
                    this.closeOnMove = false;
                    this.mc.setCurrentScreen(null);
                    return;
                }
                if (canWalkInScreen()) {
                    if (!this.pendingClicks.isEmpty() || player.currentScreenHandler.getCursorStack().isEmpty()) {
                        for (KeyBinding keyBinding : MovementInputHelper.getMovementKeys(false, true)) {
                            keyBinding.setPressed(InputUtil.isKeyPressed(this.mc.getWindow().getHandle(), keyBinding.getDefaultKey().getCode()));
                        }
                    }
                }
            }
        });
        register(SlotClickEvent.class, class233Var -> {
            if (isState() && this.mc.isWorldLoaded() && (this.mc.getCurrentScreen() instanceof InventoryScreen) && isMovingInInventory() && ServerUtil.isConnectedToServer("holyworld") && class233Var.getSlotId() == 6 && List.of(SlotActionType.SWAP, SlotActionType.PICKUP).contains(class233Var.getActionType())) {
                class233Var.cancel();
            }
        });
        register(CloseScreenEvent.class, class270Var -> {
            ClientPlayerEntity player = this.mc.getPlayer();
            if (isState() && this.mc.isWorldLoaded() && isNonVanillaMode()) {
                player.currentScreenHandler.setCursorStack(ItemStack.EMPTY);
                if (!this.pendingClicks.isEmpty()) {
                    GrimDelayHandler.addTask(() -> {
                        this.pendingClicks.forEach((v0) -> {
                            PacketSender.sendPacket(v0);
                        });
                        this.pendingClicks.clear();
                        player.networkHandler.sendPacket(new CloseHandledScreenC2SPacket(player.currentScreenHandler.syncId));
                    });
                    this.mc.setCurrentScreen(null);
                    class270Var.cancel();
                } else if (MovementInputHelper.hasPlayerMovement() && (Mc.INSTANCE.getCurrentScreen() instanceof InventoryScreen)) {
                    this.mc.setCurrentScreen(null);
                    class270Var.cancel();
                }
                this.closeOnMove = false;
            }
        });
    }

    @Override
    public void deactivate() {
        this.pendingClicks.clear();
        super.deactivate();
    }

    public boolean isNonVanillaMode() {
        return this.swapMethodSetting.currentValue() != RotationDispatchMode.VANILLA;
    }

    public boolean isMovingInInventory() {
        ClientPlayerEntity player = Mc.INSTANCE.getPlayer();
        return (this.mc.getCurrentScreen() instanceof HandledScreen) && (!this.pendingClicks.isEmpty() || DirectionalInput.fromPlayerInput(player).isMoving() || player.input.playerInput.jump());
    }

    public RotationDispatchMode getSwapMethod() {
        return (RotationDispatchMode) this.swapMethodSetting.currentValue();
    }

    public boolean canWalkInScreen() {
        Screen currentScreen = Mc.INSTANCE.getCurrentScreen();
        return currentScreen != null && !(currentScreen instanceof ChatScreen) && !(currentScreen instanceof SignEditScreen) && !(currentScreen instanceof AnvilScreen) && !(currentScreen instanceof AbstractCommandBlockScreen);
    }
}
