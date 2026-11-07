package aethereal.module;
import aethereal.annotation.Aliases;
import aethereal.Lang;
import aethereal.type.Mc;
import aethereal.ui.ModuleTab;
import aethereal.ui.setting.BooleanSetting;

@Aliases(aliases = {"Item Physic", "Item Physics", "Item Rotate", "Dropped Item Physics"})
public class ItemPhysicsModule extends Module {
    public final BooleanSetting shrinkItems;
    public final Mc mc;

    public ItemPhysicsModule() {
        super(ModuleTab.RENDER, "ItemPhysics");
        this.mc = Mc.INSTANCE;
        this.shrinkItems = new BooleanSetting(Lang.ITEMPHYSICS_SHRINK_ITEMS).setValue(true);
        addSettings(this.shrinkItems);
    }

    public boolean physicsActive() {
        return isState() && this.mc.isWorldLoaded();
    }

    public boolean shrinkActive() {
        return physicsActive() && this.shrinkItems.isValue();
    }
}