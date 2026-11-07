package ru.expensive.mixin;

import aethereal.Expensive;
import aethereal.ui.ExpensiveMainMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
public abstract class TitleScreenMixin extends Screen {
    protected TitleScreenMixin(Text title) {
        super(title);
    }

    @Inject(method = "init", at = @At("HEAD"), cancellable = true)
    private void expensive$replaceTitleScreen(CallbackInfo callbackInfo) {
        if (Expensive.INSTANCE != null && Expensive.INSTANCE.configManager() != null) {
            try {
                Class<?> menuClass = Class.forName("sg.ec.\u04118");
                this.client.setScreen((Screen) menuClass.getConstructor().newInstance());
            } catch (Throwable throwable) {
                this.client.setScreen(new ExpensiveMainMenuScreen());
            }
            callbackInfo.cancel();
        }
    }
}