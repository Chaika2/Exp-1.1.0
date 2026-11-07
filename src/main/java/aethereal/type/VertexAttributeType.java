package aethereal.type;

public enum VertexAttributeType {
    UNSIGNED_BYTE(5121, 1, 1, false, true),
    UNSIGNED_INT(5125, 1, 4, false, true),
    IVEC2(5125, 2, 4, false, true),
    IVEC3(5125, 3, 4, false, true),
    IVEC4(5125, 4, 4, false, true),
    FLOAT(5126, 1, 4, false),
    VEC2(5126, 2, 4, false),
    VEC3(5126, 3, 4, false),
    VEC4(5126, 4, 4, false),
    NORMALIZED_VEC4(5121, 4, 1, true);

    public final int type;
    public final int count;
    public final int componentSize;
    public final boolean normalized;
    public final boolean integer;

    VertexAttributeType(int i, int i2, int i3, boolean z) {
        this(i, i2, i3, z, false);
    }

    public int size() {
        return this.componentSize * this.count;
    }

    public int type() {
        return this.type;
    }

    public int count() {
        return this.count;
    }

    public int componentSize() {
        return this.componentSize;
    }

    public boolean normalized() {
        return this.normalized;
    }

    public boolean integer() {
        return this.integer;
    }

    VertexAttributeType(int i, int i2, int i3, boolean z, boolean z2) {
        this.type = i;
        this.count = i2;
        this.componentSize = i3;
        this.normalized = z;
        this.integer = z2;
    }
}
