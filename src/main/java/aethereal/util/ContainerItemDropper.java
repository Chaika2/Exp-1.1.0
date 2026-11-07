package aethereal.util;

import aethereal.Expensive;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.item.Item;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;

public class ContainerItemDropper implements ItemDropper {
    @Override
    public void dropItems(ScreenHandler screenHandler, int i, Item item) {
        if (!(screenHandler instanceof GenericContainerScreenHandler)) {
            return;
        }
        GenericContainerScreenHandler genericContainerScreenHandler = (GenericContainerScreenHandler) screenHandler;
        int size = genericContainerScreenHandler.getInventory().size();
        // Индексы копим заранее: clickSlot меняет хендлер на месте, и проверка
        // содержимого после клика уже не соответствует действительности.
        List<Integer> targets = new ArrayList<>();
        for (int i2 = 0; i2 < size; i2++) {
            Slot slot = genericContainerScreenHandler.slots.get(i2);
            if (slot.isEnabled() && !slot.getStack().isEmpty() && slot.getStack().getItem() == item) {
                targets.add(i2);
            }
        }
        for (int i2 : targets) {
            PlayerActionUtil.INSTANCE.windowClick(SlotActionType.THROW, i2, 1);
        }
        if (!targets.isEmpty()) {
            Expensive.LOGGER.info("dropAllContainer: item={} slots={}", item, targets.size());
        }
    }
}