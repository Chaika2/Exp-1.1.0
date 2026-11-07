package aethereal.render;
import aethereal.util.ColorUtil;
import aethereal.util.DrawEngine;
import aethereal.util.MsdfFontBuilder;
import aethereal.config.SpriteRenderConfig;

import it.unimi.dsi.fastutil.ints.Int2FloatMap;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.text.Text;
import org.joml.Matrix4f;

public class MsdfFont {
    public final String name;
    public final GlTexture texture;
    public final SpriteRenderConfig atlas;

    public final FontMetrics metrics;
    public final int[] glyphCodes;
    public final Int2IntMap glyphIndexMap;

    public final float[] atlasU0;
    public final float[] atlasU1;

    public final float[] atlasV0;
    public final float[] atlasV1;
    public final float[] advance;
    public final float[] bearingY;
    public final float[] planeWidth;
    public final float[] planeHeight;

    public final Int2ObjectMap<Int2FloatMap> kernings;

    public int getTextureId() {
        return this.texture.textureWithSTB();
    }

    public int[] glyphCodes() {
        return this.glyphCodes;
    }

    public int glyphIndex(int i) {
        return this.glyphIndexMap.get(i);
    }

    public float kerningAdvance(int i, int i2) {
        Int2FloatMap int2FloatMap = (Int2FloatMap) this.kernings.get(i);
        if (int2FloatMap == null) {
            return 0.0f;
        }
        return int2FloatMap.get(i2);
    }

    public float glyphWidth(int i, float f) {
        int iGlyphIndex = glyphIndex(i);
        if (iGlyphIndex == -1) {
            return 0.0f;
        }
        return this.advance[iGlyphIndex] * f;
    }

    public float applyGlyph(Matrix4f matrix4f, DrawEngine class154Var, int i, float f, float f2, float f3, int i2, int i3) {
        class154Var.textureMsdf(matrix4f, f2, f3 - (this.bearingY[i] * f), this.planeWidth[i] * f, this.planeHeight[i] * f, this.atlasU0[i], this.atlasV0[i], this.atlasU1[i], this.atlasV1[i], i3, i2);
        return this.advance[i] * f;
    }

    public float applyGlyph(Matrix4f matrix4f, DrawEngine class154Var, int i, float f, float f2, float f3, int i2, int i3, int i4, int i5, int i6) {
        class154Var.textureMsdf(matrix4f, f2, f3 - (this.bearingY[i] * f), this.planeWidth[i] * f, this.planeHeight[i] * f, this.atlasU0[i], this.atlasV0[i], this.atlasU1[i], this.atlasV1[i], i6, i2, i3, i4, i5);
        return this.advance[i] * f;
    }

    public void applyGlyphs(Matrix4f matrix4f, DrawEngine class154Var, int i, String str, float f, float f2, float f3, float f4, float f5, int i2) {
        Int2FloatMap int2FloatMap;
        float height = getHeight(f);
        float startX = f4;
        Int2FloatMap int2FloatMap2 = null;
        int length = str.length();
        for (int i3 = 0; i3 < length; i3++) {
            char cCharAt = str.charAt(i3);
            if (cCharAt == '\n') {
                f4 = startX;
                f5 += height;
                int2FloatMap = null;
            } else {
                int iGlyphIndex = glyphIndex(cCharAt);
                if (iGlyphIndex == -1) {
                    int2FloatMap = null;
                } else {
                    if (int2FloatMap2 != null) {
                        f4 += int2FloatMap2.get(cCharAt) * f;
                    }
                    f4 += applyGlyph(matrix4f, class154Var, iGlyphIndex, f, f4, f5, i2, i) + f2 + f3;
                    int2FloatMap = (Int2FloatMap) this.kernings.get(cCharAt);
                }
            }
            int2FloatMap2 = int2FloatMap;
        }
    }

    public void applyGlyphsWithHorizontalGradient(Matrix4f matrix4f, DrawEngine class154Var, int i, String str, float f, float f2, float f3, float f4, float f5, int i2, int i3) {
        Int2FloatMap int2FloatMap;
        float height = getHeight(f);
        float startX = f4;
        Int2FloatMap int2FloatMap2 = null;
        int length = str.length();
        for (int i4 = 0; i4 < length; i4++) {
            char cCharAt = str.charAt(i4);
            if (cCharAt == '\n') {
                f4 = startX;
                f5 += height;
                int2FloatMap = null;
            } else {
                int iGlyphIndex = glyphIndex(cCharAt);
                if (iGlyphIndex == -1) {
                    int2FloatMap = null;
                } else {
                    if (int2FloatMap2 != null) {
                        f4 += int2FloatMap2.get(cCharAt) * f;
                    }
                    int iInterpolateColor = ColorUtil.interpolateColor(i2, i3, i4 / length);
                    int iInterpolateColor2 = ColorUtil.interpolateColor(i2, i3, (i4 + 1) / length);
                    f4 += applyGlyph(matrix4f, class154Var, iGlyphIndex, f, f4, f5, iInterpolateColor, iInterpolateColor2, iInterpolateColor2, iInterpolateColor, i) + f2 + f3;
                    int2FloatMap = (Int2FloatMap) this.kernings.get(cCharAt);
                }
            }
            int2FloatMap2 = int2FloatMap;
        }
    }

