package aethereal.util;

import aethereal.Expensive;
import aethereal.type.Mc;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.item.Item;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.SlotActionType;

/**
 * Выброс всего содержимого. Механика повторяет рабочий вариант из MixinInventoryScreen:
 * цикл крутится в отдельном потоке, каждый клик уходит в MinecraftClient.execute(),
 * между кликами Thread.sleep(50).
 *
 * Почему именно так, а не клики по тику:
 * 1. Thread.sleep в отдельном потоке не блокирует render thread. Наш прежний
 *    вариант печатал System.out.println прямо между кликами - это синхронный
 *    блокирующий I/O, который рвал темп на десятки миллисекунд.
 * 2. execute() ставит клик в очередь задач главного потока, то есть клик уходит
 *    не из игрового тика, а в ближайший кадр. Раньше клик слался строго из
 *    PlayerTickEvent.PRE, то есть в самом начале тика.
 * 3. 50 мс между кликами - это ровно один тик при 20 TPS, то есть 20 кликов в
 *    секунду, темп живого игрока. Замеры стабильно показывают 4 предмета при
 *    любом темпе выше этого: ограничение серверное, обойти его клиентом нельзя,
 *    на каждый стак нужен отдельный ClickSlotC2SPacket.
 */
public class PlayerInventoryDropper implements ItemDropper {
    private static final long DELAY_MS = 50L;

    @Override
    public void dropItems(ScreenHandler screenHandler, int i, Item item) {
        MinecraftClient client = Mc.INSTANCE.getMinecraft();
        if (client == null || client.player == null || client.interactionManager == null) {
            return;
        }
        // В сундуке первыми идут слоты контейнера, дальше - слоты игрока.
        int start = screenHandler instanceof GenericContainerScreenHandler
            ? ((GenericContainerScreenHandler) screenHandler).getInventory().size() : 0;
        int slots = screenHandler.slots.size();
        Thread thread = new Thread(() -> {
            int dropped = 0;
            for (int slot = start; slot < slots; slot++) {
                // Экран закрыли или открыли другой - прекращаем.
                if (client.currentScreen == null || !(client.currentScreen instanceof HandledScreen<?>)
                    || client.player == null
                    || client.player.currentScreenHandler != screenHandler) {
                    break;
                }
                // Пустые слоты пропускаем, но задержку на них не тратим.
                if (screenHandler.slots.get(slot).getStack().isEmpty()) {
                    continue;
                }
                int slotId = slot;
                client.execute(() -> {
                    if (client.player == null || client.interactionManager == null) {
                        return;
                    }
                    // button = 1, а не 0: в ScreenHandler THROW считает
                    // int j = button == 0 ? 1 : stack.getCount(), то есть 0 выбросил бы
                    // ровно один предмет, а 1 - весь стак.
                    client.interactionManager.clickSlot(screenHandler.syncId, slotId, 1, SlotActionType.THROW, client.player);
                });
                dropped++;
                try {
                    Thread.sleep(DELAY_MS);
                } catch (InterruptedException ignored) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
            Expensive.LOGGER.info("dropAll: sent {} clicks range=[{},{})", dropped, start, slots);
        }, "Expensive-DropAll");
        thread.setDaemon(true);
        thread.start();
    }
}