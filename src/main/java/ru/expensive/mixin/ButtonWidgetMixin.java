package ru.expensive.mixin;

import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({ButtonWidget.Builder.class})
public class ButtonWidgetMixin {

    @Shadow
    @Final
    private Text field_40756;

    @Shadow
    private int field_40759;

    @Inject(method = {"position"}, at = {@At("TAIL")})
    private void builderHook(int i, int i2, CallbackInfoReturnable<ButtonWidget.Builder> callbackInfoReturnable) {
        if (this.field_40756.getString().equals("ViaFabricPlus")) {
            this.field_40759 = 5;
        }
    }
}
