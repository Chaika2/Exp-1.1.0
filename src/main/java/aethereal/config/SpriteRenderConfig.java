package aethereal.config;

import com.google.gson.annotations.SerializedName;

public class SpriteRenderConfig {
    @SerializedName("type")
    public String type;
    @SerializedName("distanceRange")
    public float distanceRange;
    @SerializedName("distanceRangeMiddle")
    public float distanceRangeMiddle;
    @SerializedName("size")
    public float size;
    @SerializedName("width")
    public int width;
    @SerializedName("height")
    public int height;
    @SerializedName("yOrigin")
    public String yOrigin;

    public String type() {
        return this.type;
    }

    public float distanceRange() {
        return this.distanceRange;
    }

    public float size() {
        return this.size;
    }

    public int width() {
        return this.width;
    }

    public int height() {
        return this.height;
    }
}
