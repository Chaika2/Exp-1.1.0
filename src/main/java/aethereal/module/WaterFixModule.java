package aethereal.module;

import aethereal.annotation.Aliases;
import aethereal.model.Translation;
import aethereal.ui.ModuleTab;
import aethereal.ui.setting.BooleanSetting;

/**
 * Обход ложного срабатывания серверного античита на Depth Strider.
 *
 * На mlegacy.ru в воде с ботинками на Depth Strider ("Подводная ходьба")
 * приходит предупреждение "Проверьте настройки клиента. FastMath или
 * несовместимые модификации могут создавать неверное движение". Проверено
 * на живом сервере: без ботинок предупреждения нет, с ботинками - есть,
 * а со щитом в руке (скорость падает в 5 раз) - тоже нет. Значит это
 * speed/prediction-чек: античит не учитывает WATER_MOVEMENT_EFFICIENCY,
 * который даёт Depth Strider, и видит клиент вдвое быстрее ожидаемого.
 *
 * Лечится только скоростью: клиент перестаёт применять Depth Strider
 * именно в воде (суша, прыжки и всё остальное не меняются). Включается
 * по умолчанию, потому что цель - не получать флаги; если скорость в воде
 * важнее предупреждения - настройку можно выключить.
 */
@Aliases(aliases = {"Water Fix", "WaterSafe", "Depth Strider Fix", "Water Speed Limit"})
public class WaterFixModule extends Module {
    /**
     * Значение WATER_MOVEMENT_EFFICIENCY, которое реально ушло в расчёт
     * travelInFluid на прошлом тике (-1/NaN = подмена ещё не срабатывала).
     * Нужно только для диагностики: по нему видно, применился ли миксин.
     */
    public static volatile double lastWaterEfficiency = Double.NaN;

    public final BooleanSetting ignoreDepthStrider;

    public WaterFixModule() {
        super(ModuleTab.MOVEMENT, "Water Fix");
        this.ignoreDepthStrider = new BooleanSetting(
                Translation.clearText("Без Depth Strider в воде"),
                Translation.clearText("Двигаться в воде так, как будто на ботинках нет Depth Strider. Убирает ложный флаг античита на скорость в воде."))
                .setValue(true);
        addSettings(this.ignoreDepthStrider);
        // Включён по умолчанию: иначе предупреждение античита останется.
        setStateSilent(true);
    }

    /**
     * Активно ли ограничение водной скорости (Depth Strider + спринт).
     */
    public boolean isWaterSpeedLimited() {
        return isState() && this.ignoreDepthStrider.isValue();
    }
}
