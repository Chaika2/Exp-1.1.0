package aethereal.model;

public class ChatStackEntry {
    public final boolean stack;
    public final String id;
    public final boolean remove;
    public final int count;

    public ChatStackEntry() {
        this.stack = false;
        this.id = null;
        this.remove = false;
        this.count = 0;
    }

    public ChatStackEntry(boolean z, String str, boolean z2, int i) {
        this.stack = z;
        this.id = str;
        this.remove = z2;
        this.count = i;
    }

    public String id() {
        return this.id;
    }

    public boolean remove() {
        return this.remove;
    }

    public boolean stack() {
        return this.stack;
    }

    public int count() {
        return this.count;
    }
}
