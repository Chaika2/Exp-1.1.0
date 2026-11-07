package ru.expensive.mixin;

import aethereal.util.StencilBufferUtil;
import aethereal.util.PinnedServersController;
import aethereal.model.PinnedServerEntry;
import aethereal.Expensive;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerServerListWidget;
import net.minecraft.client.network.ServerInfo;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({MultiplayerServerListWidget.ServerEntry.class})
public class MultiplayerServerListWidget$ServerEntryMixin {

    @Shadow
    @Final
    private ServerInfo field_19120;

    @Shadow
    @Final
    private MinecraftClient field_19119;

    @Inject(method = {"render"}, at = {@At("TAIL")})
    private void expensive$renderPinned(DrawContext drawContext, int i, int i2, int i3, int i4, int i5, int i6, int i7, boolean z, float f, CallbackInfo callbackInfo) {
        PinnedServersController class028VarPinnedServersController = Expensive.INSTANCE.pinnedServersController();
        if (class028VarPinnedServersController == null) {
            return;
        }
        String str = this.field_19120.address;
        if (class028VarPinnedServersController.isPinned(str)) {
            int iExpensive$getAccentColor = expensive$getAccentColor(class028VarPinnedServersController, str);
            if (iExpensive$getAccentColor == 0) {
                iExpensive$getAccentColor = 52945;
            }
            int iExpensive$withAlpha = expensive$withAlpha(iExpensive$getAccentColor, z ? 53 : 34);
            int iExpensive$withAlpha2 = expensive$withAlpha(iExpensive$getAccentColor, 204);
            drawContext.fill(i3, i2, i3 + i4, i2 + i5, iExpensive$withAlpha);
            drawContext.fill(i3, i2, i3 + 3, i2 + i5, iExpensive$withAlpha2);
        }
    }

    @Unique
    private static int expensive$getAccentColor(PinnedServersController class028Var, String str) {
        String strExpensive$normalize = expensive$normalize(str);
        for (PinnedServerEntry class029Var : class028Var.favorites()) {
            if (expensive$normalize(class029Var.address()).equals(strExpensive$normalize)) {
                return class029Var.accentColor();
            }
        }
        return 0;
    }

    @Unique
    private static String expensive$normalize(String str) {
        if (str == null) {
            return "";
        }
        String strTrim = str.trim();
        return !strTrim.contains(":") ? strTrim + ":25565" : strTrim;
    }

    @Unique
    private static int expensive$withAlpha(int i, int i2) {
        return ((i2 & StencilBufferUtil.STENCIL_MASK) << 24) | (i & 16777215);
    }
}
