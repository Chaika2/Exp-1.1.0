package ru.expensive.mixin;

import aethereal.util.PinnedServersController;
import aethereal.Expensive;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerServerListWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({MultiplayerScreen.class})
public class MultiplayerScreenMixin {

    @Shadow
    protected MultiplayerServerListWidget field_3043;

    @Shadow
    private ButtonWidget field_3041;

    @Shadow
    private ButtonWidget field_3047;

    @Inject(method = {"updateButtonActivationStates"}, at = {@At("TAIL")})
    private void expensive$disableButtonsForPinned(CallbackInfo callbackInfo) {
        if (this.field_3043.getSelectedOrNull() instanceof MultiplayerServerListWidget.ServerEntry serverEntry) {
            if (Expensive.INSTANCE.pinnedServersController().isPinned(serverEntry.getServer().address)) {
                this.field_3041.active = false;
                this.field_3047.active = false;
            }
        }
    }

    @Inject(method = {"removeEntry"}, at = {@At("HEAD")}, cancellable = true)
    public void expensive$removeEntry(boolean z, CallbackInfo callbackInfo) {
        PinnedServersController class028VarPinnedServersController = Expensive.INSTANCE.pinnedServersController();
        if (z && (this.field_3043.getSelectedOrNull() instanceof MultiplayerServerListWidget.ServerEntry serverEntry) && class028VarPinnedServersController.isPinned(serverEntry.getServer().address)) {
            callbackInfo.cancel();
        }
    }

    @Inject(method = {"editEntry"}, at = {@At("HEAD")}, cancellable = true)
    public void expensive$editEntry(boolean z, CallbackInfo callbackInfo) {
        PinnedServersController class028VarPinnedServersController = Expensive.INSTANCE.pinnedServersController();
        if (z && (this.field_3043.getSelectedOrNull() instanceof MultiplayerServerListWidget.ServerEntry serverEntry) && class028VarPinnedServersController.isPinned(serverEntry.getServer().address)) {
            callbackInfo.cancel();
        }
    }
}
