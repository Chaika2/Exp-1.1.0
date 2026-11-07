package aethereal.model;

import com.google.gson.annotations.SerializedName;

public class GlyphData {
    @SerializedName("unicode")
    public int unicode;
    @SerializedName("advance")
    public float advance;
    @SerializedName("planeBounds")
    public GlyphBounds planeBounds;
    @SerializedName("atlasBounds")
    public GlyphBounds atlasBounds;

    public float advance() {
        return this.advance;
    }

    public GlyphBounds atlasBounds() {
        return this.atlasBounds;
    }

    public GlyphBounds planeBounds() {
        return this.planeBounds;
    }

    public int unicode() {
        return this.unicode;
    }
}
