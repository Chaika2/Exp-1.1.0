package aethereal.accessor;
import aethereal.natives.NativeMemory;

import sun.misc.Unsafe;

public class UnsafeMemoryAccessor implements NativeMemory {
    public final Unsafe unsafe;

    @Override
    public void writeByte(long j, int i) {
        this.unsafe.putByte(j, (byte) i);
    }

    @Override
    public void writeShort(long j, int i) {
        this.unsafe.putShort(j, (short) i);
    }

    @Override
    public void writeInt(long j, int i) {
        this.unsafe.putInt(j, i);
    }

    @Override
    public void writeFloat(long j, float f) {
        this.unsafe.putFloat(j, f);
    }

    @Override
    public void writeLong(long j, long j2) {
        this.unsafe.putLong(j, j2);
    }

    @Override
    public int readByte(long j) {
        return this.unsafe.getByte(j) & 255;
    }

    @Override
    public int readShort(long j) {
        return this.unsafe.getShort(j) & 65535;
    }

    @Override
    public int readInt(long j) {
        return this.unsafe.getInt(j);
    }

    @Override
    public long readLong(long j) {
        return this.unsafe.getLong(j);
    }

    @Override
    public void memoryCopy(long j, long j2, long j3) {
        this.unsafe.copyMemory(j, j2, j3);
    }

    @Override
    public long allocate(long j) {
        return this.unsafe.allocateMemory(j);
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
        this.unsafe.freeMemory(j);
    }

    public UnsafeMemoryAccessor(Unsafe unsafe) {
        this.unsafe = unsafe;
    }
}