    public float getHeight(float f) {
        return (this.metrics.ascent() - this.metrics.descent()) * f;
    }

    public float getHeightWithLineBreaks(String str, int i) {
        int i2 = 1;
        for (char c : str.toCharArray()) {
            if (c == '\n') {
                i2++;
            }
        }
        return getHeight(i) * i2;
    }

    public float getWidthWithStyles(Text text, float f) {
        float[] fArr = {0.0f};
        float[] fArr2 = {0.0f};
        int[] iArr = {-1};
        glyphCodes();
        ThreadLocalRandom.current();
        text.asOrderedText().accept((i, style, i2) -> {
            char c = (char) i2;
            if (c == '\n') {
                fArr2[0] = Math.max(fArr2[0], fArr[0]);
                fArr[0] = 0.0f;
                iArr[0] = -1;
                return true;
            }
            if (c == '\r') {
                return true;
            }
            char c2 = style.isObfuscated() ? '?' : c;
            if (style.isObfuscated()) {
                int iGlyphIndex = glyphIndex(63);
                if (iGlyphIndex != -1) {
                    fArr[0] = fArr[0] + (this.advance[iGlyphIndex] * f * 1.2f);
                }
                iArr[0] = -1;
                return true;
            }
            int iGlyphIndex2 = glyphIndex(c2);
            if (iGlyphIndex2 == -1) {
                iArr[0] = -1;
                return true;
            }
            if (iArr[0] != -1) {
                fArr[0] = fArr[0] + (kerningAdvance(iArr[0], c2) * f);
            }
            fArr[0] = fArr[0] + (this.advance[iGlyphIndex2] * f);
            iArr[0] = c2;
            return true;
        });
        return Math.max(fArr2[0], fArr[0]);
    }

    public float getWidth(String str, float f) {
        char c = 65535;
        float f2 = 0.0f;
        float fMax = 0.0f;
        char[] charArray = str.toCharArray();
        boolean z = false;
        ThreadLocalRandom.current();
        int length = this.glyphCodes.length;
        int i = 0;
        while (i < str.length()) {
            char cCharAt = str.charAt(i);
            if (cCharAt == '\n') {
                fMax = Math.max(fMax, f2);
                f2 = 0.0f;
                c = 65535;
            } else if (cCharAt != '\r') {
                if (cCharAt != 167 || i + 1 >= charArray.length) {
                    if (z) {
                        cCharAt = '?';
                    }
                    int iGlyphIndex = glyphIndex(cCharAt);
                    if (iGlyphIndex == -1) {
                        c = 65535;
                    } else {
                        Int2FloatMap int2FloatMap = (Int2FloatMap) this.kernings.get(c);
                        if (int2FloatMap != null) {
                            f2 += int2FloatMap.get(cCharAt) * f;
                        }
                        f2 += this.advance[iGlyphIndex] * f;
                        c = cCharAt;
                    }
                } else {
                    i++;
                    char c2 = charArray[i];
                    if (c2 == 'k') {
                        z = true;
                    } else if (c2 == 'r') {
                        z = false;
                    }
                }
            }
            i++;
        }
        return Math.max(fMax, f2);
    }

    public static MsdfFontBuilder builder() {
        return new MsdfFontBuilder();
    }

    public MsdfFont(String str, GlTexture class073Var, SpriteRenderConfig iresid004, FontMetrics class296Var, int[] iArr, Int2IntMap int2IntMap, float[] fArr, float[] fArr2, float[] fArr3, float[] fArr4, float[] fArr5, float[] fArr6, float[] fArr7, float[] fArr8, Int2ObjectMap<Int2FloatMap> int2ObjectMap) {
        this.name = str;
        this.texture = class073Var;
        this.atlas = iresid004;
        this.metrics = class296Var;
        this.glyphCodes = iArr;
        this.glyphIndexMap = int2IntMap;
        this.atlasU0 = fArr;
        this.atlasU1 = fArr2;
        this.atlasV0 = fArr3;
        this.atlasV1 = fArr4;
        this.advance = fArr5;
        this.bearingY = fArr6;
        this.planeWidth = fArr7;
        this.planeHeight = fArr8;
        this.kernings = int2ObjectMap;
    }

    public String name() {
        return this.name;
    }

    public SpriteRenderConfig atlas() {
        return this.atlas;
    }

    public FontMetrics metrics() {
        return this.metrics;
    }

    public Int2ObjectMap<Int2FloatMap> kernings() {
        return this.kernings;
    }
}
