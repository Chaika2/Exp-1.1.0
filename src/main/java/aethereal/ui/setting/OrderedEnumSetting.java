package aethereal.ui.setting;
import aethereal.model.DisplayNamed;
import aethereal.model.Translation;

import java.lang.Enum;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

public class OrderedEnumSetting<E extends Enum<E>> extends Setting {
    public E[] options;
    public final Set<E> selected;
    public final List<E> order;

    public OrderedEnumSetting(Translation class254Var) {
        super(class254Var, null);
        this.selected = new HashSet();
        this.order = new ArrayList();
    }

    public OrderedEnumSetting(Translation class254Var, Translation class254Var2) {
        super(class254Var, class254Var2);
        this.selected = new HashSet();
        this.order = new ArrayList();
    }

    public OrderedEnumSetting<E> values(Class<E> cls) {
        E[] enumConstants = cls.getEnumConstants();
        if (enumConstants == null || enumConstants.length == 0) {
            throw new IllegalArgumentException("Enum " + cls.getSimpleName() + " пуст.");
        }
        this.options = enumConstants;
        if (this.order.isEmpty()) {
            this.order.addAll(Arrays.asList(enumConstants));
        }
        return this;
    }

    @SafeVarargs
    public final OrderedEnumSetting<E> selected(E... eArr) {
        for (E e : eArr) {
            validateValue(e);
            this.selected.add(e);
        }
        return this;
    }

    public Set<E> getSelected() {
        return this.selected;
    }

    @SafeVarargs
    public final OrderedEnumSetting<E> ordered(E... eArr) {
        this.order.clear();
        this.order.addAll(Arrays.asList(eArr));
        return this;
    }

    public OrderedEnumSetting<E> visible(Supplier<Boolean> supplier) {
        setVisible(supplier);
        return this;
    }

    public List<E> orderedValues() {
        return Collections.unmodifiableList(this.order);
    }

    public void move(int i, int i2) {
        if (i == i2) {
            return;
        }
        this.order.add(i2, this.order.remove(i));
    }

    public void select(E e) {
        validateValue(e);
        this.selected.add(e);
    }

    public void deselect(E e) {
        this.selected.remove(e);
    }

    public boolean isSelected(E e) {
        return this.selected.contains(e);
    }

    public Translation optionName(E e) {
        return e instanceof DisplayNamed ? ((DisplayNamed) e).getDisplayName() : Translation.clearText(e.name());
    }

    public void validateValue(E e) {
        if (this.options == null) {
            throw new IllegalStateException("Сначала вызови values()!");
        }
        for (E e2 : this.options) {
            if (e2 == e) {
                return;
            }
        }
        throw new IllegalArgumentException("Недопустимое значение enum: " + String.valueOf(e));
    }

    @Override
    public Map<String, Object> toSerializedData() {
        ArrayList arrayList = new ArrayList();
        Iterator<E> it = this.selected.iterator();
        while (it.hasNext()) {
            arrayList.add(it.next().name());
        }
        ArrayList arrayList2 = new ArrayList();
        Iterator<E> it2 = this.order.iterator();
        while (it2.hasNext()) {
            arrayList2.add(it2.next().name());
        }
        return Map.of("selected", arrayList, "order", arrayList2);
    }

    @Override
    public void loadSerializedData(Map<String, Object> map) {
        Class<E> declaringClass = this.options[0].getDeclaringClass();
        Object obj = map.get("selected");
        if (obj instanceof List) {
            this.selected.clear();
            for (Object obj2 : (List) obj) {
                if (obj2 instanceof String) {
                    try {
                        this.selected.add((E) Enum.valueOf(declaringClass, (String) obj2));
                    } catch (IllegalArgumentException e) {
                    }
                }
            }
        }
        Object obj3 = map.get("order");
        if (obj3 instanceof List) {
            this.order.clear();
            for (Object obj4 : (List) obj3) {
                if (obj4 instanceof String) {
                    try {
                        this.order.add((E) Enum.valueOf(declaringClass, (String) obj4));
                    } catch (IllegalArgumentException e2) {
                    }
                }
            }
        }
    }

    public E[] options() {
        return this.options;
    }

    public List<E> order() {
        return this.order;
    }

    public OrderedEnumSetting<E> options(E[] eArr) {
        this.options = eArr;
        return this;
    }
}
