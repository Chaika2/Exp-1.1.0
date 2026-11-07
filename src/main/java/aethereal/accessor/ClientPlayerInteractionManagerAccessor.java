package aethereal.accessor;

public interface ClientPlayerInteractionManagerAccessor {
    /**
     * Синхронизирует внутренний lastSelectedSlot с тем слотом, который сервер уже
     * знает. Нужен после ручного UpdateSelectedSlotC2SPacket, иначе следующий
     * syncSelectedSlot() решит, что пакет отправлять не нужно.
     */
    void expensive_ru$setLastSelectedSlot(int slot);

    int expensive_ru$getLastSelectedSlot();
}
