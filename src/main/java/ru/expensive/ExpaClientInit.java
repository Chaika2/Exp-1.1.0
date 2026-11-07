package ru.expensive;

import aethereal.ExpensiveBootstrap;
import net.fabricmc.api.ClientModInitializer;

public class ExpaClientInit implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ExpensiveBootstrap.init();
    }
}
