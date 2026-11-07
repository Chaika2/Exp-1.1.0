package aethereal.ui;
import aethereal.event.ClientListener;
import aethereal.module.EnderChestPlusModule;
import aethereal.Expensive;
import aethereal.type.Mc;
import aethereal.event.ScreenOpenEvent;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;

public class EnderChestScreenRedirect implements ClientListener {
    public EnderChestScreenRedirect() {
        Expensive.INSTANCE.eventDispatcher().register(ScreenOpenEvent.class, class135Var -> {
            EnderChestPlusModule class491Var = (EnderChestPlusModule) Expensive.INSTANCE.moduleRepository().get(EnderChestPlusModule.class);
            if (Mc.INSTANCE.isWorldLoaded()) {
                MinecraftClient minecraft = Mc.INSTANCE.getMinecraft();
                net.minecraft.client.gui.screen.Screen genericContainerScreenScreen = class135Var.screen();
                if ((genericContainerScreenScreen instanceof InventoryScreen) && class491Var.getScreen() == null) {
                    minecraft.setScreen(new InventoryActionsScreen(minecraft.player));
                    class135Var.cancel();
                }
                if (genericContainerScreenScreen instanceof GenericContainerScreen) {
                    GenericContainerScreen genericContainerScreen = (GenericContainerScreen) genericContainerScreenScreen;
                    minecraft.setScreen(new ContainerActionsScreen(genericContainerScreen.getScreenHandler(), minecraft.player.getInventory(), genericContainerScreen.getTitle()));
                    class135Var.cancel();
                }
            }
        });
    }
}
