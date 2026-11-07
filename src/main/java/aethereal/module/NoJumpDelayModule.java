package aethereal.module;
import aethereal.annotation.Aliases;
import aethereal.type.Mc;
import aethereal.ui.ModuleTab;
import aethereal.event.PlayerTickEvent;

@Aliases(aliases = {"No Delay", "No Jump Delay", "Anti Jump Delay", "Anti Delay", "Fast Jump", "Quick Jump"})
public class NoJumpDelayModule extends Module {
    public NoJumpDelayModule() {
        super(ModuleTab.MOVEMENT, "No Jump Delay");
        register(PlayerTickEvent.class, class130Var -> {
            Mc class815Var = Mc.INSTANCE;
            if (isState() && class815Var.isWorldLoaded() && !class815Var.getPlayer().getAbilities().flying && class130Var.isPre()) {
                // В воде/лаве обнулять кулдаун нельзя. Ванильный прыжок ставит
                // jumpingCooldown = 10, и серверный prediction этот кулдаун
                // воспроизводит. С нулём клиент прыгает каждый тик, как только
                // касается опоры у воды/на дне, а сервер предсказывает один
                // прыжок на 10 тиков - позиция расходится, и prediction-чек
                // выдаёт предупреждение про FastMath/модификации.
                // Собственно плавание от jumpingCooldown не зависит (подъём
                // в воде считается по input.jump каждый тик), поэтому здесь
                // ничего не теряем.
                if (!class815Var.getPlayer().isTouchingWater()
                        && !class815Var.getPlayer().isSwimming()
                        && !class815Var.getPlayer().isInLava()) {
                    class815Var.getPlayer().jumpingCooldown = 0;
                }
            }
        });
    }
}
