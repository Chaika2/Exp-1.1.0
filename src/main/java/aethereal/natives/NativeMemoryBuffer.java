package aethereal.natives;
import aethereal.accessor.MemoryAccessor;

import java.nio.ByteBuffer;
import org.lwjgl.system.MemoryUtil;

public class NativeMemoryBuffer implements MemoryAccessor, AutoCloseable {
    public long address;
    public long position;
    public long capacity;
    public final NativeMemory memory;

    public NativeMemoryBuffer(NativeMemory class031Var, long j) {
        this.memory = class031Var;
        this.address = class031Var.allocate(j);
        this.capacity = j;
        if (this.address == 0) {
            throw new OutOfMemoryError("Failed to allocate " + j + " bytes of memory");
        }
    }

    @Override
    public void close() {
        this.memory.free(this.address);
    }

    public void offset(long j) {
        this.position += j;
    }

    public void reset() {
        this.position = 0L;
    }

    public long effectiveAddress() {
        return this.address + this.position;
    }

    public ByteBuffer directByteBuffer() {
        return directByteBuffer((int) this.position);
    }

    public ByteBuffer directByteBuffer(int i) {
        return MemoryUtil.memByteBuffer(this.address, i);
    }

    public void requireMoreFreeBytes(long j) {
        if (this.position + j > this.capacity) {
            long jMax = Math.max(this.capacity * 2, this.position + j + 1024);
            long jReallocate = this.memory.reallocate(this.address, this.capacity, jMax);
            if (jReallocate == 0) {
                OutOfMemoryError outOfMemoryError = new OutOfMemoryError("Failed to reallocate memory from " + this.capacity + " to " + jMax + " bytes");
                throw outOfMemoryError;
            }
            this.address = jReallocate;
            this.capacity = jMax;
        }
    }

    @Override
    public void writeByte(long j, int i) {
        this.memory.writeByte(j, i);
    }

    @Override
    public void writeShort(long j, int i) {
        this.memory.writeShort(j, i);
    }

    @Override
    public void writeInt(long j, int i) {
        this.memory.writeInt(j, i);
    }

    @Override
    public void writeFloat(long j, float f) {
        this.memory.writeFloat(j, f);
    }

    @Override
    public void writeLong(long j, long j2) {
        this.memory.writeLong(j, j2);
    }

    @Override
    public int readByte(long j) {
        return this.memory.readByte(j);
    }

    @Override
    public int readShort(long j) {
        return this.memory.readShort(j);
    }

    @Override
    public int readInt(long j) {
        return this.memory.readInt(j);
    }

    @Override
    public long readLong(long j) {
        return this.memory.readLong(j);
    }

    @Override
    public void memoryCopy(long j, long j2, long j3) {
        this.memory.memoryCopy(j, j2, j3);
    }
}
