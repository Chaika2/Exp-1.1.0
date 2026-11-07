package aethereal.ui.setting;
import aethereal.model.Translation;

import java.util.function.Supplier;

public class ButtonSetting extends Setting {
    public Runnable runnable;
    public Translation buttonName;

    public ButtonSetting(Translation class254Var, Translation class254Var2) {
        super(class254Var, class254Var2);
    }

    public ButtonSetting(Translation class254Var) {
        this(class254Var, null);
    }

    public ButtonSetting visible(Supplier<Boolean> supplier) {
        setVisible(supplier);
        return this;
    }

    public Runnable getRunnable() {
        return this.runnable;
    }

    public Translation getButtonName() {
        return this.buttonName;
    }

    public ButtonSetting setRunnable(Runnable runnable) {
        this.runnable = runnable;
        return this;
    }

    public ButtonSetting setButtonName(Translation class254Var) {
        this.buttonName = class254Var;
        return this;
    }
}
