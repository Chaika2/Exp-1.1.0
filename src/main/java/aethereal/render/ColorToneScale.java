package aethereal.render;

import java.util.Map;

public final class ColorToneScale {
    public final Map<Integer, ColorValue> all;

    public ColorToneScale(Map<Integer, ColorValue> map) {
        this.all = map;
    }

    public ColorValue tone(int i) {
        ColorValue class761Var = this.all.get(Integer.valueOf(i));
        if (class761Var == null) {
            throw new IllegalArgumentException("Tone " + i + " does not exist in scale");
        }
        return class761Var;
    }

    public boolean has(int i) {
        return this.all.containsKey(Integer.valueOf(i));
    }

    public Map<Integer, ColorValue> all() {
        return this.all;
    }
}
