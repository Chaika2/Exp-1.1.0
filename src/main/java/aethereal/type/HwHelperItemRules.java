package aethereal.type;
import aethereal.ui.setting.KeybindSetting;

import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.item.Item;

public class HwHelperItemRules {
    public final Map<KeybindSetting, ItemSearchRule> rules = new LinkedHashMap();

    public HwHelperItemRules addByName(KeybindSetting class663Var, String str, Item item) {
        this.rules.put(class663Var, new ItemSearchRule(true, str, item, false));
        return this;
    }

    public HwHelperItemRules addByItem(KeybindSetting class663Var, Item item) {
        this.rules.put(class663Var, new ItemSearchRule(false, null, item, false));
        return this;
    }

    public HwHelperItemRules addByItemFlagged(KeybindSetting class663Var, Item item, boolean z) {
        this.rules.put(class663Var, new ItemSearchRule(false, null, item, z));
        return this;
    }
}
