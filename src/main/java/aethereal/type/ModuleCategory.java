package aethereal.type;
import aethereal.resource.ClasspathResource;
import aethereal.render.GlTexture;
import aethereal.Lang;
import aethereal.ui.ModuleTab;
import aethereal.internal.ModuleTabSwitchMap;
import aethereal.model.Translation;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum ModuleCategory {
    ATTACK(Lang.CATEGORY_ATTACK, "/icons/menu/new/tabs/combat.png", ModuleTab.COMBAT),
    DEFENSE(Lang.CATEGORY_DEFENSE, "/icons/menu/new/shield.png", ModuleTab.COMBAT),
    AUTOMATION(Lang.CATEGORY_AUTOMATION, "/icons/menu/new/binary.png", ModuleTab.COMBAT, ModuleTab.PLAYER, ModuleTab.MISC, ModuleTab.MOVEMENT),
    EXPLOITS(Lang.CATEGORY_EXPLOITS, "/icons/menu/new/debug.png", ModuleTab.MOVEMENT, ModuleTab.MISC),
    BOOST(Lang.CATEGORY_BOOST, "/icons/menu/new/bolt.png", ModuleTab.MOVEMENT),
    ANTI_LIMITS(Lang.CATEGORY_ANTI_LIMITS, "/icons/menu/new/cross.png", ModuleTab.MOVEMENT, ModuleTab.PLAYER),
    CONVENIENCE(Lang.CATEGORY_CONVENIENCE, "/icons/menu/new/spark.png", ModuleTab.PLAYER, ModuleTab.MISC, ModuleTab.COMBAT),
    VISUALIZATION(Lang.CATEGORY_VISUALIZATION, "/icons/menu/new/tabs/themes.png", ModuleTab.RENDER),
    INTERFACE(Lang.CATEGORY_INTERFACE, "/icons/menu/new/frame.png", ModuleTab.RENDER),
    WORLD(Lang.CATEGORY_WORLD, "/icons/menu/new/planet.png", ModuleTab.RENDER),
    UTILITIES(Lang.CATEGORY_UTILITIES, "/icons/menu/new/leaf.png", ModuleTab.MISC);

    public final Translation displayName;
    public final String iconPath;
    public GlTexture iconTexture = null;
    public final Set<ModuleTab> moduleTabs;

    ModuleCategory(Translation class254Var, String str, ModuleTab... class847VarArr) {
        this.displayName = class254Var;
        this.iconPath = str;
        this.moduleTabs = Set.of(class847VarArr);
    }

    public String displayName() {
        return this.displayName.effective();
    }

    public GlTexture icon() {
        if (this.iconTexture == null) {
            this.iconTexture = new GlTexture(new ClasspathResource(this.iconPath));
        }
        return this.iconTexture;
    }

    public boolean supports(ModuleTab class847Var) {
        return class847Var != null && this.moduleTabs.contains(class847Var);
    }

    public static List<ModuleCategory> categoriesForTab(ModuleTab class847Var) {
        if (class847Var == null) {
            return Collections.emptyList();
        }
        switch (ModuleTabSwitchMap.tabSwitchMap[class847Var.ordinal()]) {
            case 1:
                return List.of(ATTACK, DEFENSE, AUTOMATION, CONVENIENCE);
            case 2:
                return List.of(EXPLOITS, BOOST, ANTI_LIMITS, AUTOMATION);
            case 3:
                return List.of(AUTOMATION, CONVENIENCE, ANTI_LIMITS);
            case 4:
                return List.of(VISUALIZATION, INTERFACE, WORLD);
            case 5:
                return List.of(AUTOMATION, UTILITIES, CONVENIENCE, EXPLOITS);
            default:
                return Collections.emptyList();
        }
    }

    public static List<ModuleTab> supportedTabs(ModuleCategory class672Var) {
        Stream stream = Arrays.stream(ModuleTab.values());
        Set<ModuleTab> set = class672Var.moduleTabs;
        Objects.requireNonNull(set);
        return (List) stream.filter((v1) -> {
            return set.contains(v1);
        }).collect(Collectors.toList());
    }

    public Translation getDisplayName() {
        return this.displayName;
    }

    public String getIconPath() {
        return this.iconPath;
    }

    public Set<ModuleTab> getModuleTabs() {
        return this.moduleTabs;
    }
}
