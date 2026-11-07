package aethereal.render;

import com.google.gson.annotations.SerializedName;

public class KerningPair {
    @SerializedName("unicode1")
    public int unicode1;
    @SerializedName("unicode2")
    public int unicode2;
    @SerializedName("advance")
    public float advance;

    public int unicode1() {
        return this.unicode1;
    }

    public int unicode2() {
        return this.unicode2;
    }

    public float advance() {
        return this.advance;
    }
}
