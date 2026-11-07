package aethereal.model;

import com.google.gson.annotations.SerializedName;

public class GlyphBounds {
    @SerializedName("left")
    public float left;
    @SerializedName("bottom")
    public float bottom;
    @SerializedName("right")
    public float right;
    @SerializedName("top")
    public float top;

    public float bottom() {
        return this.bottom;
    }

    public float left() {
        return this.left;
    }

    public float right() {
        return this.right;
    }

    public float top() {
        return this.top;
    }
}
