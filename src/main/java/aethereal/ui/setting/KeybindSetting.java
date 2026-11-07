package aethereal.ui.setting;
import aethereal.type.BindMode;
import aethereal.type.KeyAction;
import aethereal.model.Translation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class KeybindSetting extends Setting {
    public BindMode type;
    public final List<Integer> boundKeys;
    public Consumer<KeyAction> consumer;

    public KeybindSetting(Translation class254Var, Translation class254Var2) {
        super(class254Var, class254Var2);
        this.type = BindMode.TOGGLE;
        this.boundKeys = new ArrayList();
        this.consumer = class664Var -> {
        };
    }

    public KeybindSetting(Translation class254Var) {
        this(class254Var, null);
    }

    public KeybindSetting visible(Supplier<Boolean> supplier) {
        setVisible(supplier);
        return this;
    }

    public KeybindSetting consumer(Consumer<KeyAction> consumer) {
        this.consumer = consumer;
        return this;
    }

    public KeybindSetting consumer(BindMode class660Var, Consumer<KeyAction> consumer) {
        this.type = class660Var;
        this.consumer = consumer;
        return this;
    }

    public List<Integer> getKeyBind() {
        return Collections.unmodifiableList(this.boundKeys);
    }

    public int getKey() {
        if (this.boundKeys.isEmpty()) {
            return -1;
        }
        return ((Integer) this.boundKeys.getFirst()).intValue();
    }

    public void setKey(int i) {
        setKey(i == -1 ? List.of() : List.of(Integer.valueOf(i)));
    }

    public void setKey(List<Integer> list) {
        this.boundKeys.clear();
        if (list == null) {
            return;
        }
        Stream<Integer> streamLimit = list.stream().filter(num -> {
            return num.intValue() != -1;
        }).limit(2L);
        List<Integer> list2 = this.boundKeys;
        Objects.requireNonNull(list2);
        streamLimit.forEach((v1) -> {
            list2.add(v1);
        });
    }

    @Override
    public Map<String, Object> toSerializedData() {
        return Map.of("keys", new ArrayList(getKeyBind()), "bindType", this.type.name());
    }

    @Override
    public void loadSerializedData(Map<String, Object> map) {
        Object obj = map.get("keys");
        if (obj instanceof List) {
            Stream stream = ((List) obj).stream();
            Class<Number> cls = Number.class;
            Objects.requireNonNull(Number.class);
            Stream streamFilter = stream.filter(cls::isInstance);
            Class<Number> cls2 = Number.class;
            Objects.requireNonNull(Number.class);
            setKey(streamFilter.map(cls2::cast).map((v0) -> {
                return ((Number) v0).intValue();
            }).toList());
        } else {
            Object obj2 = map.get("key");
            if (obj2 instanceof Number) {
                setKey(((Number) obj2).intValue());
            }
        }
        Object obj3 = map.get("bindType");
        if (obj3 instanceof String) {
            setType(BindMode.valueOf((String) obj3));
        }
    }

    public BindMode getType() {
        return this.type;
    }

    public Consumer<KeyAction> getConsumer() {
        return this.consumer;
    }

    public KeybindSetting setType(BindMode class660Var) {
        this.type = class660Var;
        return this;
    }

    public KeybindSetting setConsumer(Consumer<KeyAction> consumer) {
        this.consumer = consumer;
        return this;
    }
}
