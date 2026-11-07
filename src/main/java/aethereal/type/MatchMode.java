package aethereal.type;

public enum MatchMode {
    EXACT(0),
    STARTS_WITH(1),
    CONTAINS(2),
    ALIAS(3);

    public final int priority;

    MatchMode(int i) {
        this.priority = i;
    }

    public int getPriority() {
        return this.priority;
    }
}
