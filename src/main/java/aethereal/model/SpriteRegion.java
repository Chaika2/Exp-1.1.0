package aethereal.model;

import net.minecraft.client.texture.Sprite;
import net.minecraft.util.Identifier;

public class SpriteRegion {
    private Identifier atlasId;
    private float minU;
    private float minV;
    private float maxU;
    private float maxV;
    private int color;

    public SpriteRegion() {
    }

    public SpriteRegion(Identifier identifier, float f, float f2, float f3, float f4, int i) {
        this.atlasId = identifier;
        this.minU = f;
        this.minV = f2;
        this.maxU = f3;
        this.maxV = f4;
        this.color = i;
    }

    public Identifier atlasId() {
        return this.atlasId;
    }

    public int color() {
        return this.color;
    }

    public float maxU() {
        return this.maxU;
    }

    public float maxV() {
        return this.maxV;
    }

    public float minU() {
        return this.minU;
    }

    public float minV() {
        return this.minV;
    }

    public static SpriteRegion of(Sprite sprite, int i) {
        return new SpriteRegion(sprite.getAtlasId(), sprite.getMinU(), sprite.getMinV(), sprite.getMaxU(), sprite.getMaxV(), i);
    }

    public static SpriteRegion of(Identifier identifier, float f, float f2, float f3, float f4, int i) {
        return new SpriteRegion(identifier, f, f2, f3, f4, i);
    }
}
