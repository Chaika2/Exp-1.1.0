package aethereal.type;

public enum Priority {
    LOW(0),
    NORMAL(5),
    HIGH(10),
    CRITICAL(20);

    public final int level;

    public int getLevel() {
        return this.level;
    }

    Priority(int i) {
        this.level = i;
    }
}
