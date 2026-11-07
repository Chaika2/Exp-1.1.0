package aethereal.ui.setting;
import aethereal.model.DisplayNamed;
import aethereal.model.Translation;

import java.lang.Enum;
import java.util.Map;
import java.util.function.Supplier;

public class ModeSetting<T extends Enum<T>> extends Setting {
    public T[] options;

    public T currentValue;
    public Runnable action;

    public ModeSetting(Translation class254Var, Translation class254Var2) {
        super(class254Var, class254Var2);
    }

    public ModeSetting(Translation class254Var) {
        this(class254Var, null);
    }

    public ModeSetting<T> values(Class<T> cls) {
        T[] enumConstants = cls.getEnumConstants();
        if (enumConstants == null || enumConstants.length == 0) {
            throw new IllegalArgumentException("Enum class " + cls.getSimpleName() + " has no constants.");
        }
        this.options = enumConstants;
        this.currentValue = enumConstants[0];
        return this;
    }

    public ModeSetting<T> visible(Supplier<Boolean> supplier) {
        setVisible(supplier);
        return this;
    }

    public ModeSetting<T> select(T t) {
        if (!isValidOption(t)) {
            throw new IllegalArgumentException("Invalid enum value: " + String.valueOf(t));
        }
        this.currentValue = t;
        return this;
    }

    public void setCurrentValue(T t) {
        if (!isValidOption(t)) {
            throw new IllegalArgumentException("Invalid enum value: " + String.valueOf(t));
        }
        this.currentValue = t;
    }

    public void startAction() {
        if (this.action != null) {
            this.action.run();
        }
    }

    public int selectedIndex() {
        for (int i = 0; i < this.options.length; i++) {
            if (this.options[i] == this.currentValue) {
                return i;
            }
        }
        throw new IllegalStateException("Current value is not in options list.");
    }

    public boolean isSelected(T t) {
        return this.currentValue == t;
    }

    public boolean isValidOption(T t) {
        for (T t2 : this.options) {
            if (t2 == t) {
                return true;
            }
        }
        return false;
    }

    public Translation optionName(T t) {
        if (t instanceof DisplayNamed) {
            return ((DisplayNamed) t).getDisplayName();
        }
        throw new IllegalArgumentException(String.valueOf(t) + " called without displayable");
    }

    @Override
    public Map<String, Object> toSerializedData() {
        return Map.of("value", this.currentValue.name());
    }

    @Override
    public void loadSerializedData(Map<String, Object> map) {
        Object obj = map.get("value");
        if (obj instanceof String) {
            try {
                select(Enum.valueOf(this.currentValue.getDeclaringClass(), (String) obj));
            } catch (IllegalArgumentException e) {
            }
        }
    }

    public T[] options() {
        return this.options;
    }

    public T currentValue() {
        return this.currentValue;
    }

    public Runnable action() {
        return this.action;
    }

    public ModeSetting<T> options(T[] tArr) {
        this.options = tArr;
        return this;
    }

    public ModeSetting<T> currentValue(T t) {
        this.currentValue = t;
        return this;
    }

    public ModeSetting<T> action(Runnable runnable) {
        this.action = runnable;
        return this;
    }
    public T getValue() {
        return this.currentValue;
    }
}
