package aethereal.util;
import aethereal.resource.ClasspathResource;
import aethereal.model.FontData;
import aethereal.render.FontMetrics;
import aethereal.render.GlTexture;
import aethereal.model.GlyphBounds;
import aethereal.model.GlyphData;
import aethereal.render.KerningPair;
import aethereal.render.MsdfFont;
import aethereal.config.SpriteRenderConfig;

import com.google.gson.Gson;
import it.unimi.dsi.fastutil.ints.Int2FloatMap;
import it.unimi.dsi.fastutil.ints.Int2FloatOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.List;

public class MsdfFontBuilder {
    private static final Gson GSON = new Gson();

    private String atlasName;
    private String dataName;
    private GlTexture texture;
    private FontData data;

    public MsdfFontBuilder atlas(String str) {
        this.atlasName = str;
        this.texture = new GlTexture(new ClasspathResource("fonts/" + str + ".png"))
                .magFilter(9729)
                .minFilter(9729)
                .wrapX(33071)
                .wrapY(33071);
        return this;
    }

    public MsdfFontBuilder data(String str) {
        this.dataName = str;
        String json = new ClasspathResource("fonts/" + str + ".json").utf8();
        this.data = GSON.fromJson(json, FontData.class);
        return this;
    }

    public MsdfFontBuilder atlasPath(String str) {
        this.atlasName = str;
        this.texture = new GlTexture(new ClasspathResource(str + ".png"))
                .magFilter(9729)
                .minFilter(9729)
                .wrapX(33071)
                .wrapY(33071);
        return this;
    }

    public MsdfFontBuilder dataPath(String str) {
        this.dataName = str;
        String json = new ClasspathResource(str + ".json").utf8();
        this.data = GSON.fromJson(json, FontData.class);
        return this;
    }

    public MsdfFont build() {
        SpriteRenderConfig atlasInfo = this.data.atlas();
        FontMetrics metrics = this.data.metrics();
        List<GlyphData> glyphs = this.data.glyphs();
        float aw = atlasInfo.width();
        float ah = atlasInfo.height();

        int n = glyphs.size();
        int[] glyphCodes = new int[n];
        Int2IntMap glyphIndex = new Int2IntOpenHashMap();
        glyphIndex.defaultReturnValue(-1);

        float[] minU = new float[n];
        float[] maxU = new float[n];
        float[] minV = new float[n];
        float[] maxV = new float[n];
        float[] advance = new float[n];
        float[] planeTop = new float[n];
        float[] planeWidth = new float[n];
        float[] planeHeight = new float[n];

        for (int i = 0; i < n; i++) {
            GlyphData g = glyphs.get(i);
            int code = g.unicode();
            glyphCodes[i] = code;
            glyphIndex.put(code, i);
            advance[i] = g.advance();

            GlyphBounds ab = g.atlasBounds();
            if (ab != null) {
                minU[i] = ab.left() / aw;
                maxU[i] = ab.right() / aw;
                minV[i] = 1.0f - (ab.top() / ah);
                maxV[i] = 1.0f - (ab.bottom() / ah);
            }

            GlyphBounds pb = g.planeBounds();
            if (pb != null) {
                planeTop[i] = pb.top();
                planeWidth[i] = pb.right() - pb.left();
                planeHeight[i] = pb.top() - pb.bottom();
            }
        }

        Int2ObjectMap<Int2FloatMap> kernings = new Int2ObjectOpenHashMap<>();
        List<KerningPair> kerns = this.data.kernings();
        if (kerns != null) {
            for (KerningPair k : kerns) {
                Int2FloatMap inner = kernings.get(k.unicode1());
                if (inner == null) {
                    inner = new Int2FloatOpenHashMap();
                    inner.defaultReturnValue(0.0f);
                    kernings.put(k.unicode1(), inner);
                }
                inner.put(k.unicode2(), k.advance());
            }
        }

        return new MsdfFont(
                this.dataName,
                this.texture,
                atlasInfo,
                metrics,
                glyphCodes,
                glyphIndex,
                minU,
                maxU,
                minV,
                maxV,
                advance,
                planeTop,
                planeWidth,
                planeHeight,
                kernings);
    }
}
