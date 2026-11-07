package aethereal.module;
import aethereal.annotation.Aliases;
import aethereal.ui.setting.BooleanSetting;
import aethereal.ui.setting.ButtonSetting;
import aethereal.ui.setting.ExpandableSetting;
import aethereal.ui.setting.MultiSelectSetting;
import aethereal.type.EventPriority;
import aethereal.type.SprintIgnore;
import aethereal.Lang;
import aethereal.model.Translation;
import aethereal.type.Mc;
import aethereal.ui.ModuleTab;
import aethereal.event.MovementUpdateEvent;
import aethereal.event.MovementUpdateSource;
import aethereal.event.OverlayEffectEvent;
import aethereal.event.SprintStopEvent;

import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.client.network.ClientPlayerEntity;

@Aliases(aliases = {"Auto Sprint", "Sprint", "Auto Run", "Always Sprint", "Automatic Sprint", "Toggle Sprint"})
public class SprintModule extends Module {
    Mc mc;
    BooleanSetting keepSprintSetting;
    MultiSelectSetting<SprintIgnore> ignoreSetting;

    public SprintModule() {
        super(ModuleTab.MOVEMENT, "Sprint");
        this.mc = Mc.INSTANCE;
        this.keepSprintSetting = new BooleanSetting(Lang.SPRINT_KEEP_SPRINT, Lang.SPRINT_KEEP_SPRINT_DESC);
        this.ignoreSetting = new MultiSelectSetting<SprintIgnore>(Lang.SPRINT_IGNORE)
                .values(SprintIgnore.class)
                .select(SprintIgnore.BLINDNESS, SprintIgnore.SNEAK, SprintIgnore.CRAWL, SprintIgnore.ITEM_USE, SprintIgnore.HORIZONTAL_COLLISION);
        ButtonSetting spookyTimeSetting = new ButtonSetting(Translation.clearText("SpookyTime"), Lang.SPRINT_PRESET_SPOOKYTIME_DESC)
                .setButtonName(Lang.SPRINT_PRESET_APPLY)
                .setRunnable(() -> applyPreset(false, true, true, true, true, true));
        ButtonSetting funtimeSetting = new ButtonSetting(Translation.clearText("Funtime"), Lang.SPRINT_PRESET_FUNTIME_DESC)
                .setButtonName(Lang.SPRINT_PRESET_APPLY)
                .setRunnable(() -> applyPreset(false, true, true, true, true, true));
        ButtonSetting grimSetting = new ButtonSetting(Translation.clearText("Grim"), Lang.SPRINT_PRESET_GRIM_DESC)
                .setButtonName(Lang.SPRINT_PRESET_APPLY)
                .setRunnable(() -> applyPreset(false, false, false, false, false, false));
        ExpandableSetting presetsSetting = new ExpandableSetting(Lang.SPRINT_PRESETS)
                .settings(spookyTimeSetting, funtimeSetting, grimSetting);
        addSettings(presetsSetting, this.ignoreSetting, this.keepSprintSetting);
        register(OverlayEffectEvent.class, class069Var -> {
            if (isState() && this.mc.isWorldLoaded()) {
                if ((passesHungerGate() && class069Var.getType().isHunger()) || (ignoreBlindness() && class069Var.getType().isBlidness())) {
                    class069Var.cancel();
                }
            }
        });
        register(MovementUpdateEvent.class, class308Var -> {
            if (isState() && this.mc.isWorldLoaded() && class308Var.getDirectionalInput().isMoving()) {
                // В жидкости спринт строго ванильный. Модуль не должен ни форсить
                // sprint-бит во входном пакете, ни продлевать бег: серверный
                // prediction считает воду по своему состоянию спринта, и любое
                // расхождение выходит как "FastMath ... неверное движение".
                ClientPlayerEntity player = this.mc.getPlayer();
                if (player != null && (player.isTouchingWater() || player.isSwimming() || player.isInLava())) {
                    return;
                }
                if (isUsingItem() && !ignoreItemUse()) {
                    return;
                }
                if (isSneaking() && !ignoreSneak()) {
                    return;
                }
                if (hasBlindness() && !ignoreBlindness()) {
                    return;
                }
                if (!passesHungerGate() && !hasEnoughFood()) {
                    return;
                }
                boolean z = class308Var.getSource() == MovementUpdateSource.MOVEMENT_TICK;
                if ((class308Var.getSource() == MovementUpdateSource.INPUT) || z) {
                    class308Var.setSprint(true);
                }
            }
        }, EventPriority.LOW);
        register(SprintStopEvent.class, class337Var -> {
            if (isState() && this.mc.isWorldLoaded() && this.keepSprintSetting.isValue()) {
                class337Var.cancel();
            }
        });
    }

    private void applyPreset(boolean hunger, boolean blindness, boolean sneak, boolean crawl, boolean itemUse, boolean horizontalCollision) {
        this.ignoreSetting.deselect(SprintIgnore.values());
        if (hunger) {
            this.ignoreSetting.select(SprintIgnore.HUNGER);
        }
        if (blindness) {
            this.ignoreSetting.select(SprintIgnore.BLINDNESS);
        }
        if (sneak) {
            this.ignoreSetting.select(SprintIgnore.SNEAK);
        }
        if (crawl) {
            this.ignoreSetting.select(SprintIgnore.CRAWL);
        }
        if (itemUse) {
            this.ignoreSetting.select(SprintIgnore.ITEM_USE);
        }
        if (horizontalCollision) {
            this.ignoreSetting.select(SprintIgnore.HORIZONTAL_COLLISION);
        }
    }

    public boolean ignoreHunger() {
        return isState() && this.ignoreSetting.isSelected(SprintIgnore.HUNGER);
    }

    public boolean ignoreBlindness() {
        return isState() && this.ignoreSetting.isSelected(SprintIgnore.BLINDNESS);
    }

    public boolean ignoreSneak() {
        return isState() && this.ignoreSetting.isSelected(SprintIgnore.SNEAK);
    }

    public boolean ignoreCrawl() {
        return isState() && this.ignoreSetting.isSelected(SprintIgnore.CRAWL);
    }

    public boolean ignoreItemUse() {
        return isState() && this.ignoreSetting.isSelected(SprintIgnore.ITEM_USE);
    }

    public boolean ignoreHorizontalCollision() {
        return isState() && this.ignoreSetting.isSelected(SprintIgnore.HORIZONTAL_COLLISION);
    }

    public boolean passesHungerGate() {
        return ignoreHunger() || (ignoreSneak() && isSneaking()) || (ignoreCrawl() && isSwimming());
    }

    public boolean hasEnoughFood() {
        return this.mc.getPlayer().getHungerManager().getFoodLevel() > 6;
    }

    public boolean hasBlindness() {
        return this.mc.getPlayer().hasStatusEffect(StatusEffects.BLINDNESS);
    }

    public boolean isSneaking() {
        return this.mc.getPlayer().isSneaking();
    }

    public boolean isSwimming() {
        return this.mc.getPlayer().isSwimming();
    }

    public boolean isUsingItem() {
        return this.mc.getPlayer().isUsingItem();
    }
}
