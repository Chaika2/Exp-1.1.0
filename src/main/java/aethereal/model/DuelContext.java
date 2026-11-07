package aethereal.model;
import aethereal.type.AutoDuelArmorType;
import aethereal.type.AutoDuelOffhandItem;
import aethereal.type.DuelKitType;
import aethereal.event.ModuleDeactivateCallback;
import aethereal.ui.setting.MultiSelectSetting;
import aethereal.ui.setting.NumberSetting;


public final class DuelContext {
    public final MultiSelectSetting<DuelKitType> kits;
    public final NumberSetting nextDuelDelay;
    public final AutoDuelArmorType armorType;

    public final AutoDuelOffhandItem offhandItem;

    public final ModuleDeactivateCallback callback;

    public DuelContext(MultiSelectSetting<DuelKitType> class671Var, NumberSetting class613Var, AutoDuelArmorType class440Var, AutoDuelOffhandItem class439Var, ModuleDeactivateCallback class443Var) {
        this.kits = class671Var;
        this.nextDuelDelay = class613Var;
        this.armorType = class440Var;
        this.offhandItem = class439Var;
        this.callback = class443Var;
    }

        @Override
    public final String toString() {
        return getClass().getSimpleName() + "[" + "kits=" + this.kits + ", " + "nextDuelDelay=" + this.nextDuelDelay + ", " + "armorType=" + this.armorType + ", " + "offhandItem=" + this.offhandItem + ", " + "callback=" + this.callback + "]";
    }
    @Override
    public final int hashCode() {
        return java.util.Objects.hash(this.kits, this.nextDuelDelay, this.armorType, this.offhandItem, this.callback);
    }
    @Override
    public final boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof DuelContext)) return false;
        DuelContext o = (DuelContext) obj;
        return java.util.Objects.equals(this.kits, o.kits) && java.util.Objects.equals(this.nextDuelDelay, o.nextDuelDelay) && java.util.Objects.equals(this.armorType, o.armorType) && java.util.Objects.equals(this.offhandItem, o.offhandItem) && java.util.Objects.equals(this.callback, o.callback);
    }
public MultiSelectSetting<DuelKitType> kits() {
        return this.kits;
    }

    public NumberSetting nextDuelDelay() {
        return this.nextDuelDelay;
    }

    public AutoDuelArmorType armorType() {
        return this.armorType;
    }

    public AutoDuelOffhandItem offhandItem() {
        return this.offhandItem;
    }

    public ModuleDeactivateCallback callback() {
        return this.callback;
    }
}
