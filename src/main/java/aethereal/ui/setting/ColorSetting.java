package aethereal.ui.setting;
import aethereal.util.ColorUtil;
import aethereal.util.StencilBufferUtil;
import aethereal.model.Translation;

import java.awt.Color;
import java.util.Map;
import java.util.function.Supplier;

public class ColorSetting extends Setting {
    public float hue;
    public float saturation;
    public float brightness;
    public float alpha;

    public ColorSetting(Translation class254Var, Translation class254Var2) {
        super(class254Var, class254Var2);
        this.hue = 0.0f;
        this.saturation = 1.0f;
        this.brightness = 1.0f;
        this.alpha = 1.0f;
        setColor(-1);
    }

    public ColorSetting(Translation class254Var) {
        this(class254Var, null);
    }

    public ColorSetting value(int i) {
        setColor(i);
        return this;
    }

    public ColorSetting visible(Supplier<Boolean> supplier) {
        setVisible(supplier);
        return this;
    }

    public int getColor() {
        return (getColorWithoutAlpha() & 16777215) | (Math.round(this.alpha * 255.0f) << 24);
    }

    public int getRed() {
        return (getColorWithoutAlpha() >> 16) & StencilBufferUtil.STENCIL_MASK;
    }

    public int getGreen() {
        return (getColorWithoutAlpha() >> 8) & StencilBufferUtil.STENCIL_MASK;
    }

    public int getBlue() {
        return getColorWithoutAlpha() & StencilBufferUtil.STENCIL_MASK;
    }

    public int getColorWithoutAlpha() {
        return Color.HSBtoRGB(this.hue, this.saturation, this.brightness);
    }

    public ColorSetting setColor(int i) {
        float[] fArrRGBtoHSB = Color.RGBtoHSB(ColorUtil.red(i), ColorUtil.green(i), ColorUtil.blue(i), (float[]) null);
        this.hue = fArrRGBtoHSB[0];
        this.saturation = fArrRGBtoHSB[1];
        this.brightness = fArrRGBtoHSB[2];
        int iAlpha = ColorUtil.alpha(i);
        if (iAlpha == 0 && (i >>> 24) == 0) {
            this.alpha = 1.0f;
        } else {
            this.alpha = iAlpha / 255.0f;
        }
        return this;
    }

    @Override
    public Map<String, Object> toSerializedData() {
        return Map.of("color", Integer.valueOf(getColor()));
    }

    @Override
    public void loadSerializedData(Map<String, Object> map) {
        Object obj = map.get("color");
        if (obj instanceof Number) {
            setColor(((Number) obj).intValue());
        }
    }

    public float getHue() {
        return this.hue;
    }

    public float getSaturation() {
        return this.saturation;
    }

    public float getBrightness() {
        return this.brightness;
    }

    public float getAlpha() {
        return this.alpha;
    }

    public void setHue(float f) {
        this.hue = f;
    }

    public void setSaturation(float f) {
        this.saturation = f;
    }

    public void setBrightness(float f) {
        this.brightness = f;
    }

    public void setAlpha(float f) {
        this.alpha = f;
    }
}
