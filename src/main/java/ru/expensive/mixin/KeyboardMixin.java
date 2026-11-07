package ru.expensive.mixin;

import aethereal.event.KeyInputEvent;
import aethereal.type.KeyPressState;
import aethereal.Expensive;
import aethereal.type.Mc;
import net.minecraft.client.Keyboard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({Keyboard.class})
public class KeyboardMixin {
    @Inject(method = {"onKey"}, at = {@At("HEAD")})
    private void onKey(long j, int i, int i2, int i3, int i4, CallbackInfo callbackInfo) {
        KeyPressState class050Var;
        Mc class815Var = Mc.INSTANCE;
        if (j == class815Var.getWindow().getHandle() && class815Var.getCurrentScreen() == null) {
            switch (i3) {
                case 0:
                    class050Var = KeyPressState.RELEASE;
                    break;
                case 1:
                    class050Var = KeyPressState.PRESS;
                    break;
                default:
                    class050Var = null;
                    break;
            }
            KeyPressState class050Var2 = class050Var;
            if (class050Var2 == null || i == -1) {
                return;
            }
            Expensive.INSTANCE.eventDispatcher().dispatch(new KeyInputEvent(class050Var2, i));
        }
    }
}
