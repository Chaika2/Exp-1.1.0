package aethereal.util;
import aethereal.Lang;
import aethereal.model.TranslatedException;
import aethereal.model.Translation;

import java.util.Collections;
import java.util.List;

public class FloatArgumentParser implements ArgumentParser<Float> {
    public final Float min;
    public final Float max;

    public FloatArgumentParser() {
        this(null, null);
    }

    public FloatArgumentParser(Float f, Float f2) {
        this.min = f;
        this.max = f2;
    }

    @Override
    public Float parse(String str) throws TranslatedException {
        try {
            float f = Float.parseFloat(str);
            if (this.min != null && f < this.min.floatValue()) {
                throw new TranslatedException(Translation.clearText(Lang.TYPE_MIN_VALUE.effective().replace("{min}", String.valueOf(this.min))));
            }
            if (this.max == null || f <= this.max.floatValue()) {
                return Float.valueOf(f);
            }
            throw new TranslatedException(Translation.clearText(Lang.TYPE_MAX_VALUE.effective().replace("{max}", String.valueOf(this.max))));
        } catch (NumberFormatException e) {
            throw new TranslatedException(Translation.clearText(Lang.TYPE_INVALID_FLOAT.effective().replace("{input}", str)));
        }
    }

    @Override
    public List<String> getSuggestions(String str) {
        return Collections.emptyList();
    }

    @Override
    public String getName() {
        return "float";
    }
}
