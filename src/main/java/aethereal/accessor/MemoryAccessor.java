package aethereal.accessor;

public interface MemoryAccessor {
    void writeByte(long j, int i);

    void writeShort(long j, int i);

    void writeInt(long j, int i);

    void writeFloat(long j, float f);

    void writeLong(long j, long j2);

    int readByte(long j);

    int readShort(long j);

    int readInt(long j);

    long readLong(long j);

    void memoryCopy(long j, long j2, long j3);
}
