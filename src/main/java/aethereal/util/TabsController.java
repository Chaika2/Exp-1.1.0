package aethereal.util;
import aethereal.Lang;
import aethereal.ui.MenuTabElement;
import aethereal.type.ModuleCategory;
import aethereal.ui.ModuleGridLayout;
import aethereal.ui.ModuleTab;

import java.util.Collections;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TabsController {
    public final MenuTabElement combat = new MenuTabElement(ModuleTab.COMBAT, Lang.COMBAT, "combat", new ModuleGridLayout());
    public final MenuTabElement render = new MenuTabElement(ModuleTab.RENDER, Lang.RENDER, "render", new ModuleGridLayout());
    public final MenuTabElement player = new MenuTabElement(ModuleTab.PLAYER, Lang.PLAYER, "player", new ModuleGridLayout());
    public final MenuTabElement movement = new MenuTabElement(ModuleTab.MOVEMENT, Lang.MOVEMENT, "movement", new ModuleGridLayout());
    public final MenuTabElement misc = new MenuTabElement(ModuleTab.MISC, Lang.MISC, "misc", new ModuleGridLayout());
    public final MenuTabElement theme = new MenuTabElement(null, Lang.THEMES, "themes");
    public final MenuTabElement config = new MenuTabElement(null, Lang.CONFIGS, "configs");
    public final List<MenuTabElement> tabElements = List.of(this.combat, this.movement, this.player, this.render, this.misc, this.config, this.theme);
    public final Map<ModuleTab, MenuTabElement> tabByModuleTab = Map.of(ModuleTab.COMBAT, this.combat, ModuleTab.MOVEMENT, this.movement, ModuleTab.PLAYER, this.player, ModuleTab.RENDER, this.render, ModuleTab.MISC, this.misc);

    public final EnumMap<ModuleTab, Set<ModuleCategory>> selectedCategories = new EnumMap<>(ModuleTab.class);
    public MenuTabElement current = this.combat;
    public MenuTabElement previousTabElement;

    public TabsController() {
        for (ModuleTab class847Var : ModuleTab.values()) {
            this.selectedCategories.put(class847Var, new HashSet());
        }
        this.theme.setActive(false);
    }

    public void open(MenuTabElement class732Var) {
        if (class732Var == null || !class732Var.active()) {
            return;
        }
        this.previousTabElement = this.current;
        this.current = class732Var;
        class732Var.open();
    }

    public void revert() {
        open(this.previousTabElement);
    }

    public MenuTabElement tabElement(ModuleTab class847Var) {
        if (class847Var == null) {
            return null;
        }
        return this.tabByModuleTab.get(class847Var);
    }

    public void ensureCategoryVisible(MenuTabElement class732Var, ModuleCategory class672Var) {
        Set<ModuleCategory> set;
        if (class732Var == null || class672Var == null) {
            return;
        }
        ModuleTab class847VarModuleTab = class732Var.moduleTab();
        if (class672Var.supports(class847VarModuleTab) && (set = this.selectedCategories.get(class847VarModuleTab)) != null && !set.isEmpty() && set.add(class672Var)) {
            applyCategoryFilter(class847VarModuleTab);
        }
    }

    public Set<ModuleCategory> selectedCategories(ModuleTab class847Var) {
        return class847Var == null ? Set.of() : Collections.unmodifiableSet((Set) this.selectedCategories.getOrDefault(class847Var, Set.of()));
    }

    public void toggleCategory(ModuleTab class847Var, ModuleCategory class672Var) {
        if (class847Var == null || class672Var == null || !class672Var.supports(class847Var)) {
            return;
        }
        Set<ModuleCategory> set = this.selectedCategories.get(class847Var);
        if (set.contains(class672Var)) {
            set.remove(class672Var);
        } else {
            set.add(class672Var);
        }
        applyCategoryFilter(class847Var);
    }

    public void applyCategoryFilter(ModuleTab class847Var) {
        MenuTabElement class732VarTabElement = tabElement(class847Var);
        if (class732VarTabElement == null) {
            return;
        }
        class732VarTabElement.applyCategoryFilter(selectedCategories(class847Var));
    }

    public void setActive(MenuTabElement class732Var, boolean z) {
        if (class732Var == null) {
            return;
        }
        class732Var.setActive(z);
    }

    public MenuTabElement combat() {
        return this.combat;
    }

    public MenuTabElement render() {
        return this.render;
    }

    public MenuTabElement player() {
        return this.player;
    }

    public MenuTabElement movement() {
        return this.movement;
    }

    public MenuTabElement misc() {
        return this.misc;
    }

    public MenuTabElement theme() {
        return this.theme;
    }

    public MenuTabElement config() {
        return this.config;
    }

    public Map<ModuleTab, MenuTabElement> tabByModuleTab() {
        return this.tabByModuleTab;
    }

    public EnumMap<ModuleTab, Set<ModuleCategory>> selectedCategories() {
        return this.selectedCategories;
    }

    public MenuTabElement current() {
        return this.current;
    }

    public MenuTabElement previousTabElement() {
        return this.previousTabElement;
    }

    public List<MenuTabElement> tabElements() {
        return this.tabElements;
    }
}
