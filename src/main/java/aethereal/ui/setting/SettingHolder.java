package aethereal.ui.setting;

import com.google.common.collect.Lists;
import java.util.Arrays;
import java.util.List;

public class SettingHolder {
    public final List<Setting> settings = Lists.newArrayList();

    public void addSettings(Setting... class661VarArr) {
        this.settings.addAll(Arrays.asList(class661VarArr));
    }

    public Setting get(String str) {
        return this.settings.stream().filter(class661Var -> {
            return class661Var.getName().effective().equalsIgnoreCase(str);
        }).findFirst().orElse(null);
    }

    public List<Setting> getSettings() {
        return this.settings;
    }
}
