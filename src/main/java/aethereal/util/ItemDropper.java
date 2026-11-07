package aethereal.util;

import net.minecraft.item.Item;
import net.minecraft.screen.ScreenHandler;

public interface ItemDropper {
    void dropItems(ScreenHandler screenHandler, int i, Item item);
}
