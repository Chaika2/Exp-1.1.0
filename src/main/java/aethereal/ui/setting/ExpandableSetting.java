package aethereal.ui.setting;
import aethereal.type.BindMode;
import aethereal.model.Translation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class ExpandableSetting extends Setting {
    public List<Setting> serializableChildren;
    public boolean value;
    public BindMode type;
    public final List<Integer> keyBind;

    public ExpandableSetting(Translation class254Var, Translation class254Var2) {
        super(class254Var, class254Var2);
        this.serializableChildren = new ArrayList();
        this.type = BindMode.TOGGLE;
        this.keyBind = new ArrayList();
    }

    public ExpandableSetting(Translation class254Var) {
        this(class254Var, null);
    }

    public ExpandableSetting settings(Setting... class661VarArr) {
        this.serializableChildren.addAll(Arrays.asList(class661VarArr));
        return this;
    }

    public ExpandableSetting visible(Supplier<Boolean> supplier) {
        setVisible(supplier);
        return this;
    }

    public void switchValue() {
        this.value = !this.value;
    }

    public Setting getSubSetting(String str) {
        return this.serializableChildren.stream().filter(class661Var -> {
            return class661Var.getName().original().equalsIgnoreCase(str);
        }).findFirst().orElse(null);
    }

    public List<Integer> getKeybinds() {
        return Collections.unmodifiableList(this.keyBind);
    }

    public int getKey() {
        if (this.keyBind.isEmpty()) {
            return -1;
        }
        return ((Integer) this.keyBind.getFirst()).intValue();
    }

    public void setKey(int i) {
        setKey(i == -1 ? List.of() : List.of(Integer.valueOf(i)));
    }

    public void setKey(List<Integer> list) {
        this.keyBind.clear();
        if (list == null) {
            return;
        }
        Stream<Integer> streamLimit = list.stream().filter(num -> {
            return num.intValue() != -1;
        }).limit(2L);
        List<Integer> list2 = this.keyBind;
        Objects.requireNonNull(list2);
        streamLimit.forEach((v1) -> {
            list2.add(v1);
        });
    }

    @Override
    public Map<String, Object> toSerializedData() {
        HashMap map = new HashMap();
        map.put("value", Boolean.valueOf(this.value));
        map.put("keys", new ArrayList(getKeybinds()));
        map.put("bindType", this.type.name());
        return map;
    }

    @Override
    public void loadSerializedData(Map<String, Object> map) {
        Object obj = map.get("value");
        if (obj instanceof Boolean) {
            setValue(((Boolean) obj).booleanValue());
        }
        Object obj2 = map.get("keys");
        if (obj2 instanceof List) {
            Stream stream = ((List) obj2).stream();
            Class<Number> cls = Number.class;
            Objects.requireNonNull(Number.class);
            Stream streamFilter = stream.filter(cls::isInstance);
            Class<Number> cls2 = Number.class;
            Objects.requireNonNull(Number.class);
            setKey(streamFilter.map(cls2::cast).map((v0) -> {
                return ((Number) v0).intValue();
            }).toList());
        } else {
            Object obj3 = map.get("key");
            if (obj3 instanceof Number) {
                setKey(((Number) obj3).intValue());
            }
        }
        Object obj4 = map.get("bindType");
        if (obj4 instanceof String) {
            setType(BindMode.valueOf((String) obj4));
        }
    }

    @Override
    public List<Setting> getSerializableChildren() {
        return this.serializableChildren;
    }

    public List<Setting> getSubSettings() {
        return this.serializableChildren;
    }

    public boolean isValue() {
        return this.value;
    }

    public BindMode getType() {
        return this.type;
    }

    public List<Integer> getKeyBind() {
        return this.keyBind;
    }

    public ExpandableSetting setSubSettings(List<Setting> list) {
        this.serializableChildren = list;
        return this;
    }

    public ExpandableSetting setValue(boolean z) {
        this.value = z;
        return this;
    }

    public ExpandableSetting setType(BindMode class660Var) {
        this.type = class660Var;
        return this;
    }
}
