package aethereal.render;

import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import java.nio.IntBuffer;
import org.lwjgl.opengl.GL33;
import org.lwjgl.system.MemoryUtil;

public class TextureSlotAllocator {
    public final Int2IntMap slotByTexture = new Int2IntOpenHashMap();
    public final IntBuffer textureSlots;
    public final int capacity;
    public int nextSlot;

    public TextureSlotAllocator(int i) {
        this.capacity = i;
        this.textureSlots = MemoryUtil.memAllocInt(i);
        for (int i2 = 0; i2 < i; i2++) {
            this.textureSlots.put(i2);
        }
        this.textureSlots.flip();
    }

    public int bindTexture(int i) {
        if (this.slotByTexture.containsKey(i)) {
            return this.slotByTexture.get(i);
        }
        int i2 = this.nextSlot;
        this.nextSlot = i2 + 1;
        this.slotByTexture.put(i, i2);
        GL33.glActiveTexture(33984 + i2);
        GL33.glBindTexture(3553, i);
        return i2;
    }

    public boolean contains(int i) {
        return this.slotByTexture.containsKey(i);
    }

    public boolean isFull() {
        return this.nextSlot >= this.capacity;
    }

    public void clear() {
        this.slotByTexture.clear();
        this.nextSlot = 0;
    }

    public IntBuffer textureSlots() {
        return this.textureSlots;
    }
}
