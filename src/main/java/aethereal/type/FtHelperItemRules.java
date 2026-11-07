package aethereal.type;
import aethereal.ui.setting.KeybindSetting;

import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.item.Item;
import net.minecraft.item.Items;

public class FtHelperItemRules {
    public final Map<KeybindSetting, ItemSearchRule> rules = new LinkedHashMap();

    public FtHelperItemRules addByName(KeybindSetting class663Var, String str, Item item) {
        this.rules.put(class663Var, new ItemSearchRule(true, str, item, false));
        return this;
    }

    public FtHelperItemRules addPotionByName(KeybindSetting class663Var, String str, boolean z) {
        this.rules.put(class663Var, new ItemSearchRule(true, str, Items.SPLASH_POTION, z));
        return this;
    }

    public FtHelperItemRules addByItemFlagged(KeybindSetting class663Var, Item item, boolean z) {
        this.rules.put(class663Var, new ItemSearchRule(false, null, item, z));
        return this;
    }

    public FtHelperItemRules addByItem(KeybindSetting class663Var, Item item) {
        this.rules.put(class663Var, new ItemSearchRule(false, null, item, false));
        return this;
    }
}
