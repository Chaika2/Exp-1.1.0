package aethereal.model;
import aethereal.module.Module;
import aethereal.render.ToggleAnimator;

import java.util.ArrayList;
import java.util.List;

public class HotkeyEntry {
    public final String name;
    public final String moduleName;
    public final List<Integer> keys;
    public final ToggleAnimator anim;

    public HotkeyEntry(String str, List<Integer> list) {
        this.keys = new ArrayList();
        this.anim = ToggleAnimator.times(2, 80);
        this.moduleName = null;
        this.name = str;
        if (list != null) {
            this.keys.addAll(list);
        }
    }

    public HotkeyEntry(Module class605Var, String str, List<Integer> list) {
        this.keys = new ArrayList();
        this.anim = ToggleAnimator.times(2, 80);
        this.moduleName = class605Var != null ? class605Var.getName() : null;
        this.name = str;
        if (list != null) {
            this.keys.addAll(list);
        }
    }

    public boolean hasModule() {
        return this.moduleName != null;
    }

    public String name() {
        return this.name;
    }

    public String moduleName() {
        return this.moduleName;
    }

    public List<Integer> keys() {
        return this.keys;
    }

    public ToggleAnimator anim() {
        return this.anim;
    }
}
