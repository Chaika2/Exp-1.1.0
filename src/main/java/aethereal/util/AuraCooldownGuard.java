package aethereal.util;

import aethereal.Expensive;

import aethereal.module.AttackAuraModule;

import java.util.Optional;

// Подавление сброса кулдауна ауры на время чужого действия (боевого модуля-соседа).
//
// Аура (AttackAuraModule) в PacketSendEvent сбрасывает свой
// attackCooldown на КАЖДЫЙ HandSwingC2SPacket и UpdateSelectedSlotC2SPacket - так они
// держат ритм своих ударов. Но те же пакеты шлют и другие модули: бросок зелья из
// AutoPotion (UpdateSelectedSlot слота зелья, свинг от interactItem, возврат слота) и
// бросок перла из TargetPearl. Раньше каждый такой бросок перезаряжал кулдаун ауры на
// полную задержку атаки (10-13 тиков): аура бьёт в ClientTickEvent, а бросок идёт сразу
// после, в PlayerTickEvent, - удар "сгорал", и аура в бою переставала бить, пока шло
// бафание/броски. При повторных бросках (эффект с сервера ещё не применился, а delay
// меньше кулдауна) сбросы шли подряд - аура молчала почти всё время.
//
// Модуль оборачивает своё действие в run(): пакеты, ушедшие внутри окна, кулдаун ауры
// не сбрасывают. Окно живёт строго внутри вызова (try/finally) и захватывает только
// пакеты самого действия - свои удары аура планирует в другом месте тика, ритм её
// ударов не меняется.
public enum AuraCooldownGuard {
    INSTANCE;

    private int depth;

    public boolean isSuppressed() {
        return this.depth > 0;
    }

    public void run(Runnable action) {
        call(() -> {
            action.run();
            return null;
        });
    }

    public <T> T call(java.util.function.Supplier<T> action) {
        this.depth++;
        try {
            return action.get();
        } finally {
            this.depth--;
        }
    }
}
