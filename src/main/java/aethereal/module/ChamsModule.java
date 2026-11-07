package aethereal.module;
import aethereal.annotation.Aliases;
import aethereal.ui.setting.BooleanSetting;
import aethereal.type.ChamsTargetType;
import aethereal.ui.setting.ColorSetting;
import aethereal.type.EntityCategory;
import aethereal.model.EntityFilter;
import aethereal.Lang;
import aethereal.ui.ModuleTab;
import aethereal.ui.setting.MultiSelectSetting;

import net.minecraft.entity.Entity;

@Aliases(aliases = {"Chams", "Model", "Entity ESP", "ESP"})
public class ChamsModule extends Module {
    public final MultiSelectSetting<ChamsTargetType> selectTargets;
    public final ColorSetting colorSetting;
    public final BooleanSetting blendingSetting;
    public Entity currentEntity;

    public ChamsModule() {
        super(ModuleTab.RENDER, "Chams");
        this.selectTargets = new MultiSelectSetting(Lang.SELECTTARGETS, Lang.SELECTTARGETS_DESC).values(ChamsTargetType.class);
        this.colorSetting = new ColorSetting(Lang.CHAMS_COLOR);
        this.blendingSetting = new BooleanSetting(Lang.CHAMS_BLENDING);
        this.currentEntity = null;
        addSettings(this.selectTargets, this.colorSetting, this.blendingSetting);
    }

    public boolean shouldRender(Entity entity) {
        if (entity == null) {
            return false;
        }
        EntityFilter class095VarMethod001 = buildEntityFilter();
        return !class095VarMethod001.isEmpty() && class095VarMethod001.matches(entity);
    }

    public EntityFilter buildEntityFilter() {
        EntityFilter class095Var = new EntityFilter();
        if (this.selectTargets.isSelected(ChamsTargetType.SELF)) {
            class095Var.add(EntityCategory.SELF);
        }
        if (this.selectTargets.isSelected(ChamsTargetType.PLAYERS)) {
            class095Var.add(EntityCategory.PLAYER);
        }
        if (this.selectTargets.isSelected(ChamsTargetType.FRIENDS)) {
            class095Var.add(EntityCategory.FRIEND);
        }
        if (this.selectTargets.isSelected(ChamsTargetType.MOBS)) {
            class095Var.add(EntityCategory.MOB);
        }
        if (this.selectTargets.isSelected(ChamsTargetType.ANIMALS)) {
            class095Var.add(EntityCategory.ANIMAL);
        }
        return class095Var;
    }

    public MultiSelectSetting<ChamsTargetType> selectTargets() {
        return this.selectTargets;
    }

    public ColorSetting colorSetting() {
        return this.colorSetting;
    }

    public BooleanSetting blendingSetting() {
        return this.blendingSetting;
    }

    public Entity currentEntity() {
        return this.currentEntity;
    }

    public ChamsModule currentEntity(Entity entity) {
        this.currentEntity = entity;
        return this;
    }
}
