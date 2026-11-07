package aethereal.accessor;
import aethereal.natives.NativeMemory;

import org.lwjgl.system.MemoryUtil;

public class NativeMemoryAccessor implements NativeMemory {
    @Override
    public void writeByte(long j, int i) {
        MemoryUtil.memPutByte(j, (byte) i);
    }

    @Override
    public void writeShort(long j, int i) {
        MemoryUtil.memPutShort(j, (short) i);
    }

    @Override
    public void writeInt(long j, int i) {
        MemoryUtil.memPutInt(j, i);
    }

    @Override
    public void writeFloat(long j, float f) {
        MemoryUtil.memPutFloat(j, f);
    }

    @Override
    public void writeLong(long j, long j2) {
        MemoryUtil.memPutLong(j, j2);
    }

    @Override
    public int readByte(long j) {
        return MemoryUtil.memGetByte(j) & 255;
    }

    @Override
    public int readShort(long j) {
        return MemoryUtil.memGetShort(j) & 65535;
    }

    @Override
    public int readInt(long j) {
        return MemoryUtil.memGetInt(j);
    }

    @Override
    public long readLong(long j) {
        return MemoryUtil.memGetLong(j);
    }

    @Override
    public void memoryCopy(long j, long j2, long j3) {
        MemoryUtil.memCopy(j, j2, j3);
    }

    @Override
    public long allocate(long j) {
        return MemoryUtil.nmemAllocChecked(j);
    }

    @Override
    public long reallocate(long j, long j2, long j3) {
        long jAllocate = allocate(j3);
        memoryCopy(j, jAllocate, j2);
        free(j);
        return jAllocate;
    }

    @Override
    public void free(long j) {
        MemoryUtil.nmemFree(j);
    }
}
