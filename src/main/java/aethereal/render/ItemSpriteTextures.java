package aethereal.render;
import aethereal.model.SpriteRegion;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.texture.Sprite;
import net.minecraft.util.math.Direction;

public class ItemSpriteTextures {
    private String type;
    private Map sprites;
    private boolean glint;

    public ItemSpriteTextures() {
    }

    public ItemSpriteTextures(String str, Map map, boolean z) {
        this.type = str;
        this.sprites = map;
        this.glint = z;
    }

    public static ItemSpriteTextures of(String str, Map map, boolean z, int i) {
        HashMap hashMap = new HashMap();
        for (Object obj : map.entrySet()) {
            Map.Entry entry = (Map.Entry) obj;
            hashMap.put((Direction) entry.getKey(), SpriteRegion.of((Sprite) entry.getValue(), i));
        }
        return new ItemSpriteTextures(str, hashMap, z);
    }

    public Map sprites() {
        return this.sprites;
    }

    public boolean glint() {
        return this.glint;
    }

    public String type() {
        return this.type;
    }
}
