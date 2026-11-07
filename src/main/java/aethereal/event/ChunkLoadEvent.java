package aethereal.event;

import net.minecraft.util.math.ChunkPos;

public final class ChunkLoadEvent implements Event {
    public final ChunkPos chunkPos;

    public ChunkLoadEvent(ChunkPos chunkPos) {
        this.chunkPos = chunkPos;
    }

        @Override
    public final String toString() {
        return getClass().getSimpleName() + "[" + "chunkPos=" + this.chunkPos + "]";
    }
    @Override
    public final int hashCode() {
        return java.util.Objects.hash(this.chunkPos);
    }
    @Override
    public final boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof ChunkLoadEvent)) return false;
        ChunkLoadEvent o = (ChunkLoadEvent) obj;
        return java.util.Objects.equals(this.chunkPos, o.chunkPos);
    }
public ChunkPos chunkPos() {
        return this.chunkPos;
    }
}
