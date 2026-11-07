package aethereal.model;

public class PinnedServerEntry {
    public final String name;
    public final String address;
    public final int accentColor;

    public PinnedServerEntry() {
        this.name = null;
        this.address = null;
        this.accentColor = 0;
    }

    public PinnedServerEntry(String str, String str2, int i) {
        this.name = str;
        this.address = str2;
        this.accentColor = i;
    }

    public int accentColor() {
        return this.accentColor;
    }

    public String address() {
        return this.address;
    }

    public String name() {
        return this.name;
    }
}
