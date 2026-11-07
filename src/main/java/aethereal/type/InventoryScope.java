package aethereal.type;

public enum InventoryScope {
    ALL(0, 46),
    HOTBAR(0, 8),
    INVENTORY(9, 35),
    OFFHAND(40, 40),
    ARMOR(36, 39);

    public final int start;
    public final int end;

    public int start() {
        return this.start;
    }

    public int end() {
        return this.end;
    }

    InventoryScope(int i, int i2) {
        this.start = i;
        this.end = i2;
    }
}
