package aethereal.ui.setting;
import aethereal.model.DisplayNamed;
import aethereal.model.Translation;

import java.lang.Enum;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

public class MultiSelectSetting<T extends Enum<T>> extends Setting {
    public Set<T> selectedValues;
    public T[] options;

    public MultiSelectSetting(Translation class254Var, Translation class254Var2) {
        super(class254Var, class254Var2);
        this.selectedValues = new LinkedHashSet();
    }

    public MultiSelectSetting(Translation class254Var) {
        this(class254Var, null);
    }

    public MultiSelectSetting<T> values(Class<T> cls) {
        T[] enumConstants = cls.getEnumConstants();
        if (enumConstants == null || enumConstants.length == 0) {
            throw new IllegalArgumentException("Enum class " + cls.getSimpleName() + " has no constants.");
        }
        this.options = enumConstants;
        return this;
    }

    public MultiSelectSetting<T> visible(Supplier<Boolean> supplier) {
        setVisible(supplier);
        return this;
    }

    @SafeVarargs
    public final MultiSelectSetting<T> select(T... tArr) {
        for (T t : tArr) {
            select(t);
        }
        return this;
    }

    public MultiSelectSetting<T> toggle(T t) {
        if (isSelected(t)) {
            deselect(t);
        } else {
            select(t);
        }
        return this;
    }

    public MultiSelectSetting<T> select(T t) {
        if (!isValidOption(t)) {
            throw new IllegalArgumentException("Invalid enum value: " + String.valueOf(t));
        }
        this.selectedValues.add(t);
        return this;
    }

    public MultiSelectSetting<T> deselect(T... tArr) {
        for (T t : tArr) {
            this.selectedValues.remove(t);
        }
        return this;
    }

    public MultiSelectSetting<T> deselect(T t) {
        this.selectedValues.remove(t);
        return this;
    }

    public boolean isSelected(T t) {
        return this.selectedValues.contains(t);
    }

    public Translation optionName(T t) {
        if (t instanceof DisplayNamed) {
            return ((DisplayNamed) t).getDisplayName();
        }
        throw new IllegalArgumentException(String.valueOf(t) + " called without displayable");
    }

    public boolean isValidOption(T t) {
        return Set.of(this.options).contains(t);
    }

    @Override
    public Map<String, Object> toSerializedData() {
        ArrayList arrayList = new ArrayList();
        Iterator<T> it = this.selectedValues.iterator();
        while (it.hasNext()) {
            arrayList.add(it.next().name());
        }
        return Map.of("selected", arrayList);
    }

    @Override
    public void loadSerializedData(Map<String, Object> map) {
        Object obj = map.get("selected");
        if (obj instanceof List) {
            List list = (List) obj;
            this.selectedValues.clear();
            Class declaringClass = this.options[0].getDeclaringClass();
            for (Object obj2 : list) {
                if (obj2 instanceof String) {
                    try {
                        this.selectedValues.add((T) Enum.valueOf(declaringClass, (String) obj2));
                    } catch (IllegalArgumentException e) {
                    }
                }
            }
        }
    }

    public Set<T> selectedValues() {
        return this.selectedValues;
    }

    public T[] options() {
        return this.options;
    }

    public MultiSelectSetting<T> selectedValues(Set<T> set) {
        this.selectedValues = set;
        return this;
    }

    public MultiSelectSetting<T> options(T[] tArr) {
        this.options = tArr;
        return this;
    }
}
