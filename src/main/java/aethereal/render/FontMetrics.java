package aethereal.render;

import com.google.gson.annotations.SerializedName;

public class FontMetrics {
    @SerializedName("emSize")
    public float emSize;
    @SerializedName("lineHeight")
    public float lineHeight;
    @SerializedName("ascender")
    public float ascender;
    @SerializedName("descender")
    public float descender;
    @SerializedName("underlineY")
    public float underlineY;
    @SerializedName("underlineThickness")
    public float underlineThickness;

    public float ascent() {
        return this.ascender;
    }

    public float descent() {
        return this.descender;
    }

    public float lineHeight() {
        return this.lineHeight;
    }
}
